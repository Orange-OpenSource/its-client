# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import glob
import importlib.util
import linuxfd
import os.path
import sys
import time

plugins = {
    "collectors": {},
    "emitters": {},
}


def init(*args, **kwargs):
    files = [
        f
        for f in glob.glob(os.path.join(os.path.dirname(__file__), "*.py"))
        if os.path.isfile(f) and not os.path.basename(f) == "__init__.py"
    ]

    for f in files:
        f_name = os.path.basename(f)[:-3]
        if f_name.startswith("collector."):
            name = f_name[10:]
            plugin_type = "collectors"
        elif f_name.startswith("emitter."):
            name = f_name[8:]
            plugin_type = "emitters"
        else:
            continue
        spec = importlib.util.spec_from_file_location(name, f)
        mod = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(mod)
        plugins[plugin_type][name] = mod.Status(*args, **kwargs)


def loop(*args, **kwargs):
    freq = kwargs["cfg"].getfloat("generic", "frequency", fallback=1.0)
    collect_ts = kwargs["cfg"].getboolean(
        "generic", "timestamp_collect", fallback=False
    )

    # We never close it, because we always need it; when we exit, the
    # kernel will close it for us. That's not quite clean, but it is
    # so much easier rather than enclosing the whole loop in a big
    # try-except.
    timer = linuxfd.timerfd(closeOnExec=True)
    timer.settime(value=1.0 / freq, interval=1.0 / freq)

    tick = 0
    while True:
        try:
            evt = timer.read()
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
        for c in plugins["collectors"]:
            if collect_ts:
                status["collect"][c] = {"start": time.time()}
            plugins["collectors"][c].capture()
            if collect_ts:
                status["collect"][c]["duration"] = (
                    time.time() - status["collect"][c]["start"]
                )

        for c in plugins["collectors"]:
            s = plugins["collectors"][c].collect()
            if c == "static":
                status.update(s)
            else:
                status[c] = s

        if collect_ts:
            status["collect"]["duration"] = time.time() - status["collect"]["start"]

        # Here, we'd send them to MQTT or anywhere else
        status["timestamp"] = time.time()
        for e in plugins["emitters"]:
            plugins["emitters"][e].emit(status)
