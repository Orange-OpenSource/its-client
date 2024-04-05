# its-client

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
* To compile and build the binary and docker image, use cargo and the `docker` command

## Run

TODO add details on the logger levels

TODO add details on the log files

TODO add details on the 2 targets (debug and release)

TODO add details on the arguments.

### Binary

Go to the target directory and execute the binary. For example:

* in debug mode
* with an online its platform

```shell script
$ cd target/release
$ RUST_LOG=debug ./its_client -H <IP_OR_HOST> -P <PORT> -u its_client -p ****
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
$ docker container run -it --name its_client --env RUST_LOG=debug its_client -H <IP_OR_HOST> -P <PORT> -u its_client -p ****
```

With another terminal, you can check the logs:

```shell script
$ docker container exec -it its_client /bin/bash
$ its_client@033fe953c429:/usr/src/app$ tail -f log/its-client_rCURRENT.log
...
$ its_client@033fe953c429:/usr/src/app$ exit
```

At the end, you must remove the container:

```shell script
$ docker container stop its_client
its_client
$ docker container rm its_client
its_client
```

## Development

### Suggested Environment

* Have docker installed
* Have CLion
* Configure a CLion shortcut for rustfmt as explained here: https://github.com/rust-lang/rustfmt/blob/master/intellij.md


### Next features

* increase the Region Of Interest from 1 to 9 tiles
* manage TLS
* detect the position using IP instead of GPS or a stub 