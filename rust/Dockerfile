# Software Name: its-client
# SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
# SPDX-License-Identifier: MIT License
#
# This software is distributed under the MIT license, see LICENSE.txt file for more details.
#
# Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
# Software description: This Intelligent Transportation
# Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](
# https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the
# mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
FROM rust:buster as builder

LABEL description="Rust builder to compile"
LABEL maintainer="nicolas.buffon@orange.com"
LABEL author="frederic.gardes@orange.com"
LABEL url="https://github.com/Orange-OpenSource/its-client"

RUN rustc --version && cargo --version

WORKDIR /usr/src/app

COPY src src
COPY Cargo.toml .

RUN cargo build --verbose --all && \
    cargo test --verbose --all && \
    cargo build --release

