# Software Name: IoT3 Mobility
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import enum
import hashlib
import json
import time

from typing import Optional, TypeAlias

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

    class SubCause:
        class TrafficCondition(enum.IntEnum):
            unavailable = 0
            increasedVolumeOfTraffic = 1
            trafficJamSlowlyIncreasing = 2
            trafficJamIncreasing = 3
            trafficJamStronglyIncreasing = 4
            trafficStationary = 5
            trafficJamSlightlyDecreasing = 6
            trafficJamDecreasing = 7
            trafficJamStronglyDecreasing = 8

        class Accident(enum.IntEnum):
            unavailable = 0
            multiVehicleAccident = 1
            heavyAccident = 2
            accidentInvolvingLorry = 3
            accidentInvolvingBus = 4
            accidentInvolvingHazardousMaterials = 5
            accidentOnOppositeLane = 6
            unsecuredAccident = 7
            assistanceRequested = 8

        class Roadworks(enum.IntEnum):
            unavailable = 0
            majorRoadworks = 1
            roadMarkingWork = 2
            slowMovingRoadMaintenance = 3
            shortTermStationaryRoadworks = 4
            streetCleaning = 5
            winterService = 6

        class HumanPresenceOnTheRoad(enum.IntEnum):
            unavailable = 0
            childrenOnRoadway = 1
            cyclistOnRoadway = 2
            motorcyclistOnRoadway = 3

        class WrongWayDriving(enum.IntEnum):
            unavailable = 0
            wrongLane = 1
            wrongDirection = 2

        class AdverseWeatherCondition_ExtremeWeatherCondition(enum.IntEnum):
            unavailable = 0
            strongWinds = 1
            damagingHail = 2
            hurricane = 3
            thunderstorm = 4
            tornado = 5
            blizzard = 6

        class AdverseWeatherCondition_Adhesion(enum.IntEnum):
            unavailable = 0
            heavyFrostOnRoad = 1
            fuelOnRoad = 2
            mudOnRoad = 3
            snowOnRoad = 4
            iceOnRoad = 5
            blackIceOnRoad = 6
            oilOnRoad = 7
            looseChippings = 8
            instantBlackIce = 9
            roadsSalted = 10

        class AdverseWeatherCondition_Visibility(enum.IntEnum):
            unavailable = 0
            fog = 1
            smoke = 2
            heavySnowfall = 3
            heavyRain = 4
            heavyHail = 5
            lowSunGlare = 6
            sandstorms = 7
            swarmsOfInsects = 8

        class AdverseWeatherCondition_Precipitation(enum.IntEnum):
            unavailable = 0
            heavyRain = 1
            heavySnowfall = 2
            softHail = 3

        class SlowVehicle(enum.IntEnum):
            unavailable = 0
            maintenanceVehicle = 1
            vehiclesSlowingToLookAtAccident = 2
            abnormalLoad = 3
            abnormalWideLoad = 4
            convoy = 5
            snowplough = 6
            deicing = 7
            saltingVehicles = 8

        class StationaryVehicle(enum.IntEnum):
            unavailable = 0
            humanProblem = 1
            vehicleBreakdown = 2
            postCrash = 3
            publicTransportStop = 4
            carryingDangerousGoods = 5

        class HumanProblem(enum.IntEnum):
            unavailable = 0
            glycemiaProblem = 1
            heartProblem = 2

        class EmergencyVehicleApproaching(enum.IntEnum):
            unavailable = 0
            emergencyVehicleApproaching = 1
            prioritizedVehicleApproaching = 2

        class HazardousLocation_DangerousCurve(enum.IntEnum):
            unavailable = 0
            dangerousLeftTurnCurve = 1
            dangerousRightTurnCurve = 2
            multipleCurvesStartingWithUnknownTurningDirection = 3
            multipleCurvesStartingWithLeftTurn = 4
            multipleCurvesStartingWithRightTurn = 5

        class HazardousLocation_SurfaceCondition(enum.IntEnum):
            unavailable = 0
            rockfalls = 1
            earthquakeDamage = 2
            sewerCollapse = 3
            subsidence = 4
            snowDrifts = 5
            stormDamage = 6
            burstPipe = 7
            volcanoEruption = 8
            fallingIce = 9

        class HazardousLocation_ObstacleOnTheRoad(enum.IntEnum):
            unavailable = 0
            shedLoad = 1
            partsOfVehicles = 2
            partsOfTyres = 3
            bigObjects = 4
            fallenTrees = 5
            hubCaps = 6
            waitingVehicles = 7

        class HazardousLocation_AnimalOnTheRoad(enum.IntEnum):
            unavailable = 0
            wildAnimals = 1
            herdOfAnimals = 2
            smallAnimals = 3
            largeAnimals = 4

        class CollisionRisk(enum.IntEnum):
            unavailable = 0
            longitudinalCollisionRisk = 1
            crossingCollisionRisk = 2
            lateralCollisionRisk = 3
            vulnerableRoadUser = 4

        class SignalViolation(enum.IntEnum):
            unavailable = 0
            stopSignViolation = 1
            trafficLightViolation = 2
            turningRegulationViolation = 3

        class RescueAndRecoveryWorkInProgress(enum.IntEnum):
            unavailable = 0
            emergencyVehicles = 1
            rescueHelicopterLanding = 2
            policeActivityOngoing = 3
            medicalEmergencyOngoing = 4
            childAbductionInProgress = 5

        class DangerousEndOfQueue(enum.IntEnum):
            unavailable = 0
            suddenEndOfQueue = 1
            queueOverHill = 2
            queueAroundBend = 3
            queueInTunnel = 4

        class DangerousSituation(enum.IntEnum):
            unavailable = 0
            emergencyElectronicBrakeEngaged = 1
            preCrashSystemEngaged = 2
            espEngaged = 3
            absEngaged = 4
            aebEngaged = 5
            brakeWarningEngaged = 6
            collisionRiskWarningEngaged = 7

        class VehicleBreakdown(enum.IntEnum):
            unavailable = 0
            lackOfFuel = 1
            lackOfBatteryPower = 2
            engineProblem = 3
            transmissionProblem = 4
            engineCoolingProblem = 5
            brakingSystemProblem = 6
            steeringProblem = 7
            tyrePuncture = 8

        class PostCrash(enum.IntEnum):
            unavailable = 0
            accidentWithoutECallTriggered = 1
            accidentWithECallManuallyTriggered = 2
            accidentWithECallAutomaticallyTriggered = 3
            accidentWithECallTriggeredWithoutAccessToCellularNetwork = 4

        Any: TypeAlias = (
            TrafficCondition
            | Accident
            | Roadworks
            | HumanPresenceOnTheRoad
            | WrongWayDriving
            | AdverseWeatherCondition_ExtremeWeatherCondition
            | AdverseWeatherCondition_Adhesion
            | AdverseWeatherCondition_Visibility
            | AdverseWeatherCondition_Precipitation
            | SlowVehicle
            | StationaryVehicle
            | HumanProblem
            | EmergencyVehicleApproaching
            | HazardousLocation_DangerousCurve
            | HazardousLocation_SurfaceCondition
            | HazardousLocation_ObstacleOnTheRoad
            | HazardousLocation_AnimalOnTheRoad
            | CollisionRisk
            | SignalViolation
            | RescueAndRecoveryWorkInProgress
            | DangerousEndOfQueue
            | DangerousSituation
            | VehicleBreakdown
            | PostCrash
        )

    _seq_nums = dict()

    def __init__(
        self,
        *,
        uuid: str,
        gnss_report: GNSSReport,
        detection_time: Optional[float] = None,
        cause: Cause = Cause.dangerousSituation,
        subcause: Optional[SubCause.Any] = None,
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
            ] = termination.value
        if validity_duration is not None:
            self._message["message"]["management_container"][
                "validity_duration"
            ] = validity_duration
        if subcause is not None:
            self.subcause = subcause

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
    def reference_time(self) -> float:
        return etsi.ETSI.etsi2unix_time(
            self._message["message"]["management_container"]["reference_time"]
        )

    @reference_time.setter
    def reference_time(self, reference_time: float):
        self._message["message"]["management_container"]["reference_time"] = (
            etsi.ETSI.unix2etsi_time(reference_time)
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
    def subcause(self) -> SubCause.Any | None:
        try:
            subcause = self._message["message"]["situation_container"]["event_type"][
                "subcause"
            ]
        except KeyError:
            return None
        cause_name = self.cause.name[0].upper() + self.cause.name[1:]
        SubCauseClass = getattr(self.SubCause, cause_name)
        return SubCauseClass(subcause)

    @subcause.setter
    def subcause(self, subcause: SubCause.Any | None):
        if subcause is None:
            try:
                del self._message["message"]["situation_container"]["event_type"][
                    "subcause"
                ]
            except KeyError:
                # Already missing, ignore
                pass
        else:
            cause_name = self.cause.name[0].upper() + self.cause.name[1:]
            SubCauseClass = getattr(self.SubCause, cause_name)
            if not isinstance(subcause, SubCauseClass):
                raise ValueError(
                    f"subcause should be a DENM.SubCause.{cause_name} (not a {type(subcause)})"
                )
            self._message["message"]["situation_container"]["event_type"][
                "subcause"
            ] = subcause.value

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
