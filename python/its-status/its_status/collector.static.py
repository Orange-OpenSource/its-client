# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>


class Status():
    def __init__(self, cfg):
        self.data = {
            'version': '1.0.0',
            'type': 'obu',
            'id': cfg['generic']['id'],
        }

    def capture(self):
        pass

    def collect(self):
        return self.data
