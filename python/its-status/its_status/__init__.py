# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import glob
import importlib.util
import linuxfd
import os.path
import sys
import threading
import time

# This global variable does not only store the lists of plugins.
# It is also (ab)used to store the global state (not least of which,
# the eventfd used by plugins to relase the main loop).
plugins = {
    "collectors": {},
    "emitters": {},
}

###############################################################################
# Note about timerfd and eventfd:
# In this file, we make use of a timerfd, and multiple eventfd filedescriptors.
# We never close any of them, because we always need them; when we exit, the
# kernel will close them for us. That's not quite clean, but it is so much
# easier rather than enclosing every loops in big try-except blocks...
###############################################################################


def init(*args, **kwargs):
    files = [
        f
        for f in glob.glob(os.path.join(os.path.dirname(__file__), "*.py"))
        if os.path.isfile(f) and not os.path.basename(f) == "__init__.py"
    ]

    # See note about timerfd and eventfd, above.
    plugins["release_fd"] = os.eventfd(0)
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
        plugins[plugin_type][name] = {
            "name": name,
            "obj": mod.Status(*args, **kwargs),
        }
        if plugin_type == "collectors":
            plugins["collectors"][name].update(
                {
                    # See note about timerfd and eventfd, above.
                    "trigger_fd": os.eventfd(0),
                    "release_fd": plugins["release_fd"],
                    "thread": threading.Thread(
                        target=_plugin_loop,
                        name=f"collector.{name}",
                        args=(plugins["collectors"][name], *args),
                        kwargs=kwargs,
                        daemon=True,
                    ),
                }
            )


def loop(*args, **kwargs):
    freq = kwargs["cfg"].getfloat("generic", "frequency", fallback=1.0)
    collect_ts = kwargs["cfg"].getboolean(
        "generic", "timestamp_collect", fallback=False
    )

    for n in plugins["collectors"]:
        plugins["collectors"][n]["thread"].start()

    # See note about timerfd and eventfd, above.
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

        # Trigger all collectors to start collecting their data
        for c in plugins["collectors"]:
            os.eventfd_write(plugins["collectors"][c]["trigger_fd"], 1)

        # Wait for all collectors to have finished collecting their data
        cpt = len(plugins["collectors"])
        while cpt:
            # We can get release events from more than one plugin at a time,
            # so we actually need to count them
            cpt -= os.eventfd_read(plugins["release_fd"])

        # Gather the data from all collectors
        for c in plugins["collectors"]:
            if c == "static":
                status.update(plugins["collectors"][c]["data"])
            else:
                status[c] = plugins["collectors"][c]["data"]

        if collect_ts:
            status["collect"] = {"start": now}
            for c in plugins["collectors"]:
                status["collect"][c] = plugins["collectors"][c]["collect_ts"]
            status["collect"]["duration"] = time.time() - now

        # Here, we'd send them to MQTT or anywhere else
        status["timestamp"] = time.time()
        for e in plugins["emitters"]:
            plugins["emitters"][e]["obj"].emit(status)


def _plugin_loop(plugin, *args, **kwargs):
    collect_ts = kwargs["cfg"].getboolean(
        "generic", "timestamp_collect", fallback=False
    )
    while True:
        try:
            evt = os.eventfd_read(plugin["trigger_fd"])
        except InterruptedError:
            # Someone sent a signal to this thread...
            continue

        if collect_ts:
            start = time.time()
        plugin["obj"].capture()
        if collect_ts:
            plugin["collect_ts"] = {
                "start": start,
                "duration": time.time() - start,
            }

        plugin["data"] = plugin["obj"].collect()

        os.eventfd_write(plugin["release_fd"], 1)
