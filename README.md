its-client
==========

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

This repository provides reference implementation of message exchanging for IoT3 over [MQTT][1] (v5).\
On top of this, it also provides a proposal of implementation for V2X messages in [JSON][2] based on [ETSI][3]'s
Intelligent Transportation Systems (ITS).

For both of these use cases, telemetry is both automatically done and abstracted to send custom traces.

Features
--------

|                       | Rust | Python | Java |
|-----------------------|:----:|:------:|:----:|
| **MQTTv5**            |  ✓   |   ✓    |  ✓   |
| **Telemetry**         |  ✓   |   ✓    |  ✓   |
| **Device Management** |      |        |      |

### Message exchange

Each implementation provides a client implementation allowing to subscribe and publish to an MQTT broker.

### Telemetry

Each implementation might provide an abstraction of Open Telemetry features.

| Language | Traces | Logs | Metrics |
|----------|--------|------|---------|
| Rust     | ✓      |      |         |
| Python   | ✓      |      |         |
| Java     | ✓      |      |         |

#### Traces

Depending on client's choice or implementation, MQTT message publishing and reception might be automatically traced.\
The link between the span of a message publish and the span of its reception is automatically made
by propagating the [W3C Trace Context][12] using MQTTv5 properties.

Sent traces include the following parameters:

- `service.name` client app name (configurable)
- `span.status` Error if anything went wrong, Unset otherwise
- `span.name` IoT3 Core MQTT Message
- `span.kind`
    - `consumer` when receiving a message
    - `producer` when publishing a message
- attributes:
    - `iot3.core.mqtt.topic`
    - `iot3.core.mqtt.payload_size`
    - `iot3.core.sdk_language`

### Device management

_**Work in progress**_

Each implementation is _intended_ to provide an implementation of LwM2M protocol to bootstrap the device or application,
and to send periodical alive messages.

| Language | Bootstrap | Run |
|----------|-----------|-----|
| Rust     |           |     |
| Python   |           |     |
| Java     |           |     |

JSON schemas
------------

As mentioned, you can find in the `schemas` directory a proposal of implementation using the [JSON][2] language
(instead of ASN.1 UPER by default) of the following [ETSI.org][3] messages:

- CooperativeAwarenessMessage (CAM)
- CollectivePerceptionMessage (CPM)
- DecentralizedEmergencyNotificationMessage (DENM)

But also schemas of custom messages for V2X:

- Information (information about service instance)
- Status (status of an instance)

_Note: none of the provided implementation is able to use different versions of a schema,
they are using the following versions:_

- _[cam_schema_1-1-3](schema/cam_schema_1-1-3.json)_
- _[cpm_schema_1-2-1](schema/cpm_schema_1-2-1.json)_
- _[denm_schema_1-1-3](schema/denm_schema_1-1-3.json)_
- _[information_schema_1-2-0](schema/information_schema_1-2-0.json)_
- _[status_schema_1-2-0](schema/status_schema_1-2-0.json)_

Languages
---------

The features presented above are hereby provided in several languages in an SDK-like form.

Choice has been made to respect each language paradigm over providing a unique implementation;
it might therefore differ depending on the language.

### [Rust](rust/README.md)

[![Build Status](https://github.com/Orange-OpenSource/its-client/workflows/Rust/badge.svg)][4]
[![crates.io](https://img.shields.io/crates/v/libits-client)](https://crates.io/crates/libits-client)

Provides the basic abstraction for IoT3 and an application oriented V2X (mobility) set of functions and traits.

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

### [Java IoT3 Core and Mobility libraries](java/iot3/README.md)

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

[12]: https://www.w3.org/TR/trace-context/
