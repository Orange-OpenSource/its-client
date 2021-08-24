# its-client

[![Build Status](https://github.com/tigroo31/its-client/workflows/Rust/badge.svg)](https://github.com/tigroo31/its-client/actions)
[![crates.io](https://img.shields.io/crates/v/its-client)](https://crates.io/crates/its-client)

The Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) Rust packages based on
the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription.

## Build

* Install Rust using rustup `curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh`
* Install rustfmt

```shell script
$ source $HOME/.cargo/env
$ rustup component add rustfmt
...
$
```

* Install docker
* To compile and build the binary and the docker image, just run `./build.sh`

## Run

TODO add details on the logger levels

TODO add details on the log files

TODO add details on the 2 targets

TODO add details on the arguments.

### Binary

Go to the target directory and execute the binary. For example:

* in debug mode
* with an online its platform

```shell script
$ cd target/release
$ RUST_LOG=debug ./its-client -H <IP_OR_HOST> -P <PORT> -u its-client -p ****
```

With another terminal, you can check the logs:

```shell script
$ cd target/release
$ tail -f log/its-client_rCURRENT.log
...
```

### Docker

Run a container. For example:

* in debug mode
* with an online its platform

```shell script
$ docker container run -it --name its-client --env RUST_LOG=debug its-client -H <IP_OR_HOST> -P <PORT> -u its-client -p ****
```

With another terminal, you can check the logs:

```shell script
$ docker container exec -it its-client /bin/bash
$ its-client@033fe953c429:/usr/src/app$ tail -f log/its-client_rCURRENT.log
...
$ its-client@033fe953c429:/usr/src/app$ exit
```

At the end, you must remove the container:

```shell script
$ docker container stop its-client
its-client
$ docker container rm its-client
its-client
```

## Development

### Suggested Environment

* Have docker installed
* Have CLion
* Configure a CLion shortcut for rustfmt as explained here: https://github.com/rust-lang/rustfmt/blob/master/intellij.md
