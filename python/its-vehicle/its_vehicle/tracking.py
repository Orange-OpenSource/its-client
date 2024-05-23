# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import random
from .gpsd import GNSSProvider
from .mqtt import MqttDirection


class Tracking:
    def __init__(self, *, cfg: dict, gpsd: GNSSProvider):
        self.cfg = cfg
        self.gpsd = gpsd

        # Type coertion
        self.cfg["id-length"] = int(self.cfg["id-length"])

    def id(self) -> str | dict:
        # One day, we'll ask a higher-level library (e.g; OpenTelemetry)
        # to generate an ID for us; until then, just generate a random
        # string with a very low probability of collision.
        return random.randbytes(self.cfg["id-length"]).hex()

    def track(self, *, direction: MqttDirection, payload: str):
        pass

    def start(self):
        pass

    def stop(self):
        pass
