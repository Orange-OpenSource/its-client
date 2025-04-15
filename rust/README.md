libits-client
=============

[![Build Status](https://github.com/Orange-OpenSource/its-client/workflows/Rust/badge.svg)][1]
[![crates.io](https://img.shields.io/crates/v/libits-client)][2]

This crate provides IoT3 [MQTT][3] and [OpenTelemetry][4] generic clients and,
on top of this an [ETSI][5] [Intelligent Transport System][6] messages implementation using [JSON][7]

Examples
--------

### Common environment

1. Let's be sure to have unrestricted access to _test.mosquitto.org_ (IPv4 and IPv6)
2. In a terminal, prepare a collector implementing the OpenTelemetry API, on localhost. If you don't have one,
   you may use an existing one, like using docker:
    ```shell
    docker container run \
        --rm \
        -p 16686:16686 \
        -p 4318:4318 \
        jaegertracing/all-in-one:1.58
    ```
   Then open a browser on the Jaegger UI (or that of your own collector if you have one):
    ```
    http://localhost:16686/
    ```

### json_counter

This example demonstrates how to use the IoT3 message exchange feature.

Subscribes to `test.mosquitto.org` and yields how much messages were received
and the number of them whose payload is in JSON.

```shell
cargo run --example json_counter
```

Logs are redirected to output:

```
Transport: standard MQTT; TLS enabled
INFO [libits::client::configuration] logger ready on stdout
INFO [libits::transport::mqtt::mqtt_router] Registered route for topic: no_routing
Received 1000 messages including 997 as JSON
Received 2000 messages including 1997 as JSON
...
```

### telemetry

This example describes how to send OpenTelemetry traces and how to transmit W3C Context to link spans between traces.

We don't expose any public OTLP collector (yet?),
so you either have to use one of your own already available or spawn one:

```shell
docker container run --rm --name jaeger \
   -p 16686:16686 \
   -p 4318:4318 \
jaegertracing/all-in-one:1.58
```

Before running the example, you may edit the `[telemetry]` section of configuration
with the proper values:

```config
# telemetry feature settings
[telemetry]
# the host is the telemetry server to connect to
host = localhost
# the port is the port to connect to
port = 4318
# true to use the TLS protocol
use_tls = false
# optional, defaults to 'v1/traces'
#path = custom/v1/traces
# optional, defaults to 2048
#max_batch_size = 10
# optional, for basic auth
#username = username
# optional, for basic auth
#password = password
```

Then you can run the example:

```shell
cargo run --example telemetry --features telemetry
```

Logs are redirected to output:

```
Transport: standard MQTT; TLS enabled
INFO [libits::client::configuration] logger ready on stdout
INFO [telemetry] Send a trace with a single span 'ping' root span
INFO [telemetry] └─ Ping                  trace_id: c56c9edbfb9184ed08994e29623c2126, span_id: 75f8defbe7e6138a
INFO [telemetry] Send a trace with a single span 'pong' root span linked with the previous one 'ping'
INFO [telemetry] └─ Pong                  trace_id: 8576b9b4674d7661276df1520088c277, span_id: 1ec6b5b02a672e0f
INFO [telemetry] Send a single trace with two spans
INFO [telemetry] └─ Root                  trace_id: 57d2342bc9b27d35951bd6beb75b316d, span_id: 528468c581b9cc14
INFO [telemetry]    └─ Child              trace_id: 57d2342bc9b27d35951bd6beb75b316d, span_id: 7d3842acee425be0
INFO [telemetry] Send a trace with 3 spans from 3 threads
INFO [telemetry] └─ Main thread           trace_id: 2409551c828c0168c3828c6621c2df11, span_id: 0b27d386ff26555e
INFO [telemetry]    ├─ Sender thread      trace_id: 2409551c828c0168c3828c6621c2df11, span_id: 6cc2a49841e217be
INFO [telemetry]    └─ Listener thread    trace_id: 2409551c828c0168c3828c6621c2df11, span_id: 9c780d071865479c
```

If the `mobility` features is enabled, the `client_id` fiels is used as service name.

```shell
cargo run --example telemetry --features telemetry,mobility
```

### copycat

This example subscribes to ITS CAM and CPM messages, stores them and sends a copy 3 seconds later.

```shell
cargo run --example copycat --features geo_routing
```

Logs are redirected to output:

```
Transport: standard MQTT; TLS enabled
INFO [libits::client::configuration] logger ready on stdout
INFO [libits::client::application::pipeline] analysis thread count set to: 4
INFO [libits::client::application::pipeline] mqtt client subscribing starting...
INFO [libits::client::application::pipeline] mqtt client subscribing finished
INFO [libits::client::application::pipeline] starting MQTT listening thread...
INFO [libits::client::application::pipeline] MQTT listening thread started
INFO [libits::client::application::pipeline] starting mqtt router dispatching...
INFO [libits::client::application::pipeline] mqtt router dispatching started
INFO [libits::client::application::pipeline] starting monitor reception thread...
INFO [libits::client::application::pipeline] monitor reception thread started
INFO [libits::transport::mqtt::mqtt_client] listening started
INFO [libits::client::application::pipeline] starting filtering...
INFO [libits::client::application::pipeline] starting analyser generation...
INFO [libits::client::application::pipeline] starting analyser generation...
INFO [libits::transport::mqtt::mqtt_router] Registered route for topic: default/outQueue/v2x/cam
INFO [libits::client::application::pipeline] filter started
INFO [libits::client::application::pipeline] starting configuration reader thread...
INFO [libits::transport::mqtt::mqtt_router] Registered route for topic: default/outQueue/v2x/cpm
INFO [libits::transport::mqtt::mqtt_router] Registered route for topic: default/outQueue/v2x/denm
INFO [libits::transport::mqtt::mqtt_router] Registered route for topic: default/outQueue/v2x/info
INFO [libits::client::application::pipeline] starting analyser generation...
INFO [libits::client::application::pipeline] configuration reader thread started
INFO [libits::client::application::pipeline] starting monitor reception thread...
INFO [libits::client::application::pipeline] starting analyser generation...
INFO [libits::client::application::pipeline] monitor reception thread started
INFO [libits::client::application::pipeline] starting MQTT publishing thread...
...
```

**Note: this example does not send any message so it has to be used with a sender example from the python
implementation to work relevantly**

You can manually send an information and a stopped CAM message with the following commands:

```shell
docker container run -it --rm eclipse-mosquitto mosquitto_pub -h test.mosquitto.org -p 8886 -t default/outQueue/v2x/info --tls-version tlsv1.2 --capath /etc/ssl/certs/ -m "{\"message_type\": \"information\", \"version\": \"2.1.0\", \"source_uuid\": \"ora_app_info-002\", \"instance_id\": \"ora_app_message-002\", \"instance_type\": \"central\", \"running\": true, \"timestamp\": 1742226701618, \"validity_duration\": 3600, \"service_area\": {\"type\": \"tiles\", \"quadkeys\": [\"0\", \"1\", \"2\", \"3\"]}}"
docker container run -it --rm eclipse-mosquitto mosquitto_pub -h test.mosquitto.org -p 8886 -t default/outQueue/v2x/cam/com_car_555/0/3/1/3/3/3/1/1/1/2/0/2/1/0/0/1/2/1/2/1/2/1 --tls-version tlsv1.2 --capath /etc/ssl/certs/ -m "{\"type\":\"cam\",\"origin\":\"self\",\"version\":\"1.1.3\",\"source_uuid\":\"com_car_555\",\"timestamp\":1742227617044,\"message\":{\"protocol_version\":1,\"station_id\":555,\"generation_delta_time\":64291,\"basic_container\":{\"station_type\":5,\"reference_position\":{\"latitude\":447753167,\"longitude\":-6518623,\"altitude\":14750},\"confidence\":{\"position_confidence_ellipse\":{\"semi_major_confidence\":10,\"semi_minor_confidence\":50,\"semi_major_orientation\":1},\"altitude\":1}},\"high_frequency_container\":{\"heading\":3601,\"speed\":0,\"longitudinal_acceleration\":161,\"drive_direction\":0,\"vehicle_length\":40,\"vehicle_width\":20,\"confidence\":{\"heading\":2,\"speed\":3,\"vehicle_length\":0}}}}"
```

The application will receive the messages and log the actions:

```
...
INFO [libits::client::application::pipeline] We received an new information
...
INFO [copycat] We received an item from com_car_555 as stopped: we don't copy cat
...
```

Then you can send a non-stopped CAM message many times during more than 3 seconds in loop:

```shell
docker container run -it --rm eclipse-mosquitto mosquitto_pub -h test.mosquitto.org -p 8886 -t default/outQueue/v2x/cam/com_car_555/0/3/1/3/3/3/1/1/1/2/0/2/1/0/0/1/2/1/2/1/2/1 --tls-version tlsv1.2 --capath /etc/ssl/certs/ -m "{\"type\":\"cam\",\"origin\":\"self\",\"version\":\"1.1.3\",\"source_uuid\":\"com_car_555\",\"timestamp\":1742227617044,\"message\":{\"protocol_version\":1,\"station_id\":555,\"generation_delta_time\":64291,\"basic_container\":{\"station_type\":5,\"reference_position\":{\"latitude\":447753167,\"longitude\":-6518623,\"altitude\":14750},\"confidence\":{\"position_confidence_ellipse\":{\"semi_major_confidence\":10,\"semi_minor_confidence\":50,\"semi_major_orientation\":1},\"altitude\":1}},\"high_frequency_container\":{\"heading\":3601,\"speed\":141,\"longitudinal_acceleration\":161,\"drive_direction\":0,\"vehicle_length\":40,\"vehicle_width\":20,\"confidence\":{\"heading\":2,\"speed\":3,\"vehicle_length\":0}}}}"
```

The application will receive the messages and log the actions:

```
...
INFO [copycat] we start to schedule from com_car_555 (555)
...
INFO [copycat] we treat the scheduled item 1 from com_car_555 (555)
...
```

If the `telemetry` features is enabled both message reception and publish are traced;
it requires an OTLP collector as mentioned in the telemetry example section.

```shell
cargo run --example copycat --features geo_routing,telemetry
```

### collector

This example subscribes to messages and sends it to an exporter.

```shell
cargo run --example collector
```

Logs are redirected to output:

```
INFO [libits::client::configuration] Logger ready on stdout
Transport: standard MQTT; TLS enabled
INFO [libits::transport::mqtt::mqtt_router] Registered route for topic: #
INFO [collector] Exporter stdout activated
INFO [collector] Exporter file activated on /data/collector with switch each 10000 lines
INFO [collector] Exporter mqtt deactivated
...
```

The stdout export prints it to the console, with the logs:

```
...
{
"lasterror":"cipherkey not set!",
"uptime":"0000:23:32:50",
"UTC":"2025-03-26T10:53:47"
}
65432 OK
{"metrics":[{"alias":2,"datatype":2,"name":"DB6.DBW52","timestamp":1742986438000,"value":22517}],"seq":83,"timestamp":1742986438018}
value
...
```

The file export saves it to a file, rotating and compressing each 10000 lines:

```shell
cat /data/collector/*.log | wc -l && ls -lh /data/collector/
8073
total 1,3M
-rw-rw-r-- 1 user group 156K abr.  11 10:41 collector_20250411_104132_771.tar.gz
-rw-rw-r-- 1 user group 234K abr.  11 10:41 collector_20250411_104136_120.tar.gz
-rw-rw-r-- 1 user group 241K abr.  11 10:41 collector_20250411_104140_217.tar.gz
-rw-rw-r-- 1 user group 204K abr.  11 10:41 collector_20250411_104144_181.tar.gz
-rw-rw-r-- 1 user group 452K abr.  11 10:41 collector_20250411_104148_210.log
```

If the `telemetry` features is enabled both message reception and publish are traced;
it requires an OTLP collector as mentioned in the telemetry example section.

```shell
cargo run --example collector --features telemetry
```

[1]: https://github.com/Orange-OpenSource/its-client/actions/workflows/rust.yml

[2]: https://crates.io/crates/libits-client

[3]: https://mqtt.org/

[4]: https://opentelemetry.io/

[5]: https://www.etsi.org

[6]: https://www.etsi.org/committee/its

[7]: https://www.json.org
