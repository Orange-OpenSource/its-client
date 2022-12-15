# Software Name: its-info
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import argparse
import configparser
import json
import linuxfd
import netifaces
import os
import paho.mqtt.client
import time


def main():
    try:
        info = MQTTInfoClient()
        info.loop_forever()
    except Exception as e:
        print(e)
        os._exit(1)


class MQTTInfoClient:
    CFG = "/etc/its/its-info.cfg"

    def __init__(self):
        parser = argparse.ArgumentParser()
        parser.add_argument(
            "--config",
            "-c",
            default=MQTTInfoClient.CFG,
            help=f"Path to the configuration file (default: {MQTTInfoClient.CFG})",
        )
        args = parser.parse_args()

        cfg = configparser.ConfigParser()
        with open(args.config) as f:
            cfg.read_file(f)

        self.instance_id = cfg.get("general", "instance_id")
        self.instance_type = cfg.get("general", "instance_type", fallback="local")
        self.period = cfg.getint("general", "period", fallback=600)
        self.dns_ip = cfg.get("general", "dns_ip", fallback=None)
        self.ntp_host = cfg.get("general", "ntp_host", fallback=None)
        self.interface = cfg.get("general", "interface", fallback=None)

        self.client_id = cfg.get("mqtt", "client_id", fallback=self.instance_id)
        self.host = cfg.get("mqtt", "host", fallback="127.0.0.1")
        self.port = cfg.getint("mqtt", "port", fallback=1883)
        self.username = cfg.get("mqtt", "username", fallback=None)
        self.password = cfg.get("mqtt", "password", fallback=None)
        self.topic = cfg.get("mqtt", "topic", fallback="info")
        self.retry = cfg.getint("mqtt", "retry", fallback=2)

        self.client = paho.mqtt.client.Client(
            client_id=self.client_id,
        )
        self.client.reconnect_delay_set(min_delay=1, max_delay=self.retry)
        self.client.username_pw_set(self.username, self.password)
        self.client.on_connect = self.on_connect
        self.client.on_disconnect = self.on_disconnect
        self.client.on_socket_close = self.on_socket_close
        self.client.connect_async(host=self.host, port=self.port)

        self.timer = linuxfd.timerfd(closeOnExec=True)

    def loop_forever(self):
        self.client.loop_start()
        while True:
            try:
                # We don't care about the number of time the timer has
                # fired that we missed; given the periodicity is in the
                # order of many seconds, it is highly unlikely that we
                # ever miss a tick...
                self.timer.read()
                self.info()
            except InterruptedError:
                # Someone sent a signal to this thread...
                continue
            except KeyboardInterrupt:
                break

        self.client.loop_stop()
        self.client.disconnect()

    def on_connect(self, _client, _userdata, _flags, _rc, _properties=None):
        self.timer.settime(value=self.period, interval=self.period)
        self.info()

    def on_disconnect(self, _client, _userdata, _rc, _properties=None):
        self.timer.settime(value=0)

    def on_socket_close(self, _client, _userdata, _sock):
        self.timer.settime(value=0)

    def info(self):
        data = {
            "type": "broker",
            "version": "1.2.0",
            "instance_id": self.instance_id,
            "instance_type": self.instance_type,
            "running": True,
            "timestamp": int(1000 * time.time()),
            "validity_duration": int(self.period * 2),
        }

        # The IP adresses of the interface may change at runtime
        # so we must grab them every time.
        ips = list()
        if self.interface:
            ifa = netifaces.ifaddresses(self.interface)
            for familly in [netifaces.AF_INET, netifaces.AF_INET6]:
                if familly in ifa:
                    # IPv6 addresses can look like: 01::EF%iface but we just want 01::EF
                    ips += [i["addr"].split("%")[0] for i in ifa[familly]]

        if self.dns_ip:
            data["domain_name_servers"] = [self.dns_ip]
        elif ips:
            data["domain_name_servers"] = ips
        if self.ntp_host:
            data["ntp_servers"] = [self.ntp_host]
        elif ips:
            data["ntp_servers"] = ips

        self.client.publish(
            topic=self.topic,
            payload=json.dumps(data),
            retain=True,
        )
