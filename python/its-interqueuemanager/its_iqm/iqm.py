# Software Name: its-interqueuemanager
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

from __future__ import annotations
import configparser
import its_iqm.mqtt_client
import its_iqm.authority
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
        conn = {
            "host": None,
            "port": None,
            "socket": None,
        }
        try:
            conn["host"] = cfg["local"]["host"]
            conn["port"] = int(cfg["local"]["port"])
        except TypeError:
            conn["socket"] = cfg["local"]["socket-path"]

        self.local_qm = its_iqm.mqtt_client.MQTTClient(
            name="local",
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
            **conn,
        )

        # The central authority will call our update_cb(), for which we
        # will need to have a valid local_qm to pass to the neighbours
        # queue managers, so we need to handle the central authority
        # after we create the local QM.
        authority_type = self.cfg["authority"]["type"]
        if authority_type == "file":
            self.authority = its_iqm.authority.file.Authority(self.cfg, self.update_cb)
        elif authority_type == "http":
            self.authority = its_iqm.authority.http.Authority(self.cfg, self.update_cb)
        elif authority_type == "mqtt":
            self.authority = its_iqm.authority.mqtt.Authority(self.cfg, self.update_cb)
        else:
            raise ValueError(f"unknown central authority type {authority_type}")

    def run_forever(self):
        self.neighbours = dict()
        self.neighbours_clients = dict()
        self.local_qm.start()
        self.authority.start()
        try:
            while True:
                time.sleep(60)
        except KeyboardInterrupt:
            # Ctrl-C on a controlling tty
            pass
        except InterruptedError:
            # Killed by a signal (e.g. TERM)
            pass

        for nghb_id in self.neighbours:
            self.neighbours_clients[nghb_id].stop()
        self.authority.stop()
        self.local_qm.stop()

    def update_cb(self, loaded_nghbs):
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
                port=int(loaded_nghbs[nghb_id]["port"]),
                socket=None,
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
