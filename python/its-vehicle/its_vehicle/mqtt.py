# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import logging
import paho.mqtt.client

try:
    import paho_socket
except ModuleNotFoundError:
    paho_socket_available = False
else:
    paho_socket_available = True


class MqttClient:
    def __init__(self, *, cfg: dict):
        self.cfg = cfg

        # Type coercion
        # port can be missing when socket-path is set
        self.cfg["port"] = int(self.cfg.get("port", -1))

        if "socket-path" in self.cfg:
            if not paho_socket_available:
                raise RuntimeError(
                    f"paho_socket not available for {self.cfg['socket-path']}",
                )
            Client = paho_socket.Client
            connect_fn = "sock_connect_async"
            connect_args = (self.cfg["socket-path"],)
            self.name = self.cfg["socket-path"]
        else:
            Client = paho.mqtt.client.Client
            connect_fn = "connect_async"
            connect_args = (self.cfg["host"], self.cfg["port"])
            self.name = f"{self.cfg['host']}:{self.cfg['port']}"
        logging.debug("creating MQTT client %s with %s", self.name, self.cfg)

        username = self.cfg.get("username", None)
        password = self.cfg.get("password", None)

        self.client = Client(
            client_id=self.cfg["client-id"],
            protocol=paho.mqtt.client.MQTTv5,
        )
        self.client.reconnect_delay_set()
        self.client.username_pw_set(username, password)
        self.client.on_connect = self.__on_connect
        self.client.on_disconnect = self.__on_disconnect
        self.client.on_socket_close = self.__on_socket_close

        self.connected = False
        getattr(self.client, connect_fn)(
            *connect_args,
            clean_start=True,
        )

    def start(self):
        logging.debug("starting MQTT client %s", self.name)
        self.client.loop_start()

    def stop(self):
        logging.debug("stopping MQTT client %s", self.name)
        self.client.disconnect()
        self.client.loop_stop()

    def __on_connect(self, _client, _userdata, _flags, _rc, _properties=None):
        logging.debug("connected to MQTT client %s", self.name)
        self.connected = True

    def __on_disconnect(self, _client, _userdata, _rc, _properties=None):
        logging.debug(f"disconnected from %s", self.name)
        self.connected = False

    def __on_socket_close(self, _client, _userdata, _sock):
        logging.debug(f"disconnected (socket) from %s", self.name)
        self.connected = False
