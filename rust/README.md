libits-client
=============

[![Build Status](https://github.com/Orange-OpenSource/its-client/workflows/Rust/badge.svg)][1]
[![crates.io](https://img.shields.io/crates/v/libits-client)][2]

This crate provides IoT3 [MQTT][3] and [OpenTelemetry][4] generic clients and,
on top of this an [ETSI][5] [Intelligent Transport System][6] messages implementation using [JSON][7]

Examples
--------

### json_counter

Demonstrates how to use the IoT3 message exchange feature.

Subscribes to `test.mosquitto.org` and yields how much messages were received
and the number of them whose payload is in JSON.

```
cargo run --example json_counter
```

Logs are redirected to a file

```
tail -F log/json_counter_rCURRENT.log
```

### telemetry

This example describes how to send OpenTelemetry traces and how to transmit W3C context to link spans between traces.

We don't expose any public OTLP collector (yet?), so you either have to
use one of your own already available
or spawn one:

  ```
  docker run --rm --name jaeger \
      -p 16686:16686 \
      -p 4318:4318 \
  jaegertracing/all-in-one:1.58
  ```

Before running the example, you may edit the `[telemetry]` section of configuration
with the proper values.

Then you can run the example:

```
cargo run --example telemetry --features telemetry
```

### copycat

Subscribes to ITS CAM and CPM messages, stores them and sends a copy 3 seconds later.

```
cargo run --example copycat --features geo_routing
```

If the `telemetry` features is enabled both message reception and publish are traced
(it requires an OTLP collector as mentioned in the telemetry example section).

```
cargo run --example copycat --features geo_routing,telemetry
```

**Note: this example does not send any message so it has to be used with a sender example from the python
implementation to work relevantly**

[1]: https://github.com/Orange-OpenSource/its-client/actions/workflows/rust.yml

[2]: https://crates.io/crates/its-client

[3]: https://mqtt.org/

[4]: https://opentelemetry.io/

[5]: https://www.etsi.org

[6]: https://www.etsi.org/committee/its

[7]: https://www.json.org
