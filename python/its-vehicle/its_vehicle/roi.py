# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import its_quadkeys


class RegionOfInterest:
    def __init__(
        self,
        *,
        depths: dict[str, int],
        speeds: list[float],
    ):
        self.depths = depths
        self.speeds = speeds

    def get(
        self,
        *,
        quadkey: its_quadkeys.QuadKey,
        speed: float,
        msg_type: str,
    ):
        depth = self.depths[msg_type]
        for s in self.speeds:
            if speed < s or depth == 1:
                break
            depth -= 1

        # Note: this finds the quadkeys around the current one; at the
        # equator, that gives roughly a square. However, the further
        # away we get from the equator, the narrower the horizontal
        # side of a quadkey, making for a RoI that is squeezed. A
        # better approach would be compute the quadkeys enclosed in a
        # circle, but that's non-obvious...

        shallow = quadkey.make_shallower(depth)
        roi = shallow.neighbours(as_zone=True)
        roi.add(shallow)

        return [str(q) for q in roi]
