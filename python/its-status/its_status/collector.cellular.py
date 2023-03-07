# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import json
from its_status import helpers


class Status:
    def __init__(self, *, cfg):
        pass

    def capture(self):
        data = list()
        for modem in self._modem_list():
            m_ret = self._mmcli("-m", modem)
            if m_ret.returncode:
                continue
            m_j = json.loads(m_ret.stdout.decode())
            item = {
                "hardware": {
                    "vendor": m_j["modem"]["generic"]["manufacturer"],
                    "model": m_j["modem"]["generic"]["model"],
                    "revision": m_j["modem"]["generic"]["hardware-revision"],
                },
            }
            if m_j["modem"]["generic"]["state"] == "connected":
                item["operator"] = {
                    "code": m_j["modem"]["3gpp"]["operator-code"],
                    "name": m_j["modem"]["3gpp"]["operator-name"],
                }
                tech = m_j["modem"]["generic"]["access-technologies"][0]
                item["connection"] = {
                    "technology": tech,
                    "signal": [],
                }
                s_ret = self._mmcli("-m", modem, "--signal-get")
                if s_ret.returncode == 0:
                    s_j = json.loads(s_ret.stdout.decode())
                    if int(s_j["modem"]["signal"]["refresh"]["rate"]) == 0:
                        self._mmcli("-m", modem, "--signal-setup", "5")
                    else:
                        for tech in s_j["modem"]["signal"]:
                            if tech == "refresh":
                                continue
                            kpis = dict()
                            for kpi in s_j["modem"]["signal"][tech]:
                                try:
                                    kpis[kpi] = float(s_j["modem"]["signal"][tech][kpi])
                                except ValueError:
                                    pass
                            if kpis:
                                kpis["technology"] = tech
                                item["connection"]["signal"].append(kpis)
            data.append(item)

        return data

    def _modem_list(self):
        ret = self._mmcli("-L")
        if ret.returncode == 0:
            yield from json.loads(ret.stdout.decode())["modem-list"]

    def _mmcli(self, *args):
        cmd = ["mmcli", "-J"] + list(args)
        return helpers.run(cmd)
