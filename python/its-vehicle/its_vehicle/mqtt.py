# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import enum
import logging
import paho.mqtt.client
import threading
from typing import Callable

try:
    import paho_socket
except ModuleNotFoundError:
    paho_socket_available = False
else:
    paho_socket_available = True


class MqttDirection(enum.Enum):
    RECEIVED = enum.auto()
    SENT = enum.auto()


class MqttClient:
    def __init__(self, *, cfg: dict):
        self.cfg = cfg

        # Type coercion
        # port can be missing when socket-path is set
        self.cfg["port"] = int(self.cfg.get("port", -1))

        if "socket-path" in self.cfg:
            if not paho_socket_available:
                raise RuntimeError(
                    f"paho_socket not available for {self.cfg['socket-path']}",
                )
            Client = paho_socket.Client
            connect_fn = "sock_connect_async"
            connect_args = (self.cfg["socket-path"],)
            self.name = self.cfg["socket-path"]
        else:
            Client = paho.mqtt.client.Client
            connect_fn = "connect_async"
            connect_args = (self.cfg["host"], self.cfg["port"])
            self.name = f"{self.cfg['host']}:{self.cfg['port']}"
        logging.debug("creating MQTT client %s with %s", self.name, self.cfg)

        username = self.cfg.get("username", None)
        password = self.cfg.get("password", None)

        self.client = Client(
            client_id=self.cfg["client-id"],
            protocol=paho.mqtt.client.MQTTv5,
        )
        self.client.reconnect_delay_set(min_delay=1, max_delay=2)
        self.client.username_pw_set(username, password)
        self.client.on_connect = self.__on_connect
        self.client.on_disconnect = self.__on_disconnect
        self.client.on_socket_close = self.__on_socket_close
        self.client.on_message = self.__on_message

        self.subscriptions = set()
        self.subscriptions_lock = threading.RLock()
        self.connected = False
        getattr(self.client, connect_fn)(
            *connect_args,
            clean_start=True,
        )

    def start(self):
        logging.debug("starting MQTT client %s", self.name)
        self.client.loop_start()

    def stop(self):
        logging.debug("stopping MQTT client %s", self.name)
        self.client.disconnect()
        self.client.loop_stop()

    def publish(self, topic: str, payload: bytes | str):
        if not self.connected:
            logging.debug(
                "not sending MQTT message to not connected client %s on %s: %s",
                self.name,
                abbrev(topic, length=32),
                abbrev(payload),
            )
            return
        logging.debug(
            "sending MQTT message to client %s on %s: %s",
            self.name,
            abbrev(topic, length=32),
            abbrev(payload),
        )
        self.client.publish(topic=topic, payload=payload)

    def set_msg_cb(self, msg_cb: Callable | None):
        if msg_cb is None and self.subscriptions:
            raise RuntimeError(
                f"MQTT client {self.name}: removing message callback with pending subscriptions {self.subscriptions}",
            )
        self.msg_cb = msg_cb

    def subscribe(self, topics: list[str]):
        if self.msg_cb is None:
            raise RuntimeError(
                f"MQTT client {self.name}: subscribing without a message callback",
            )
        topics = set(topics)
        with self.subscriptions_lock:
            sub = topics.difference(self.subscriptions)
            if sub and self.connected:
                logging.debug(
                    "subscribing MQTT client %s to new %s",
                    self.name,
                    sub,
                )
                self.client.subscribe(list(map(lambda t: (t, 0), sub)))
            self.subscriptions.update(topics)

    def subscribe_replace(self, topics: list[str]):
        if self.msg_cb is None:
            raise RuntimeError(
                f"MQTT client {self.name}: subscribing without a message callback",
            )
        topics = set(topics)
        with self.subscriptions_lock:
            if self.connected:
                unsub = self.subscriptions.difference(topics)
                sub = topics.difference(self.subscriptions)
                if unsub:
                    logging.debug(
                        "unsubscribing MQTT client %s from %s",
                        self.name,
                        unsub,
                    )
                    self.client.unsubscribe(list(unsub))
                if sub:
                    logging.debug(
                        "subscribing MQTT client %s to %s",
                        self.name,
                        sub,
                    )
                    self.client.subscribe(list(map(lambda t: (t, 0), sub)))
            self.subscriptions.clear()
            self.subscriptions.update(topics)

    def unsubscribe(self, topics: list[str]):
        topics = set(topics)
        with self.subscriptions_lock:
            unsub = topics.intersection(self.subscriptions)
            if unsub and self.connected:
                logging.debug(
                    "unsubscribing MQTT client %s from %s",
                    self.name,
                    unsub,
                )
                self.client.unsubscribe(list(unsub))
            self.subscriptions.difference_update(topics)

    def unsubscribe_all(self):
        logging.debug("unsubscribing MQTT client %s from everything", self.name)
        # We _can_ lock here even if unsubscribe() also locks by itself,
        # because this is a RLock, i.e. a reentrant lock.
        # We _must_ lock here, to avoid another thread from changing
        # self.subscriptions between here and the locking in
        # unsubscribe().
        with self.subscriptions_lock:
            self.unsubscribe(self.subscriptions)

    # In theory, we would not need this method, as we could very well
    # have set   self.client.on_message = msg_cb   and be done with
    # that and rely with the callback to log, but we'd have lost the
    # ablity to also log the incoming MQTT client name.
    def __on_message(
        self,
        _client,
        _userdata,
        message: paho.mqtt.client.MQTTMessage,
    ):
        logging.debug(
            "received from MQTT client %s on topic %s: %s",
            self.name,
            abbrev(message.topic, length=32),
            abbrev(message.payload),
        )
        # We need to check that we do have a callback available, because
        # there is a window where we may get a message to deliver even
        # though the user has unsubscribed and removed the callbak:
        # - broker sends a message, queued in paho MQTT internals
        # - caller unsubscribes
        # - caller removes callback
        # - we try to deliver the pending message
        if self.msg_cb is not None:
            self.msg_cb(message)

    def __on_connect(self, _client, _userdata, _flags, _rc, _properties=None):
        logging.debug("connected to MQTT client %s", self.name)
        self.connected = True
        with self.subscriptions_lock:
            if self.subscriptions:
                logging.debug(
                    "subscribing MQTT client %s to %s",
                    self.name,
                    self.subscriptions,
                )
                self.client.subscribe(
                    list(map(lambda t: (t, 0), self.subscriptions)),
                )

    def __on_disconnect(self, _client, _userdata, _rc, _properties=None):
        logging.debug(f"disconnected from %s", self.name)
        self.connected = False

    def __on_socket_close(self, _client, _userdata, _sock):
        logging.debug(f"disconnected (socket) from %s", self.name)
        self.connected = False


def abbrev(msg: str | bytes, length: int = 16):
    if len(msg) > length:
        return f"{msg[:length-3]}..."
    return msg
