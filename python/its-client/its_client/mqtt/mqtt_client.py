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
import os
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

    def __init__(
        self,
        broker: dict,
        mirror_broker: dict,
        geo_position: GeoPosition,
        stop_signal=None,
    ):
        self.broker = broker
        self.mirror_broker = mirror_broker
        self.geo_position = geo_position
        self.stop_signal = stop_signal
        self.gateway_name = "broker"
        self.client = None
        self.mirror_client = None
        self.new_connection = False
        self.recv_queues = {
            "INFO": broker["root_sub"] + "/info/broker",
            "CAM": broker["root_sub"] + "/" + broker["prefix"] + "/cam",
            "CPM": broker["root_sub"] + "/" + broker["prefix"] + "/cpm",
            "DENM": broker["root_sub"] + "/" + broker["prefix"] + "/denm",
        }
        self.send_queues = {
            "CAM": broker["root_pub"] + "/" + broker["prefix"] + "/cam",
        }

    def on_disconnect(self, client, userdata, rc):
        logging.debug(
            self._format_log(f" called for {client.socket()} with {userdata}")
        )
        if not self.stop_signal.is_set():
            logging.error("Unexpected MQTT disconnect; emergency exit.")
            os._exit(42)

    def on_connect(self, client, userdata, flags, rc):
        logging.debug(
            self._format_log(
                f" called for {client.socket()} with {userdata} and {flags}"
            )
        )
        if rc == 0:
            logging.info("connected to mqtt broker")
            # gather the gateway name
            self.subscribe(topic=self.recv_queues["INFO"])
            # save the new connection status to trigger the subscriptions
            self.new_connection = True

    def on_message(self, _client, _userdata, message: paho.mqtt.client.MQTTMessage):
        logging.info(f"message received on topic {message.topic}")
        logging.debug(
            self._format_log(f"mid: {message.mid}, payload: {message.payload}")
        )
        try:
            message_dict = json.loads(message.payload)
        except json.decoder.JSONDecodeError as e:
            if message.payload:
                logging.error(
                    self._format_log(
                        f"Non-JSON payload on {message.topic}: {message.payload}"
                    )
                )
            else:
                # Empty payload: most probably a delete of a retained message.
                logging.debug(
                    self._format_log(
                        f"Non-JSON payload on {message.topic}: {message.payload}"
                    )
                )
            # Not bailing out, we may need to forward it to the mirror borker, below
            message_dict = dict()

        if self.mirror_client:
            if (
                message_dict.get("source_uuid", "") != self.broker["client_id"]
                or self.mirror_broker["mirror-self"]
            ):
                logging.debug(f"mid: {message.mid}: forwarding to mirror broker")
                self.mirror_client.publish(
                    message.topic,
                    message.payload,
                    message.qos,
                    message.retain,
                )

        if not message.payload:
            logging.debug(f"mid: {message.mid}, empty payload, skipping message")
            return

        if self.recv_queues["INFO"] in message.topic:
            logging.debug(
                self._format_log(f"Instance id: {message_dict['instance_id']}")
            )
            self.gateway_name = message_dict["instance_id"]
        elif self.recv_queues["CAM"] in message.topic:
            sender = message.topic.replace(self.recv_queues["CAM"], "").split("/")[1]
            root_cam_topic = f"{self.recv_queues['CAM']}/{sender}"
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
        elif self.recv_queues["DENM"] in message.topic:
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
                root_queue=self.recv_queues["DENM"],
                sender=message_dict["source_uuid"],
            )
            its.record(message.payload.decode())
        elif self.recv_queues["CPM"] in message.topic:
            sender = message.topic.replace(self.recv_queues["CPM"], "").split("/")[1]
            root_cpm_topic = f"{self.recv_queues['CPM']}/{sender}"
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
        if not self.stop_signal.is_set():
            logging.error("Unexpected MQTT disconnect; emergency exit.")
            os._exit(42)

    def on_socket_register_write(self, client, userdata, _sock):
        logging.debug(
            self._format_log(f" called for {client.socket()} with {userdata}")
        )

    def _connect(self):
        logging.info("connecting...")
        # Connect on the mirror client first, so that it is ready to
        # forward any message incoming from the main broker
        if self.mirror_broker:
            self.mirror_client = paho.mqtt.client.Client(
                client_id=self.mirror_broker["client_id"]
            )
            if self.mirror_broker["username"]:
                self.mirror_client.username_pw_set(
                    self.mirror_broker["username"],
                    self.mirror_broker["password"],
                )
            self.mirror_client.reconnect_delay_set()
            self.mirror_client.connect(
                host=self.mirror_broker["host"],
                port=self.mirror_broker["port"],
                keepalive=60,
            )

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
        if self.mirror_client is not None:
            # If we mirror ourselves, then we will propagate the message
            # on the outQueue when we receive it from the central broker,
            # so here we will want to keep the original topic unchanged.
            # However, if we do not mirror ourselves, then we need to
            # send the message on the outQueue, as if it were coming
            # from the central broker.
            if not self.mirror_broker["mirror-self"]:
                topic = topic.replace("/inQueue/", "/outQueue/")
            self.mirror_client.publish(topic, payload, qos, retain, properties)

    def loop_start(self):
        logging.debug(self._format_log(f"starting loop..."))
        self._connect()
        self.client.loop_start()
        if self.mirror_client is not None:
            self.mirror_client.loop_start()

    def loop_stop(self):
        logging.debug(self._format_log(f"stopping loop..."))
        self.client.loop_stop()
        self.client.disconnect()
        if self.mirror_client is not None:
            self.mirror_client.loop_stop()
            self.mirror_client.disconnect()

    def loop_restart(self):
        self.loop_stop()
        self.loop_start()

    def is_connected(self):
        # We're only interested about the connection to the main broker
        return self.client.is_connected()

    def get_recv_queue(self, name):
        # Let the caller handle KeyError is they asked for a non-existing queue
        return self.recv_queues[name]

    def get_send_queue(self, name):
        # Let the caller handle KeyError is they asked for a non-existing queue
        return self.send_queues[name]

    def _format_log(self, message=""):
        return f"{type(self).__name__}[{self.broker['client_id']}]::{getouterframes(currentframe())[1][3]} {message}"
