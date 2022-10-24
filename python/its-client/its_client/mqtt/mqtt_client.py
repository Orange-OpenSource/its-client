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
import json
import logging
import time
from inspect import currentframe
from inspect import getouterframes

import paho.mqtt.client

from its_client.logger import its, monitoring
from its_client.position import GeoPosition


class MQTTClient(object):
    """
    MQTT client.
    """

    CAM_RECEPTION_QUEUE = "5GCroCo/outQueue/v2x/cam"
    CPM_RECEPTION_QUEUE = "5GCroCo/outQueue/v2x/cpm"
    DENM_RECEPTION_QUEUE = "5GCroCo/outQueue/v2x/denm"

    def __init__(
        self,
        broker: dict,
        geo_position: GeoPosition,
        stop_signal=None,
    ):
        self.broker = broker
        self.geo_position = geo_position
        self.stop_signal = stop_signal
        self.gateway_name = "broker"
        self.client = None
        self.new_connection = False

    def on_disconnect(self, client, userdata, rc):
        logging.debug(
            self._format_log(f" called for {client.socket()} with {userdata}")
        )

    def on_connect(self, client, userdata, flags, rc):
        logging.debug(
            self._format_log(
                f" called for {client.socket()} with {userdata} and {flags}"
            )
        )
        if rc == 0:
            logging.info("connected to mqtt broker")
            # gather the gateway name
            topic = "5GCroCo/outQueue/info/broker"
            self.subscribe(topic)
            # save the new connection status to trigger the subscriptions
            self.new_connection = True

    def on_message(self, _client, _userdata, message: paho.mqtt.client.MQTTMessage):
        logging.info(f"message received on topic {message.topic}")
        logging.debug(
            self._format_log(f"mid: {message.mid}, payload: {message.payload}")
        )
        if message.topic.endswith("5GCroCo/outQueue/info/broker"):
            logging.debug(
                self._format_log(
                    f"Instance id: {json.loads(message.payload)['instance_id']}"
                )
            )
            self.gateway_name = json.loads(message.payload)["instance_id"]
        elif self.CAM_RECEPTION_QUEUE in message.topic and message.payload:
            message_dict = json.loads(message.payload)
            sender = message.topic.replace(self.CAM_RECEPTION_QUEUE, "").split("/")[1]
            root_cam_topic = f"{self.CAM_RECEPTION_QUEUE}/{sender}"
            lon, lat = self.geo_position.get_current_position()
            monitoring.monitore_cam(
                vehicle_id=self.broker["client_id"],
                direction="received_on",
                station_id=message_dict["message"]["station_id"],
                generation_delta_time=message_dict["message"]["generation_delta_time"],
                latitude=lat,
                longitude=lon,
                timestamp=int(round(time.time() * 1000)),
                partner=self.gateway_name,
                root_queue=root_cam_topic,
            )
            its.record(message.payload.decode())
        elif self.DENM_RECEPTION_QUEUE in message.topic and message.payload:
            message_dict = json.loads(message.payload)
            lon, lat = self.geo_position.get_current_position()
            monitoring.monitore_denm(
                vehicle_id=self.broker["client_id"],
                station_id=message_dict["message"]["station_id"],
                originating_station_id=message_dict["message"]["management_container"][
                    "action_id"
                ]["originating_station_id"],
                sequence_number=message_dict["message"]["management_container"][
                    "action_id"
                ]["sequence_number"],
                reference_time=message_dict["message"]["management_container"][
                    "reference_time"
                ],
                detection_time=message_dict["message"]["management_container"][
                    "detection_time"
                ],
                latitude=lat,
                longitude=lon,
                timestamp=int(round(time.time() * 1000)),
                partner=self.gateway_name,
                root_queue=self.DENM_RECEPTION_QUEUE,
                sender=message_dict["source_uuid"],
            )
            its.record(message.payload.decode())
        elif self.CPM_RECEPTION_QUEUE in message.topic and message.payload:
            message_dict = json.loads(message.payload)
            sender = message.topic.replace(self.CPM_RECEPTION_QUEUE, "").split("/")[1]
            root_cpm_topic = f"{self.CPM_RECEPTION_QUEUE}/{sender}"
            lon, lat = self.geo_position.get_current_position()
            monitoring.monitore_cpm(
                vehicle_id=self.broker["client_id"],
                direction="received_on",
                station_id=message_dict["message"]["station_id"],
                generation_delta_time=message_dict["message"]["generation_delta_time"],
                latitude=lat,
                longitude=lon,
                timestamp=int(round(time.time() * 1000)),
                partner=self.gateway_name,
                root_queue=root_cpm_topic,
            )
            its.record(message.payload.decode())

    def on_publish(self, client, userdata, _mid):
        logging.debug(
            self._format_log(f" called for {client.socket()} with {userdata}")
        )

    def on_subscribe(self, client, userdata, _mid, _granted_qos):
        logging.debug(
            self._format_log(f" called for {client.socket()} with {userdata}")
        )

    def on_unsubscribe(self, client, userdata, _mid):
        logging.debug(
            self._format_log(f" called for {client.socket()} with {userdata}")
        )

    def on_socket_open(self, client, userdata, _sock):
        logging.debug(
            self._format_log(f" called for {client.socket()} with {userdata}")
        )

    def on_socket_close(self, client, userdata, _sock):
        logging.debug(
            self._format_log(f" called for {client.socket()} with {userdata}")
        )

    def on_socket_register_write(self, client, userdata, _sock):
        logging.debug(
            self._format_log(f" called for {client.socket()} with {userdata}")
        )

    def _connect(self):
        logging.info("connecting...")
        self.client = paho.mqtt.client.Client(client_id=self.broker["client_id"])
        if self.broker["username"]:
            self.client.username_pw_set(
                self.broker["username"],
                self.broker["password"],
            )
        self.client.on_connect = self.on_connect
        self.client.on_disconnect = self.on_disconnect
        self.client.on_message = self.on_message
        self.client.on_publish = self.on_publish
        self.client.on_subscribe = self.on_subscribe
        self.client.on_unsubscribe = self.on_unsubscribe
        self.client.on_socket_open = self.on_socket_open
        self.client.on_socket_close = self.on_socket_close
        self.client.on_socket_register_write = self.on_socket_register_write
        self.client.max_inflight_messages_set(20)
        self.client.max_queued_messages_set(100)
        self.client.reconnect_delay_set(1, 2)
        self.client.connect(
            host=self.broker["host"],
            port=self.broker["port"],
            keepalive=60,
        )

    def subscribe(self, topic):
        logging.debug(self._format_log(f"subscribing to {topic}..."))
        if self.client.is_connected():
            self.client.subscribe(topic)
            logging.info(f"we subscribed on topic {topic}")
        else:
            logging.warning(
                f"we didn't subscribe to the topic {topic} because we aren't connected"
            )

    def unsubscribe(self, topic):
        logging.debug(self._format_log(f"unsubscribing to {topic}..."))
        if self.client.is_connected():
            self.client.unsubscribe(topic)
            logging.info(f"we unsubscribed to the topic {topic}")
        else:
            logging.warning(
                f"we didn't unsubscribe to the topic {topic} because we aren't connected"
            )

    def publish(self, topic, payload=None, qos=1, retain=False, properties=None):
        logging.debug(self._format_log(f"publishing payload: {payload}"))
        if self.client.is_connected():
            self.client.publish(topic, payload, qos, retain, properties)
            logging.info(f"message sent on topic {topic}")
        else:
            logging.warning(
                f"message not sent on topic {topic} because we aren't connected"
            )

    def loop_start(self):
        logging.debug(self._format_log(f"starting loop..."))
        self._connect()
        self.client.loop_start()

    def loop_stop(self):
        logging.debug(self._format_log(f"stopping loop..."))
        self.client.loop_stop()
        self.client.disconnect()

    def loop_restart(self):
        self.loop_stop()
        self.loop_start()

    def is_connected(self):
        return self.client.is_connected()

    def _format_log(self, message=""):
        return f"{type(self).__name__}[{self.broker['client_id']}]::{getouterframes(currentframe())[1][3]} {message}"
