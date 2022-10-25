# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import glob
import importlib.util
import os.path

plugins = {
    "collectors": {},
    "emitters": {},
}


def init(*args, **kwargs):
    files = [
        f
        for f in glob.glob(os.path.join(os.path.dirname(__file__), "*.py"))
        if os.path.isfile(f) and not os.path.basename(f) == "__init__.py"
    ]

    for f in files:
        f_name = os.path.basename(f)[:-3]
        if f_name.startswith("collector."):
            name = f_name[10:]
            plugin_type = "collectors"
        elif f_name.startswith("emitter."):
            name = f_name[8:]
            plugin_type = "emitters"
        else:
            continue
        spec = importlib.util.spec_from_file_location(name, f)
        mod = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(mod)
        plugins[plugin_type][name] = mod.Status(*args, **kwargs)
