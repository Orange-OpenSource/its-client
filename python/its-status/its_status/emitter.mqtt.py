# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import json
import paho.mqtt.client


class Status():
    def __init__(self):
        self.client = paho.mqtt.client.Client(client_id='foo')
        self.client.reconnect_delay_set()
        self.client.connect(host='127.0.0.1', port=1883)
        self.client.loop_start()

    def emit(self, data):
        self.client.publish('status/system', json.dumps(data))
