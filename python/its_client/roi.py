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
from its_client import quadtree
import logging

from its_client.mqtt.mqtt_client import MQTTClient


class RegionOfResponsibility:
    ZOOM_BASE_DENM = 15
    ZOOM_BASE_CAM = 18

    def __init__(
        self,
    ):
        self._reinit()

    def _reinit(
        self,
    ):
        # CAM
        self.cam_position = None
        self.cam_left = None
        self.cam_right = None
        self.cam_up = None
        self.cam_down = None
        # DENM
        self.denm_position = None
        self.denm_left = None
        self.denm_right = None
        self.denm_up = None
        self.denm_down = None

    def update_subscription(
        self, latitude, longitude, speed: float, client: MQTTClient
    ):
        logging.debug(f"we update the Region Of Interest")
        if client.new_connection is True:
            logging.info(f"a new connection is detected, we reinitialise")
            self._reinit()
            client.new_connection = False
        self._update_cam_subscription(latitude, longitude, speed, client)
        self._update_denm_subscription(latitude, longitude, speed, client)

    def _update_cam_subscription(
        self, latitude, longitude, speed: float, client: MQTTClient
    ):
        zoom_base = self._correct_zoom_level(speed, self.ZOOM_BASE_CAM)
        new_position = quadtree.lat_lng_to_quad_key(
            latitude, longitude, zoom_base, True
        )
        self.cam_position = self._update_subscription(
            new_position, self.cam_position, client.CAM_RECEPTION_QUEUE, client
        )

    def _update_denm_subscription(
        self, latitude, longitude, speed: float, client: MQTTClient
    ):
        zoom_base = self._correct_zoom_level(speed, self.ZOOM_BASE_DENM)
        new_position = quadtree.lat_lng_to_quad_key(
            latitude, longitude, zoom_base, True
        )
        self.denm_position = self._update_subscription(
            new_position, self.denm_position, client.DENM_RECEPTION_QUEUE, client
        )

    @staticmethod
    def _correct_zoom_level(speed: float, level_of_detail: int) -> int:
        if speed > 135:
            zoom_base = level_of_detail - 2
        elif speed > 95:
            zoom_base = level_of_detail - 1
        else:
            zoom_base = level_of_detail
        return zoom_base

    @staticmethod
    def _update_subscription(
        new_position: str, old_position: str, root_queue: str, client: MQTTClient
    ) -> str:
        logging.debug(
            f"we compare the current position {new_position} with the previous one {old_position}"
        )
        if new_position != old_position:
            new_topic = f"{root_queue}/+{new_position}/#"
            logging.debug(f"we subscribe to {new_topic}")
            client.subscribe(new_topic)
            if old_position is not None:
                old_topic = f"{root_queue}/+{old_position}/#"
                logging.debug(f"we unsubscribe to {old_topic}")
                client.unsubscribe(old_topic)
            old_position = new_position
        return old_position
