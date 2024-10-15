# Software Name: its-interqueuemanager
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import argparse
import configparser
import its_iqm.iqm
import logging
import sys

CFG = "/etc/its/iqm.cfg"
DEFAULTS = {
    "general": {
        "prefix": None,
        "suffix": "v2x",
    },
    "telemetry": {
        "endpoint": None,
        "username": None,
        "password": None,
    },
    "local": {
        "username": None,
        "password": None,
        "client_id": "iqm",
        "interqueue": "interQueue",
    },
    "authority": {
        "type": "file",
        "path": "/etc/its/neighbours.cfg",
        "reload": 60,
        "username": None,
        "password": None,
        "client_id": None,
    },
    "neighbours": {},
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
        action="store_true",
        help="Print tons of debug messages on stderr",
    )
    args = parser.parse_args()

    logging.basicConfig(
        stream=sys.stderr,
        format="%(asctime)s %(module)s: %(message)s",
        level=logging.DEBUG if args.debug else logging.INFO,
    )

    logging.info(f"loading config file {args.config}...")
    cfg = configparser.ConfigParser()
    with open(args.config) as f:
        cfg.read_file(f)

    # configparser.ConfigParser() only accepts strings as values, but we
    # need None for some defaults, so make it a true dict() of dicts()s,
    # which is easier to work with.
    cfg = {s: {k: cfg[s][k] for k in cfg[s]} for s in cfg if s != "DEFAULT"}

    def _set_default(section, key, default):
        if section not in cfg:
            cfg[section] = dict()
        if key not in cfg[section]:
            cfg[section][key] = default

    _set_default("neighbours", "client_id", cfg["general"]["instance-id"])
    for s in DEFAULTS:
        for k in DEFAULTS[s]:
            _set_default(s, k, DEFAULTS[s][k])

    logging.info("create IQM...")
    iqm = its_iqm.iqm.IQM(cfg)
    logging.info("run IQM...")
    iqm.run_forever()
