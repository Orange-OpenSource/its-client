# Software Name: its-client
# SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
# SPDX-License-Identifier: MIT License
#
# This software is distributed under the MIT license, see LICENSE.txt file for more details.
#
# Author: Frédéric GARDES <frederic.gardes@orange.com> et al. Software description: This Intelligent Transportation
# Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](
# https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the
# mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
import logging


def record(
    json: str,
):
    logging.getLogger("obu.its").info(f"{json}")


def create_cam(
    json_cam: str,
):
    logging.getLogger("obu.creation").info(f"{json_cam}")
