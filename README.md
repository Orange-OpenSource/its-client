# its-client

[![Build Status](https://github.com/tigroo31/its-client/workflows/Rust/badge.svg)](https://github.com/tigroo31/its-client/actions)
[![crates.io](https://img.shields.io/crates/v/its-client)](https://crates.io/crates/its-client)

This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on
the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility
(connected and autonomous vehicles, road side units, vulnerable road users,...).

Let's connect your device or application to our Intelligent Transport Systems (ITS) platform!

## Packages

We provide many packages into the same project.

### Rust libits-client

The Rust library to build a client.

### Rust libits-copycat

A Rust library example cloning each message and waiting 3 seconds before to send it as un new mobile thing.

### Rust its-client

A Rust binary example to connect on the ITS platform using the libits-copycat example.
