# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import linuxfd
import logging
import threading
from .gpsd import GNSSProvider
from .mqtt import MqttClient
from .its.cam import CooperativeAwarenessMessage as CAM


class ITSClient:
    TYPES = {
        "CAM": {"topic": "cam", "message": CAM},
    }

    def __init__(
        self,
        *,
        cfg: dict,
        gpsd: GNSSProvider,
        mqtt_main: MqttClient,
        mqtt_mirror: MqttClient = None,
    ):
        self.cfg = cfg
        self.gpsd = gpsd
        self.mqtt_main = mqtt_main
        self.mqtt_mirror = mqtt_mirror

        # Type coercion
        self.cfg["report-freq"] = float(self.cfg["report-freq"])

        if self.cfg["type"] not in ITSClient.TYPES:
            raise ValueError(f"unknown ITS message type {self.cfg['type']}")

        self.ITSMessage = ITSClient.TYPES[self.cfg["type"]]["message"]

        self.should_stop = False
        self.thread = threading.Thread(
            target=self._loop,
            name="its-client",
            daemon=True,
        )

    def start(self):
        logging.debug("starting ITS client")
        self.thread.start()

    def stop(self, wait=False):
        logging.debug("stopping ITS client")
        self.should_stop = True
        if wait:
            self.join()

    def join(self):
        self.thread.join()
        logging.debug("stopped ITS client")

    def _loop(self):
        timer = linuxfd.timerfd(closeOnExec=True)
        # value==0.0 disables the timer, which means we can't configure it
        # to "expire now already!", so we instead just tell it to "expire
        # really, really soon!" (i.e. in the next microsecond).
        timer.settime(
            value=0.000001,
            interval=1.0 / self.cfg["report-freq"],
        )
        while True:
            evt = timer.read()
            if evt > 1:
                logging.warning("Resuming after %d missed events", evt - 1)

            if self.should_stop:
                break

            g = self.gpsd.get()
            if g is not None:
                self.mqtt_main.publish("gnss", repr(g))

        timer.close()
