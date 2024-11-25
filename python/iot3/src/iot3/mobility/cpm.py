# Software Name: IoT3 Mobility
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import dataclasses
import enum
import hashlib
import json
import time

from typing import Iterable, Optional

from . import etsi
from .gnss import GNSSReport


class CollectivePerceptionMessage(etsi.Message):
    msg_type = "cpm"

    @dataclasses.dataclass
    class PerceivedObject:
        """A simple wrapper to define a basice Perceived Object

        All values are in SI units, with times in seconds relative to
        the UNIX EPOCH, and ages in seconds.
        """

        object_id: int
        object_age: float
        time_of_measurement: float
        x_distance: float
        y_distance: float
        x_speed: Optional[float] = None
        y_speed: Optional[float] = None
        confidence: Optional[int] = 0

    def __init__(
        self,
        *,
        uuid: str,
        station_type: Optional[
            etsi.Message.StationType
        ] = etsi.Message.StationType.unknown,
        gnss_report: GNSSReport,
        perceived_objects: Optional[Iterable[PerceivedObject]] = [],
    ):
        """Create a basic Cooperative Awareness Message

        :param uuid: the UUID of this station
        :param station_type: The type of this station
        :param gnss_report: a GNSS report, coming from a GNSS device
        :param perceived_objects: A list of perceived objects
        """
        self._gnss_report = gnss_report

        self._message = dict(
            {
                "type": "cpm",
                "origin": "self",
                "version": "1.2.2",
                "source_uuid": uuid,
                "timestamp": (
                    etsi.ETSI.si2etsi(
                        time.time(),
                        etsi.ETSI.MILLI_SECOND,
                    )
                ),
                "message": {
                    "protocol_version": 1,
                    "station_id": self.station_id(uuid),
                    "generation_delta_time": (
                        etsi.ETSI.generation_delta_time(gnss_report.timestamp)
                    ),
                    "management_container": {
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
                            # Encoding the altitude error is a non-linear search in
                            # an array... Let's consider it unavailable for now.
                            "altitude": etsi.ETSI.si2etsi(
                                None,
                                etsi.ETSI.CENTI_METER,
                                15,
                            ),
                        },
                    },
                    "perceived_object_container": [],
                },
            },
        )

        if station_type == etsi.Message.StationType.unknown:
            # Unknown station type, no station data.
            pass
        elif station_type == etsi.Message.StationType.roadSideUnit:
            # Everything is optional in an RSU data, and we don't (yet)
            # know how to fill the optional fields in...
            self._message["message"]["station_data_container"] = {
                "originating_rsu_container": {},
            }
        else:
            self._message["message"]["station_data_container"] = {
                "originating_vehicle_container": {
                    "heading": etsi.ETSI.si2etsi(
                        gnss_report.true_heading,
                        etsi.ETSI.DECI_DEGREE,
                        3601,
                    ),
                    "speed": etsi.ETSI.si2etsi(
                        gnss_report.speed,
                        etsi.ETSI.CENTI_METER_PER_SECOND,
                        16383,
                    ),
                    # Confidence is unknown, we have no way of knowing it.
                    "confidence": {
                        "heading": 127,
                        "speed": 127,
                    },
                },
            }

        for po in perceived_objects:
            self.add_perceived_object(perceived_object=po)

    def add_perceived_object(
        self,
        *,
        perceived_object: PerceivedObject,
    ):
        po = dict(
            {
                "object_id": perceived_object.object_id,
                "time_of_measurement": etsi.ETSI.si2etsi(
                    perceived_object.time_of_measurement,
                    etsi.ETSI.MILLI_SECOND,
                )
                - self._message["timestamp"],
                "object_age": etsi.ETSI.si2etsi(
                    perceived_object.object_age,
                    etsi.ETSI.MILLI_SECOND,
                    # Unusual: "1500" means "1500 or more"
                    range={"min": 0, "max": 1500},
                ),
                "x_distance": etsi.ETSI.si2etsi(
                    perceived_object.x_distance,
                    etsi.ETSI.CENTI_METER,
                ),
                "y_distance": etsi.ETSI.si2etsi(
                    perceived_object.y_distance,
                    etsi.ETSI.CENTI_METER,
                ),
                "x_speed": etsi.ETSI.si2etsi(
                    perceived_object.x_speed,
                    etsi.ETSI.CENTI_METER_PER_SECOND,
                    16383,
                ),
                "y_speed": etsi.ETSI.si2etsi(
                    perceived_object.y_speed,
                    etsi.ETSI.CENTI_METER_PER_SECOND,
                    16383,
                ),
                "confidence": {
                    "object": perceived_object.confidence,
                    # All other required confidence fields set to unknwn
                    # for now...
                    "x_distance": 4095,
                    "y_distance": 4095,
                    "x_speed": 0,
                    "y_speed": 0,
                },
            },
        )

        self._message["message"]["perceived_object_container"].append(po)

    @property
    def latitude(self):
        return etsi.ETSI.etsi2si(
            self._message["message"]["management_container"]["reference_position"][
                "latitude"
            ],
            etsi.ETSI.DECI_MICRO_DEGREE,
            900000001,
        )

    @latitude.setter
    def latitude(self, latitude):
        self._message["message"]["management_container"]["reference_position"][
            "latitude"
        ] = etsi.ETSI.si2etsi(
            latitude,
            etsi.ETSI.DECI_MICRO_DEGREE,
            900000001,
        )

    @property
    def longitude(self):
        return etsi.ETSI.etsi2si(
            self._message["message"]["management_container"]["reference_position"][
                "longitude"
            ],
            etsi.ETSI.DECI_MICRO_DEGREE,
            1800000001,
        )

    @longitude.setter
    def longitude(self, longitude):
        self._message["message"]["management_container"]["reference_position"][
            "longitude"
        ] = etsi.ETSI.si2etsi(
            longitude,
            etsi.ETSI.DECI_MICRO_DEGREE,
            1800000001,
        )

    @property
    def altitude(self):
        return etsi.ETSI.etsi2si(
            self._message["message"]["management_container"]["reference_position"][
                "altitude"
            ],
            etsi.ETSI.CENTI_METER,
            800001,
        )

    @altitude.setter
    def altitude(self, altitude):
        self._message["message"]["management_container"]["reference_position"][
            "altitude"
        ] = etsi.ETSI.si2etsi(
            altitude,
            etsi.ETSI.CENTI_METER,
            800001,
        )

    @property
    def perceived_objects(self):
        for po in self._message["message"]["perceived_object_container"]:
            yield self.PerceivedObject(
                object_id=po["object_id"],
                object_age=etsi.ETSI.etsi2si(
                    po["object_age"],
                    etsi.ETSI.MILLI_SECOND,
                ),
                time_of_measurement=etsi.ETSI.etsi2si(
                    po["time_of_measurement"] + self._message["timestamp"],
                    etsi.ETSI.MILLI_SECOND,
                ),
                x_distance=etsi.ETSI.etsi2si(
                    po["x_distance"],
                    etsi.ETSI.CENTI_METER,
                ),
                y_distance=etsi.ETSI.etsi2si(
                    po["y_distance"],
                    etsi.ETSI.CENTI_METER,
                ),
                x_speed=etsi.ETSI.etsi2si(
                    po["x_speed"],
                    etsi.ETSI.CENTI_METER_PER_SECOND,
                    16383,
                ),
                y_speed=etsi.ETSI.etsi2si(
                    po["y_speed"],
                    etsi.ETSI.CENTI_METER_PER_SECOND,
                    16383,
                ),
                confidence=po["confidence"]["object"],
            )


# Shorthand
CPM = CollectivePerceptionMessage
