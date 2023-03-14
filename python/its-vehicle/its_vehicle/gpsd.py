# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import dataclasses
import datetime
import errno
import json
import logging
import os
import select
import socket
import threading
import time


@dataclasses.dataclass(frozen=True)
class GNSSReport:
    """A GNSS report.

    All values in SI units, with arbitrary precision.
    Any value (but timestamp) may be None, when unknown or unavailable.
    """

    # UNIX timestamp this object was created at, with arbitrary sub-second precision
    timestamp: float
    # Time as sent by the GNSS service, with arbitrary sub-second precision
    time: float | None = None
    longitude: float | None = None
    latitude: float | None = None
    altitude: float | None = None
    # Speed over ground (i.e. without vertical component)
    speed: float | None = None
    # Orientation from true North (geographic North, not magnetic North!)
    track: float | None = None


class GNSSProvider:
    ClassesOfInterest = ["TPV"]
    DefaultData = {
        "TPV": None,
    }

    def __init__(self, *, cfg):
        logging.debug("creating a gpsd GNSS client")

        self.cfg = dict(cfg)

        # Even if we do have code for the "timestamp" heuristic, it is
        # not officially supported, for being untested and having other
        # drawbacks. Only explicitly accept the "order" heuristic.
        if self.cfg["heuristic"] != "order":
            raise NotImplementedError(
                f"gpsd '{self.cfg['heuristic']}' heuristic not supported",
            )

        # Type coercion
        self.cfg["port"] = int(self.cfg["port"])
        logging.debug("starting gpsd provider with: %s", repr(self.cfg))

        self.data = None
        self.sock = None
        self.sock_fd = None
        self.should_stop = False
        self.event_fd = os.eventfd(0)
        self.poll = select.poll()
        self.poll.register(self.event_fd, select.POLLIN)
        self.thread = threading.Thread(
            target=self._loop,
            name="gnss.gpsd",
            daemon=True,
        )

    def start(self):
        logging.debug("starting gpsd GNSS client")
        self.thread.start()

    def stop(self, wait=True):
        logging.debug("stopping gpsd GNSS client")
        self.should_stop = True
        os.eventfd_write(self.event_fd, 1)
        if wait:
            self.join()

    def join(self):
        self.thread.join()
        logging.debug("stopped gpsd GNSS client")

    def get(self) -> GNSSReport | None:
        return self.data

    def _connect(self):
        logging.debug("connecting to %s:%s", self.cfg["host"], self.cfg["port"])
        if self.sock is not None:
            raise RuntimeError("Already connected")

        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.settimeout(2.0)
        self.sock.connect((self.cfg["host"], self.cfg["port"]))
        self.sock_fd = self.sock.makefile("rwb")
        # sock.makefile() objects do not have a fileno() method,
        # so we must watch the actual socket.
        self.poll.register(self.sock, select.POLLIN)
        logging.debug("starting WATCH mode")
        self.sock_fd.write('?WATCH={"enable":true,"json":true};\n'.encode())
        self.sock_fd.flush()
        self._data = self._new_epoch_data()
        logging.debug("connected and listening")

    def _new_epoch_data(self):
        data = dict(GNSSProvider.DefaultData)
        for k in data:
            data[k] = dict()

        return data

    def _disconnect(self):
        logging.debug("disconnecting")
        if self.sock_fd:
            logging.debug("closing sock_fd")
            self.sock_fd.close()
            self.sock_fd = None
            # The socket is only registered after we create sock_fd.
            # so if we have a self.sock_fd, we had a self.sock.
            self.poll.unregister(self.sock)
        if self.sock:
            logging.debug("closing sock")
            self.sock.close()
            self.sock = None
        self.data = None
        logging.debug("disconnected")

    def _loop(self):
        logging.debug("gpsd GNSS client started")
        while True:
            if self.should_stop:
                break

            if not self.sock:
                try:
                    self._connect()
                except (
                    socket.timeout,
                    TimeoutError,
                    ConnectionRefusedError,
                    OSError,
                ) as e:
                    logging.debug("connection failed: %s", repr(e))
                    if type(e) is OSError:
                        # We're only interested in a few errno:
                        #  - EBADF (9): Bad file descriptor (e.g. interrupted during connection)
                        #  - EHOSTUNREACH (113): No route to host
                        # If not, just bubble the error up...
                        if e.errno not in [errno.EBADF, errno.EHOSTUNREACH]:
                            raise
                    # if there's no pending event, loop to retry the connection
                    if not self.poll.poll(0):
                        self._disconnect()
                        time.sleep(1)
                        continue

            poll_lst = self.poll.poll()
            for fd, _ in poll_lst:
                if fd == self.event_fd:
                    logging.debug("event, maybe we need to stop?")
                    break
                elif fd == self.sock.fileno():
                    logging.debug("gpsd is talking")
                    try:
                        self._read_sock()
                    except (socket.timeout, TimeoutError, ConnectionResetError) as e:
                        logging.debug("connection error: %s", repr(e))
                        self._disconnect()
                else:
                    raise RuntimeError("Unexpected filedescriptor while polling")

        # Out of the loop, cleanup and close
        self._disconnect()

    def _read_sock(self):
        # Socket-related exceptions are caught by the caller
        msg_json = self.sock_fd.readline()
        # When the socket got severed, we don't always notice (no idea
        # why we don't always get TimeoutError or ConnectionResetError)
        # but we get a short-read, which gives an empty message. When
        # the socket is not in error, we never get a short read, so an
        # empty message is a very good indication that the socket has
        # some issue...
        if not msg_json:
            raise ConnectionResetError("short read")

        try:
            msg = json.loads(msg_json)
        except json.decoder.JSONDecodeError:
            logging.debug(
                "invalid JSON message: %s...%s of %d bytes",
                msg[:16],
                msg[-16:],
                len(msg),
            )
            # The GPSD protocol specifies a maximum length of messages,
            # and that, as a consequence, the JSON sentence may get
            # truncated. So, we just ignore any invalid JSON sentence.
            return

        try:
            if msg["class"] not in GNSSProvider.ClassesOfInterest:
                logging.debug(
                    "not an interesting message (%s)",
                    msg["class"],
                )
                return
        except KeyError:
            # Hopefully the first 42 bytes will be enough to recognise
            # why the message had no class.
            logging.debug("message with no 'class' [%s]", msg_json[:42])
            return

        # TPV, GST, ATT (and others) messages are emitted as separate
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
        # Warning: last but not least, some messages can be emitted at
        # least twice in a single epoch; this is the case for the SKY
        # message, with the first message having all the satellites data
        # but the second having none of it (not even nSat or uSat).
        # So care must be had when storing such message. Fortunately,
        # we've never seen ATT or GST messages being duplicated so far
        # (for the single good reason that we have no GNSS device at
        # hand that provide the corresponding data...).
        if self.cfg["heuristic"] == "order":
            # So, we use a crude heuristic: we assume that the TPV
            # message is the last to be emitted in a GNSS epoch, so we
            # store all messages we receive, and when we get a TPV one,
            # we bundle everything we have about this epoch, queue it
            # for further computations, and drop all the stored messages
            # to start a new epoch afresh.

            # .update() so that  messages accumulate rather than replace
            # any previous one (critical for SKY messages for example,
            # and even though we don't use those, we have no guarantee
            # those we do listen for don't behave similarly).
            self._data[msg["class"]].update(msg)
            if msg["class"] == "TPV":
                self._set_data()
                self._data = self._new_epoch_data()

        elif self.cfg["heuristic"] == "timestamp":
            # Altenate heuristic, based on the 'time' field:
            # if:
            #  - the current message has a 'time' field, and
            #  - all the stored messages have a 'time' field, and
            #  - the 'time' field of the current message is greater
            #    than the 'time' field of stored messages
            # then:
            #  - send all stored messages
            #  - drop stored messages
            #  - store the current message
            # else:
            #    - store the current message
            #   if:
            #    - the current message is a 'TPV', and
            #    - any of the stored message has no 'time' field
            #   then:
            #    - send all stored messages
            #    - drop all stored messages
            #
            # The advantage of this time-based heuristic, is that we are
            # (to a great extent) pretty sure that all the messages we did
            # aggregate so far and that we are sending as a single unit,
            # are strongly correlated one to the others, so this makes for
            # a good unit to work KPIs and other computations on.
            #
            # The disadvantage, though, and this is a big one, is that we
            # send data of a specific epoch at the begining of the next
            # epoch. So, we get to do our KPIs and computations on data that
            # is now aged of one period (e.g. 0.2s for a 5Hz rate). This is
            # a pretty big drawback, so much so that pursuing this heuristic
            # is not very interesting...
            #
            # This is all a bit convoluted, though, but we keep it below
            # for reference (it never got tested at all).

            any_msg = self._data[list(self._data)[0]]
            if (
                "time" in msg
                and all([("time" in self._data[c]) for c in self._data])
                and (
                    datetime.datetime.fromisoformat(msg["time"])
                    > datetime.datetime.fromisoformat(any_msg["time"])
                )
            ):
                self._set_data()
                self._data = dict(GNSSProvider.DefaultData)
                self._data[msg["class"]] = msg
            else:
                self._data[msg["class"]] = msg
                if msg["class"] == "TPV" and any(
                    [("time" not in self._data[c]) for c in self._data]
                ):
                    self._set_data()
                    self._data = dict(GNSSProvider.DefaultData)

    def _set_data(self):
        tpv = self._data["TPV"]
        tpv_time = tpv.get("time", None)
        if tpv_time is not None:
            if tpv_time[-1] == "Z":
                tpv_time = tpv_time[:-1]
            tpv_time = datetime.datetime.fromisoformat(tpv_time).timestamp()

        self.data = GNSSReport(
            timestamp=time.time(),
            time=tpv_time,
            longitude=tpv.get("lon", None),
            latitude=tpv.get("lat", None),
            altitude=tpv.get("altHAE", None),
            speed=tpv.get("speed", None),
            track=tpv.get("track", None),
        )
        logging.debug("gpsd GNSS client data available: %s", self.data)
