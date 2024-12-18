# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import datetime as _datetime


class ETSI:
    # ETSI EPOCH, as a UNIX timestamp (int)
    EPOCH = int(
        _datetime.datetime.fromisoformat("2004-01-01T00:00:00.000+00:00").timestamp()
    )

    @staticmethod
    def generation_delta_time(timestamp: float) -> int:
        return (
            SI2ETSI.seconds(
                timestamp - ETSI.EPOCH,
                SI2ETSI.MILLI_SECOND,
                0,
            )
            % 65536
        )


class SI2ETSI:
    # Length units
    METER = 1.0
    DECI_METER = METER / 10
    CENTI_METER = METER / 100

    # Time units
    SECOND = 1.0
    MILLI_SECOND = SECOND / 1_000

    # Speed units
    METER_PER_SECOND = METER / SECOND
    CENTI_METER_PER_SECOND = CENTI_METER / SECOND

    # Acceleration units
    METER_PER_SECOND_SECOND = METER / (SECOND * SECOND)
    DECI_METER_PER_SECOND_SECOND = DECI_METER / (SECOND * SECOND)

    # Degrees angle units
    DEGREE = 1.0
    DECI_DEGREE = DEGREE / 10
    DECI_MICRO_DEGREE = DEGREE / 10_000_000

    @staticmethod
    def meters(meters: float | None, scale: float, undef: int) -> int:
        return SI2ETSI._do_convert(meters, scale, undef)

    @staticmethod
    def seconds(seconds: float | None, scale: float, undef: int) -> int:
        return SI2ETSI._do_convert(seconds, scale, undef)

    @staticmethod
    def meters_per_second(mps: float | None, scale: float, undef: int) -> int:
        return SI2ETSI._do_convert(mps, scale, undef)

    @staticmethod
    def meters_per_second_second(mpss: float | None, scale: float, undef: int) -> int:
        return SI2ETSI._do_convert(mpss, scale, undef)

    @staticmethod
    def degrees(degrees: float | None, scale: float, undef: int) -> int:
        return SI2ETSI._do_convert(degrees, scale, undef)

    @staticmethod
    def _do_convert(value: float | None, scale: float, undef: int) -> int:
        return undef if value is None else int(round(value / scale))


__all__ = [
    "ETSI",
    "SI2ETSI",
]
