# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import warnings


class Status:
    def __init__(self, *, cfg):
        try:
            station_uuid = cfg["generic"]["station-uuid"]
        except KeyError as k_err:
            # Try the legacy 'id' field
            try:
                station_uuid = cfg["generic"]["id"]
            except KeyError:
                raise k_err from None
            warnings.warn(
                "Using the legacy general.id field; swith to general.station-uuid instead.",
            )

        self.data = {
            "version": "1.2.0",
            "type": "status",
            "station-uuid": station_uuid,
        }

    def capture(self):
        return self.data
