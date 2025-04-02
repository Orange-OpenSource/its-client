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


class DecentralizedEnvironmentalNotificationMessage(etsi.Message):
    msg_type = "denm"

    class TerminationType(enum.IntEnum):
        # We need those values to *exactly* match those defined in the spec
        isCancellation = 0
        isNegation = 1

    class Cause(enum.IntEnum):
        # We need those values to *exactly* match those defined in the spec
        reserved = 0
        trafficCondition = 1
        accident = 2
        roadworks = 3
        adverseWeatherCondition_Adhesion = 6
        hazardousLocation_SurfaceCondition = 9
        hazardousLocation_ObstacleOnTheRoad = 10
        hazardousLocation_AnimalOnTheRoad = 11
        humanPresenceOnTheRoad = 12
        wrongWayDriving = 14
        rescueAndRecoveryWorkInProgress = 15
        adverseWeatherCondition_ExtremeWeatherCondition = 17
        adverseWeatherCondition_Visibility = 18
        adverseWeatherCondition_Precipitation = 19
        slowVehicle = 26
        dangerousEndOfQueue = 27
        vehicleBreakdown = 91
        postCrash = 92
        humanProblem = 93
        stationaryVehicle = 94
        emergencyVehicleApproaching = 95
        hazardousLocation_DangerousCurve = 96
        collisionRisk = 97
        signalViolation = 98
        dangerousSituation = 99

    _seq_nums = dict()

    def __init__(
        self,
        *,
        uuid: str,
        gnss_report: GNSSReport,
        detection_time: Optional[float] = None,
        cause: Cause = Cause.dangerousSituation,
        validity_duration: Optional[int | float] = None,
        termination: Optional[TerminationType] = None,
        sequence_number: Optional[int] = None,
    ):
        """Create a basic Decentralized Environmental Notification Message

        :param uuid: the UUID of this station
        :param gnss_report: a GNSS report, coming from a GNSS device
        :param detect_time: time of detection of the event
        :param cause: cause of the event
        :param validity_duration: duration the event is valid for
        :param termination: the type of termination for this event
        :param sequence_number: the sequence number this alert is a
                                continuation of; don't set if this alery
                                is not a continuation of a previous one.
        """
        self._gnss_report = gnss_report

        now = time.time()
        if detection_time is None:
            detection_time = now

        if sequence_number is None:
            sequence_number = self._get_seq_num(uuid)

        self._message = dict(
            {
                "type": "denm",
                "origin": "self",
                "version": "1.1.3",
                "source_uuid": uuid,
                "timestamp": etsi.ETSI.si2etsi(now, etsi.ETSI.MILLI_SECOND, 0),
                "message": {
                    "protocol_version": 1,
                    "station_id": self.station_id(uuid),
                    "management_container": {
                        "action_id": {
                            "originating_station_id": self.station_id(uuid),
                            "sequence_number": sequence_number,
                        },
                        "detection_time": etsi.ETSI.unix2etsi_time(detection_time),
                        "reference_time": etsi.ETSI.unix2etsi_time(now),
                        "event_position": {
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
                    },
                    "situation_container": {
                        "event_type": {
                            "cause": cause.value,
                        },
                    },
                },
            },
        )
        if termination is not None:
            self._message["message"]["management_container"][
                "termination"
            ] = termination
        if validity_duration is not None:
            self._message["message"]["management_container"][
                "validity_duration"
            ] = validity_duration

    # We allow retrieving the sequence_number, so the caller can propagate it
    # to a future continuation, if any, but we do not allow setting it, except
    # via the constructor, so no @sequence_number.setter
    @property
    def sequence_number(self) -> int:
        return self._message["message"]["management_container"]["action_id"][
            "sequence_number"
        ]

    @property
    def detection_time(self) -> float:
        return etsi.ETSI.etsi2unix_time(
            self._message["message"]["management_container"]["detection_time"]
        )

    @detection_time.setter
    def detection_time(self, detection_time: float):
        self._message["message"]["management_container"]["detection_time"] = (
            etsi.ETSI.unix2etsi_time(detection_time)
        )

    @property
    def latitude(self) -> float:
        return etsi.ETSI.etsi2si(
            self._message["message"]["management_container"]["event_position"][
                "latitude"
            ],
            etsi.ETSI.DECI_MICRO_DEGREE,
            900000001,
        )

    @latitude.setter
    def latitude(self, latitude: float):
        self._message["message"]["management_container"]["event_position"][
            "latitude"
        ] = etsi.ETSI.si2etsi(
            latitude,
            etsi.ETSI.DECI_MICRO_DEGREE,
            900000001,
        )

    @property
    def longitude(self) -> float:
        return etsi.ETSI.etsi2si(
            self._message["message"]["management_container"]["event_position"][
                "longitude"
            ],
            etsi.ETSI.DECI_MICRO_DEGREE,
            1800000001,
        )

    @longitude.setter
    def longitude(self, longitude: float):
        self._message["message"]["management_container"]["event_position"][
            "longitude"
        ] = etsi.ETSI.si2etsi(
            longitude,
            etsi.ETSI.DECI_MICRO_DEGREE,
            1800000001,
        )

    @property
    def altitude(self) -> float:
        return etsi.ETSI.etsi2si(
            self._message["message"]["management_container"]["event_position"][
                "altitude"
            ],
            etsi.ETSI.CENTI_METER,
            800001,
        )

    @altitude.setter
    def altitude(self, altitude: float):
        self._message["message"]["management_container"]["event_position"][
            "altitude"
        ] = etsi.ETSI.si2etsi(
            altitude,
            etsi.ETSI.CENTI_METER,
            800001,
        )

    @property
    def cause(self) -> Cause:
        return self.Cause(
            self._message["message"]["situation_container"]["event_type"]["cause"]
        )

    @cause.setter
    def cause(self, cause: Cause):
        self._message["message"]["situation_container"]["event_type"][
            "cause"
        ] = cause.value

    @property
    def termination(self) -> TerminationType:
        try:
            return self._message["message"]["management_container"]["termination"]
        except KeyError:
            return None

    @termination.setter
    def termination(self, termination: TerminationType):
        self._message["message"]["management_container"]["termination"] = termination

    @classmethod
    def _get_seq_num(
        cls,
        uuid: str,
    ) -> int:
        try:
            cls._seq_nums[uuid] = (cls._seq_nums[uuid] + 1) % 65_536
        except KeyError:
            cls._seq_nums[uuid] = 0
        return cls._seq_nums[uuid]


# Shorthand
DENM = DecentralizedEnvironmentalNotificationMessage
