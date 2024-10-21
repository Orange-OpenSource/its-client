# Software Name: iot3
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import paho.mqtt.client
import paho.mqtt.enums
import paho.mqtt.packettypes
import paho.mqtt.properties
import ssl
import threading
import time
from typing import Any, Callable, Optional, TypeAlias, Unpack
from . import otel


MsgCallbackType: TypeAlias = Callable[[Any, str, bytes], None]
SpanCallableType: TypeAlias = Callable[Unpack[otel.Otel.span], otel.Span]


class MqttClient:
    def __init__(
        self,
        *,
        client_id: str,
        host: Optional[str] = None,
        port: Optional[int] = None,
        websocket_path: Optional[str] = None,
        socket_path: Optional[str] = None,
        tls: Optional[bool] = None,
        username: Optional[str] = None,
        password: Optional[str] = None,
        msg_cb: Optional[MsgCallbackType] = None,
        msg_cb_data: Any = None,
        span_ctxmgr_cb: Optional[SpanCallableType] = otel.Otel.noexport_span,
    ):
        """
        Create an MQTT client

        :param client_id: The MQTT client ID to identify as.
        :param host: The host name (or IP) of the MQTT broker.
        :param port: The port the MQTT broker listens on.
        :param websocket_path: The path of the websocket the broker is
                               reachable at.
        :param socket_path: The path of the UNIX socket.
        :param tls: Whether to use TLS or not. See below for details.
        :param username: The username to authenticate against the MQTT broker.
        :param password: The password to authenticate against the MQTT broker.
        :param msg_cb: The function to call when a message is received from
                       the MQTT broker.
        :param msg_cb_data: Arbitrary data that will be passed back to the
                            msg_cb function.
        :param span_ctx_cb: The function to obtain a context manager for an
                            OpenTelemetry span.

        There are two ways to connect to the MQTT broker:
         * via TCP: both host and port must be specified;
         * via websockets: all of host, port, and websocket_path must
           be specified;
         * via UNIX socket (only for local broker): socket_path must
           be specified.

        If socket_path is specified, then a connection through the UNIX
        socket is used; if websocket_path is specified, then a websocket
        connection is used; otherwise a TCP connection is used.

        If tls is not specified, then a heuristic will be made: if the
        port is the default well-known clear port, (1883 for MQTT over
        TCP, 80 for MQTT over WebSockets), then the connection will be
        attempted without TLS, i.e. in clear; for any other port, TLS is
        used and there is no fallback to connecting in clear. If tls is
        specified and is None, then the same heuristic is used as if it
        were not specified; if it is False, then no TLS is used for the
        connection; if it is True, TLS is used for the connection. Using
        TLS requires that the certificate of the authority that signed
        the server certificate, be present in the system certificate
        store (e.g. ca-certificates from Mozilla). TLS is never
        attempted over UNIX socket.

        Specifying a message callback as msg_cb allows subscribing to,
        and thus receiving messages from specific topics. If no msg_cb
        is provided, it is not possible to subscribe, and only emitting
        is permitted. msg_cb must be a callable that accepts at least
        those keyword arguments: data, topic, payload. Ideally, to be
        future-proof, it should be declared with:
            def my_cb(
                *_args,
                *,
                data: Any,
                topic: str,
                payload: bytes,
                **_kwargs,
            ) -> None

        If msg_cb_data is specified, it is passed as the data keyword
        argument of msg_cb, otherwise None is passed.
        """

        self.msg_cb = msg_cb
        self.msg_cb_data = msg_cb_data

        if socket_path is not None:
            transport = "unix"
            self.host = socket_path
            # Fake a valid TCP port to make paho.mqtt happy
            self.port = 1
            self.name = socket_path
            tls = False
        elif websocket_path:
            transport = "websockets"
            self.host = host
            self.port = port
            self.name = f"{host}:{port}/{websocket_path}"
            if tls is None:
                tls = port != 80
        else:
            transport = "tcp"
            self.host = host
            self.port = port
            self.name = f"{host}:{port}"
            if tls is None:
                tls = port != 1883

        self.span_ctxmgr_cb = span_ctxmgr_cb

        self.client = paho.mqtt.client.Client(
            callback_api_version=paho.mqtt.enums.CallbackAPIVersion.VERSION2,
            client_id=client_id,
            protocol=paho.mqtt.client.MQTTv5,
            transport=transport,
        )
        if tls:
            self.client.tls_set()
        if transport == "websockets":
            self.client.ws_set_options(path=websocket_path)

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
        with self.span_ctxmgr_cb(
            name="IoT3 Core MQTT Message",
            kind=otel.SpanKind.PRODUCER,
        ) as span:
            new_traceparent = span.to_traceparent()
            span.set_attribute(key="iot3.core.mqtt.topic", value=topic)
            properties = paho.mqtt.properties.Properties(
                paho.mqtt.packettypes.PacketTypes.PUBLISH,
            )
            if new_traceparent:
                properties.UserProperty = ("traceparent", new_traceparent)
            msg_info = self.client.publish(
                topic=topic,
                payload=payload,
                properties=properties,
            )
            if msg_info.rc:
                span.set_status(
                    status_code=otel.SpanStatus.ERROR,
                    status_message=paho.mqtt.client.error_string(msg_info.rc),
                )

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
            self.unsubscribe(topics=self.subscriptions)

    # In theory, we would not need this method, as we could very well
    # have set   self.client.on_message = msg_cb   and be done with
    # that. Having this intermediate __on_message() allows us to do
    # telemetry.
    def __on_message(
        self,
        _client,
        _userdata,
        message: paho.mqtt.client.MQTTMessage,
    ):
        span_kwargs = {
            "name": "IoT3 Core MQTT Message",
            "kind": otel.SpanKind.CONSUMER,
        }
        try:
            properties = dict(message.properties.UserProperty)
            span_kwargs["span_links"] = [properties["traceparent"]]
        except Exception:
            # There was ultimately no traceparent in that message, ignore
            pass
        with self.span_ctxmgr_cb(**span_kwargs) as span:
            new_traceparent = span.to_traceparent()
            span.set_attribute(key="iot3.core.mqtt.topic", value=message.topic)
            self.msg_cb(
                data=self.msg_cb_data,
                topic=message.topic,
                payload=message.payload,
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
