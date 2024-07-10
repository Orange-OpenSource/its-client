# Software Name: iot3
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import paho.mqtt.client
import paho.mqtt.enums
import threading
import time
from typing import Any, Callable, Optional, TypeAlias


MsgCallbackType: TypeAlias = Callable[[Any, str, bytes], None]


class MqttClient:
    def __init__(
        self,
        *,
        client_id: str,
        host: Optional[str] = None,
        port: Optional[int] = None,
        socket_path: Optional[str] = None,
        username: Optional[str] = None,
        password: Optional[str] = None,
        msg_cb: Optional[MsgCallbackType] = None,
        msg_cb_data: Any = None,
    ):
        """
        Create an MQTT client

        :param client_id: The MQT client ID to identify as.
        :param host: The host name (or IP) of the MQTT broker.
        :param port: The port the MQTT broker listens on.
        :param socket_path: The path of the UNIX socket.
        :param username: The username to authenticate against the MQTT broker.
        :param password: The password to authenticate against the MQTT broker.
        :param msg_cb: The function to call when a message is received from
                       the MQTT broker.
        :param msg_cb_data: Arbitrary data that will be passed back to the
                            msg_cb function.
        """

        self.msg_cb = msg_cb
        self.msg_cb_data = msg_cb_data

        if socket_path is not None:
            transport = "unix"
            self.host = socket_path
            self.port = -1
            self.name = socket_path
        else:
            transport = "tcp"
            self.host = host
            self.port = port
            self.name = f"{host}:{port}"

        self.client = paho.mqtt.client.Client(
            callback_api_version=paho.mqtt.enums.CallbackAPIVersion.VERSION2,
            client_id=client_id,
            protocol=paho.mqtt.client.MQTTv5,
            transport=transport,
        )
        self.client.reconnect_delay_set(min_delay=1, max_delay=2)
        self.client.username_pw_set(username, password)
        self.client.on_connect = self.__on_connect
        self.client.on_message = self.__on_message

        self.subscriptions = set()
        self.subscriptions_lock = threading.RLock()

    def start(self):
        """Start the MQTT client"""
        self.client.connect_async(
            host=self.host,
            port=self.port,
            clean_start=True,
        )
        self.client.loop_start()

    def is_ready(self):
        """Returns whether the MQTT client is ready.

        The client is ready when it is connected to the MQTT broker.
        """
        return self.client.is_connected()

    def wait_for_ready(self):
        """Wait for the client to be ready.

        This can block forever.
        """
        while not self.client.is_connected():
            time.sleep(0.1)

    def stop(self):
        """Stop the MQTT client"""
        self.client.disconnect()
        self.client.loop_stop()

    def publish(self, *, topic: str, payload: bytes | str):
        """Publish an MQTT message

        :param topic: The MQTT topic to post on.
        :param payload: The payload to post.
        """
        if not self.client.is_connected():
            return
        self.client.publish(topic=topic, payload=payload)

    def subscribe(self, *, topics: list[str]):
        """Subscribe to additional topics.

        :param topics: The list of additional topics to subscribe to. It is OK to
                       pass topics that were previously subscribed to.
        """
        if self.msg_cb is None:
            raise RuntimeError(
                f"MQTT client {self.name}: subscribing without a message callback",
            )
        topics = set(topics)
        with self.subscriptions_lock:
            sub = topics.difference(self.subscriptions)
            if sub and self.client.is_connected():
                self.client.subscribe(list(map(lambda t: (t, 0), sub)))
            self.subscriptions.update(topics)

    def subscribe_replace(self, *, topics: list[str]):
        """Replace the existing subscriptions with the new list.

        This is equivalent to calling in sequence:
            unsubscribe_all()
            subscribe(topics)

        except that subscribe_replace(topics) is atomic.

        :param topics: The list of topics to subscribe to, in replacement to
                       any previously subscribed topics. It is OK to pass
                       topics that were previously subscribed to.
        """
        if self.msg_cb is None:
            raise RuntimeError(
                f"MQTT client {self.name}: subscribing without a message callback",
            )
        topics = set(topics)
        with self.subscriptions_lock:
            if self.client.is_connected():
                unsub = self.subscriptions.difference(topics)
                sub = topics.difference(self.subscriptions)
                if unsub:
                    self.client.unsubscribe(list(unsub))
                if sub:
                    self.client.subscribe(list(map(lambda t: (t, 0), sub)))
            self.subscriptions.clear()
            self.subscriptions.update(topics)

    def unsubscribe(self, *, topics: list[str]):
        """Unsubscribe from a list of topics.

        :param topics: The list of topics to remove from the current
                       subscription set. It is OK to pass topics that were
                       not previously subscribed to.
        """
        topics = set(topics)
        with self.subscriptions_lock:
            unsub = topics.intersection(self.subscriptions)
            if unsub and self.client.is_connected():
                self.client.unsubscribe(list(unsub))
            self.subscriptions.difference_update(topics)

    def unsubscribe_all(self):
        """Unsubscribe from all topics"""
        # We _can_ lock here even if unsubscribe() also locks by itself,
        # because this is a RLock, i.e. a reentrant lock.
        # We _must_ lock here, to avoid another thread from changing
        # self.subscriptions between here and the locking in
        # unsubscribe().
        with self.subscriptions_lock:
            self.unsubscribe(self.subscriptions)

    # In theory, we would not need this method, as we could very well
    # have set   self.client.on_message = msg_cb   and be done with
    # that. Having this intermediate __on_message() will help with
    # telemetry when we add it.
    def __on_message(
        self,
        _client,
        _userdata,
        message: paho.mqtt.client.MQTTMessage,
    ):
        self.msg_cb(
            self.msg_cb_data,
            message.topic,
            message.payload,
        )

    def __on_connect(
        self,
        _client,
        _userdata,
        _connect_flags,
        _rc,
        _properties=None,
    ):
        with self.subscriptions_lock:
            if self.subscriptions:
                self.client.subscribe(
                    list(map(lambda t: (t, 0), self.subscriptions)),
                )
