# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import argparse
import configparser
import its_status
import traceback

CFG = "/etc/its-status/its-status.cfg"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--config",
        "-c",
        default=CFG,
        help=f"Path to the configuration file (default: {CFG})",
    )
    args = parser.parse_args()

    with open(args.config) as f:
        cfg = configparser.ConfigParser()
        cfg.read_file(f)

    its_status.init(cfg)
    collect_ts = cfg.getboolean("generic", "timestamp_collect", fallback=False)
    freq = cfg.getfloat("generic", "frequency", fallback=1.0)

    try:
        its_status.loop(freq, collect_ts)
    except KeyboardInterrupt:
        pass
    except Exception as e:
        traceback.print_exc()
        print(e)
