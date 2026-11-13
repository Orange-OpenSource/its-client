# Software Name: its-info
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import argparse
import configparser
import json
import linuxfd
import logging
import netifaces
import os
import sys
import time
import warnings

from iot3.core.mqtt import MqttClient
from iot3.mobility.etsi import Message
from its_quadkeys import QuadZone


def main():
    try:
        info = MQTTInfoClient()
        info.loop_forever()
    except Exception as e:
        print(e)
        os._exit(1)


class MQTTInfoClient:
    CFG = "/etc/its/its-info.cfg"
    DEFAULTS = {
        "general": {
            "instance_type": "local",
            "period": 600,
            "dns_ip": None,
            "ntp_host": None,
            "interface": None,
        },
        "mqtt": {
            "socket-path": "/run/mosquitto/mqtt.socket",
            "username": None,
            "password": None,
            "topic": "info",
            "retry": 2,
        },
        "RoR": {
            "type": "none",
            "reload": False,
        },
    }

    def __init__(self):
        parser = argparse.ArgumentParser()
        parser.add_argument(
            "--config",
            "-c",
            default=MQTTInfoClient.CFG,
            help=f"Path to the configuration file (default: {MQTTInfoClient.CFG})",
        )
        parser.add_argument(
            "--debug",
            action="store_true",
            help="Print tons of debug messages on stderr",
        )
        args = parser.parse_args()

        logging.basicConfig(
            stream=sys.stderr,
            format="%(asctime)s %(module)s: %(message)s",
            level=logging.DEBUG if args.debug else logging.INFO,
        )

        logging.info("instanciating info object")

        logging.debug(f"loading config file {args.config}...")
        self.cfg = configparser.ConfigParser()
        with open(args.config) as f:
            self.cfg.read_file(f)

        # configparser.ConfigParser() only accets strings as values, but we
        # need None for example, so make it a true dict() of dicts()s, which
        # is easier to work with
        self.cfg = {
            s: {k: self.cfg[s][k] for k in self.cfg[s]}
            for s in self.cfg
            if s != "DEFAULT"
        }

        def _set_default(section, key, default):
            if key not in self.cfg[section]:
                self.cfg[section][key] = default

        if "station-uuid" not in self.cfg["general"]:
            # Try with the legacy instance_id
            try:
                self.cfg["general"]["station-uuid"] = self.cfg["general"]["instance_id"]
            except KeyError:
                raise KeyError(
                    f"{args.config}: station-uuid missing in [general]",
                ) from None
            warnings.warn(
                f"{args.config}: using legacy instance_id; swith to station-uuid instead.",
            )

        _set_default("mqtt", "client_id", self.cfg["general"]["station-uuid"])
        for s in MQTTInfoClient.DEFAULTS:
            if s not in self.cfg:
                self.cfg[s] = {}
            for k in MQTTInfoClient.DEFAULTS[s]:
                _set_default(s, k, MQTTInfoClient.DEFAULTS[s][k])

        if self.cfg["RoR"]["type"] == "none":
            self.ror = None
        elif self.cfg["RoR"]["type"] == "static":
            self.ror = QuadZone()
            try:
                self.ror.load(self.cfg["RoR"]["path"])
                self.ror.optimise()
            except FileNotFoundError:
                self.ror = None
        else:
            raise RuntimeError(
                f"{self.cfg['RoR']['type']}: unknown or unimplemented RoR type"
            )

        self.timer = linuxfd.timerfd(closeOnExec=True)

        logging.debug("creating MQTT client")
        self.client = MqttClient(
            client_id=self.cfg["mqtt"]["client_id"],
            socket_path=self.cfg["mqtt"]["socket-path"],
            username=self.cfg["mqtt"]["username"],
            password=self.cfg["mqtt"]["password"],
            conn_cb=self.on_conn_change,
        )

        logging.debug("finished instanciating info object")

    def loop_forever(self):
        logging.info("starting info loop")
        self.client.start()
        while True:
            try:
                # We don't care about the number of time the timer has
                # fired that we missed; given the periodicity is in the
                # order of many seconds, it is highly unlikely that we
                # ever miss a tick...
                logging.debug("waiting for tick")
                self.timer.read()
                logging.debug("got tick")
                self.info()
            except InterruptedError:
                # Someone sent a signal to this thread...
                logging.debug("got signal")
                continue
            except KeyboardInterrupt:
                logging.debug("got Ctrl-C")
                break

        logging.info("stopping info loop")
        self.client.stop()
        logging.debug("stopped info loop")

    def on_conn_change(self, *, data, connected):
        if connected:
            logging.debug("connected to MQTT broker")
            self.timer.settime(
                value=int(self.cfg["general"]["period"]),
                interval=int(self.cfg["general"]["period"]),
            )
            self.info()
        else:
            logging.debug("disconnected from MQTT broker")
            self.timer.settime(value=0)

    def info(self):
        logging.debug("preparing info data")
        data = {
            "message_type": "information",
            "source_uuid": self.cfg["general"]["station-uuid"],
            "timestamp": int(1000 * time.time()),
            "version": "2.1.0",
            "instance_id": Message.station_id(self.cfg["general"]["station-uuid"]),
            "instance_type": self.cfg["general"]["instance_type"],
            "running": True,
            "validity_duration": int(self.cfg["general"]["period"]) * 2,
        }

        # The IP adresses of the interface may change at runtime
        # so we must grab them every time.
        ips = list()
        if self.cfg["general"]["interface"]:
            ifa = netifaces.ifaddresses(self.cfg["general"]["interface"])
            for familly in [netifaces.AF_INET, netifaces.AF_INET6]:
                if familly in ifa:
                    # IPv6 addresses can look like: 01::EF%iface but we just want 01::EF
                    ips += [i["addr"].split("%")[0] for i in ifa[familly]]

        if self.cfg["general"]["dns_ip"]:
            data["domain_name_servers"] = [self.cfg["general"]["dns_ip"]]
        elif ips:
            data["domain_name_servers"] = ips
        if self.cfg["general"]["ntp_host"]:
            data["ntp_servers"] = [self.cfg["general"]["ntp_host"]]
        elif ips:
            data["ntp_servers"] = ips

        if self.cfg["RoR"]["type"] == "static" and self.cfg["RoR"]["reload"]:
            if self.ror is None:
                self.ror = QuadZone()
            try:
                self.ror.load(self.cfg["RoR"]["path"])
                self.ror.optimise()
                logging.debug(f"loaded RoR '{self.ror}'")
            except FileNotFoundError:
                # File not found: consider we have no RoR.
                self.ror = None

        if self.ror is not None:
            data["service_area"] = {"type": "tiles", "quadkeys": sorted(self.ror)}

        data_str = json.dumps(data, separators=(",", ":"))

        logging.debug(
            f"sending info data '{data_str[:128]+('...' if len(data_str)>128 else '')}'",
        )
        self.client.publish(
            topic=self.cfg["mqtt"]["topic"],
            payload=data_str,
            retain=data["validity_duration"],
        )
