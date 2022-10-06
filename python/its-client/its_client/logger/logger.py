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
import logging.handlers
import sys
from pathlib import Path


def filter_monitoring(record):
    return (
        record.funcName == "monitore_cam"
        or record.funcName == "monitore_cpm"
        or record.funcName == "monitore_denm"
    )


def filter_reception(record):
    return record.funcName == "record"


def filter_sending(record):
    return record.funcName == "create_cam" or record.funcName == "create_denm"


def filter_default(record):
    return (
        not record.funcName.startswith("create_")
        and not record.funcName == "record"
        and not record.funcName.startswith("monitore_")
    )


def log_setup(directory: str = "/data", log_level=logging.WARNING):
    path = Path(directory + "/its_client")
    path.mkdir(parents=True, exist_ok=True)
    # monitoring
    monitoring_logger = logging.getLogger("monitoring")
    monitoring_handler = logging.handlers.RotatingFileHandler(
        filename=path / "monitoring.csv",
        maxBytes=2000000,
        backupCount=10,
    )
    monitoring_handler.addFilter(filter_monitoring)
    monitoring_logger.addHandler(monitoring_handler)
    # let's monitor on any level
    monitoring_logger.setLevel("DEBUG")

    # reception
    reception_logger = logging.getLogger("reception")
    reception_handler = logging.handlers.RotatingFileHandler(
        filename=path / "reception.txt", maxBytes=200000000, backupCount=10
    )
    reception_handler.addFilter(filter_reception)
    reception_logger.addHandler(reception_handler)
    reception_logger.setLevel(log_level)

    # sending
    sending_logger = logging.getLogger("sending")
    sending_handler = logging.handlers.RotatingFileHandler(
        filename=path / "sending.txt",
        maxBytes=2000000,
        backupCount=10,
    )
    sending_handler.addFilter(filter_sending)
    sending_logger.addHandler(sending_handler)
    sending_logger.setLevel(log_level)

    # default log
    logger = logging.getLogger()
    logger_handler = logging.StreamHandler(stream=sys.stdout)
    # this is just to make the output look nice
    logger_formatter = logging.Formatter(fmt="%(asctime)s %(levelname)s: %(message)s")
    logger_handler.setFormatter(logger_formatter)
    logger_handler.addFilter(filter_default)
    logger.addHandler(logger_handler)
    logger.setLevel(log_level)
