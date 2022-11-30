# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import argparse
import configparser
import its_status
import os
import signal
import sys
import time
import traceback

CFG = "/etc/its-status/its-status.cfg"


def loop(freq, collect_ts=False):
    # We never close it, because we always need it; when we exit, the
    # kernel will close it for us. That's not quite clean, but it is
    # so much easier rather than enclosing the whole loop in a big
    # try-except.
    evt_fd = os.eventfd(0)

    def tick(_signum, _frame):
        os.eventfd_write(evt_fd, 1)

    signal.signal(signal.SIGALRM, tick)
    signal.setitimer(signal.ITIMER_REAL, 1 / freq, 1 / freq)

    tick = 0
    while True:
        try:
            evt = os.eventfd_read(evt_fd)
        except InterruptedError:
            # Someone sent a signal to this thread...
            continue

        now = time.time()
        tick += evt

        status = dict()
        if evt > 1:
            status["errors"] = {
                "timestamp": now,
                "tick": tick,
                "missed_ticks": evt - 1,
            }
            print(
                f"tick: resuming #{tick} after {evt-1} missed ticks",
                file=sys.stderr,
            )

        if collect_ts:
            status["collect"] = {"start": now}
        for c in its_status.plugins["collectors"]:
            if collect_ts:
                status["collect"][c] = {"start": time.time()}
            its_status.plugins["collectors"][c].capture()
            if collect_ts:
                status["collect"][c]["duration"] = (
                    time.time() - status["collect"][c]["start"]
                )

        for c in its_status.plugins["collectors"]:
            s = its_status.plugins["collectors"][c].collect()
            if c == "static":
                status.update(s)
            else:
                status[c] = s

        if collect_ts:
            status["collect"]["duration"] = time.time() - status["collect"]["start"]

        # Here, we'd send them to MQTT or anywhere else
        status["timestamp"] = time.time()
        for e in its_status.plugins["emitters"]:
            its_status.plugins["emitters"][e].emit(status)


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
        loop(freq, collect_ts)
    except KeyboardInterrupt:
        pass
    except Exception as e:
        traceback.print_exc()
        print(e)
