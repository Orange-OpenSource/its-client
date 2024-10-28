# Java IoT3 Core and Mobility libraries

## IoT3 Core [![Build status](https://github.com/Orange-OpenSource/its-client/actions/workflows/java_iot3-core.yml/badge.svg)][1]
The Java IoT3 Core library serves to connect to the Orange IoT3 platform, and is intended to be used as a foundation for
applications requiring a secure, low latency and high volume exchange of messages, with measurable performance.

### Example
You will find an [example implementation][3] of the core library in the `examples` package.

## IoT3 Mobility [![Build status](https://github.com/Orange-OpenSource/its-client/actions/workflows/java_iot3-mobility.yml/badge.svg)][2]
The Java IoT3 Mobility library - powered by the IoT3 Core library - makes it easy to build applications able to:
- connect to our ITS platform
- send and receive ITS messages through it (CAM, DENM, CPM, etc.)
- subscribe to regions of interest for each type of message, thanks to a powerful tile-based system

### Example
You will find an [example implementation][4] of the mobility library in the `examples` package.

[1]: https://github.com/Orange-OpenSource/its-client/actions/workflows/java_iot3-core.yml
[2]: https://github.com/Orange-OpenSource/its-client/actions/workflows/java_iot3-mobility.yml
[3]: https://github.com/Orange-OpenSource/its-client/blob/master/java/iot3/examples/src/main/java/com/orange/Iot3CoreExample.java
[4]: https://github.com/Orange-OpenSource/its-client/blob/master/java/iot3/examples/src/main/java/com/orange/Iot3MobilityExample.java
