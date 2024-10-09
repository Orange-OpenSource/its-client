# Software Name: IoT3 Mobility
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import json

from . import etsi
from .cam import CAM
from .denm import DENM
from .gnss import GNSSReport


def message_from_json(
    *,
    msg_json: str,
) -> etsi.Message:
    """Create a new ITS message from a json sentence"""
    _MSG_TYPE_CLASS = {
        "cam": CAM,
        "denm": DENM,
    }
    # We should validate msg_json here before loading it,
    # but we need to know the type of message first
    try:
        msg_py_obj = json.loads(msg_json)
    except json.decoder.JSONDecodeError:
        raise RuntimeError("Not a known ITS message")
    try:
        cls = _MSG_TYPE_CLASS[msg_py_obj["type"]]
        source_uuid = msg_py_obj["source_uuid"]
    except (KeyError, TypeError):
        raise RuntimeError("Not a known ITS message")
    # Create a raw ITS mesage
    msg = cls(uuid=source_uuid, gnss_report=GNSSReport())
    # And now just use the origianl message content
    msg._message = msg_py_obj

    return msg
