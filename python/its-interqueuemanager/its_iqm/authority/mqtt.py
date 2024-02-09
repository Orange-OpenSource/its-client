# Software Name: its-iqm
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

from __future__ import annotations
import configparser
import json
import logging
import paho.mqtt.client
import paho.mqtt.subscribe
import time


class Authority:
    def __init__(
        self,
        cfg: configparser.ConfigParser,
        update_cb: Callable[[Sequence[Any]], None],
    ):
        self.cfg = cfg
        self.update_cb = update_cb

        try:
            client_id = self.cfg["authority"]["client_id"]
        except KeyError:
            client_id = self.cfg["general"]["instance_id"]

        self.prefix = str()
        self.suffix = str()
        # Compare against "not None" instead of truthness, as an
        # empty prefix is valid and means starting the topic with a /
        if self.cfg["general"]["prefix"] is not None:
            self.prefix = f"{self.cfg['general']['prefix']}"
            # prefix must end with a '/'
            if self.prefix == "" or self.prefix[-1] != "/":
                self.prefix += "/"
        # Test for truthness, because an empty suffix is the same as no suffix
        if self.cfg["general"]["suffix"]:
            self.suffix = self.cfg["general"]["suffix"]
            if self.suffix[-1] != "/":
                self.suffix += "/"

        self.topic = f"{self.prefix}neighbours/{self.suffix}{self.username}"

        self.authority_client = paho.mqtt.client.Client(
            client_id=client_id,
            protocol=paho.mqtt.client.MQTTv5,
        )
        self.authority_client.reconnect_delay_set()
        self.authority_client.username_pw_set(
            self.cfg["authority"]["username"] or None,
            self.cfg["authority"]["password"] or None,
        )
        self.authority_client.on_connect = self._on_connect
        self.authority_client.on_disconnect = self._on_disconnect
        self.authority_client.on_socket_close = self._on_socket_close
        self.authority_client.on_message = self._on_message

        self.authority_client.connect_async(
            host=self.cfg["authority"]["host"],
            port=int(self.cfg["authority"]["port"]),
            clean_start=True,
        )

    def start(self):
        logging.info(
            f"starting authority MQTT client to {self.cfg['authority']['host']}:{self.cfg['authority']['port']}"
        )
        self.authority_client.loop_start()

    def stop(self):
        logging.info(
            f"stopping authority MQTT client to {self.cfg['authority']['host']}:{self.cfg['authority']['port']}"
        )
        self.authority_client.disconnect()
        self.authority_client.loop_stop()

    def join(self):
        pass

    def _on_connect(self, _client, _userdata, _flags, _rc, _properties=None):
        self.authority_client.subscribe(self.topic)

    def _on_disconnect(self, _client, _userdata, _rc, _properties=None):
        pass

    def _on_socket_close(self, _client, _userdata, _sock):
        pass

    def _on_message(self, _client, _userdata, message):
        logging.info("received neighbours")
        loaded_nghbs = json.loads(message.payload)
        self.update_cb(loaded_nghbs)
