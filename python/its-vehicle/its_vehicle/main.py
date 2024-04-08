# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import argparse
import configparser
import logging
import os
import signal
import sys
from . import client
from . import gpsd
from . import mqtt
from . import tracking


CFG = "/etc/its/vehicle.cfg"
DEFAULTS = {
    "general": {
        "instance-id": None,
        "report-freq": None,
        "mirror-self": False,
    },
    "broker.main": {
        "port": 1883,
        "username": None,
        "password": None,
    },
    "broker.mirror": {
        "port": 1883,
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

    cfg = configparser.ConfigParser()
    with open(args.config, "r") as f:
        cfg.read_file(f)

    def _set_default(section, key, default):
        if section not in cfg:
            cfg[section] = dict()
        if key not in cfg[section]:
            cfg[section][key] = default

    # Make the config a dict() rather than a ConfigParser(), so that
    # we can store None in there.
    cfg = {s: {k: cfg[s][k] for k in cfg[s]} for s in cfg if s != "DEFAULT"}
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

    gnss = gpsd.GNSSProvider(cfg=cfg["gpsd"])
    gnss.start()

    mqtt_main = mqtt.MqttClient(cfg=cfg["broker.main"])
    mqtt_main.start()

    if "host" in cfg["broker.mirror"] or "socket-path" in cfg["broker.mirror"]:
        mqtt_mirror = mqtt.MqttClient(cfg=cfg["broker.mirror"])
        mqtt_mirror.start()
    else:
        mqtt_mirror = None

    tracker = tracking.Tracking(cfg=cfg["tracking"], gpsd=gpsd)
    tracker.start()

    its_client = client.ITSClient(
        cfg=cfg["general"],
        gpsd=gnss,
        mqtt_main=mqtt_main,
        mqtt_mirror=mqtt_mirror,
    )
    its_client.start()

    def term_handler(_signum: int, _frame):
        raise TermSignal()

    try:
        signal.signal(signal.SIGTERM, term_handler)
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
    tracker.stop()
    # Stop main MQTT client before the mirror one, so that we don't get
    # messages from the main one that we would then try to publish on
    # the mirror one that we just stopped.
    mqtt_main.stop()
    if mqtt_mirror is not None:
        mqtt_mirror.stop()
    gnss.stop()


if __name__ == "__main__":
    main()
