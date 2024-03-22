# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import argparse
import configparser
import linuxfd
import logging
import os
import sys
from . import gpsd


CFG = "/etc/its/vehicle.cfg"
DEFAULTS = {
    "general": {
        "instance-id": None,
    },
    "gpsd": {
        "host": "127.0.0.1",
        "port": 2947,
        "persistence": 2.0,
        "heuristic": "order",
    },
}


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

    # The instance-id is required
    if cfg["general"]["instance-id"] is None:
        raise RuntimeError("configuration key general.instace-id is required")

    logging.basicConfig(
        stream=sys.stderr,
        format="%(asctime)s %(module)s: %(message)s",
        level="DEBUG" if args.debug else "INFO",
    )

    gnss = gpsd.GNSSProvider(cfg=cfg["gpsd"])
    gnss.start()

    timer = linuxfd.timerfd(closeOnExec=True)
    # value==0.0 disables the timer, which means we can't configure it
    # to "expire now already!", so we instead just tell it to "expire
    # really, really soon!" (i.e. in the next microsecond).
    timer.settime(value=0.000001, interval=0.2)
    try:
        while True:
            evt = timer.read()
            if evt > 1:
                logging.warning("Resuming after %d missed events", evt - 1)
            g = gnss.get()
            logging.info("Got: %s", g)
    except KeyboardInterrupt:
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
    gnss.stop()


if __name__ == "__main__":
    main()
