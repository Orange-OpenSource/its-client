# Software Name: its-quadkeys
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

from __future__ import annotations

import collections
import itertools


class QuadKey(str):
    """
    Manages QuadKeys
    """

    # A QuadKey is immutable, and its hash is actually used, so
    # we make sure any operation on a QuadKey does not mutate it.

    def __init__(self, quadkey: QuadKey | str = "0", *, separator: str = ""):
        """
        Create a new QuadKey

        quadkey: the string representation of the quadkey
        separator: the string which is used to separate the quadkey levels
        """
        super().__init__()
        if type(quadkey) is QuadKey:
            quadkey = str(quadkey)
        if type(quadkey) is not str:
            raise TypeError(
                f"cannot create a QuadKey from a {type(quadkey)}={quadkey!s}"
            )
        qk = quadkey.replace(separator, "")
        err = "".join(set([q for q in qk if q not in "0123"]))
        if err:
            raise ValueError(f"QuadKey can oly contain '0123', not any of '{err}'")
        self.quadkey = qk

    def to_str(self, separator=""):
        """Return a string representing the QuadKey"""
        return separator.join(self.quadkey)

    def depth(self):
        """Return the depth of this QuadKey"""
        return len(self.quadkey)

    def make_shallower(self, depth: int):
        """Return a new QuadKey that is shallower than this QuadKey

        If depth is strictly positive, set this QuadKey to this exact depth;
        if this QuadKey is already shallower than the requested depth, the
        QuadKey is not modified.
        If depth is negative or zero, decrease the depth of this QuadKey
        by that much; the new depth will be clamped to at least 1.

        E.g. for a QuadKey of depth 22:
            requested depth     new depth
            30                  22
            22                  22
            12                  12
            1                   1
            0                   22
            -5                  17
            -21                 1
            -22                 1
            -30                 1
        """
        if depth <= 0:
            new_depth = max(1, len(self.quadkey) + depth)
        else:
            new_depth = min(len(self.quadkey), depth)
        return QuadKey(self.quadkey[:depth])

    def split(self, *, depth: int = None, extra_depth: int = None):
        """Split this QuadKey into an extra_depth-deeper QuadZone"""
        if (depth is None and extra_depth is None) or (depth and extra_depth):
            raise RuntimeError(
                "QuadKey split needs either depth or extra_detph (and not both)"
            )
        if depth:
            extra_depth = depth - len(self.quadkey)
        z = QuadZone()
        for tail in _mk_tail_s("ALL", depth=extra_depth):
            z.add(QuadKey(self.quadkey + tail))
        return z

    def north_of(self):
        """Returns the QuadKey North of, and at the same depth as this QuadKey.

        Returns None if this QuadKey is the Northern-most QuadKey.
        """
        return QuadKey(QuadKey.__north_of_s(self.quadkey))

    def south_of(self):
        """Returns the QuadKey South of, and at the same depth as this QuadKey.

        Returns None if this QuadKey is the Southern-most QuadKey.
        """
        return QuadKey(QuadKey.__south_of_s(self.quadkey))

    def east_of(self):
        """Returns the QuadKey East of, and at the same depth as this QuadKey."""
        return QuadKey(QuadKey.__east_of_s(self.quadkey))

    def west_of(self):
        """Returns the QuadKey West of, and at the same depth as this QuadKey."""
        return QuadKey(QuadKey.__west_of_s(self.quadkey))

    def north_west_of(self):
        """Returns the QuadKey North-West of, and at the same depth as this QuadKey."""
        return QuadKey(QuadKey.__north_west_of_s(self.quadkey))

    def north_east_of(self):
        """Returns the QuadKey North-East of, and at the same depth as this QuadKey."""
        return QuadKey(QuadKey.__north_east_of_s(self.quadkey))

    def south_west_of(self):
        """Returns the QuadKey South-West of, and at the same depth as this QuadKey."""
        return QuadKey(QuadKey.__south_west_of_s(self.quadkey))

    def south_east_of(self):
        """Returns the QuadKey South-East of, and at the same depth as this QuadKey."""
        return QuadKey(QuadKey.__south_east_of_s(self.quadkey))

    def neighbours(self, *, as_zone: bool = False):
        """Return the QuadKeys neighbouring this QuadKey

        as_zone: if False (the defaut), return a namedtuple QuadKey.Neighbours;
                 if True, return a QuadZone

        If this QuadKey is the Nothern-most (Southern-most) QuadKey, no
        QuadKey to the North (South) will be returned in the QuadZone,
        while the corresponding fields in the namedtuple will be set to
        None.
        """
        nghb = _QuadNeighbours(
            NW=self.north_west_of(),
            N=self.north_of(),
            NE=self.north_east_of(),
            W=self.west_of(),
            E=self.east_of(),
            SW=self.south_west_of(),
            S=self.south_of(),
            SE=self.south_east_of(),
        )
        return QuadZone([n for n in nghb if n is not None]) if as_zone else nghb

    def __repr__(self):
        return "QuadKey('" + self.quadkey + "')"

    def __str__(self):
        return self.quadkey

    def __hash__(self):
        return hash(self.quadkey)

    def __lt__(self, other):
        return self.quadkey < str(other)

    def __le__(self, other):
        return self.quadkey <= str(other)

    def __eq__(self, other):
        return self.quadkey == str(other)

    def __ge__(self, other):
        return self.quadkey >= str(other)

    def __gt__(self, other):
        return self.quadkey > str(other)

    def __contains__(self, other):
        return str(other).startswith(self.quadkey)

    def __add__(self, other: str):
        return QuadKey(self.quadkey + other)

    _NORTH = {"0": None, "1": None, "2": "0", "3": "1"}
    _SOUTH = {"0": "2", "1": "3", "2": None, "3": None}
    _EAST_WEST = {"0": "1", "1": "0", "2": "3", "3": "2"}

    # Notes for the x_of() functions below: they are not optimised at all so
    # that it is more obvious what they do; also, they will evolve quickly to
    # support returning a list of sub-depth QuadKeys, and the way they are
    # written will (hopefully) make that easier to achieve.

    @staticmethod
    def __north_of_s(q: str):
        if q in QuadKey._NORTH:
            n_q = QuadKey._NORTH[q]
        else:
            try:
                if q[-1] == "0":
                    n_q = QuadKey.__north_of_s(q[:-1]) + "2"
                if q[-1] == "1":
                    n_q = QuadKey.__north_of_s(q[:-1]) + "3"
            except TypeError:
                n_q = None
            if q[-1] == "2":
                n_q = q[:-1] + "0"
            if q[-1] == "3":
                n_q = q[:-1] + "1"

        return n_q

    @staticmethod
    def __south_of_s(q: str):
        if q in QuadKey._SOUTH:
            s_q = QuadKey._SOUTH[q]
        else:
            if q[-1] == "0":
                s_q = q[:-1] + "2"
            if q[-1] == "1":
                s_q = q[:-1] + "3"
            try:
                if q[-1] == "2":
                    s_q = QuadKey.__south_of_s(q[:-1]) + "0"
                if q[-1] == "3":
                    s_q = QuadKey.__south_of_s(q[:-1]) + "1"
            except TypeError:
                s_q = None

        return s_q

    @staticmethod
    def __west_of_s(q: str):
        if q in QuadKey._EAST_WEST:
            w_q = QuadKey._EAST_WEST[q]
        else:
            if q[-1] == "0":
                w_q = QuadKey.__west_of_s(q[:-1]) + "1"
            if q[-1] == "1":
                w_q = q[:-1] + "0"
            if q[-1] == "2":
                w_q = QuadKey.__west_of_s(q[:-1]) + "3"
            if q[-1] == "3":
                w_q = q[:-1] + "2"

        return w_q

    @staticmethod
    def __east_of_s(q: str):
        if q in QuadKey._EAST_WEST:
            e_q = QuadKey._EAST_WEST[q]
        else:
            if q[-1] == "0":
                e_q = q[:-1] + "1"
            if q[-1] == "1":
                e_q = QuadKey.__east_of_s(q[:-1]) + "0"
            if q[-1] == "2":
                e_q = q[:-1] + "3"
            if q[-1] == "3":
                e_q = QuadKey.__east_of_s(q[:-1]) + "2"

        return e_q

    # Note: for North-West: North can be None, so it is important
    # that we take the North of the West, rather than the West of
    # the North. Similar applies to NE, and to SW and SE.
    @staticmethod
    def __north_west_of_s(q: str):
        return QuadKey.__north_of_s(QuadKey.__west_of_s(q))

    @staticmethod
    def __north_east_of_s(q: str):
        return QuadKey.__north_of_s(QuadKey.__east_of_s(q))

    @staticmethod
    def __south_west_of_s(q: str):
        return QuadKey.__south_of_s(QuadKey.__west_of_s(q))

    @staticmethod
    def __south_east_of_s(q: str):
        return QuadKey.__south_of_s(QuadKey.__east_of_s(q))


class QuadZone:
    def __init__(self, *args: list[QuadKey | str | list[QuadKey | str]]):
        """Create a new QuadZone from an iterable of QuadKeys"""
        self.quadkeys = set()
        for arg in args:
            if type(arg) is list or type(arg) is QuadZone:
                for qk in arg:
                    self.add(qk)
            else:
                self.add(arg)

    def add(self, quadkey: QuadKey | str):
        if type(quadkey) is str:
            quadkey = QuadKey(quadkey)
        if type(quadkey) is not QuadKey:
            raise ValueError(f"cannot add a {type(quadkey)}={quadkey!s}")
        if quadkey not in self:
            self.quadkeys.add(quadkey)

    def depth(self):
        """Return a tuple of the minimum and maximum depths

        The minimum depth is the depth of the shallowest QuadKey; the
        maximum depth is the depth of the deepest QuadKey.
        """
        d_min = None
        d_max = None
        for quadkey in self.quadkeys:
            d_q = quadkey.depth()
            if d_min is None or d_q < d_min:
                d_min = d_q
            if d_max is None or d_q > d_max:
                d_max = d_q
        return (d_min, d_max)

    def optimise(self):
        """Optimise the zone by coalescing smaller QuadKeys

        Recursively merge QuadKeys that form a super QuadKey:

            [Q00, Q0013021, Q01, Q02, Q03, Q1, Q2, Q3, P, P0, P2]
             |___________/                             |_______/
             |                               /¯¯¯¯¯¯¯¯¯
            [Q00, Q01, Q02, Q03, Q1, Q2, Q3, P]
             |________________/
             |
            [Q0, Q1, Q2, Q3, P]
             |____________/
             |
            [Q, P]

        (the above does _not_ represent the actual algorithm)
        """
        new_quadkeys = sorted(self.quadkeys)
        prev_nb_qk = len(new_quadkeys) + 1
        # If the previous iteration managed to reduce the set of
        # QuadKeys, then there might be an opportunity to reduce
        # even further (like coalescing reduced QuadKeys).
        # If the previous iteration could not reduce the set of
        # QuadKeys then there is nothing to do anymore.
        while prev_nb_qk > len(new_quadkeys):
            prev_nb_qk = len(new_quadkeys)
            to_merge = new_quadkeys
            new_quadkeys = list()
            while to_merge:
                quadkey = to_merge.pop(0)

                # Is previous QuadKey (if any) a root for this one?
                if new_quadkeys and quadkey in new_quadkeys[-1]:
                    continue

                # If there's less than 3 QuadKeys left after this one,
                # we're certainly not going to be able to merge them
                # with the current one; we could still coalesce them
                # with the previous one however, so let's loop to try.
                if len(to_merge) < 3:
                    new_quadkeys.append(quadkey)
                    continue

                # Are this QuadKey and the following three making a super
                # QuadKey? I.e. do we have root0, root1, root2, and root3?
                qk_depth = quadkey.depth()
                root = quadkey.make_shallower(-1)
                if (
                    to_merge[0] == root + "1"
                    and to_merge[1] == root + "2"
                    and to_merge[2] == root + "3"
                ):
                    del to_merge[:3]
                    new_quadkeys.append(root)
                else:
                    new_quadkeys.append(quadkey)

        self.quadkeys = set(new_quadkeys)

    def neighbours(self, depth: int):
        """Return a QuadZone of neighbour QuadKeys of the specified depth

        For a QuadZone of depth D:

          - neighbours of depth d0 >= D are all QuadKeys of depth d0, that have
            one or more sides or one or more corners, that touches the side or
            the corner of one or more QuadKeys of the QuadZone, and that are not
            in the QuadZone. Note that such neighbours do not overlap the
            QuadZone;

          - neighbours of depth d1 < D are all the QuadKeys of depth d1 that
            contain the neighbours of depth D. Note that such neighbours may
            partially overlap the QuadZone.

        Note: the list of neighbours may differ whether the QuadZone is optimised
        or not. For reproducibility, be sure to first optimise the QuadZone.
        """
        all_nghbs = QuadZone()
        for quadkey in self.quadkeys:
            nghbs = quadkey.neighbours()
            if quadkey.depth() >= depth:
                # If the QuadKey is deeper than (or the same depth as)
                # the requested depth, we just need to expand each of
                # its neighbours to the requested depth. If they are
                # already the correct depth, expanding will do nothing.
                for q in nghbs:
                    all_nghbs.add(q.make_shallower(depth))
            else:
                # If the QuadKey is shallower than the requested depth,
                # we need to split it down to the correct depth, and
                # keep only the border-most QuadKeys, for each type of
                # borders.
                for card in nghbs._asdict():
                    root = getattr(nghbs, card).to_str()
                    for tail in _mk_tail_s(card, depth=depth - quadkey.depth()):
                        all_nghbs.add(QuadKey(root + tail))

        final_nghbs = QuadZone()
        for quadkey in all_nghbs:
            if quadkey not in self:
                final_nghbs.add(quadkey)

        return final_nghbs

    def __len__(self):
        return len(self.quadkeys)

    def __iter__(self):
        return (quadkey for quadkey in self.quadkeys)

    def __iadd__(self, other):
        for qk in other:
            self.add(qk)
        return self

    def __add__(self, other):
        return QuadZone(self, other)

    def __contains__(self, other):
        if type(other) is str:
            other = QuadKey(other)
        for quadkey in self.quadkeys:
            if other in quadkey:
                return True
        return False

    def __str__(self):
        return str(sorted(str(q) for q in self.quadkeys))

    def __repr__(self):
        return "QuadZone('" + str(sorted(self.quadkeys)) + "')"


def _mk_tail_s(what: str, *, depth: int):
    _TAILS_FOR = {
        # fmt: off
        "NW": "3",   "N": "23",   "NE": "2",
        "W": "13", "ALL": "0123", "E": "02",
        "SW": "1",   "S": "01",   "SE": "0",
        # fmt: on
    }
    for t in itertools.product(_TAILS_FOR[what], repeat=depth):
        yield "".join(t)


_QuadNeighbours = collections.namedtuple(
    "Neighbours",
    [
        # fmt: off
        "NW", "N", "NE",
        "W",       "E",
        "SW", "S", "SE"
        # fmt: on
    ],
)
