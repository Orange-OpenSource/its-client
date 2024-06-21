libits
======

[![Build Status](https://github.com/Orange-OpenSource/its-client/workflows/Rust/badge.svg)][1]
[![crates.io](https://img.shields.io/crates/v/its-client)][2]

The Intelligent Transportation Systems (ITS) [MQTT][3] Rust package based on
the [JSON][4] [ETSI][5] specification transcription.

Examples
--------

By default, all example logs into `./log/<example>_rCURRENT.log`
You can inspect the logs using `tail -F log/its-client_rCURRENT.log`

### copycat

Subscribes to ITS CAM and CPM messages, stores them and sends a copy 3 seconds later

[1]: https://github.com/Orange-OpenSource/its-client/actions/workflows/rust.yml
[2]: https://crates.io/crates/its-client
[3]: https://mqtt.org/
[4]: https://www.json.org
[5]: https://www.etsi.org/committee/its 