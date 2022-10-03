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
            self.client.connect(host=self.host, port=self.port)
            self.client.loop_start()

    def emit(self, data):
        if self.enabled:
            self.client.publish(self.topic, json.dumps(data))
