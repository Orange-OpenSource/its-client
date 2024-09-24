# Software Name: its-iqm
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

from __future__ import annotations
import json
import logging
import iot3.core.mqtt


class Authority:
    def __init__(
        self,
        instance_id: str,
        cfg: dict,
        update_cb: Callable[[its_iqm.iqm.IQM, dict], None],
    ):
        self.cfg = cfg
        self.update_cb = update_cb

        try:
            client_id = self.cfg["client_id"]
        except KeyError:
            client_id = instance_id

        self.authority_client = iot3.core.mqtt.MqttClient(
            client_id=client_id,
            host=self.cfg["host"],
            port=int(self.cfg["port"]),
            username=self.cfg["username"] or None,
            password=self.cfg["password"] or None,
            msg_cb=self.msg_cb,
        )
        self.authority_client.subscribe(topics=[self.cfg["topic"]])

    def start(self):
        logging.info(
            f"starting authority MQTT client to {self.cfg['host']}:{self.cfg['port']}"
        )
        self.authority_client.start()

    def stop(self):
        logging.info(
            f"stopping authority MQTT client to {self.cfg['host']}:{self.cfg['port']}"
        )
        self.authority_client.stop()

    def msg_cb(
        self,
        *_args,
        payload: bytes,
        **_kwargs,
    ):
        logging.info("received neighbours")
        loaded_nghbs = json.loads(payload)
        # Contrary to the 'file' or 'http' methods, which use a .cfg style
        # content, the 'mqtt' method uses a json blob. So there is no
        # "DEFAULT" section to ignore here.
        logging.debug(f"loaded {len(loaded_nghbs)} neighbour(s)")
        self.update_cb(loaded_nghbs)
