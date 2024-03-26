# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import its_quadkeys
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
        self.cfg["depth"] = int(self.cfg["depth"])
        self.cfg["mirror-self"] = bool(
            self.cfg["mirror-self"].lower() in ["true", "yes"]
            if type(self.cfg["mirror-self"]) is str
            else self.cfg["mirror-self"]
        )

        if self.cfg["type"] not in ITSClient.TYPES:
            raise ValueError(f"unknown ITS message type {self.cfg['type']}")

        self.ITSMessage = ITSClient.TYPES[self.cfg["type"]]["message"]

        if not self.cfg["topic-pub-prefix"] or self.cfg["topic-pub-prefix"][-1] != "/":
            raise ValueError(
                f"configuration key general.topic-pub-prefix must end in a / ({self.cfg['topic-pub-prefix']})"
            )
        if not self.cfg["topic-sub-prefix"] or self.cfg["topic-sub-prefix"][-1] != "/":
            raise ValueError(
                f"configuration key general.topic-sub-prefix must end in a / ({self.cfg['topic-sub-prefix']})"
            )

        self.pub_topic_root = (
            self.cfg["topic-pub-prefix"]
            + ITSClient.TYPES[self.cfg["type"]]["topic"]
            + "/"
            + self.cfg["instance-id"]
            + "/"
        )

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

            gnss_report = self.gpsd.get()
            if (
                gnss_report is None
                or gnss_report.latitude is None
                or gnss_report.longitude is None
            ):
                continue

            msg = self.ITSMessage(
                uuid=self.cfg["instance-id"],
                gnss_report=gnss_report,
            )
            quadkey = its_quadkeys.QuadKey(
                (
                    gnss_report.latitude,
                    gnss_report.longitude,
                    self.cfg["depth"],
                )
            )
            topic = self.pub_topic_root + quadkey.to_str("/")
            msg_json = msg.to_json()
            self.mqtt_main.publish(topic, msg_json)
            if self.mqtt_mirror and not self.cfg["mirror-self"]:
                # Use the sub-prefix, to simulate the message as coming
                # from the main broker as if we had subscribed to it.
                mirror_topic = (
                    self.cfg["topic-sub-prefix"]
                    + topic[len(self.cfg["topic-pub-prefix"]) :]
                )
                self.mqtt_mirror.publish(mirror_topic, msg_json)

        timer.close()
