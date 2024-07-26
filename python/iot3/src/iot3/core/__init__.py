# Software Name: iot3
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import time as _time
from typing import Any as _Any
from typing import Callable as _Callable
from typing import Optional as _Optional
from typing import TypeAlias as _TypeAlias
from . import mqtt as _mqtt
from . import otel as _otel

"""
This module provides a simple API wrapping MQTT and OpenTelemetry.

The simple API consists in a few functions and a type annotation:

* Functions:
  * start(): start the MQTT and the OpenTelemetry clients
  * ready(): return whether the MQTT client is ready
  * wait_for_ready(): endlessly loop until ready() returns True
  * publish(): send an MQTT message
  * subscribe(): subscribe to one or more topics
  * stop(): stop the MQTT and OpenTelemetry clients

* Type annotation:
  * MsgCallbackType: the type annotation of a function called when an
                     MQTT message is received; the first argument will
                     be the value passed to start() as data_callback,
                     the second argument will be the topic the message
                     was received on, the third argument will be the
                     payload of the message; the function shall only
                     return None (but any returned value is ignored).

The simple API also provides a single object that is a sample of a basic
configuration, which can be used as a template and adpated to a local
setup.
"""


sample_config = {
    "mqtt": {
        "host": "localhost",
        "port": 1883,
        "client-id": "mqtt-client-id",
        "username": "mqtt-user",
        "password": "mqtt-secret",
    },
    "otel": {
        "service_name": "my-service",
        "endpoint": "http://localhost:4318",
        "auth": "basic",
        "username": "otel-user",
        "password": "otel-secret",
        "batch_period": 5,
        "max_backlog": 100,
        "compression": "gzip",
    },
}


MsgCallbackType: _TypeAlias = _Callable[[_Any, str, bytes], None]


def start(
    *,
    config: dict,
    message_callback: _Optional[MsgCallbackType] = None,
    callback_data: _Any = None,
) -> None:
    """Start the IoT3 Core SDK.

    :param config: The SDK configuration.
    :param message_callback: The function to call when a message is
                             received; when called, the first argument
                             will be callback_data, the second will be
                             the topic the mesage was received on, and
                             the third and last argument will be the
                             message payload.
    :param callback_data: The data to pass as first argument to
                          message_callback(), above.
    """
    global _core

    if _core is not None:
        raise RuntimeError("IoT3 Core SDK already intialised.")

    o_kwargs = dict()

    for auth in _otel.Auth:
        if config["otel"]["auth"] == auth.value:
            o_kwargs["auth"] = auth
            if auth != otel.Auth.NONE:
                o_kwargs["username"] = config["otel"]["username"]
                o_kwargs["password"] = config["otel"]["password"]
            break
    else:
        raise ValueError(f"unknown authentication {config['otel']['auth']}")

    for comp in _otel.Compression:
        if config["otel"]["compression"] == comp.value:
            o_kwargs["compression"] = comp
            break
    else:
        raise ValueError(f"unknown compression {config['otel']['compression']}")

    o = _otel.Otel(
        service_name=config["otel"]["service_name"],
        endpoint=config["otel"]["endpoint"],
        batch_period=config["otel"]["batch_period"],
        max_backlog=config["otel"]["max_backlog"],
        **o_kwargs,
    )
    o.start()

    # Wrap the callback to avoid leaking telemetry into the simple API
    def _msg_cb(*, data, topic, payload, **_kwargs):
        message_callback(data, topic, payload)

    m = _mqtt.MqttClient(
        client_id=config["mqtt"]["client-id"],
        host=config["mqtt"]["host"],
        port=config["mqtt"]["port"],
        username=config["mqtt"]["username"],
        password=config["mqtt"]["password"],
        msg_cb=_msg_cb if message_callback else None,
        msg_cb_data=callback_data,
        span_ctxmgr_cb=o.span,
    )
    m.start()

    _core = dict([("otel", o), ("mqtt", m)])


def stop():
    """Stop the Iot3 Core SDK."""
    global _core

    if _core is None:
        raise RuntimeError("IoT3 Core SDK not initialised.")

    _core["mqtt"].stop()
    _core["otel"].stop()
    _core = None


def is_ready() -> bool:
    """Checks whether the IoT3 Core SDK is ready.

    The IoT3 Core SDK is ready when the MQTT client is connected to
    the MQTT broker. Messages sent when the SDK is not ready, are
    lost without notice.
    """
    global _core

    if _core is None:
        raise RuntimeError("IoT3 Core SDK not initialised.")

    return _core["mqtt"].is_ready()


def wait_for_ready():
    """Wait until the IoT2 Core SDK is ready.

    Beware that this may take an indeterminate amount of time; in
    case the MQTT client can't connect at all, wait_for_ready()
    will wait forever and never return.
    """
    while True:
        if is_ready():
            break
        _time.sleep(0.1)


def publish(
    topic: str,
    payload: str | bytes,
):
    """Send an MQTT message

    "param topic: The MQTT topic on which to send the message.
    :param payload: The payload of the MQTT message to send.
    """
    global _core

    if _core is None:
        raise RuntimeError("IoT3 Core SDK not initialised.")

    _core["mqtt"].publish(topic=topic, payload=payload)


def subscribe(
    topics: str | list[str],
):
    """Subscribe to a list of MQTT topics.

    If a subscription already existed, it is entirely replaced.

    :param topics: A topic, or a list of topics.
    """
    global _core

    if _core is None:
        raise RuntimeError("IoT3 Core SDK not initialised.")

    topics = topics if isinstance(topics, list) else [topics]
    _core["mqtt"].subscribe_replace(topics=topics)


_core = None
