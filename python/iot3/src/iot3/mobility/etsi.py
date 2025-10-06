# Software Name: IoT3 Mobility
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import abc
import datetime
import enum
import hashlib
import its_quadkeys
import json
import math
from typing import Optional
from . import leapseconds


class ETSI(abc.ABC):
    """Various ETSI-related constants

    Constants that help convert from SI units to ETSI scaled values.

    Note that the SI unit for angles is the radian, not the degree. However,
    when dealing with geo coordinates, it is more usual to use degrees to
    represent latitude and longitude. Converting between degrees and radians
    is readily available in python (using math.degrees() or math.radians()),
    so we do not provide a scale for the conversion.
    """

    # ETSI EPOCH, as a UNIX timestamp (int)
    EPOCH: int = int(
        leapseconds.utc_to_tai(
            datetime.datetime.fromisoformat(
                "2004-01-01T00:00:00.000",
            )
        ).timestamp()
    )

    # Length scales
    METER: float = 1.0
    DECI_METER: float = METER / 10
    CENTI_METER: float = METER / 100
    MILLI_METER: float = METER / 1_000
    KILO_METER: float = METER * 1_000

    # Time scales
    SECOND: float = 1.0
    MILLI_SECOND: float = SECOND / 1_000
    MICRO_SECOND: float = SECOND / 1_000_000
    NANO_SECOND: float = SECOND / 1_000_000_000
    HOUR: float = 3600.0 * SECOND

    # Speed scales
    METER_PER_SECOND: float = METER / SECOND
    CENTI_METER_PER_SECOND: float = CENTI_METER / SECOND
    KILO_METER_PER_HOUR: float = KILO_METER / HOUR

    # Acceleration scales
    METER_PER_SECOND_SECOND: float = METER / (SECOND * SECOND)
    DECI_METER_PER_SECOND_SECOND: float = DECI_METER / (SECOND * SECOND)

    # Angle scales
    DEGREE: float = 1.0
    DECI_DEGREE: float = DEGREE / 10
    CENTI_DEGREE: float = DEGREE / 100
    DECI_MICRO_DEGREE: float = DEGREE / 10_000_000

    # Rotation speed scales
    DEGREE_PER_SECOND: float = DEGREE / SECOND
    CENTI_DEGREE_PER_SECOND: float = CENTI_DEGREE / SECOND

    @staticmethod
    def si2etsi(
        value: float | None,
        scale: float,
        undef: Optional[int] = None,
        validity_range: Optional[dict] = None,
        out_of_range: Optional[int] = None,
    ) -> int:
        """SI to ETSI unit conversions

        Each key in an ETSI object has its own scale, so there is no "ETSI unit"
        per-se, but a myriad of scales, each applicable to a specific key of a
        specific object. This function converts from an SI unit to an ETSI scale.

        :param value: the value in the SI unit, or None when the value is unknown
        :param scale: the ETSI scale of the key
        :param undef: the special ETSI-scaled value to use when the value is unknown;
                      if undef is None, the value must not be None.
        :param validity_range: the lower and upper bounds of the value range as a
                      dict with keys "min" and "max", in ETSI scale; the bounds are
                      inclusive, but must not include undef and out_of_range.
        :param out_of_range: the special ETSI-scaled value to use when the value is
                             out of range; if out_of_range is None, the encoded
                             value is capped by the specified range if provided.
        :return: the special ETSI-scaled value 'undef' when the value is None, the
                 special ETSI-scaled value 'out_of_range' if the value is out of
                 range, or the value scaled to the ETSI scale otherwise

        For example:
            si2etsi(
                get_altitude_or_None(),
                ETSI.CENTI_METER,
                800001,
                {"min": 0, "max": 800000},
                800002,
            )

        Assuming that get_altitude_or_None() returns a number when the altitude is
        known, or None when it is unknown, the above code would return 800001 if the
        altitude is not known, 800002 if the altitude is strictly lower than 0 or
        strictly greater than 800000 (8000m), or the altitude as an integral number
        of centimeters.
        """
        if value is None:
            if undef is None:
                raise AttributeError(
                    "This conversion to an ETSI scale does not accept an unknown value (None)",
                )
            return undef
        etsi_value = int(round(value / scale))
        if validity_range is not None:
            if etsi_value < validity_range["min"]:
                etsi_value = (
                    out_of_range if out_of_range is not None else validity_range["min"]
                )
            if etsi_value > validity_range["max"]:
                etsi_value = (
                    out_of_range if out_of_range is not None else validity_range["max"]
                )
        return etsi_value

    @staticmethod
    def etsi2si(
        value: int,
        scale: float,
        undef: Optional[int] = None,
        out_of_range: Optional[int] = None,
    ) -> float | None:
        """ETSI to SI unit conversions

        Each key in an ETSI object has its own scale, so there is no "ETSI unit"
        per-se, but a myriad of scales, each applicable to a specific key of a
        specific object. This function converts from an ETSI scale to an SI unit.

        :param value: the value in an ETSI scale, or its special value when it
                      is unknown
        :param scale: the ETSI scale of the key
        :param undef: the special ETSI-scaled value used when the value is unknown
        :param out_of_range: the special ETSI-scaled value to use when the value is
                             out of range
        :return: None if the value is either one of the special ETSI-scaled values
                 'undef' or 'out_of_range', or the value scaled back to SI units
                 otherwise

        For example:
            etsi2si(my_cam.altitude, ETSI.CENTI_METER, 800001)

        Assuming that my_cam.altitude contains an integer when the altitude is
        known, or the special value 800001 when it is unknown, or the special
        value 800002 when it is out of range, the above code would return the
        altitude as a floating point numbers of meters, or None if the altitude
        is not known or out of range.
        """
        if undef is not None and value == undef:
            return None
        if out_of_range is not None and value == out_of_range:
            return None
        return float(value) * scale

    @staticmethod
    def unix2etsi_time(
        unix_time: float,
    ) -> int:
        """Convert UNIX timestamp to ETSI timestamp

        :param unix_time: The UNIX timestamp, in seconds since the UNIX EPOCH,
                          with arbitrary sub-second precision
        :return: The ETSI timestamp, as the number of milliseconds elapsed since
                 the ETSI EPOCH.

        If the value is a date before the ETSI EPOCH, the returned value is
        negative; it is left to the caller to decide whether that is usable
        in their case.

        NOTE: the result is undefined during a leap second.

        Test from the ETSI ITS CDD 2.1.1 example:
        https://forge.etsi.org/rep/ITS/asn1/cpm_ts103324/-/blob/v2.1.1/docs/ETSI-ITS-CDD.md#timestampits

        >>> from datetime import datetime
        >>> unix2etsi_time(datetime(2007, 1, 1).timestamp())
        94694401000
        """
        tai_time = leapseconds.utc_to_tai(
            datetime.datetime.utcfromtimestamp(unix_time)
        ).timestamp()
        return ETSI.si2etsi(tai_time - ETSI.EPOCH, ETSI.MILLI_SECOND, 0)

    @staticmethod
    def etsi2unix_time(
        etsi_time: int,
    ) -> float:
        """Convert ETSI timestamp to UNIX timestamp

        :param etsi_time: The ETSI timestamp, as the number of milliseconds
                          elapsed since the ETSI EPOCH.
        :return: The UNIX timestamp, in seconds since the UNIX EPOCH, with
                 arbitrary sub-second precision (in practice, down to millisecond
                 precision).

        Test from the ETSI ITS CDD 2.1.1 example:
        https://forge.etsi.org/rep/ITS/asn1/cpm_ts103324/-/blob/v2.1.1/docs/ETSI-ITS-CDD.md#timestampits

        >>> from datetime import datetime
        >>> etsi2unix_time(94694401000)
        1167609600.0
        >>> datetime.datetime.utcfromtimestamp(etsi2unix_time(94694401000))
        datetime.datetime(2007, 1, 1, 0, 0)
        """
        tai_time = ETSI.etsi2si(etsi_time, ETSI.MILLI_SECOND, 0) + ETSI.EPOCH
        return leapseconds.tai_to_utc(
            datetime.datetime.utcfromtimestamp(tai_time)
        ).timestamp()

    @staticmethod
    def generation_delta_time(
        unix_time: float,
    ) -> int:
        """Convert a UNIX timestamp into an ETSI generation delta time

        :param unix_time:
        """
        return ETSI.unix2etsi_time(unix_time) % 65536

    @abc.abstractmethod
    def __init__(self, *args, **kwargs):
        """It is not allowed to create an instance of this class."""
        ...


class Message(abc.ABC):
    msg_type = ...

    class StationType(enum.IntEnum):
        # We need those values to *exactly* match those defined in the spec
        unknown = 0
        pedestrian = 1
        cyclist = 2
        moped = 3
        motorcycle = 4
        passengerCar = 5
        bus = 6
        lightTruck = 7
        heavyTruck = 8
        trailer = 9
        specialVehicles = 10
        tram = 11
        roadSideUnit = 15

    @abc.abstractmethod
    def __init__(self, *args, **kwargs):
        """Sub-classes must provide their own, explicit constructor."""
        ...

    @staticmethod
    def station_id(uuid: str) -> int:
        return int(
            hashlib.sha256(uuid.encode()).hexdigest()[:6],
            16,
        )

    def __getitem__(self, key):
        return self._message[key]

    def __setitem__(self, key, value):
        self._message[key] = value

    def __delitem__(self, key):
        del self._message[key]

    def topic(
        self,
        *,
        template: str,
        depth: int = 22,
    ):
        """Formats a template with fields from the message

        :param template: The template string to format
        :param depth: The depth of the quadkey

        Available fields:
          - source_uuid     the source_uuid of the message
          - msg_type        the type of the message, in lower-case (cam, denm...)
          - quadkey         the quadkey, at the specified depth, with each level
                            separated by a forward slash '/'

        For example:
            message.topic(
                template="prefix/{source_uuid}/{msg_type}/{quadkey}",
                depth=8,
            )
        would return something like:
            "prefix/entity_id/cam/0/1/2/3/0/1/2/3"
        """
        if self.msg_type is ...:
            raise NameError(f"No message type for {self}")

        if self.latitude is None or self.longitude is None:
            raise RuntimeError(f"Missing latitude and/or longitude")

        quadkey = its_quadkeys.QuadKey(
            (
                self.latitude,
                self.longitude,
                depth,
            )
        )

        return template.format(
            source_uuid=self._message["source_uuid"],
            msg_type=self.msg_type,
            quadkey=quadkey.to_str("/"),
        )

    def to_json(self) -> str:
        # Return the densest-possible JSON sentence
        return json.dumps(self._message, separators=(",", ":"))
