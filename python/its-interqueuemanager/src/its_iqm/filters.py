# Software Name: its-interqueuemanager
# SPDX-FileCopyrightText: Copyright (c) 2025 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import itertools
import json
import logging
import re


class Filter:
    def __init__(
        self,
        *,
        name: str,
        filter_cfg: dict,
        instance_id: str,
        prefix: str,
        suffix: str,
        queues: dict,
    ):
        self.name = name

        placeholders = {
            "instance-id": instance_id,
            "prefix": prefix,
            "suffix": suffix,
        }
        placeholders.update(queues)

        self._patterns = None
        for filter_type, filter_kind in itertools.product(
            ["in", "out"],
            ["prefix", "regex"],
        ):
            try:
                patterns = filter_cfg[filter_type + "_" + filter_kind]
            except KeyError:
                continue

            if self._patterns is not None:
                raise ValueError(f"Filter {name} defines multiple patterns")
            patterns = list(filter(None, patterns.split("\n")))
            for ph in placeholders:
                patterns = list(
                    map(
                        lambda s: s.replace("{{" + ph + "}}", placeholders[ph]),
                        patterns,
                    ),
                )
            if filter_kind == "regex":
                patterns = list(map(re.compile, patterns))

            self._patterns = patterns
            self._type = filter_type
            self._kind = filter_kind

        if patterns is None:
            raise ValueError(f"Filter '{name}' does not define patterns")

        self._drop = "drop" in filter_cfg

        match filter_cfg.get("retain"):
            case None:
                self._retain = None
            case "True" | "true" | "False" | "false" as b:
                self._retain = b in ["True", "true"]
            case str(s) if re.match("^\d+$", s):
                self._retain = int(s)
            case str(s) if s.startswith("json:") and re.match(".* \d+$", s):
                path, fallback = s[5:].split(" ")
                self._retain = {
                    "path": path.split("."),
                    "fallback": int(fallback),
                }
            case str(s) if s.startswith("json:"):
                self._retain = {
                    "path": s[5:].split("."),
                    "fallback": None,
                }
            case _v:
                raise ValueError(f"Unable to parse retain value '{_v}'")

        logging.debug(f"Created new filter {name}:")
        logging.debug(f"  - type: {self._type}")
        logging.debug(f"  - kind: {self._kind}")
        logging.debug(f"  - patterns: {self._patterns}")
        logging.debug(f"  - drop: {self._drop}")
        logging.debug(f"  - retain: {self._retain}")

    @property
    def type(self):
        return self._type

    def apply(
        self,
        *,
        topic: str,
        payload: bytes,
        retain: bool | int,
    ):
        for pattern in self._patterns:
            if self._kind == "prefix" and topic.startswith(pattern):
                break
            if self._kind == "regex" and pattern.match(topic):
                break
        else:
            logging.debug(f"{self.name}[{self._type}]: no match for {topic}")
            return topic, payload, retain

        logging.debug(
            f"{self.name}[{self._type}]: match for {topic} with {self._kind} {pattern}"
        )

        if self._drop:
            logging.debug(f"{self.name}:   - dropped")
            return None, None, None

        match self._retain:
            case None:
                logging.debug("{self.name}:   - retain: None")
            case bool(b):
                logging.debug(f"{self.name}:   - retain: {b}")
                retain = b
            case int(i):
                logging.debug(f"{self.name}:   - retain: {i}")
                retain = i
            case {"path": path, "fallback": fallback}:
                logging.debug(f"{self.name}:   - retain: {path}")
                try:
                    data = json.loads(payload)
                    for p in path:
                        data = data[p]
                except (json.decoder.JSONDecodeError, KeyError, TypeError):
                    if fallback is not None:
                        logging.debug(f"{self.name}:     - using fallback {fallback}")
                        retain = fallback
                else:
                    retain = data

        logging.debug(f"{self.name}: Result of filter:")
        logging.debug(f"{self.name}:   - topic: {topic}")
        logging.debug(f"{self.name}:   - payload: {payload}")
        logging.debug(f"{self.name}:   - retain: {retain}")

        return topic, payload, retain
