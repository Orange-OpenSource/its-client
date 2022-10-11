# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import json
import paho.mqtt.client


class Status():
    def __init__(self, cfg):
        self.enabled = cfg.get('mqtt', 'enabled', fallback=True)
        if self.enabled:
            self.host = cfg.get('mqtt', 'host', fallback='127.0.0.1')
            self.port = cfg.getint('mqtt', 'port', fallback=1883)
            self.username = cfg.get('mqtt', 'username', fallback=None)
            self.password = cfg.get('mqtt', 'password', fallback=None)
            self.client_id = cfg.get('mqtt', 'client_id', fallback='its-status')
            self.topic = cfg.get('mqtt', 'topic', fallback='status/system')

            self.client = paho.mqtt.client.Client(client_id=self.client_id)
            self.client.reconnect_delay_set()
            self.client.username_pw_set(self.username, self.password)
            self.connected = False
            self._try_connect()

    def emit(self, data):
        self._emit(self.topic, data)

    def error(self, data):
        self._emit(self.topic + '/errors', data)

    def _emit(self, topic, data):
        if not self.enabled:
            return

        if not self.connected:
            self._try_connect()

        if self.connected:
            self.client.publish(topic, json.dumps(data))

    def _try_connect(self):
        if self.connected:
            raise RuntimeError('MQTT connection already established')

        try:
            self.client.connect(host=self.host, port=self.port)
            self.connected = True
        except Exception:
            pass

        if self.connected:
            self.client.loop_start()
