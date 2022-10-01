# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import psutil


class Status():
    def __init__(self, cfg):
        self.data = None
        self.static_data = {'hardware': 'oci'}
        with open('/etc/os-release', 'r') as f:
            self.static_data['os_release'] = dict()
            for l in f.readlines():
                key = l.split('=', maxsplit=1)[0]
                val = l.split('=', maxsplit=1)[1].rstrip().strip('"')
                self.static_data['os_release'][key] = val

    def capture(self):
        data = self.static_data
        data['cpu_load'] = psutil.getloadavg()
        mem = psutil.virtual_memory()
        data['memory'] = (mem.total, mem.available)
        disk = psutil.disk_usage('/data')
        data['storage'] = (disk.total, disk.free)
        self.data = data

    def collect(self):
        return self.data
