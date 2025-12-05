# Software Name: its-interqueuemanager
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

from configparser import ConfigParser
from typing import Any, Optional


def str2bool(
    value: Optional[str],
    *,
    fallback: Any = None,
):
    """Convert a string to a boolean

    If value is None, default is returned. Otherwise, the boolean
    corresponding to value is returned; value can be any of the
    boolean values define by the module configparser, e.g. "False",
    "True", etc…

    The signature is similar to that of configparser.ConfigParser.get(),
    with fallback a keyword-only.
    """
    if value is None:
        return fallback

    try:
        return ConfigParser.BOOLEAN_STATES[value]
    except KeyError:
        raise ValueError(f"Expected boolean value, got '{value}'")
