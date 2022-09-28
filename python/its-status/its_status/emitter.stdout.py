# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import json


class Status():
    def __init__(self):
        pass

    def emit(self, data):
        print(json.dumps(data), flush=True)
