#!/usr/bin/python3
# coding: utf-8
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
import signal
import threading
import time

from its_client import configuration
from its_client.mqtt.mqtt_client import MQTTClient
from its_client.mqtt.mqtt_worker import MqttWorker
from its_client.position import gpsd_py3
from its_client.position import static

stop_signal = threading.Event()
global worker_process
global mqtt_client


def signal_handler(_sig, _frame):
    global mqtt_client
    global worker_process
    logging.info("stop signal received")
    stop_signal.set()
    worker_process.join()
    mqtt_client.loop_stop()
    exit(3)


def main():
    global mqtt_client
    global worker_process
    start_time = time.time()

    config = configuration.build()

    if config.getboolean("broker", "tls", fallback=False):
        logging.error("TLS not yet supported")
        exit(1)

    logging.info(f"started at {int(round(start_time * 1000))}")

    if config.getboolean("position", "static"):
        latitude = config.getfloat("position", "latitude")
        longitude = config.getfloat("position", "longitude")
        altitude = config.getfloat("position", "altitude")
        heading = config.getfloat("position", "heading")
        speed = config.getfloat("position", "speed")
        logging.info(
            f"we use a static position:{latitude}, {longitude}, {altitude}, {speed}, {heading}"
        )
        position_client = static.GeoPosition(
            latitude=latitude,
            longitude=longitude,
            altitude=altitude,
            speed=speed,
            heading=heading,
        )
    else:
        logging.info(f"we use the gps position")
        position_client = gpsd_py3.GeoPosition()

    logging.debug("handling stop signal...")
    signal.signal(signal.SIGINT, signal_handler)

    broker = {
        # Not handling TLS, as we're not ready for it yet.
        "host": config.get(section="broker", option="host"),
        "port": config.getint(section="broker", option="port"),
        "username": config.get(section="broker", option="username"),
        "password": config.get(section="broker", option="password"),
        "client_id": config.get(section="broker", option="client_id"),
        "root_pub": config.get(section="broker", option="root_pub"),
        "root_sub": config.get(section="broker", option="root_sub"),
        "prefix": config.get(section="broker", option="prefix"),
    }
    if config.has_section("mirror-broker"):
        if config.getboolean("mirror-broker", "tls", fallback=False):
            logging.error("TLS not yet supported on mirror broker")
            exit(1)
        mirror_broker = {
            # Not handling TLS, as we're not ready for it yet.
            "host": config.get("mirror-broker", "host"),
            "port": config.getint("mirror-broker", "port"),
            "username": config.get("mirror-broker", "username", fallback=""),
            "password": config.get("mirror-broker", "password", fallback=""),
            "client_id": config.get(
                "mirror-broker",
                "client_id",
                fallback=config.get(section="broker", option="client_id"),
            ),
            "mirror-self": config.getboolean(
                "mirror-broker",
                "mirror-self",
                fallback=False,
            ),
        }
    else:
        mirror_broker = None

    logging.info("starting mqtt client...")
    mqtt_client = MQTTClient(
        broker=broker,
        mirror_broker=mirror_broker,
        geo_position=position_client,
        stop_signal=stop_signal,
    )
    mqtt_client.loop_start()

    logging.info("starting worker...")
    worker = MqttWorker(
        mqtt_client=mqtt_client,
        client_name=broker["client_id"],
        geo_position=position_client,
    )
    worker_process = threading.Thread(target=worker.run, args=(stop_signal,))
    worker_process.start()
    worker_process.join()

    if mqtt_client.is_connected():
        logging.info("stopping mqtt client...")
        mqtt_client.loop_stop()
        return_code = 0
    else:
        logging.warning("unexpected end of mqtt client, stopping...")
        return_code = 5
    logging.info(f"ended at {int(round(time.time() * 1000))}")
    exit(return_code)


if __name__ == "__main__":
    main()
