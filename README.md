its-client
==========

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://github.com/Orange-OpenSource/its-client/workflows/Docker/badge.svg)](https://github.com/Orange-OpenSource/its-client/actions/workflows/docker.yml)

This Intelligent Transportation Systems (ITS) [MQTT][1] client based on
the [JSON][2] [ETSI][3] specification transcription provides a ready to connect project for the mobility
(connected and autonomous vehicles, road side units, vulnerable road users,...).

Let's connect your device or application to our Intelligent Transport Systems (ITS) platform!

Packages
--------

We provide many packages into the same project.

### JSON Schema

[ETSI.org][3] proposal of implementation using the [JSON][2] language (instead of ASN.1 UPER by default).

### Rust

[![Build Status](https://github.com/Orange-OpenSource/its-client/workflows/Rust/badge.svg)][4]
[![crates.io](https://img.shields.io/crates/v/its-client)](https://crates.io/crates/its-client)

The Rust library to build a client.

### [Python](python/README.md)

#### [iot3](python/iot3/README.md)

Provides an abstraction of IoT3 for easy manipulation in Python.

#### [its-quadkeys](python/its-quadkeys/README)

[![Build status](https://github.com/Orange-OpenSource/its-client/actions/workflows/python_its-quadkeys.yml/badge.svg)][5]

Usefull abstractions around quadtrees, suitable for the ITS clients.

#### its-info

[![Build status](https://github.com/Orange-OpenSource/its-client/actions/workflows/python_its-info.yml/badge.svg)][6]

Application periodically sending Information message.

#### its-status

[![Build status](https://github.com/Orange-OpenSource/its-client/actions/workflows/python_its-status.yml/badge.svg)][7]

Application periodically sending Status message.

#### its-interqueuemanager

[![Build status](https://github.com/Orange-OpenSource/its-client/actions/workflows/python_its-iqm.yml/badge.svg)][8]

Application 

#### its-vehicle

[![Build status](https://github.com/Orange-OpenSource/its-client/actions/workflows/python_its-vehicle.yml/badge.svg)][9]

On-board Unit oriented application sending CAM messages at high frequency.

### Java IoT3 Core and Mobility libraries

[![Build status](https://github.com/Orange-OpenSource/its-client/actions/workflows/java_iot3-core.yml/badge.svg)][10]
[![Build status](https://github.com/Orange-OpenSource/its-client/actions/workflows/java_iot3-mobility.yml/badge.svg)][11]

The Java IoT3 Mobility library - powered by the IoT3 Core library - makes it easy to build applications able to:
- connect to our ITS platform
- send and receive ITS messages through it

[1]: https://mqtt.org/
[2]: https://www.json.org
[3]: https://www.etsi.org/committee/its
[4]: https://github.com/Orange-OpenSource/its-client/actions/workflows/rust.yml
[5]: https://github.com/Orange-OpenSource/its-client/actions/workflows/python_its-quadkeys.yml
[6]: https://github.com/Orange-OpenSource/its-client/actions/workflows/python_its-info.yml
[7]: https://github.com/Orange-OpenSource/its-client/actions/workflows/python_its-status.yml
[8]: https://github.com/Orange-OpenSource/its-client/actions/workflows/python_its-iqm.yml
[9]: https://github.com/Orange-OpenSource/its-client/actions/workflows/python_its-vehicle.yml
[10]: https://github.com/Orange-OpenSource/its-client/actions/workflows/java_iot3-core.yml
[11]: https://github.com/Orange-OpenSource/its-client/actions/workflows/java_iot3-mobility.yml