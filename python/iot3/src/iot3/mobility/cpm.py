# Software Name: IoT3 Mobility
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import dataclasses
import time

from typing import Iterable, Optional

from . import etsi
from .gnss import GNSSReport


class CollectivePerceptionMessage(etsi.Message):
    msg_type = "cpm"

    @dataclasses.dataclass
    class PerceivedObject:
        """A simple wrapper to define a basice Perceived Object

        All measures are in SI units, with times in seconds relative to
        the UNIX EPOCH, and ages in seconds.

        The object_class is a dict matching the object_class of the
        perceived object classification (see CPM schema).
        """

        class Vehicle:
            unknown = {"vehicle": etsi.Message.TrafficParticipantType.unknown}
            pedestrian = {"vehicle": etsi.Message.TrafficParticipantType.pedestrian}
            cyclist = {"vehicle": etsi.Message.TrafficParticipantType.cyclist}
            moped = {"vehicle": etsi.Message.TrafficParticipantType.moped}
            motorcycle = {"vehicle": etsi.Message.TrafficParticipantType.motorcycle}
            passengerCar = {"vehicle": etsi.Message.TrafficParticipantType.passengerCar}
            bus = {"vehicle": etsi.Message.TrafficParticipantType.bus}
            lightTruck = {"vehicle": etsi.Message.TrafficParticipantType.lightTruck}
            heavyTruck = {"vehicle": etsi.Message.TrafficParticipantType.heavyTruck}
            trailer = {"vehicle": etsi.Message.TrafficParticipantType.trailer}
            specialVehicles = {
                "vehicle": etsi.Message.TrafficParticipantType.specialVehicles
            }
            tram = {"vehicle": etsi.Message.TrafficParticipantType.tram}
            lightVruVehicle = {
                "vehicle": etsi.Message.TrafficParticipantType.lightVruVehicle
            }
            animal = {"vehicle": etsi.Message.TrafficParticipantType.animal}
            roadSideUnit = {"vehicle": etsi.Message.TrafficParticipantType.roadSideUnit}

        class Vru:
            class Pedestrian:
                unavailable = {"vru": {"pedestrian": 0}}
                ordinary_pedestrian = {"vru": {"pedestrian": 1}}
                road_worker = {"vru": {"pedestrian": 2}}
                first_responder = {"vru": {"pedestrian": 3}}

            class BicyclistAndLightVruVehicle:
                unavailable = {"vru": {"bicyclist_and_light_vru_vehicle": 0}}
                bicyclist = {"vru": {"bicyclist_and_light_vru_vehicle": 1}}
                wheelchair_user = {"vru": {"bicyclist_and_light_vru_vehicle": 2}}
                horse_and_rider = {"vru": {"bicyclist_and_light_vru_vehicle": 3}}
                rollerskater = {"vru": {"bicyclist_and_light_vru_vehicle": 4}}
                e_scooter = {"vru": {"bicyclist_and_light_vru_vehicle": 5}}
                personal_transporter = {"vru": {"bicyclist_and_light_vru_vehicle": 6}}
                pedelec = {"vru": {"bicyclist_and_light_vru_vehicle": 7}}
                speed_pedelec = {"vru": {"bicyclist_and_light_vru_vehicle": 8}}

            class Motorcylist:
                unavailable = {"vru": {"bicyclist_and_light_vru_vehicle": 0}}
                moped = {"vru": {"bicyclist_and_light_vru_vehicle": 1}}
                motorcycle = {"vru": {"bicyclist_and_light_vru_vehicle": 2}}
                motorcycle_and_sidecar_right = {
                    "vru": {"bicyclist_and_light_vru_vehicle": 3}
                }
                motorcycle_and_sidecar_left = {
                    "vru": {"bicyclist_and_light_vru_vehicle": 4}
                }

        class Other:
            unknown = {"other": 0}
            single_object = {"other": 1}
            multiple_objects = {"other": 2}
            bulk_material = {"other": 3}

        object_id: int
        measurement_delta_time: float
        x_distance: float
        y_distance: float
        object_age: Optional[float] = None
        x_speed: Optional[float] = None
        y_speed: Optional[float] = None
        quality: Optional[int] = 0
        object_class: Optional[Vehicle | Vru | Other] = None
        object_class_confidence: int = 0

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

        self._timestamp = time.time()

        self._message = dict(
            {
                "message_type": "cpm",
                "source_uuid": uuid,
                "timestamp": (
                    etsi.ETSI.si2etsi(
                        self._timestamp,
                        etsi.ETSI.MILLI_SECOND,
                    )
                ),
                "version": "2.1.1",
                "message": {
                    "protocol_version": 1,
                    "station_id": self.station_id(uuid),
                    "management_container": {
                        "station_type": station_type,
                        "reference_time": etsi.ETSI.unix2etsi_time(self._timestamp),
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
                            "altitude": {
                                "value": etsi.ETSI.si2etsi(
                                    gnss_report.altitude,
                                    etsi.ETSI.CENTI_METER,
                                    800001,
                                ),
                                # Encoding the altitude error is a non-linear search in
                                # an array... Let's consider it unavailable for now.
                                "confidence": etsi.ETSI.si2etsi(
                                    None,
                                    etsi.ETSI.CENTI_METER,
                                    15,
                                ),
                            },
                            "position_confidence_ellipse": {
                                # We treat the 2D error as a circle, so semi-major
                                # and semi-minor are eqal, and thus the orientation
                                # of the elipse does not matter.
                                "semi_major": etsi.ETSI.si2etsi(
                                    gnss_report.horizontal_error,
                                    etsi.ETSI.CENTI_METER,
                                    4095,
                                    {"min": 0, "max": 4093},
                                    4094,
                                ),
                                "semi_minor": etsi.ETSI.si2etsi(
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
                    "perceived_object_container": [],
                },
            },
        )

        if station_type == etsi.Message.StationType.unknown:
            # Unknown station type, no station data.
            pass
        elif station_type == etsi.Message.StationType.roadSideUnit:
            # We for now don't have enough to handle the MAPEM to define an RSU;
            # the RSU container is just a list of stuff, so just provide an empty
            # list for now.
            self._message["message"]["originating_rsu_container"] = []
        else:
            self._message["message"]["originating_vehicle_container"] = {
                "orientation_angle": {
                    "value": etsi.ETSI.si2etsi(
                        gnss_report.true_heading,
                        etsi.ETSI.DECI_DEGREE,
                        3601,
                    ),
                    "confidence": 127,
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
                "measurement_delta_time": etsi.ETSI.si2etsi(
                    perceived_object.measurement_delta_time,
                    etsi.ETSI.MILLI_SECOND,
                    None,
                    {"min": -2048, "max": 2047},
                ),
                "position": {
                    "x_coordinate": {
                        "value": etsi.ETSI.si2etsi(
                            perceived_object.x_distance,
                            etsi.ETSI.CENTI_METER,
                        ),
                        "confidence": 4096,
                    },
                    "y_coordinate": {
                        "value": etsi.ETSI.si2etsi(
                            perceived_object.y_distance,
                            etsi.ETSI.CENTI_METER,
                        ),
                        "confidence": 4096,
                    },
                },
                "velocity": {
                    "cartesian_velocity": {
                        "x_velocity": {
                            "value": etsi.ETSI.si2etsi(
                                perceived_object.x_speed,
                                etsi.ETSI.CENTI_METER_PER_SECOND,
                                16_383,
                                {"min": -16_383, "max": 16_382},
                            ),
                            "confidence": 127,
                        },
                        "y_velocity": {
                            "value": etsi.ETSI.si2etsi(
                                perceived_object.y_speed,
                                etsi.ETSI.CENTI_METER_PER_SECOND,
                                16_383,
                                {"min": -16_383, "max": 16_382},
                            ),
                            "confidence": 127,
                        },
                    },
                },
                "object_perception_quality": perceived_object.quality,
            },
        )
        if perceived_object.object_age is not None:
            po["object_age"] = etsi.ETSI.si2etsi(
                perceived_object.object_age,
                etsi.ETSI.MILLI_SECOND,
                None,
                {"min": 0, "max": 2047},
            )

        if perceived_object.object_class is not None:
            c = dict(
                {
                    "object_class": perceived_object.object_class,
                    "confidence": perceived_object.object_class_confidence,
                }
            )
            po["classification"] = list([c])

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
            ]["value"],
            etsi.ETSI.CENTI_METER,
            800001,
        )

    @altitude.setter
    def altitude(self, altitude):
        self._message["message"]["management_container"]["reference_position"][
            "altitude"
        ]["value"] = etsi.ETSI.si2etsi(
            altitude,
            etsi.ETSI.CENTI_METER,
            800001,
        )

    @property
    def perceived_objects(self):
        for po in self._message["message"]["perceived_object_container"]:
            try:
                x_speed = po["velocity"]["cartesian_velocity"]["x_velocity"]["value"]
            except KeyError:
                x_speed = None
            try:
                y_speed = po["velocity"]["cartesian_velocity"]["y_velocity"]["value"]
            except KeyError:
                y_speed = None

            kwargs = {
                "object_id": po["object_id"],
                "object_age": etsi.ETSI.etsi2si(
                    po.get("object_age", None),
                    etsi.ETSI.MILLI_SECOND,
                ),
                "measurement_delta_time": etsi.ETSI.etsi2si(
                    po["measurement_delta_time"],
                    etsi.ETSI.MILLI_SECOND,
                )
                + self._timestamp,
                "x_distance": etsi.ETSI.etsi2si(
                    po["position"]["x_coordinate"]["value"],
                    etsi.ETSI.CENTI_METER,
                ),
                "y_distance": etsi.ETSI.etsi2si(
                    po["position"]["y_coordinate"]["value"],
                    etsi.ETSI.CENTI_METER,
                ),
                "x_speed": etsi.ETSI.etsi2si(
                    x_speed,
                    etsi.ETSI.CENTI_METER_PER_SECOND,
                    16_383,
                ),
                "y_speed": etsi.ETSI.etsi2si(
                    y_speed,
                    etsi.ETSI.CENTI_METER_PER_SECOND,
                    16_383,
                ),
                "quality": po["object_perception_quality"],
            }

            if "classification" in po:
                # Only keep the best classification
                best = sorted(
                    po["classification"],
                    key=lambda k: k["confidence"],
                    reverse=True,
                )[0]
                kwargs.update(
                    {
                        "object_class": best["object_class"],
                        "object_class_confidence": best["confidence"],
                    },
                )

            yield self.PerceivedObject(**kwargs)


# Shorthand
CPM = CollectivePerceptionMessage
