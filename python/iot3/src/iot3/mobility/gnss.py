# Software Name: IoT3 Mobility
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import copy
import dataclasses
import json
import math
import socket
import threading
import time

from typing import Optional


@dataclasses.dataclass(frozen=True)
class GNSSReport:
    """A GNSS report.

    All values in SI units (except where noted), with arbitrary precision.
    Any value (but timestamp) may be None, when unknown or unavailable.

    When a field exist in both radians and degrees, only one may be set when
    instantiating the class, not both. The other will automatically be set.

    If the error ellipse is not known, it will be emulated with the horizontal
    error, if that is known.

    :param timestamp: UNIX timestamp this object was created at, with
                      arbitrary sub-second precision; this must _not_ be
                      specified when creating a GNSSReport
    :param time: Time of the GSS measurement as sent by the GNSS service,
                 with arbitrary sub-second precision
    :param latitude: Latitude in degrees
    :param longitude: Longitude in degrees
    :param latitude_r: Latitude, in radians
    :param longitude_r: Longitude, in radians
    :param altitude: Altitude
    :param speed: Speed over ground (i.e. without vertical component)
    :param acceleration: Acceleration
    :param track: Orientation from true North (geographic North, not
                  magnetic North!)
    :param horizontal_error: Error in horizontal (2D) measurement.
    :param altitude_error: Error in altitude measurement.
    :param true_heading: True headings (may or may not be equal to track,
                         above), in degrees
    :param true_heading_r: True headings (may or may not be equal to track,
                           above), in radians
    :param magnetic_heading: Magnetic heading, in degrees
    :param magnetic_heading_r: Magnetic heading, in radians
    :param ellipse_semi_major: Length of the error ellipse semi-major axis
    :param ellipse_semi_minor: Length of the error ellipse semi-minor axis
    :param ellipse_orient: Orientation of semi-major axis of error ellipse,
                           in degrees from true North.
    """

    timestamp: float = None
    time: float | None = None
    latitude: float | None = None
    latitude_r: float | None = None
    longitude: float | None = None
    longitude_r: float | None = None
    altitude: float | None = None
    speed: float | None = None
    acceleration: float | None = None
    track: float | None = None
    horizontal_error: float | None = None
    altitude_error: float | None = None
    true_heading: float | None = None
    magnetic_heading: float | None = None
    true_heading_r: float | None = None
    magnetic_heading_r: float | None = None
    ellipse_semi_major: float | None = None
    ellipse_semi_minor: float | None = None
    ellipse_orient: float | None = None

    # Frozen dataclasses do not allow directly setting their attributes,
    # neither directly with dot notation nor with setattr(), so we must
    # use the root class 'object' to set the attributes:
    # https://docs.python.org/3/library/dataclasses.html#frozen-instances
    def __post_init__(self):
        if self.timestamp is not None:
            raise AttributeError(
                "Assigning timestamp is not allowed",
                name="timestamp",
                obj=self,
            )
        object.__setattr__(self, "timestamp", time.time())

        # Sanitise and/or emulate the error ellipse
        match self.ellipse_semi_major, self.ellipse_semi_minor, self.horizontal_error:
            # No error value, no orientation
            case None, None, None:
                object.__setattr__(self, "ellipse_orient", None)
            # If no major and no minor, use horizontal error if provided
            case None, None, float(h_error):
                object.__setattr__(self, "ellipse_semi_major", h_error)
                object.__setattr__(self, "ellipse_semi_minor", h_error)
                object.__setattr__(self, "ellipse_orient", 0.0)
            # If major but no minor, use major as minor
            case float(s_major), None, _:
                object.__setattr__(self, "ellipse_semi_minor", s_major)
                object.__setattr__(self, "ellipse_orient", 0.0)
            # If minor but no major, it does not make sense; no ellipse
            case None, float(s_minor), _:
                object.__setattr__(self, "ellipse_semi_minor", None)
                object.__setattr__(self, "ellipse_orient", None)
            # If both major and minor, check major >= minor
            case float(s_major), float(s_minor), _:
                if s_major < s_minor:
                    object.__setattr__(self, "ellipse_semi_major", s_minor)
                    object.__setattr__(self, "ellipse_semi_minor", s_major)
                    if self.ellipse_orient is not None:
                        object.__setattr__(
                            self,
                            "ellipse_orient",
                            (self.ellipse_orient + 90) % 360.0,
                        )

        fields = {
            # min_inc, max_inc: inclusive boundaries
            # min_exc, max_exc: exclusive boundaries
            "latitude": {
                "min_inc": -90.0,
                "max_inc": 90.0,
            },
            "longitude": {
                "min_exc": -180.0,
                "max_inc": 180.0,
            },
            "true_heading": {
                "min_exc": -180.0,
                "max_exc": 360.0,
            },
            "magnetic_heading": {
                "min_exc": -180.0,
                "max_inc": 180.0,
            },
        }

        def _range(f, as_radians=False):
            convert = lambda x: math.radians(x) if as_radians else x
            rng = ""
            if "min_inc" in fields[f]:
                rng += f"[{convert(fields[f]['min_inc'])}"
            elif "min_exc" in fields[f]:
                rng += f"]{convert(fields[f]['min_exc'])}"
            else:
                rng += "]..."
            rng += ", "
            if "max_inc" in fields[f]:
                rng += f"{convert(fields[f]['max_inc'])}]"
            elif "max_exc" in fields[f]:
                rng += f"{convert(fields[f]['max_exc'])}["
            else:
                rng += f"...["
            return rng

        def _validate(f, v, f_r=None, v_r=None):
            if "min_inc" in fields[f] and v < fields[f]["min_inc"]:
                raise AttributeError(
                    f"{f_r or f} {v_r if f_r else v} is out of range {_range(f, f_r)}"
                )
            if "min_exc" in fields[f] and v <= fields[f]["min_exc"]:
                raise AttributeError(
                    f"{f_r or f} {v_r if f_r else v} is out of range {_range(f, f_r)}"
                )
            if "max_inc" in fields[f] and v > fields[f]["max_inc"]:
                raise AttributeError(
                    f"{f_r or f} {v_r if f_r else v} is out of range {_range(f, f_r)}"
                )
            if "max_exc" in fields[f] and v >= fields[f]["max_exc"]:
                raise AttributeError(
                    f"{f_r or f} {v_r if f_r else v} is out of range {_range(f, f_r)}"
                )

        # For each field, validate that either are set, or none, but not both,
        # and that they are in range. If one is set, convert to the other.
        for field in fields:
            field_r = f"{field}_r"
            deg = getattr(self, field)
            rad = getattr(self, field_r)
            if deg is not None and rad is not None:
                raise AttributeError(f"Only one of {field} or {field_r} can be set")
            if deg is not None:
                _validate(field, deg)
                object.__setattr__(self, field_r, math.radians(deg))
            elif rad is not None:
                deg = math.degrees(rad)
                _validate(field, deg, field_r, rad)
                object.__setattr__(self, field, deg)


class GNSS:
    """Simple abstraction to a gpsd daemon.

    The object is a callable that returns a GNSSReport when it has at least
    a valid latitude and longitude, or None otherwise.
    """

    def __init__(
        self,
        *,
        host: Optional[str] = None,
        port: Optional[int] = None,
    ):
        """Simple abstraction to a gpsd daemon.

        :param host: The hostname or IP address the gpsd daemon runs on;
                     by default, 127.0.0.1
        :param port: The TCP port the gpsd daemon listen on; by default 2947

        Both host and port are optional, as the usual setup is to have gpsd
        run on the local machine, and listen on its well-known port.
        """
        self._host = host or "127.0.0.1"
        self._port = port or 2947

        self._thread = threading.Thread(
            target=self._loop,
            name=f"{__name__}.gpsd_client",
            daemon=True,
        )
        self._full_epoch = {}
        self._current_epoch = {}
        self._sock = None
        self._should_stop = False

    def start(self):
        self._thread.start()

    def stop(self):
        self._should_stop = True
        self._disconnect()

    def join(self, timeout: Optional[float] = None):
        self._thread.join(timeout)

    def __call__(self):
        epoch = copy.deepcopy(self._full_epoch)

        try:
            tpv_data = epoch["tpv"]
        except (TypeError, KeyError):
            # No measurement yet
            return None

        now = time.time()
        if now - tpv_data["timestamp"] > 1.0:
            # Last measurement too old
            return None

        tpv = tpv_data["msg"]
        if "lat" not in tpv or "lon" not in tpv:
            # No latitude or no longitude
            return None

        params = dict()
        params["latitude"] = tpv["lat"]
        params["longitude"] = tpv["lon"]

        params["time"] = tpv.get("time")
        params["altitude"] = tpv.get("altHAE")
        params["speed"] = tpv.get("speed")
        params["track"] = tpv.get("track")
        params["horizontal_error"] = tpv.get("eph")
        params["altitude_error"] = tpv.get("epv")

        try:
            att = epoch["att"]["msg"]
        except KeyError:
            # Not all GNSS devices provide attitude data
            pass
        else:
            params["acceleration"] = att.get("acc_len")
            params["true_heading"] = att.get("heading")
            params["magnetic_heading"] = att.get("mheading")

        return GNSSReport(**params)

    def _connect(self):
        self._sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._sock.settimeout(2.0)
        self._sock.connect((self._host, self._port))
        self._sock_fd = self._sock.makefile("rwb")
        # From here on, we're only manipulating the connection via sock_fd
        self._sock.close()
        self._sock_fd.write('?WATCH={"enable":true,"json":true};\n'.encode())
        self._sock_fd.flush()

    def _disconnect(self):
        try:
            self._sock_fd.close()
        except:
            # already closed, we don't care
            pass
        try:
            self._sock.close()
        except:
            # already closed, we don't care
            pass
        self._sock_fd = None
        self._sock = None

    def _loop(self):
        while True:
            if self._should_stop:
                break

            if self._sock is None:
                try:
                    self._connect()
                except (
                    socket.gaierror,
                    socket.timeout,
                    TimeoutError,
                    ConnectionRefusedError,
                ):
                    self._disconnect()
                    time.sleep(1)

                # Whether we succeeded in connecting or not, restart the
                # loop, so that we can catch a stop tentative that was
                # triggered while we were trying to connect.
                continue

            try:
                msg_json = self._sock_fd.readline()
                # When the socket got severed, we don't always notice (no idea
                # why we don't always get TimeoutError or ConnectionResetError)
                # but we get a short-read, which gives an empty message. When
                # the socket is not in error, we never get a short read, so an
                # empty message is a very good indication that the socket has
                # some issue...
                if not msg_json:
                    raise ConnectionResetError("short read")
            except (socket.timeout, TimeoutError, ConnectionResetError):
                self._disconnect()
                continue

            try:
                msg = json.loads(msg_json)
            except json.decoder.JSONDecodeError:
                # The GPSD protocol specifies a maximum length of messages, and
                # that, as a consequence, the JSON sentence may get truncated.
                continue

            try:
                msg_class = msg["class"].lower()
            except KeyError:
                continue
            # TPV, GST, ATT messages (and others) are emitted as separate
            # json sentences, but they are usually correlated (ATT is
            # explicitly documented to be "synchronous to the GNSS epoch".
            # However, we don't know beforehand 1. in which order they will
            # be emitted, and 2. if they will be emitted at all. There is a
            # 'time' field documented for all those messages, but it may be
            # missing, or its value may be way off (the documentation says:
            # "May be absent if the mode is not 2D or 3D. May be present,
            # but invalid, if there is no fix. Verify 3 consecutive 3D fixes
            # before believing it is UTC. Even then it may be off by several
            # seconds until the current leap seconds is known"). So, we
            # can't rely on that field to aggregate correlated messages.
            #
            # So, we use a crude heuristic: we assume that the TPV
            # message is the last to be emitted in a GNSS epoch, so we
            # store all messages we receive, and when we get a TPV one,
            # we bundle everything we have about this epoch, queue it
            # for further computations, and drop all the stored messages
            # to start a new epoch afresh.
            if msg_class in ["tpv", "att", "gst"]:
                # Only store those messages we need
                self._current_epoch[msg_class] = {
                    "timestamp": time.time(),
                    "msg": msg,
                }
            if msg_class == "tpv":
                self._full_epoch = self._current_epoch
                self._current_epoch = {}

        self._disconnect()
