#!/usr/bin/python3
# coding: utf-8
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
import argparse
import logging
import os
import signal
import threading
import time
from configparser import ConfigParser

from its_client.logger import logger
from its_client.mqtt.mqtt_client import MQTTClient
from its_client.mqtt.mqtt_worker import MqttWorker
from its_client.position import static
from its_client.position import gpsd_py3

stop_signal = threading.Event()
worker_process = None
mqtt_client = None


def signal_handler(_sig, _frame):
    logging.info("stop signal received")
    stop_signal.set()
    if worker_process is not None:
        worker_process.join()
    if mqtt_client is not None:
        mqtt_client.loop_stop()
    exit(2)


signal.signal(signal.SIGINT, signal_handler)

if __name__ == "__main__":
    # argument parser
    parser = argparse.ArgumentParser(formatter_class=argparse.RawTextHelpFormatter)
    parser.add_argument(
        "-H",
        "--mqtt-host",
        dest="mqtt_hostname",
        default="localhost",
        help="hostname of the MQTT broker (default: localhost)",
    )
    parser.add_argument(
        "-P",
        "--mqtt-port",
        type=int,
        dest="mqtt_port",
        default=1883,
        help="port of the MQTT broker (default: 1883)",
    )
    parser.add_argument(
        "-u",
        "--mqtt-username",
        dest="mqtt_username",
        default="its_client",
        help="username to use when connecting to the MQTT broker (default: its_client)",
    )
    parser.add_argument(
        "-p",
        "--mqtt-password",
        dest="mqtt_password",
        default="password",
        help="password to use when connecting to the MQTT broker (default: password)",
    )
    parser.add_argument(
        "--mqtt-client-id",
        dest="mqtt_client_id",
        default=os.uname().nodename,
        help="identifier of MQTT client (default: the hostname)\n"
        "this must be unique in the broker",
    )
    parser.add_argument(
        "-g",
        "--geo-position",
        dest="geo_position",
        default="static",
        help="geo positioning: static or position",
    )
    parser.add_argument(
        "-l",
        "--log-level",
        dest="log_level",
        default="WARNING",
        help="logging level: CRITICAL, ERROR, WARNING, INFO or DEBUG",
    )
    parser.add_argument(
        "-c",
        "--config-path",
        dest="config_path",
        default=".",
        help="path to the its_config.cfg file (default: .)",
    )
    args, unknown_arguments = parser.parse_known_args()

    logger.log_setup(args.log_level)

    # config parser
    config = ConfigParser(allow_no_value=True)
    try:
        # Load the configuration file
        logging.info(f"we search the config file in {os.getcwd()}")
        with open(file=f"{args.config_path}/its_client.cfg") as file:
            config.read_file(file)
            logging.info(f"config loaded from its_client.cfg")
            # list all contents
            logging.info("list all contents:")
            for section in config.sections():
                logging.info(f"section: {section}")
                for options in config.options(section):
                    logging.info(
                        "x %s:::%s:::%s"
                        % (options, config.get(section, options), str(type(options)))
                    )
    except IOError:
        # if the user did not specify a config path and there is not a file
        # at the default path, just use the default settings.
        logging.warning(
            "no its_client.cfg config file specified or found, so using defaults"
        )
        config.add_section("position")
        config.set("position", "latitude", "43.6359296"),
        config.set("position", "longitude", "1.3752608"),

    logging.info("argument configuration:")
    for key, value in vars(args).items():
        logging.info(f"{key}: {value}")
    logging.info("unknown arguments:")
    logging.info(unknown_arguments)

    start_time = time.time()
    logging.info(f"started at {int(round(start_time * 1000))}")

    position_client = None
    if "static" == args.geo_position:
        position_client = static.GeoPosition(
            config.getfloat("position", "latitude"),
            config.getfloat("position", "longitude"),
        )
    elif "position" == args.geo_position:
        position_client = gpsd_py3.GeoPosition()
    else:
        logging.error(f"unable to detect the geo position:{args.geo_position}")
        exit(1)
    if position_client is not None:
        logging.info("starting mqtt client...")
        mqtt_client = MQTTClient(
            client_id=args.mqtt_client_id,
            host=args.mqtt_hostname,
            port=args.mqtt_port,
            geo_position=position_client,
            username=args.mqtt_username,
            password=args.mqtt_password,
        )
        mqtt_client.loop_start()

        logging.info("starting worker...")
        worker = MqttWorker(
            mqtt_client, args.mqtt_client_id, geo_position=position_client
        )
        worker_process = threading.Thread(target=worker.run, args=(stop_signal,))
        worker_process.start()
        worker_process.join()

        logging.info("waiting on mqtt client...")
        mqtt_client.loop_stop()

        logging.info(f"ended at {int(round(time.time() * 1000))}")
        exit(0)
