# Software Name: its-client
# SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
# SPDX-License-Identifier: MIT License
#
# This software is distributed under the MIT license, see LICENSE.txt file for more details.
#
# Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
# Software description: This Intelligent Transportation Systems (ITS)
# [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org)
# [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project
# for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
import random
from datetime import datetime


class GeoPosition:
    def __init__(
        self,
        *,
        latitude: float,
        longitude: float,
        heading: float,
    ):
        self.count = 0
        self.lat = latitude
        self.lon = longitude
        self.heading = heading

    def get_current_position(self):
        return self.lon, self.lat

    def get_current_value(self):
        # stub
        self.count += 1
        lon_drift = 0
        lat_drift = 0
        heading_drift = 0
        if self.count % 5 == 0:
            # 0.000002° of delta is ~0.2226m at the equator, ~0.2219m at the poles
            lon_drift = round(random.uniform(-0.000002, 0.000002), 6)
            lat_drift = round(random.uniform(-0.000002, 0.000002), 6)
            # 0.5° of heading drift.
            heading_drift = round(random.uniform(-0.5, 0.5), 3)
            self.count = 0
        lon, lat = self.get_current_position()
        lon = lon + lon_drift
        lat = lat + lat_drift
        speed = 0.103
        alt = 131.693
        heading = self.heading + heading_drift
        position_time = datetime.utcnow()
        return lon, lat, speed, alt, heading, position_time
