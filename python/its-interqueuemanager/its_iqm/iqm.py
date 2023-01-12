# Software Name: its-interqueuemanager
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

from __future__ import annotations
import configparser
import its_iqm.mqtt_client
import logging
import time


DEFAULT_AUTH = {"username": None, "passwod": None}


class IQM:
    def __init__(
        self: IQM,
        cfg: configparser.ConfigParser,
    ):
        logging.info("create")
        self.cfg = cfg
        self.instance_id = self.cfg["general"]["instance-id"]

        logging.info("create local qm")
        self.local_qm = its_iqm.mqtt_client.MQTTClient(
            name="local",
            host=cfg["local"]["host"],
            port=cfg["local"]["port"],
            username=cfg["local"]["username"],
            password=cfg["local"]["password"],
            client_id=cfg["local"]["client_id"],
            local_qm=None,
            prefix=cfg["general"]["prefix"],
            suffix=cfg["general"]["suffix"],
            copy_from="inQueue",
            copy_to=[
                "outQueue",
                cfg["local"]["interqueue"],
            ],
        )

        central_type = self.cfg["central"]["type"]
        if central_type != "file":
            raise ValueError(f"unknown central authority type {central_type}")

    def run_forever(self):
        self.neighbours = dict()
        self.neighbours_clients = dict()
        self.local_qm.start()
        self.__load_path()
        try:
            while True:
                if "reload" in self.cfg["central"]:
                    # This does not give us a period that is perfectly
                    # "reload" seconds, but we do not care much here, as
                    # it is solely to update the list of neighbours,
                    # which does not happen so frequently anyway, and we
                    # just need to reload it in a "timely manner"...
                    time.sleep(self.cfg["central"]["reload"])
                    self.__load_path()
                else:
                    self.sleep(60)
        except KeyboardInterrupt:
            # Ctrl-C on a controlling tty
            pass
        except InterruptedError:
            # Killed by a signal (e.g. TERM)
            pass

        for nghb_id in self.neighbours:
            self.neighbours_clients[nghb_id].stop()
        self.local_qm.stop()

    def __load_path(self):
        logging.info("loading neighbours")
        loaded_nghbs = configparser.ConfigParser(defaults=DEFAULT_AUTH)
        try:
            with open(self.cfg["central"]["path"], "r") as fd:
                loaded_nghbs.read_file(fd)
        except FileNotFoundError:
            # No file -> no neigbour defined, i.e. empty list
            pass

        # Old neighbours are either those that are no longer
        # present, or those which description changed.
        old_nghbs_ids = [
            nghb_id
            for nghb_id in self.neighbours
            if nghb_id not in loaded_nghbs
            or self.neighbours[nghb_id] != loaded_nghbs[nghb_id]
        ]
        # New neighbours are either those we did not know before,
        # or those which description changed.
        new_nghbs_ids = [
            nghb_id
            for nghb_id in loaded_nghbs.sections()  # Avoids section "DEFAULT"
            if nghb_id not in self.neighbours
            or self.neighbours[nghb_id] != loaded_nghbs[nghb_id]
        ]

        logging.info("stopping old neighbours (if any)...")
        for nghb_id in old_nghbs_ids:
            logging.debug(f"stopping {nghb_id}...")
            self.neighbours_clients[nghb_id].stop()
            del self.neighbours[nghb_id]
            del self.neighbours_clients[nghb_id]

        logging.info("starting new neighbours (if any)...")
        for nghb_id in new_nghbs_ids:
            logging.debug(f"creating qm for {nghb_id}")
            n_type = loaded_nghbs[nghb_id]["type"]
            if n_type != "mqtt":
                raise ValueError(
                    f"only mqtt neighbours supported, not {n_type} for {nghb_id}"
                )
            self.neighbours[nghb_id] = loaded_nghbs[nghb_id]
            self.neighbours_clients[nghb_id] = its_iqm.mqtt_client.MQTTClient(
                name=nghb_id,
                host=loaded_nghbs[nghb_id]["host"],
                port=loaded_nghbs[nghb_id]["port"],
                username=loaded_nghbs[nghb_id]["username"],
                password=loaded_nghbs[nghb_id]["password"],
                client_id=self.cfg["neighbours"]["client_id"],
                local_qm=self.local_qm,
                prefix=self.cfg["general"]["prefix"],
                suffix=self.cfg["general"]["suffix"],
                copy_from=loaded_nghbs[nghb_id]["queue"],
                copy_to=["outQueue"],
            )
            self.neighbours_clients[nghb_id].start()
