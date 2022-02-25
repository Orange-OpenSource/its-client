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
import logging.handlers
import time
from pathlib import Path


def filter_monitoring_message(record):
    return record.funcName == "monitore_cam" or record.funcName == "monitore_denm"


def filter_its_message(record):
    return record.funcName == "record"


def filter_creation_message(record):
    return record.funcName == "create_cam" or record.funcName == "create_denm"


def filter_default_message(record):
    return (
        not record.funcName.startswith("create_")
        and not record.funcName == "record"
        and not record.funcName.startswith("monitore_")
    )


def log_setup(log_level=logging.WARNING):
    Path("/var/log/its_client").mkdir(parents=True, exist_ok=True)
    # monitoring
    monitoring_logger = logging.getLogger("obu.monitoring")
    monitoring_handler = logging.handlers.RotatingFileHandler(
        filename="/var/log/its_client/monitoring-obu.csv",
        maxBytes=2000000,
        backupCount=10,
    )
    monitoring_logger.addHandler(monitoring_handler)
    monitoring_logger.addFilter(filter_monitoring_message)
    monitoring_logger.setLevel(log_level)

    # its
    its_logger = logging.getLogger("obu.its")
    its_handler = logging.handlers.RotatingFileHandler(
        filename="/var/log/its_client/its-obu.log", maxBytes=200000000, backupCount=10
    )
    its_logger.addHandler(its_handler)
    its_handler.addFilter(filter_its_message)
    its_handler.setLevel(log_level)

    # creation
    creation_logger = logging.getLogger("obu.creation")
    creation_handler = logging.handlers.RotatingFileHandler(
        filename="/var/log/its_client/creation-obu.log",
        maxBytes=2000000,
        backupCount=10,
    )
    creation_logger.addHandler(creation_handler)
    creation_handler.addFilter(filter_creation_message)
    creation_handler.setLevel(log_level)

    # default log
    logger = logging.getLogger()
    # log_handler = logging.StreamHandler()
    log_handler = logging.handlers.RotatingFileHandler(
        filename="/var/log/its_client/service.log", maxBytes=200000000, backupCount=10
    )
    log_formatter = logging.Formatter(
        "%(asctime)s.%(msecs)03d  obu [ %(thread)d %(threadName)s]: %(message)s",
        "%b %d %H:%M:%S",
    )
    log_formatter.converter = time.gmtime  # UTC time
    log_handler.setFormatter(log_formatter)
    logger.addHandler(log_handler)
    log_handler.addFilter(filter_default_message)
    logger.setLevel(log_level)
