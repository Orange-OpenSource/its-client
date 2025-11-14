libits-client
=============

[![Build Status](https://github.com/Orange-OpenSource/its-client/workflows/Rust/badge.svg)][1]
[![crates.io](https://img.shields.io/crates/v/libits-client)][2]

This crate provides IoT3 [MQTT][3] and [OpenTelemetry][4] generic clients and,
on top of this, an [ETSI][5] [Intelligent Transport System][6] messages implementation using [JSON][7]

Examples
--------

### Common environment

1. Let's be sure to have unrestricted access to [test.mosquitto.org](https://test.mosquitto.org/) (IPv4 and IPv6)
2. In a terminal, prepare a collector implementing the OpenTelemetry API, on localhost. If you don't have one,
   you may use an existing one, like using docker:
    ```shell
    docker container run \
        --name jaeger \
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

**Note: this example does not send any message, so it has to be used with a sender example from the python
implementation to work relevantly**

You can manually send an information and a stopped CAM message with the following commands:

```shell
docker container run -it --rm eclipse-mosquitto mosquitto_pub -h test.mosquitto.org -p 8886 -t default/outQueue/v2x/info --tls-version tlsv1.2 --capath /etc/ssl/certs/ -m "{\"message_type\": \"information\", \"version\": \"2.1.0\", \"source_uuid\": \"ora_app_info-002\", \"instance_id\": \"ora_app_message-002\", \"instance_type\": \"central\", \"running\": true, \"timestamp\": 1742226701618, \"validity_duration\": 3600, \"service_area\": {\"type\": \"tiles\", \"quadkeys\": [\"0\", \"1\", \"2\", \"3\"]}}"
```

```shell
docker container run -it --rm eclipse-mosquitto mosquitto_pub -h test.mosquitto.org -p 8886 -t default/outQueue/v2x/cam/com_car_555/0/3/1/3/3/3/1/1/1/2/0/2/1/0/0/1/2/1/2/1/2/1 --tls-version tlsv1.2 --capath /etc/ssl/certs/ -m "{\"message_type\":\"cam\",\"origin\":\"self\",\"version\":\"2.2.0\",\"source_uuid\":\"com_car_555\",\"timestamp\":1742227617044,\"message\":{\"protocol_version\":1,\"station_id\":555,\"generation_delta_time\":64291,\"basic_container\":{\"station_type\":5,\"reference_position\":{\"latitude\":447753167,\"longitude\":-6518623,\"position_confidence_ellipse\":{\"semi_major\":10,\"semi_minor\":50,\"semi_major_orientation\":1},\"altitude\":{\"value\":14750,\"confidence\":1}}},\"high_frequency_container\":{\"basic_vehicle_container_high_frequency\":{\"heading\":{\"value\":1800,\"confidence\":2},\"speed\":{\"value\":0,\"confidence\":3},\"drive_direction\":0,\"vehicle_length\":{\"value\":40,\"confidence\":0},\"vehicle_width\":20,\"longitudinal_acceleration\":{\"value\":10,\"confidence\":2},\"curvature\":{\"value\":11,\"confidence\":4},\"curvature_calculation_mode\":0,\"yaw_rate\":{\"value\":562,\"confidence\":2}}}}}"
```

NB: you can also use the deprecated version 1.1.3 of a CAM message:

```shell
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

Then you can send a non-stopped CAM message many times during more than 3 seconds in a loop:

```shell
docker container run -it --rm eclipse-mosquitto mosquitto_pub -h test.mosquitto.org -p 8886 -t default/outQueue/v2x/cam/com_car_555/0/3/1/3/3/3/1/1/1/2/0/2/1/0/0/1/2/1/2/1/2/1 --tls-version tlsv1.2 --capath /etc/ssl/certs/ -m "{\"message_type\":\"cam\",\"origin\":\"self\",\"version\":\"2.2.0\",\"source_uuid\":\"com_car_555\",\"timestamp\":1742227617044,\"message\":{\"protocol_version\":1,\"station_id\":555,\"generation_delta_time\":64291,\"basic_container\":{\"station_type\":5,\"reference_position\":{\"latitude\":447753167,\"longitude\":-6518623,\"position_confidence_ellipse\":{\"semi_major\":10,\"semi_minor\":50,\"semi_major_orientation\":1},\"altitude\":{\"value\":14750,\"confidence\":1}}},\"high_frequency_container\":{\"basic_vehicle_container_high_frequency\":{\"heading\":{\"value\":1800,\"confidence\":2},\"speed\":{\"value\":144,\"confidence\":3},\"drive_direction\":0,\"vehicle_length\":{\"value\":40,\"confidence\":0},\"vehicle_width\":20,\"longitudinal_acceleration\":{\"value\":10,\"confidence\":2},\"curvature\":{\"value\":11,\"confidence\":4},\"curvature_calculation_mode\":0,\"yaw_rate\":{\"value\":562,\"confidence\":2}}}}}"
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

Logs are redirected to output. By default, no exporter is used:

```
INFO [libits::client::logger] Logger ready on stdout
INFO [libits::transport::mqtt] Transport: standard MQTT; TLS enabled
INFO [collector] Receiver on ["#"]
INFO [collector] Exporter stdout not configured: Could not found field 'stdout'
INFO [collector] Exporter file not configured: Could not found field 'file'
INFO [collector] Exporter mqtt not configured: Could not found field 'mqtt'
INFO [collector] Exporter stdout deactivated
INFO [collector] Exporter file deactivated
INFO [collector] Exporter mqtt deactivated
INFO [libits::transport::mqtt::mqtt_router] Registered route for topic: #
...
```

If you want to use an exporter, you can configure it in the configuration file.

You can activate a `stdout` exporter to write the messages to the console:

```config
[exporter]
# optional, true to export the received messages to the console, default to false
stdout = true
```

The `stdout` exporter prints it to the console, with the logs:

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

You can activate a `file` exporter to write the messages to a file:

```config
# optional, true to export the received messages to files, default to false
file = true
# optional, directory where to store the files, default to '/data/collector'
#file_directory = "/data/collector"
# optional, number of lines stored before the file is rotated, default to 10000
#file_nb_line = 10000
```

The `file` exporter saves it to a file, rotating and compressing each 10000 lines:

```shell
cat /data/collector/*.log | wc -l && ls -lh /data/collector/
```

```
8073
total 1,3M
-rw-rw-r-- 1 user group 156K abr.  11 10:41 collector_20250411_104132_771.tar.gz
-rw-rw-r-- 1 user group 234K abr.  11 10:41 collector_20250411_104136_120.tar.gz
-rw-rw-r-- 1 user group 241K abr.  11 10:41 collector_20250411_104140_217.tar.gz
-rw-rw-r-- 1 user group 204K abr.  11 10:41 collector_20250411_104144_181.tar.gz
-rw-rw-r-- 1 user group 452K abr.  11 10:41 collector_20250411_104148_210.log
```

You can activate a `mqtt` exporter to write the messages to a(nother) broker:

```config
# optional, true to export the received messages to a mqtt broker, default to false
mqtt = true
# broker host to export to
host = test.mosquitto.org
# broker port is the port to export to
port = 1883
# true to use the TLS protocol
use_tls = false
# true to use the MQTT WebSocket protocol
use_websocket = false
# client id to provide at the connection
client_id = com_app_its-exporter-1
# optional, connection timeout
#connection_timeout = 60
# optional, ACL username
#username = username
# optional, ACL password
#password = password
# optional, list of topic level to update with its new value. e.g. "1=default","2=exporter"
topic_level_update_list = "1=collector"
```

The `mqtt` exporter copies the messages to `test.mosquitto.org`
on the port `1883` without TLS neither webSocket,
using the client id `com_app_its-exporter-1`
and updating topic level 1 with the new value `collector`
(to not loop here, we're using the same broker to receive and publish):

```shell
docker container run -it --rm eclipse-mosquitto mosquitto_sub -h test.mosquitto.org -p 1883 -t "collector/#" -v
```

```
...
collector/homey/shelly-powder-room-dimmer/measure-temperature "34.5"
collector/saccal/em/serial "failed to read modbus !!!"
collector/saccal/em/serial "Retrying 1 ..."
collector/GarageTemperatures/fridgeHumidity "26.5"
collector/GarageTemperatures/mqttTemperatureRec "4267"
collector/GarageTemperatures/garageTemperature "55.9"
collector/GarageTemperatures/garageHumidity "51.3"
collector "23.8"
...
```

You can filter the reception of messages by topics:

```config
[receiver]
# optional, list of topic (with a comma ',' separator) to subscribe to, default to "#"
topic_list = "test/topic1","test/topic2"
# optional, topic level number to put together into the router
#route_level = 1

[exporter]
# optional, true to export the received messages to the console, default to false
stdout = true
```

The `stdout` exporter prints only the messages from the `test/topic1` and `test/topic2` topics:

```shell
docker container run -it --rm eclipse-mosquitto mosquitto_pub -h test.mosquitto.org -p 8886 -t test/topic1 --capath /etc/ssl/certs/ -m "message of the topic 1"
docker container run -it --rm eclipse-mosquitto mosquitto_pub -h test.mosquitto.org -p 8886 -t test/topic2 --capath /etc/ssl/certs/ -m "message of the topic 2"
```

```
INFO [libits::client::logger] Logger ready on stdout
INFO [libits::transport::mqtt] Transport: standard MQTT; TLS enabled
INFO [collector] Receiver on ["test/topic1", "test/topic2"]
INFO [collector] Exporter file not configured: Could not found field 'file'
INFO [collector] Exporter mqtt not configured: Could not found field 'mqtt'
INFO [collector] Exporter stdout activated
INFO [collector] Exporter file deactivated
INFO [collector] Exporter mqtt deactivated
INFO [libits::transport::mqtt::mqtt_router] Registered route for topic: test/topic1
INFO [libits::transport::mqtt::mqtt_router] Registered route for topic: test/topic2
message of the topic 1
message of the topic 2
```

You can filter the reception of messages by topics including wld cards (`+` and/or `#`)
and indicate the topic level to put together into the router:

```config
[receiver]
# optional, list of topic (with a comma ',' separator) to subscribe to, default to "#"
topic_list = "test/topic3/#","test/topic4/+/data"
# optional, topic level number to put together into the router
route_level = 2

[exporter]
# optional, true to export the received messages to the console, default to false
stdout = true
```

The `stdout` exporter prints only the messages from the `test/topic3/#` and `test/topic4/+/data` topics
and groups all the messages on two routes of level 2, so `test/topic3` or `test/topic4`:

```shell
docker container run -it --rm eclipse-mosquitto mosquitto_pub -h test.mosquitto.org -p 8886 -t test/topic3/subinformation --capath /etc/ssl/certs/ -m "message of the topic 3"
docker container run -it --rm eclipse-mosquitto mosquitto_pub -h test.mosquitto.org -p 8886 -t test/topic4/subinformation/data --capath /etc/ssl/certs/ -m "message of the topic 4"
```

```
INFO [libits::client::logger] Logger ready on stdout
INFO [libits::transport::mqtt] Transport: standard MQTT; TLS enabled
INFO [collector] Receiver on ["test/topic3/#", "test/topic4/+/data"] with the route level 2
INFO [collector] Exporter file not configured: Could not found field 'file'
INFO [collector] Exporter mqtt not configured: Could not found field 'mqtt'
INFO [collector] Exporter stdout activated
INFO [collector] Exporter file deactivated
INFO [collector] Exporter mqtt deactivated
INFO [libits::transport::mqtt::mqtt_router] Registered route for topic: test/topic3
INFO [libits::transport::mqtt::mqtt_router] Registered route for topic: test/topic4
message of the topic 3
message of the topic 4
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
