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
import json
import logging

from hashlib import sha256

from its_client.mobility import kmph_to_mps

TIMESTAMP_ITS_START = 1072915195000  # its timestamp starts at 2004/01/01T00:00:00.000Z


def station_id(uuid: str) -> int:
    logging.debug("we compute the station id for " + uuid)

    hasher = sha256()
    hasher.update(bytes(uuid, "utf-8"))
    hashed_uuid = hasher.hexdigest()
    return int(hashed_uuid[0:6], 16)


class CooperativeAwarenessMessage:
    def __init__(
        self,
        uuid,
        timestamp,
        latitude=0.0,
        longitude=0.0,
        altitude=0.0,
        speed=0.0,
        acceleration=0.0,
        heading=0.0,
    ):
        self.uuid = uuid
        self.timestamp = int(round(timestamp * 1000))
        self.latitude = int(round(latitude * 10000000))
        self.longitude = int(round(longitude * 10000000))
        self.altitude = int(round(altitude * 100))
        self.speed = int(round(kmph_to_mps(speed) * 100))
        self.acceleration = int(round(acceleration * 10))
        self.heading = int(round(heading * 10))
        self.station_id = station_id(uuid)

    def generation_delta_time(self) -> int:
        return (self.timestamp - TIMESTAMP_ITS_START) % 65536

    def to_json(self) -> str:
        cam_json = {
            "type": "cam",
            "origin": "self",
            "version": "1.0.0",
            "source_uuid": self.uuid,
            "timestamp": self.timestamp,
            "message": {
                "protocol_version": 1,
                "station_id": self.station_id,
                "generation_delta_time": self.generation_delta_time(),
                "basic_container": {
                    "station_type": 5,
                    "reference_position": {
                        "latitude": self.latitude,
                        "longitude": self.longitude,
                        "altitude": self.altitude,
                    },
                    "confidence": {
                        "position_confidence_ellipse": {
                            "semi_major_confidence": 10,
                            "semi_minor_confidence": 50,
                            "semi_major_orientation": 1,
                        },
                        "altitude": 1,
                    },
                },
                "high_frequency_container": {
                    "heading": self.heading,
                    "speed": self.speed,
                    "longitudinal_acceleration": self.acceleration,
                    "drive_direction": 0,
                    "vehicle_length": 40,
                    "vehicle_width": 20,
                    "confidence": {"heading": 2, "speed": 3, "vehicle_length": 0},
                },
                "low_freq_container": {"vehicle_role": 2},
            },
        }
        return json.dumps(cam_json)
