# Software Name: its-iqm
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

from __future__ import annotations
import configparser
import logging
import requests
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
            name="Authority::http",
            daemon=True,
        )

    def start(self):
        logging.info(
            f"starting authority http client to {self.cfg['uri']}@{self.cfg['reload']}"
        )
        self.thread.start()

    def stop(self):
        logging.info(f"stopping authority http client to {self.cfg['uri']}")
        # We're a daemon thread, we'll get killed automatically eventually...

    def join(self):
        # We're a daemon thread, we'll get killed automatically eventually...
        pass

    def run(self):
        self.load()
        while True:
            # This does not give us a period that is perfectly "reload"
            # seconds, but we do not care much here, as it is solely to
            # update the list of neighbours, which does not happen so
            # frequently anyway, and we just need to reload it in a
            # "timely manner"...
            time.sleep(int(self.cfg["reload"]))
            self.load()

    def load(self):
        logging.info("loading neighbours")
        loaded_nghbs = configparser.ConfigParser()
        try:
            r = requests.get(self.cfg["uri"])
            loaded_nghbs.read_string(r.text)
        except Exception:
            # Can't download -> don't change the current state;
            # just keep using the neighbours we have, if any.
            logging.debug("failed to download the list of neighbours; changing nothing")
        logging.debug(f"loaded {len(loaded_nghbs)} neighbour(s)")
        self.update_cb({s: dict(loaded_nghbs[s]) for s in loaded_nghbs.sections()})
