# its-client

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://github.com/Orange-OpenSource/its-client/workflows/Rust/badge.svg)](https://github.com/Orange-OpenSource/its-client/actions)
[![crates.io](https://img.shields.io/crates/v/its-client)](https://crates.io/crates/its-client)

This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on
the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility
(connected and autonomous vehicles, road side units, vulnerable road users,...).

Let's connect your device or application to our Intelligent Transport Systems (ITS) platform!

## Packages

We provide many packages into the same project.

### JSon Schema

[ETSI.org](https://www.etsi.org/committee/its) proposal of implementation using the JSon language (instead of ASN.1 by default).

### Rust libits-client

The Rust library to build a client.

### Rust libits-copycat

A Rust library example cloning each message and waiting 3 seconds before to send it as un new mobile thing.

### Rust its-client

A Rust binary example to connect on ITS platform using the libits-copycat example.
