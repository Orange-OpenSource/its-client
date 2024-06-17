# Software Name : libits
# SPDX-FileCopyrightText: Copyright (c) Orange SA
# SPDX-License-Identifier: MIT
#
# This software is distributed under the MIT license,
# see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
#
# Authors: see CONTRIBUTORS.md
# Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) library
# based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription
# provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

FROM rust:buster as builder

LABEL description="Rust builder to compile"
LABEL maintainer="nicolas.buffon@orange.com"
LABEL author="frederic.gardes@orange.com"
LABEL url="https://github.com/Orange-OpenSource/its-client"

RUN rustc --version && cargo --version

WORKDIR /usr/src/app

COPY src src
COPY benches benches
COPY Cargo.toml .

RUN cargo build --verbose --all && \
    cargo test --verbose --all && \
    cargo build --release

