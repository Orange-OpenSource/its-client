# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import time
from its_status import helpers

CHRONYC = ["chronyc", "-c", "-n", "sources"]
SRC_STATE = {
    "*": "best",
    "+": "combined",
    "-": "not_combined",
    "x": "maybe_error",
    "~": "unstable",
    "?": "unusable",
}


class Status:
    def __init__(self, cfg):
        self.data = list()
        self.validity = cfg.getfloat("timesources", "validity", fallback=10.0)
        self.last_valid = 0.0
        self.refclocks = dict()
        self.enabled = True
        try:
            with open("/etc/chrony.conf", "r") as f:
                for l in f.readlines():
                    if not l.startswith("refclock "):
                        continue
                    fields = l.rstrip().split(" ")
                    for i in range(len(fields)):
                        if fields[i] == "refid":
                            self.refclocks[fields[i + 1]] = fields[1].lower()
                            break
        except FileNotFoundError:
            # No chrony config file means chrony not installed
            self.enabled = False

    def capture(self):
        if not self.enabled:
            return
        now = time.time()
        ret = helpers.run(CHRONYC)
        if ret.returncode != 0:
            if self.data and now - self.last_valid > self.validity:
                self.data = list()
            return

        data = list()
        for l in ret.stdout.decode().splitlines():
            fields = l.split(",")
            src = {
                "state": SRC_STATE[fields[1]],
                "offset": float(fields[-2]),
                "error": float(fields[-1]),
            }
            name = fields[2]
            if name in self.refclocks:
                if self.refclocks[name] == "pps":
                    src["type"] = "pps"
                    src["label"] = name
                else:  # Only SHM-NMEA is know apart PPS
                    src["type"] = "nmea"
                src["stratum"] = 0
            else:
                src["type"] = "ntp"
                src["host"] = name
                src["stratum"] = int(fields[3])
            data.append(src)
        self.last_valid = now
        self.data = data

    def collect(self):
        return self.data
