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
import threading
import time

from its_client.position import GeoPosition
from its_client import quadtree, cam, mobility, roi
from its_client.logger import its, monitoring


class MqttWorker:
    QUEUE = "5GCroCo/inQueue/v2x/cam"

    def __init__(self, mqtt_client, client_name, geo_position: GeoPosition):
        self.mqtt_client = mqtt_client
        self.client_name = client_name
        self.geo_position = geo_position
        self.previous_step_timestamp = None
        self.previous_step_lon = None
        self.previous_step_lat = None
        self.previous_alt = None
        self.previous_alt_step_counter = 0
        self.previous_heading = None
        self.previous_heading_step_counter = 0

        self.region = roi.RegionOfResponsibility()

    def step(self) -> bool:
        (
            lon,
            lat,
            speed,
            alt,
            heading,
            position_time,
        ) = self.geo_position.get_current_value()
        if lon is not None and lat is not None and speed is not None:
            # alt
            (
                alt,
                self.previous_alt,
                self.previous_alt_step_counter,
            ) = _manage_optional_field(
                alt, self.previous_alt, self.previous_alt_step_counter, "altitude"
            )
            # heading
            (
                heading,
                self.previous_heading,
                self.previous_heading_step_counter,
            ) = _manage_optional_field(
                heading,
                self.previous_heading,
                self.previous_heading_step_counter,
                "heading",
            )
            if heading is not None and alt is not None:
                # topic
                root_cam_topic = f"{self.QUEUE}/{self.client_name}"
                cam_topic = f"{root_cam_topic}{quadtree.lat_lng_to_quad_key(lat, lon, 22, True)}"
                # time
                now = time.time()
                difference = now - position_time.timestamp()
                if difference > 1:
                    logging.warning(
                        f"the position time is older than {difference} ms"
                    )
                # acceleration
                if (
                    self.previous_step_timestamp is None
                    or self.previous_step_lat is None
                    or self.previous_step_lon is None
                ):
                    acceleration = 161
                else:
                    acceleration = mobility.compute_acceleration(
                        latitude_start=self.previous_step_lat,
                        longitude_start=self.previous_step_lon,
                        latitude_end=lat,
                        longitude_end=lon,
                        time_start=self.previous_step_timestamp,
                        time_end=now,
                    )
                # speed
                km_speed = mobility.mps_to_kmph(speed)
                logging.debug(f"current speed: {speed} km/h")
                message = cam.CooperativeAwarenessMessage(
                    uuid=self.client_name,
                    timestamp=now,
                    latitude=lat,
                    longitude=lon,
                    altitude=alt,
                    speed=km_speed,
                    acceleration=acceleration,
                    heading=heading,
                )
                json_cam = message.to_json()
                # threading
                publish = threading.Thread(
                    target=self.mqtt_client.publish, args=(cam_topic, json_cam)
                )
                publish.start()
                # no threading
                # self.mqtt_client.publish(cam_topic, json_cam)
                monitoring.monitore_cam(
                    vehicle_id=self.client_name,
                    direction="sent_on",
                    station_id=message.station_id,
                    generation_delta_time=message.generation_delta_time(),
                    latitude=lat,
                    longitude=lon,
                    timestamp=int(round(now * 1000)),
                    partner=self.mqtt_client.gateway_name,
                    root_queue=root_cam_topic,
                )
                its.create_cam(json_cam)
                self.region.update_subscription(
                    latitude=lat, longitude=lon, speed=speed, client=self.mqtt_client
                )
                self.previous_step_timestamp = now
                self.previous_step_lat = lat
                self.previous_step_lon = lon
                # threading
                publish.join()
                del publish
            else:
                logging.debug(f"no heading or altitude, so no work processed")
        else:
            logging.debug(f"no lat or lon or speed, so no work processed")
        return True

    def run(self, stop_event):
        logging.info("mqtt worker run")
        while stop_event is None or not stop_event.is_set():
            self.step()
            time.sleep(0.2)  # tune this, you might not get values that quickly
        logging.info("mqtt worker finished")


def _manage_optional_field(
    field: float, previous_field: float, previous_field_counter: int, field_name: str
) -> (float, float, int):
    if field is None:
        if previous_field is not None:
            field = previous_field
            previous_field_counter += 1
            if previous_field_counter % 100 == 0:
                logging.warning(
                    f"no {field_name} received from {previous_field_counter} steps"
                )
    else:
        previous_field = field
        previous_field_counter = 0
    return field, previous_field, previous_field_counter
