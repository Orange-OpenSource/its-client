# Software Name: its-interqueuemanager
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

from __future__ import annotations
import logging
import paho.mqtt.client

try:
    import paho_socket
except ModuleNotFoundError:
    paho_socket_available = False
else:
    paho_socket_available = True


class MQTTClient:
    def __init__(
        self,
        name: str,
        host: typing.Union[str, None],
        port: typing.Union[int, None],
        socket: typing.Union[str, None],
        username: typing.Union[str, None],
        password: typing.Union[str, None],
        client_id: str,
        local_qm: typing.Union[MQTTClient, None],
        copy_from: str,
        copy_to: list[str],
    ):
        self.name = name
        self.host = host
        self.port = port
        self.socket = socket
        self.local_qm = local_qm
        self.copy_from = copy_from
        self.copy_to = copy_to

        if self.socket:
            if not paho_socket_available:
                raise RuntimeError(
                    f"[{self.name}]: using socket-path {self.socket} requires python module paho_socket"
                )
            self.path = self.socket
        elif self.host and self.port:
            self.path = f"{self.host}:{self.port}"
        else:
            raise LookupError(
                "Either host and port, or socket-path, are required for local MQTT client"
            )

        logging.info(
            f"[{self.name}]: create to {self.path}, listening on {self.copy_from}"
        )

        self.connected = False
        self.client = (paho_socket.Client if self.socket else paho.mqtt.client.Client)(
            client_id=client_id,
            protocol=paho.mqtt.client.MQTTv5,
        )
        self.client.reconnect_delay_set()
        self.client.username_pw_set(username, password)
        self.client.on_message = self.__on_message
        self.client.on_connect = self.__on_connect
        self.client.on_disconnect = self.__on_disconnect
        self.client.on_socket_close = self.__on_socket_close
        if self.socket:
            self.client.sock_connect_async(
                self.socket,
                clean_start=True,
            )
        else:
            self.client.connect_async(
                host=self.host,
                port=self.port,
                clean_start=True,
            )

    def start(self):
        logging.info(f"[{self.name}]: starting for {self.path}")
        self.client.loop_start()

    def stop(self):
        logging.info(f"[{self.name}]: stopping for {self.path}")
        self.client.disconnect()
        self.client.loop_stop()

    def publish(
        self,
        topic: typing.Union[str, bytes],
        payload: typing.Union[str, bytes],
    ):
        if not self.__is_local_qm():
            # If I am not the local_qm, then I'm connected to a
            # neighbour, and I am not allowed to publish there.
            raise RuntimeError("Trying to publish to a neighbour. Verboten.")
        if not self.connected:
            return
        logging.debug(f"[{self.name}]: publishing {abbrev(payload)} on {topic}")
        self.client.publish(topic=topic, payload=payload)

    def __is_local_qm(self):
        return self.local_qm is None

    def __on_message(self, _client, _userdata, message: paho.mqtt.MQTTMessage):
        logging.debug(
            f"[{self.name}]: received message on {message.topic}: {abbrev(message.payload)}"
        )
        interqueue_topic = message.topic
        for cp_to in self.copy_to:
            new_topic = cp_to + interqueue_topic[len(self.copy_from) :]
            logging.debug(f"[{self.name}]:  -> forwarding to {new_topic}")
            (self.local_qm or self).publish(new_topic, message.payload)

    def __on_connect(self, _client, _userdata, _flags, _rc, _properties=None):
        logging.info(f"[{self.name}]: connected to {self.path}")
        # For neighbours, we'll eventually restrict that to sub-quad-keys,
        # but for now, we subscribe to the whole of the sub-tree. For the
        # local broker, we will always want to listen to the full sub-tree.
        topic = self.copy_from + "/#"
        self.client.subscribe(topic)
        logging.debug(f"[{self.name}]: subscribed to {topic}")
        self.connected = True

    def __on_disconnect(self, _client, _userdata, _rc, _properties=None):
        logging.info(f"[{self.name}]: disconnected from {self.path}")
        self.connected = False

    def __on_socket_close(self, _client, _userdata, _sock):
        logging.debug(f"[{self.name}]: disconnected (socket) from {self.path}")
        self.connected = False


def abbrev(msg: typing.Union[str, bytes], length: int = 16):
    if len(msg) > length:
        return f"{msg[:length]}[...]"
    return msg
