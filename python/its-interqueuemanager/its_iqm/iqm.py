# Software Name: its-interqueuemanager
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

from __future__ import annotations
import configparser
import iot3.core.mqtt
import iot3.core.otel
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

        prefix = self.cfg["general"]["prefix"]
        suffix = self.cfg["general"]["suffix"]

        inqueue = "inQueue"
        outqueue = "outQueue"
        interqueue = self.cfg["local"]["interqueue"]
        if prefix is not None:  # can be an empty string for a /-rooted queue
            inqueue = f"{prefix}/{inqueue}"
            outqueue = f"{prefix}/{outqueue}"
            interqueue = f"{prefix}/{interqueue}"
        if suffix:  # can *not* be an empty string
            inqueue += f"/{suffix}"
            outqueue += f"/{suffix}"
            interqueue += f"/{suffix}"

        # Neighbours will publish there too, so keep it to avoid recomputing
        # it every time
        self.outqueue = outqueue

        if cfg["telemetry"]["endpoint"]:
            self.otel = iot3.core.otel.Otel(
                service_name="its-interqueuemanager",
                endpoint=cfg["telemetry"]["endpoint"],
                username=cfg["telemetry"]["username"],
                password=cfg["telemetry"]["password"],
                batch_period=5.0,
                max_backlog=500,
                compression=iot3.core.otel.Compression.GZIP,
            )
            self.span_cb = self.otel.span
        else:
            self.otel = None
            self.span_cb = iot3.core.otel.Otel.noexport_span

        logging.info("create local qm")

        qm_data = {
            "copy_qm": None,
            "copy_from": inqueue,
            "copy_to": [outqueue, interqueue],
        }

        self.local_qm = iot3.core.mqtt.MqttClient(
            client_id=cfg["local"]["client_id"],
            socket_path=cfg["local"]["socket-path"],
            username=cfg["local"]["username"],
            password=cfg["local"]["password"],
            msg_cb=self.qm_copy_cb,
            msg_cb_data=qm_data,
            span_ctxmgr_cb=self.span_cb,
        )
        self.local_qm.subscribe(topics=[inqueue + "/#"])

        # The central authority will call our update_cb(), for which we
        # will need to have a valid local_qm to pass to the neighbours
        # queue managers, so we need to handle the central authority
        # after we create the local QM.
        self.authority = its_iqm.authority.Authority(
            self.instance_id,
            self.cfg["authority"],
            self.update_cb,
        )

    def run_forever(self):
        self.neighbours = dict()
        self.neighbours_clients = dict()
        if self.otel:
            self.otel.start()
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
        if self.otel:
            self.otel.stop()

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
            for nghb_id in loaded_nghbs
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

            prefix = self.cfg["general"]["prefix"]
            suffix = self.cfg["general"]["suffix"]
            if "prefix" in loaded_nghbs[nghb_id]:
                prefix = loaded_nghbs[nghb_id]["prefix"]
            if "suffix" in loaded_nghbs[nghb_id]:
                suffix = loaded_nghbs[nghb_id]["suffix"]
            interqueue = loaded_nghbs[nghb_id]["queue"]
            if prefix is not None:  # can be an empty string for a /-rooted queue
                interqueue = f"{prefix}/{interqueue}"
            if suffix:  # can *not* be an empty string
                interqueue += f"/{suffix}"

            qm_data = {
                "copy_qm": self.local_qm,
                "copy_from": interqueue,
                "copy_to": [self.outqueue],
            }
            creds = {
                "username": None,
                "password": None,
            }
            for k in creds:
                if k in loaded_nghbs[nghb_id]:
                    creds[k] = loaded_nghbs[nghb_id][k]
            self.neighbours_clients[nghb_id] = iot3.core.mqtt.MqttClient(
                client_id=self.cfg["neighbours"]["client_id"],
                host=loaded_nghbs[nghb_id]["host"],
                port=int(loaded_nghbs[nghb_id]["port"]),
                **creds,
                msg_cb=self.qm_copy_cb,
                msg_cb_data=qm_data,
                span_ctxmgr_cb=self.span_cb,
            )
            self.local_qm.subscribe(topics=[interqueue + "/#"])
            self.neighbours_clients[nghb_id].start()

    def qm_copy_cb(
        self,
        *_args,
        data: dict,
        topic: str,
        payload: bytes,
        **_kwargs,
    ):
        mqtt_cli = data["copy_qm"] or self.local_qm
        for cp_to in data["copy_to"]:
            new_topic = cp_to + topic[len(data["copy_from"]) :]
            mqtt_cli.publish(
                topic=new_topic,
                payload=payload,
            )
