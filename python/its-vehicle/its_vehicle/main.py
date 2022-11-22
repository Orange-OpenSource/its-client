# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import argparse
import configparser
import logging
import sys


CFG = "/etc/its/vehicle.cfg"
DEFAULTS = {
    "general": {
        "instance-id": None,
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


if __name__ == "__main__":
    main()
