# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import configparser
import its_status
import signal
import time

STATIC_STATUS = {
    'version': '1.0.0',
    'type': 'obu',
}


def main():
    with open('/etc/its-status/its-status.cfg') as f:
        cfg = configparser.ConfigParser()
        cfg.read_file(f)

    basic_status = STATIC_STATUS
    basic_status['id'] = cfg['generic']['id']

    its_status.init()

    def tick(_signum, _frame):
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
        for e in its_status.plugins['emitters']:
            its_status.plugins['emitters'][e].emit(status)

    signal.signal(signal.SIGALRM, tick)
    signal.setitimer(signal.ITIMER_REAL, 1.0, 1.0)

    while True:
        time.sleep(60)
