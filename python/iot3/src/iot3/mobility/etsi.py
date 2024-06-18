# Software Name: IoT3 Mobility
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import abc
import datetime
import math
from typing import Optional


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
        datetime.datetime.fromisoformat(
            "2004-01-01T00:00:00.000+00:00",
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
        undef: int,
        range: Optional[dict] = None,
        out_of_range: Optional[int] = None,
    ) -> int:
        """SI to ETSI unit conversions

        Each key in an ETSI object has its own scale, so there is no "ETSI unit"
        per-se, but a myriad of scales, each applicable to a specific key of a
        specific object. This function converts from an SI unit to an ETSI scale.

        :param value: the value in the SI unit, or None when the value is unknown
        :param scale: the ETSI scale of the key
        :param undef: the special ETSI-scaled value to use when the value is unknown
        :param validity_range: the lower and upper bounds of the value range as a
                      dict with keys "min" and "max", in ETSI scale; the bounds are
                      inclusive, but must not include undef and out_of_range.
        :param out_of_range: the special ETSI-scaled value to use when the value is
                             out of range
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
            return undef
        etsi_value = int(round(value / scale))
        if range is not None:
            if etsi_value < range["min"]:
                etsi_value = out_of_range
            if etsi_value > range["max"]:
                etsi_value = out_of_range
        return etsi_value

    @staticmethod
    def etsi2si(
        value: int,
        scale: float,
        undef: int,
        out_of_range: Optional[int] = None,
    ) -> float | None:
        """ETSI to SI unit conversions

        Each key in an ETSI object has its own scale, so there is no "ETSI unit"
        per-se, but a myriad of scales, each applicable to a specific key of a
        specific object. This function converts from an ETSI scale to an SI unit.

        :param value: the value in an ETSI scale, or its special value when it
                      is unknown
        :param scale: the ETSI scale of the key
        :param undef: the special ETSI-scaled value to use when the value is unknown
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
        if value == undef:
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
        """
        return ETSI.si2etsi(unix_time - ETSI.EPOCH, ETSI.MILLI_SECOND, 0)

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
        """
        return ETSI.etsi2si(etsi_time, ETSI.MILLI_SECOND, 0) + ETSI.EPOCH

    @staticmethod
    def generation_delta_time(
        unix_time: float
    ) -> int:
        """Convert a UNIX timestamp into an ETSI generation delta time

        :param unix_time:
        """
        return ETSI.unix2etsi_time(unix_time) % 65536

    @abc.abstractmethod
    def __init__(self, *args, **kwargs):
        """It is not allowed to create an instance of this class."""
        ...
