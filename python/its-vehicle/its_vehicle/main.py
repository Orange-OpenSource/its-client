# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import argparse
import configparser
import iot3.core.mqtt
import iot3.core.otel
import logging
import os
import signal
import sys
from . import client
from . import gpsd


CFG = "/etc/its/vehicle.cfg"
DEFAULTS = {
    "general": {
        "instance-id": None,
        "report-freq": None,
        "mirror-self": False,
    },
    "telemetry": {
        "endpoint": None,
        "username": None,
        "password": None,
    },
    "broker.main": {
        "port": 1883,
        "username": None,
        "password": None,
    },
    "broker.mirror": {
        "username": None,
        "password": None,
    },
    "gpsd": {
        "host": "127.0.0.1",
        "port": 2947,
        "persistence": 2.0,
        "heuristic": "order",
    },
}


class TermSignal(Exception):
    pass


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--config",
        "-c",
        default=CFG,
        help=f"Path to the configuration file (default: {CFG})",
    )
    parser.add_argument(
        "--debug",
        "-d",
        action="store_true",
        help="Enable debug messages",
    )
    args = parser.parse_args()

    cfg_parsed = configparser.ConfigParser()
    with open(args.config, "r") as f:
        cfg_parsed.read_file(f)

    # Make the config a dict() rather than a ConfigParser(), so that
    # we can store None in there.
    cfg = {
        s: {k: cfg_parsed[s][k] for k in cfg_parsed[s]}
        for s in cfg_parsed
        if s != "DEFAULT"
    }
    # Special case: handle 'tls' specially, as it needs to be a bool but
    # ConfigParser() does not convert types automatically, and interpreting
    # the "false" string as a boolean would evaluate to True.
    cfg["broker.main"]["tls"] = cfg_parsed.getboolean(
        "broker.main",
        "tls",
        fallback=None,
    )

    def _set_default(section, key, default):
        if section not in cfg:
            cfg[section] = dict()
        if key not in cfg[section]:
            cfg[section][key] = default

    for s in DEFAULTS:
        for k in DEFAULTS[s]:
            _set_default(s, k, DEFAULTS[s][k])
    _set_default("broker.main", "client-id", cfg["general"]["instance-id"])
    _set_default("broker.mirror", "client-id", cfg["general"]["instance-id"])

    # The instance-id is required. Even if specific to the 'client'
    # part, we check it here because it is so central.
    if cfg["general"]["instance-id"] is None:
        raise RuntimeError("configuration key general.instace-id is required")

    logging.basicConfig(
        stream=sys.stderr,
        format="%(asctime)s %(module)s: %(message)s",
        level="DEBUG" if args.debug else "INFO",
    )

    otel = None
    otel_opts = {}
    if cfg["telemetry"]["endpoint"]:
        otel = iot3.core.otel.Otel(
            service_name="its-vehicle",
            endpoint=cfg["telemetry"]["endpoint"],
            username=cfg["telemetry"]["username"],
            password=cfg["telemetry"]["password"],
            batch_period=5.0,
            max_backlog=50,
            compression=iot3.core.otel.Compression.GZIP,
        )
        otel_opts["span_ctxmgr_cb"] = otel.span

    gnss = gpsd.GNSSProvider(cfg=cfg["gpsd"])

    def _msg_cb(*args, **kwargs):
        its_client.msg_cb(*args, **kwargs)

    if "host" in cfg["broker.main"]:
        conn_opts = {
            "host": cfg["broker.main"]["host"],
            "port": int(cfg["broker.main"]["port"]),
            "tls": cfg["broker.main"]["tls"],
            "websocket_path": cfg["broker.main"].get("websocket-path"),
        }
    else:
        conn_opts = {
            "socket_path": cfg["broker.main"]["socket-path"],
        }
    mqtt_main = iot3.core.mqtt.MqttClient(
        client_id=cfg["broker.main"]["client-id"],
        username=cfg["broker.main"]["username"],
        password=cfg["broker.main"]["password"],
        **conn_opts,
        **otel_opts,
        msg_cb=_msg_cb,
    )

    if "socket-path" in cfg["broker.mirror"]:
        mqtt_mirror = iot3.core.mqtt.MqttClient(
            client_id=cfg["broker.mirror"]["client-id"],
            socket_path=cfg["broker.mirror"]["socket-path"],
            username=cfg["broker.mirror"]["username"],
            password=cfg["broker.mirror"]["password"],
        )
    else:
        mqtt_mirror = None

    its_client = client.ITSClient(
        cfg=cfg["general"],
        gpsd=gnss,
        mqtt_main=mqtt_main,
        mqtt_mirror=mqtt_mirror,
    )

    def term_handler(_signum: int, _frame):
        raise TermSignal()

    try:
        signal.signal(signal.SIGTERM, term_handler)
        if otel:
            otel.start()
        gnss.start()
        mqtt_main.start()
        if mqtt_mirror is not None:
            mqtt_mirror.start()
        its_client.start()
        its_client.join()  # Should not terminate ever, but with an exception
    except (KeyboardInterrupt, TermSignal):
        # Proper termination, cleanup below
        pass
    except Exception as e:
        # Unexpected situation, we don't know what is still live and well and
        # running, or what already died, so we don't know what to properly
        # stop and terminate. We however know that all our threads are daemons,
        # so any leftover will be forcibly killed by the kernel when the
        # process exits. So let's throw our hands above our head and exit ASAP,
        # after dumping the current exception for post-mortem analysis... :-(
        logging.error("Unexpected exception ¯\_(ツ)_/¯", exc_info=e)
        os._exit(1)

    logging.debug("Will stop...")
    its_client.stop(wait=True)
    # Stop main MQTT client before the mirror one, so that we don't get
    # messages from the main one that we would then try to publish on
    # the mirror one that we just stopped.
    mqtt_main.stop()
    if mqtt_mirror is not None:
        mqtt_mirror.stop()
    gnss.stop()
    if otel:
        otel.stop()


if __name__ == "__main__":
    main()
