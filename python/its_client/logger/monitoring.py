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
import logging


def monitore_cam(
    vehicle_id: str,
    direction: str,
    station_id: int,
    generation_delta_time: int,
    latitude: float,
    longitude: float,
    timestamp: int,
    partner: str,
    root_queue: str,
):
    logging.getLogger("monitoring").info(
        f"{vehicle_id} cam {direction} {partner}/{root_queue}"
        f" {station_id}/{generation_delta_time}/lat:{latitude}/lng:{longitude} at {timestamp} "
    )


def monitore_denm(
    vehicle_id: str,
    station_id: int,
    originating_station_id: int,
    sequence_number: int,
    reference_time: int,
    detection_time: int,
    latitude: float,
    longitude: float,
    timestamp: int,
    partner: str,
    root_queue: str,
    sender: str,
):
    logging.getLogger("monitoring").info(
        f"{vehicle_id} denm received_on {partner}/{root_queue}/{sender}"
        f" {station_id}/{originating_station_id}/{sequence_number}/{reference_time}/{detection_time}"
        f"/lat:{latitude}/lng:{longitude} at {timestamp}"
    )
