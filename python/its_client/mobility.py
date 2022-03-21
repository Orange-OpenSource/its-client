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
from math import sin, cos, sqrt, atan2, radians


def compute_distance(
    latitude_start: float,
    longitude_start: float,
    latitude_end: float,
    longitude_end: float,
) -> float:
    radius = 6371  # Radius of the earth in km
    d_lat = radians(latitude_end - latitude_start)
    d_lon = radians(longitude_end - longitude_start)
    r_lat1 = radians(latitude_start)
    r_lat2 = radians(latitude_end)
    a = sin(d_lat / 2) * sin(d_lat / 2) + cos(r_lat1) * cos(r_lat2) * sin(
        d_lon / 2
    ) * sin(d_lon / 2)
    c = 2 * atan2(sqrt(a), sqrt(1 - a))
    d = radius * c  # Distance in km
    return d


def compute_velocity(distance, time_start, time_end) -> float:
    """Return 0 if time_start == time_end, avoid dividing by 0"""
    return distance / (time_end - time_start) if time_end > time_start else 0


def compute_acceleration(
    latitude_start: float,
    longitude_start: float,
    latitude_end: float,
    longitude_end: float,
    time_start,
    time_end,
) -> float:
    return compute_velocity(
        distance=compute_distance(
            latitude_start=latitude_start,
            longitude_start=longitude_start,
            latitude_end=latitude_end,
            longitude_end=longitude_end,
        ),
        time_start=time_start,
        time_end=time_end,
    )


def kmph_to_mps(kmph):
    """
    Function to convert speed in km/hr to m/sec
    :param kmph: the speed in km/hr
    :return: the speed in m/sec
    """
    return 0.277778 * kmph


def mps_to_kmph(mps):
    """
    Function to convert speed in m/sec to km/hr
    :param mps: the speed in m/sec
    :return: the speed in km/hr
    """
    return 3.6 * mps
