# Rust its-client

The Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) Rust client based on
the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription.

This package provides a Rust binary example to connect on the ITS platform using the libits-copycat example.

# Docker action

This action launches the ITS client providing the log output directory.

## Inputs

## `log_output`

**Required** The log directory. Default `"/usr/src/app/log"`.

## Outputs

## `return_code`

The return code of the client.

## Example usage

uses: actions/its-client@v1
with:
log_output: '/var/log'