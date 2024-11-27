# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import json


class Status:
    def __init__(self, *, cfg):
        self.enabled = cfg.getboolean("stdout", "enabled", fallback=False)

    def emit(self, data):
        if self.enabled:
            print(json.dumps(data), flush=True)
