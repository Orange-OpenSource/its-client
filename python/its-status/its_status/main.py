# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import argparse
import configparser
import its_status
import signal
import sys
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
    collect_ts = cfg.getboolean('generic', 'timestamp_collect', fallback=False)
    freq = cfg.getfloat('generic', 'frequency', fallback=1.0)

    def tick(_signum, _frame):
        tick.tick += 1

        errors = None
        if tick.in_tick:
            if tick.missed == 0:
                print(f'tick: missed #{tick.tick}', file=sys.stderr)
            tick.missed += 1
            return
        elif tick.missed:
            print(f'tick: resuming #{tick.tick} after {tick.missed} missed ticks', file=sys.stderr)
            errors = {
                'timestamp': time.time(),
                'type': 'status',
                'tick': tick.tick,
                'missed_ticks': tick.missed
            }
            tick.missed = 0
        tick.in_tick = True

        status = dict()
        if collect_ts:
            status['collect'] = {'start': time.time()}
        for c in its_status.plugins['collectors']:
            if collect_ts:
                status['collect'][c] = {'start': time.time()}
            its_status.plugins["collectors"][c].capture()
            if collect_ts:
                status['collect'][c]['duration'] = time.time() - status['collect'][c]['start']

        for c in its_status.plugins['collectors']:
            s = its_status.plugins["collectors"][c].collect()
            if c == 'static':
                status.update(s)
            else:
                status[c] = s

        if collect_ts:
            status['collect']['duration'] = time.time() - status['collect']['start']

        # Here, we'd send them to MQTT or anywhere else
        status['timestamp'] = time.time()
        for e in its_status.plugins['emitters']:
            if errors is not None:
                its_status.plugins['emitters'][e].error(errors)
            its_status.plugins['emitters'][e].emit(status)

        tick.in_tick = False

    setattr(tick, 'in_tick', False)
    setattr(tick, 'tick', 0)
    setattr(tick, 'missed', 0)

    signal.signal(signal.SIGALRM, tick)
    signal.setitimer(signal.ITIMER_REAL, 1/freq, 1/freq)

    while True:
        time.sleep(60)
