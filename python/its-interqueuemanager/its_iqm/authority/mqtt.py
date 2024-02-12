# Software Name: its-iqm
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

from __future__ import annotations
import json
import logging
import paho.mqtt.client
import paho.mqtt.subscribe


class Authority:
    def __init__(
        self,
        instance_id: str,
        cfg: dict,
        update_cb: Callable[[its_iqm.iqm.IQM, dict], None],
    ):
        self.cfg = cfg
        self.update_cb = update_cb

        try:
            client_id = self.cfg["client_id"]
        except KeyError:
            client_id = instance_id

        self.authority_client = paho.mqtt.client.Client(
            client_id=client_id,
            protocol=paho.mqtt.client.MQTTv5,
        )
        self.authority_client.reconnect_delay_set()
        self.authority_client.username_pw_set(
            self.cfg["username"] or None,
            self.cfg["password"] or None,
        )
        self.authority_client.on_connect = self._on_connect
        self.authority_client.on_disconnect = self._on_disconnect
        self.authority_client.on_socket_close = self._on_socket_close
        self.authority_client.on_message = self._on_message

        self.authority_client.connect_async(
            host=self.cfg["host"],
            port=int(self.cfg["port"]),
            clean_start=True,
        )

    def start(self):
        logging.info(
            f"starting authority MQTT client to {self.cfg['host']}:{self.cfg['port']}"
        )
        self.authority_client.loop_start()

    def stop(self):
        logging.info(
            f"stopping authority MQTT client to {self.cfg['host']}:{self.cfg['port']}"
        )
        self.authority_client.disconnect()
        self.authority_client.loop_stop()

    def join(self):
        pass

    def _on_connect(self, _client, _userdata, _flags, _rc, _properties=None):
        self.authority_client.subscribe(self.cfg["topic"])

    def _on_disconnect(self, _client, _userdata, _rc, _properties=None):
        pass

    def _on_socket_close(self, _client, _userdata, _sock):
        pass

    def _on_message(self, _client, _userdata, message):
        logging.info("received neighbours")
        loaded_nghbs = json.loads(message.payload)
        self.update_cb(loaded_nghbs)
