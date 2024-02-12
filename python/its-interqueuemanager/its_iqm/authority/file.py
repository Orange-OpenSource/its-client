# Software Name: its-iqm
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

from __future__ import annotations
import configparser
import logging
import threading
import time


class Authority:
    def __init__(
        self,
        _instance_id: str,
        cfg: dict,
        update_cb: Callable[[its_iqm.iqm.IQM, dict], None],
    ):
        self.cfg = cfg
        self.update_cb = update_cb

        self.thread = threading.Thread(
            target=self.run,
            name="Central::file",
            daemon=True,
        )

    def start(self):
        logging.info(
            f"starting authority file client to {self.cfg['path']}@{self.cfg['reload']}"
        )
        self.thread.start()

    def stop(self):
        logging.info(f"stopping authority file client to {self.cfg['path']}")
        # We're a daemon thread, we'll get killed automatically eventually...

    def join(self):
        # We're a daemon thread, we'll get joined automatically eventually...
        pass

    def run(self):
        self.load()
        # This does not give us a period that is perfectly "reload"
        # seconds, but we do not care much here, as it is solely to
        # update the list of neighbours, which does not happen so
        # frequently anyway, and we just need to reload it in a
        # "timely manner"...
        while True:
            time.sleep(int(self.cfg["reload"]))
            self.load()

    def load(self):
        logging.info("loading neighbours")
        loaded_nghbs = configparser.ConfigParser()
        try:
            with open(self.cfg["path"], "r") as fd:
                loaded_nghbs.read_file(fd)
        except FileNotFoundError:
            # No file -> no neigbour defined, i.e. empty list
            pass
        logging.debug(f"loaded {len(loaded_nghbs)} neighbour(s)")
        self.update_cb({s: dict(loaded_nghbs[s]) for s in loaded_nghbs.sections()})
