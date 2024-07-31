# Software Name: IoT3 Mobility
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import enum
import hashlib
import json
import time

from typing import Optional

from . import etsi
from .gnss import GNSSReport


class CooperativeAwarenessMessage(etsi.Message):
    msg_type = "cam"

    def __init__(
        self,
        *,
        uuid: str,
        station_type: Optional[
            etsi.Message.StationType
        ] = etsi.Message.StationType.unknown,
        gnss_report: GNSSReport,
    ):
        """Create a basic Cooperative Awareness Message

        :param uuid: the UUID of this station
        :param station_type: The type of this station
        :param gnss_report: a GNSS report, coming from a GNSS device
        """
        self._gnss_report = gnss_report

        self._message = dict(
            {
                "type": "cam",
                "origin": "self",
                "version": "1.1.3",
                "source_uuid": uuid,
                "timestamp": (
                    etsi.ETSI.si2etsi(
                        time.time(),
                        etsi.ETSI.MILLI_SECOND,
                        0,
                    )
                ),
                "message": {
                    "protocol_version": 1,
                    "station_id": self.station_id(uuid),
                    "generation_delta_time": (
                        etsi.ETSI.generation_delta_time(gnss_report.timestamp)
                    ),
                    "basic_container": {
                        "station_type": station_type,
                        "reference_position": {
                            "latitude": etsi.ETSI.si2etsi(
                                gnss_report.latitude,
                                etsi.ETSI.DECI_MICRO_DEGREE,
                                900000001,
                            ),
                            "longitude": etsi.ETSI.si2etsi(
                                gnss_report.longitude,
                                etsi.ETSI.DECI_MICRO_DEGREE,
                                1800000001,
                            ),
                            "altitude": etsi.ETSI.si2etsi(
                                gnss_report.altitude,
                                etsi.ETSI.CENTI_METER,
                                800001,
                            ),
                        },
                        "confidence": {
                            "position_confidence_ellipse": {
                                # We treat the 2D error as a circle, so semi-major
                                # and semi-minor are eqal, and thus the orientation
                                # of the elipse does not matter.
                                "semi_major_confidence": etsi.ETSI.si2etsi(
                                    gnss_report.horizontal_error,
                                    etsi.ETSI.CENTI_METER,
                                    4095,
                                    {"min": 0, "max": 4093},
                                    4094,
                                ),
                                "semi_minor_confidence": etsi.ETSI.si2etsi(
                                    gnss_report.horizontal_error,
                                    etsi.ETSI.CENTI_METER,
                                    4095,
                                    {"min": 0, "max": 4093},
                                    4094,
                                ),
                                # Any orientation is valid for a circle, just use 0.
                                "semi_major_orientation": etsi.ETSI.si2etsi(
                                    0,
                                    etsi.ETSI.DECI_DEGREE,
                                    3601,
                                ),
                            },
                        },
                    },
                    "high_frequency_container": {
                        "heading": etsi.ETSI.si2etsi(
                            gnss_report.track,
                            etsi.ETSI.DECI_DEGREE,
                            3601,
                        ),
                        "speed": etsi.ETSI.si2etsi(
                            gnss_report.speed,
                            etsi.ETSI.CENTI_METER_PER_SECOND,
                            16383,
                        ),
                        "longitudinal_acceleration": etsi.ETSI.si2etsi(
                            gnss_report.acceleration,
                            etsi.ETSI.DECI_METER_PER_SECOND_SECOND,
                            161,
                        ),
                    },
                },
            },
        )

    @property
    def latitude(self):
        return etsi.ETSI.etsi2si(
            self._message["message"]["basic_container"]["reference_position"][
                "latitude"
            ],
            etsi.ETSI.DECI_MICRO_DEGREE,
            900000001,
        )

    @latitude.setter
    def latitude(self, latitude):
        self._message["message"]["basic_container"]["reference_position"][
            "latitude"
        ] = etsi.ETSI.si2etsi(
            latitude,
            etsi.ETSI.DECI_MICRO_DEGREE,
            900000001,
        )

    @property
    def longitude(self):
        return etsi.ETSI.etsi2si(
            self._message["message"]["basic_container"]["reference_position"][
                "longitude"
            ],
            etsi.ETSI.DECI_MICRO_DEGREE,
            1800000001,
        )

    @longitude.setter
    def longitude(self, longitude):
        self._message["message"]["basic_container"]["reference_position"][
            "longitude"
        ] = etsi.ETSI.si2etsi(
            longitude,
            etsi.ETSI.DECI_MICRO_DEGREE,
            1800000001,
        )

    @property
    def altitude(self):
        return etsi.ETSI.etsi2si(
            self._message["message"]["basic_container"]["reference_position"][
                "altitude"
            ],
            etsi.ETSI.CENTI_METER,
            800001,
        )

    @altitude.setter
    def altitude(self, altitude):
        self._message["message"]["basic_container"]["reference_position"][
            "altitude"
        ] = etsi.ETSI.si2etsi(
            altitude,
            etsi.ETSI.CENTI_METER,
            800001,
        )


# Shorthand
CAM = CooperativeAwarenessMessage
