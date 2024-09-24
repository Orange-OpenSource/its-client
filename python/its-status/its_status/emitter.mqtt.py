# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import json
import iot3.core.mqtt


class Status:
    def __init__(self, *, cfg):
        self.enabled = cfg.get("mqtt", "enabled", fallback=True)
        if self.enabled:
            self.topic = cfg.get("mqtt", "topic", fallback="status/system")
            self.client = iot3.core.mqtt.MqttClient(
                client_id=cfg.get("mqtt", "client_id", fallback="its-status"),
                host=cfg.get("mqtt", "host", fallback="127.0.0.1"),
                port=cfg.getint("mqtt", "port", fallback=1883),
                username=cfg.get("mqtt", "username", fallback=None),
                password=cfg.get("mqtt", "password", fallback=None),
            )
            self.client.start()

    def emit(self, data):
        if not self.enabled:
            return

        self.client.publish(topic=self.topic, payload=json.dumps(data))
