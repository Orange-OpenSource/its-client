# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import json
import socket
import threading
import time


class Status:
    def __init__(self, *, cfg):
        self.cfg = cfg["gnss"]
        self.version = None
        self.model = None
        self.rate = None
        self.last_tpv = {
            "tpv": None,
            "timestamp": 0,
        }
        self.last_sky = {
            "sky": None,
            "timestamp": 0,
        }

        self.thread = threading.Thread(
            target=self.__loop,
            name=f"{__name__}.gpsd_client",
            daemon=True,
        )
        self.sock = None
        self.__stop = False
        self.cnt = 0
        self.thread.start()

    def __connect(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.settimeout(2.0)
        self.sock.connect((self.cfg["host"], int(self.cfg["port"])))
        self.sock_fd = self.sock.makefile("rwb")
        # From here on, we're only manipulating the connection via sock_fd
        self.sock.close()
        self.sock_fd.write("?DEVICES;\n".encode())
        self.sock_fd.write('?WATCH={"enable":true,"json":true};\n'.encode())
        self.sock_fd.flush()

    def __loop(self):
        while True:
            if self.__stop:
                break

            if self.sock is None:
                try:
                    self.__connect()
                except (socket.timeout, TimeoutError, ConnectionRefusedError):
                    self.sock = None
                    time.sleep(1)
                    continue

            # Every so often, request the status of devices: gpsd refines its
            # identification of devices as it receives more messages from the
            # devices, so the identification can vary with time (especially
            # at the beginning).
            self.cnt += 1
            if self.cnt % 50 == 0:
                self.sock_fd.write("?DEVICES;\n".encode())
                self.sock_fd.flush()

            try:
                msg_json = self.sock_fd.readline()
                # When the socket got severed, we don't always notice (no idea
                # why we don't always get TimeoutError or ConnectionResetError)
                # but we get a short-read, which gives an empty message. When
                # the socket is not in error, we never get a short read, so an
                # empty message is a very good indication that the socket has
                # some issue...
                if not msg_json:
                    raise ConnectionResetError("short read")
            except (socket.timeout, TimeoutError, ConnectionResetError):
                self.sock_fd.close()
                self.sock = None
                continue

            try:
                msg = json.loads(msg_json)
            except json.decoder.JSONDecodeError:
                # The GPSD protocol specifies a maximum length of messages, and
                # that, as a consequence, the JSON sentence may get truncated.
                continue

            if msg["class"] == "VERSION":
                self.version = msg["release"]

            elif msg["class"] == "DEVICES" and msg["devices"]:
                # We only expect exactly one device, no less, no more.
                dev = msg["devices"][0]
                model = ""
                for field in ["driver", "subtype", "subtype1"]:
                    try:
                        model += dev[field] + " "
                    except KeyError:
                        pass
                if model.rstrip():
                    self.model = model.rstrip()

                try:
                    self.rate = dev["cycle"]
                except KeyError:
                    pass

            elif msg["class"] == "TPV":
                self.last_tpv = {
                    "tpv": msg,
                    "timestamp": time.time(),
                }

            elif msg["class"] == "SKY":
                # Memorise the last SKY event only if it has both nSat
                # and uSat, or if it has the list of satellites
                if "satellites" in msg or ("nSat" in msg and "uSat" in msg):
                    self.last_sky = {
                        "sky": msg,
                        "timestamp": time.time(),
                    }

        if self.sock is not None:
            self.sock_fd.close()
            self.sock = None

    def stop(self):
        self.__stop = True
        self.thread.join()

    def capture(self):
        now = time.time()

        data = {
            "software": f"gpsd {self.version}" if self.version else "unknown",
            "model": self.model or "unknown",
            "mode": 0,
        }
        last_tpv = self.last_tpv
        last_sky = self.last_sky

        if self.rate:
            data["rate"] = self.rate

        if last_tpv["tpv"] and (
            now - last_tpv["timestamp"] <= float(self.cfg["persistence"])
        ):
            data["mode"] = last_tpv["tpv"]["mode"]
            try:
                # TPV.status is never 0 or 1, by protocol definition;
                # values 5 and above are not interesting for us
                if last_tpv["tpv"]["status"] in range(2, 5):
                    data["status"] = last_tpv["tpv"]["status"]
            except KeyError:
                # TPV.status may also be entirely missing
                pass

        if last_sky["sky"] and (
            now - last_sky["timestamp"] <= float(self.cfg["persistence"])
        ):
            # We only memorised a SKY if it either:
            #  - has both nSat and uSat, or
            #  - has satellites data
            try:
                data["nSat"] = last_sky["sky"]["nSat"]
                data["uSat"] = last_sky["sky"]["uSat"]
            except KeyError:
                data["nSat"] = len(last_sky["sky"]["satellites"])
                data["uSat"] = len(
                    [s for s in last_sky["sky"]["satellites"] if s["used"]]
                )

        return data


# To test this collector standalone:
#   python3 /path/to/collector.gnss.py HOST PORT
# Hit Ctrl-C to stop.
if __name__ == "__main__":
    import sys

    try:
        gnss = Status(
            cfg={
                "gnss": {
                    "host": sys.argv[1],
                    "port": sys.argv[2],
                    "persistence": 5.0,
                }
            }
        )
        while True:
            gnss.capture()
            data = gnss.collect()
            if data:
                print(json.dumps(data), flush=True)
            time.sleep(1)
    except KeyboardInterrupt:
        gnss.stop()
