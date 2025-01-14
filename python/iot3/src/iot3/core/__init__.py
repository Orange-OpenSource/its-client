# Software Name: iot3
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import requests
import time as _time
import urllib
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

sample_bootstrap_config = {
    "endpoint": "http://localhost:1234/bootstrap",
    "psk": {
        "login": "username",
        "password": "secret",
    },
}


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


def bootstrap(
    *,
    ue_id: str,
    role: str,
    service_name: str,
    bootstrap_config: dict,
) -> dict:
    """Initialise the IoT3 Core SDK.

    Performs the bootstrap sequence to initialise the IoT3 Core SDK.
    Returns a configuration object to start the IoT3 Core SDK.

    :param ue_id: The ID of this application (User-Equipemnt ID)
    :param role: The role of this application.
    :param service_name: The name of the service
    :param bootstrap_config: The bootstrap config, as a dict with the following
                             keys (all mandatory):
                               * endpoint: the URI of the bootstrap server
                               * psk: the bootstrap PSK, as a dict with key:
                                   * login
                                   * password
    """
    config = dict()
    req = {
        "ue_id": ue_id,
        "psk_login": bootstrap_config["psk"]["login"],
        # FIXME: psk_password has been removed, but it's not yet deployed...
        "psk_password": bootstrap_config["psk"]["password"],
        "role": role,
    }
    rep = requests.post(
        bootstrap_config["endpoint"],
        auth=(
            bootstrap_config["psk"]["login"],
            bootstrap_config["psk"]["password"],
        ),
        json=req,
    )
    rep.raise_for_status()
    bootstrap = rep.json()

    # The order of looked up protocols is important: first, we prefer a
    # TLS-enabled protocol (mqtts, mqtt-wss), then non-TLS (mqtt, mqtt-ws)
    # as a fallback; second, we prefer native (mqtt) over websockets (ws).
    for mqtt_proto in ["mqtts", "mqtt-wss", "mqtt", "mqtt-ws"]:
        try:
            mqtt = urllib.parse.urlparse(bootstrap["protocols"][mqtt_proto])
        except KeyError:
            pass
        else:
            break
        # UGLY WART!!! Depending on where the app runs, the protocol names
        # can be prefixed wth "internal-"... Sigh... :-(
        try:
            mqtt = urllib.parse.urlparse(
                bootstrap["protocols"]["internal-" + mqtt_proto]
            )
        except KeyError:
            pass
        else:
            break
    else:
        raise RuntimeError("No known MQTT protocol available")

    mqtt_tls = mqtt_proto in ["mqtts", "mqtt-wss"]
    config["mqtt"] = {
        "client_id": bootstrap["iot3_id"],
        "host": mqtt.hostname,
        "port": mqtt.port or (11883 if mqtt_tls else 1883),
        "tls": mqtt_tls,
        "username": bootstrap["psk_run_login"],
        "password": bootstrap["psk_run_password"],
    }
    if mqtt_proto.startswith("mqtt-ws"):
        config["mqtt"]["websocket_path"] = mqtt.path

    for otlp_proto in ["otlp-https", "otlp-http"]:
        if otlp_proto in bootstrap["protocols"]:
            break
        # UGLY WART!!! Depending on where the app runs, the protocol names
        # can be prefixed wth "internal-"... Sigh... :-(
        otlp_proto = "internal-" + otlp_proto
        if otlp_proto in bootstrap["protocols"]:
            break
    else:
        raise RuntimeError("No known OTLP protocol available")

    config["otel"] = {
        "endpoint": bootstrap["protocols"][otlp_proto],
        # In practice, there will *always* be credentials provided in
        # the bootstrap response, so we'll always have authentication
        # and we know the backend only implements BasicAuth. Prove me
        # wrong! ;-)
        "auth": _otel.Auth.BASIC,
        "username": bootstrap["psk_run_login"],
        "password": bootstrap["psk_run_password"],
        "service_name": service_name,
        "batch_period": 5,
        "max_backlog": 100,
        "compression": "gzip",
    }

    return config


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
    """Wait until the IoT3 Core SDK is ready.

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
