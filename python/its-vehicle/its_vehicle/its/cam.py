# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import datetime
import hashlib
import json
from . import ETSI, SI2ETSI
from ..gpsd import GNSSReport


class CooperativeAwarenessMessage:
    def __init__(
        self,
        *,
        uuid: str,
        message_id: str | dict,
        gnss_report: GNSSReport,
    ):
        # We currently do not use it, so let's just assign it
        # to avoid linters whinning
        _message_id = message_id

        self.cam = dict(
            {
                "type": "cam",
                "origin": "self",
                "version": "1.1.3",
                "source_uuid": uuid,
                "timestamp": (
                    SI2ETSI.seconds(
                        datetime.datetime.now(datetime.timezone.utc).timestamp(),
                        SI2ETSI.MILLI_SECOND,
                        0,
                    )
                ),
                "message": {
                    "protocol_version": 1,
                    "station_id": self.station_id(uuid),
                    "generation_delta_time": (
                        ETSI.generation_delta_time(gnss_report.timestamp)
                    ),
                    "basic_container": {
                        "station_type": 5,
                        "reference_position": {
                            "latitude": SI2ETSI.degrees(
                                gnss_report.latitude,
                                SI2ETSI.DECI_MICRO_DEGREE,
                                900000001,
                            ),
                            "longitude": SI2ETSI.degrees(
                                gnss_report.longitude,
                                SI2ETSI.DECI_MICRO_DEGREE,
                                1800000001,
                            ),
                            "altitude": SI2ETSI.meters(
                                gnss_report.altitude,
                                SI2ETSI.CENTI_METER,
                                800001,
                            ),
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
                        "heading": SI2ETSI.degrees(
                            gnss_report.track,
                            SI2ETSI.DECI_DEGREE,
                            3601,
                        ),
                        "speed": SI2ETSI.meters_per_second(
                            gnss_report.speed,
                            SI2ETSI.CENTI_METER_PER_SECOND,
                            16383,
                        ),
                        "longitudinal_acceleration": (
                            SI2ETSI.meters_per_second_second(
                                gnss_report.acceleration,
                                SI2ETSI.DECI_METER_PER_SECOND_SECOND,
                                161,
                            )
                        ),
                        "drive_direction": 0,
                        "vehicle_length": 40,
                        "vehicle_width": 20,
                        "confidence": {
                            "heading": 2,
                            "speed": 3,
                            "vehicle_length": 0,
                        },
                    },
                },
            },
        )

    @staticmethod
    def station_id(uuid: str) -> int:
        return int(
            hashlib.sha256(uuid.encode()).hexdigest()[:6],
            16,
        )

    def to_json(self) -> str:
        # Return the densest possible JSON sentence
        return json.dumps(self.cam, separators=(",", ":"))
