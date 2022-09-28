# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import configparser
import its_status
import json
import time

STATIC_STATUS = {
    'version': '1.0.0',
    'type': 'obu',
}


def main():
    its_status.init()
    basic_status = STATIC_STATUS
    with open('/etc/its-status/its-status.cfg') as f:
        cfg = configparser.ConfigParser()
        cfg.read_file(f)
        basic_status['id'] = cfg['generic']['id']

    while True:
        status = basic_status
        status['collect'] = {'start': time.time()}
        for c in its_status.plugins['collectors']:
            status['collect'][c] = {'start': time.time()}
            its_status.plugins["collectors"][c].capture()
            status['collect'][c]['duration'] = time.time() - status['collect'][c]['start']
        for c in its_status.plugins['collectors']:
            status[c] = its_status.plugins["collectors"][c].collect()
        status['collect']['duration'] = time.time() - status['collect']['start']

        # Here, we'd send them to MQTT or anywhere else
        status['timestamp'] = time.time()
        print(json.dumps(status), flush=True)

        # Just one iteration for now, to debug the stuff
        break
