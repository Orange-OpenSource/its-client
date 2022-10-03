# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import argparse
import configparser
import its_status
import signal
import time

CFG = '/etc/its-status/its-status.cfg'


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--config', '-c', default=CFG,
                        help=f'Path to the configuration file (default: {CFG})')
    args = parser.parse_args()

    with open(args.config) as f:
        cfg = configparser.ConfigParser()
        cfg.read_file(f)

    its_status.init(cfg)

    def tick(_signum, _frame):
        status = dict()
        status['collect'] = {'start': time.time()}
        for c in its_status.plugins['collectors']:
            status['collect'][c] = {'start': time.time()}
            its_status.plugins["collectors"][c].capture()
            status['collect'][c]['duration'] = time.time() - status['collect'][c]['start']

        for c in its_status.plugins['collectors']:
            s = its_status.plugins["collectors"][c].collect()
            if c == 'static':
                status.update(s)
            else:
                status[c] = s

        status['collect']['duration'] = time.time() - status['collect']['start']

        # Here, we'd send them to MQTT or anywhere else
        status['timestamp'] = time.time()
        for e in its_status.plugins['emitters']:
            its_status.plugins['emitters'][e].emit(status)

    signal.signal(signal.SIGALRM, tick)
    signal.setitimer(signal.ITIMER_REAL, 1.0, 1.0)

    while True:
        time.sleep(60)
