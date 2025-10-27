Changes
=======

* IoT3 SDK:
    * core: new awesome method
    * mobility: fixed something that was broken
* applications:
    * info: new stuff
    * interqueuemanager: update some message version
    * quadkeys: nothing
    * status: add a new connection life status
    * vehicle: change the frequency

Close #XXX

Close #YYY

Test
====

How to test
-----------

_**Note:** in the following, lines starting with `$` are to be executed on your
machine, as a non-root user;
lines starting with `(docker)$` are to be executed in the Docker container;
lines starting with `(docker)🐍 $` are to be executed in the Docker container,
in the python venv._

1. Prepare a test environment:
    1. be sure to have unrestricted access to _test.mosquitto.org_ (IPv4 and IPv6)
    2. be sure to have an MQTT broker that listens locally on port 1883,
       with no credentials and no ACL; if not, run your own:
        ```sh
        $ mosquitto
        ```
    3. in another terminal, prepare a collector implementing
       the OpenTelemetry API, on localhost. If you don't have one,
       you may use an existing one, like:
        ```
        $ docker container run \
            --rm \
            -p 16686:16686 \
            -p 4318:4318 \
            jaegertracing/all-in-one:1.58
        ```
       then open a browser on the Jaegger UI
       (or that of your own collector if you have one):
        ```
        http://localhost:16686/
        ```
    4. in another terminal, start a fake _gpsd_ process
       (no need for an actual GNSS device):
        ```sh
        $ TMPDIR=/tmp gpsfake -q -P 2947 -u -n -c 0.1 tests/data/NMEA.log
        ```
    5. in another terminal, start a container with Python 3.11
       and the necessary packages:
        ```sh
        $ docker container run \
            --detach \
            --name iot3 \
            --rm \
            -ti \
            --network host \
            -e http_proxy \
            -e https_proxy \
            -e no_proxy \
            --user $(id -u):$(id -u) \
            --mount type=bind,source=$(pwd),destination=$(pwd) \
            --workdir $(pwd) \
            python:3.11.9-slim-bookworm \
            /bin/bash -il

        $ docker container exec -u 0:0 iot3 apt update
        $ docker container exec -u 0:0 iot3 apt install -y git build-essential socat
        $ docker container exec -d iot3 socat UNIX-LISTEN:/tmp/mqtt.socket,fork TCP4:localhost:1883

        $ docker container attach iot3
        ```
       _**Note:**_ the socat command exposes the MQTT broker listening
       on `localhost:1883` (see 1.2., above), inside the
       container listening on the UNIX socket `/tmp/mqtt.socket`.
    6. prepare a Python 3.11 environment, with tests dependencies
       and packages' dependencies:
        ```sh
        (docker)$ python3.11 -m venv /tmp/venv
        (docker)$ . /tmp/venv/bin/activate
        (docker)🐍 $ pip --disable-pip-version-check --no-cache-dir install \
                        -r python/requirements-tests.txt \
                        $(sed -r -e '/.*"(.+(==|>=|<=).+)".*/!d; s//\1/; s/ //g;' \
                          python/*/pyproject.toml
                         )
        ```
       _**Note:**_ we install the packages dependencies manually,
       and do not rely on _pip_ to do so, because our Python packages depend
       one on the others by git hash, as they are not published on PyPi yet,
       so installing one of our packages may overwrite another.
2. Build the Python packages:
    ```sh
    (docker)🐍 $ for pkg in python/*/; do
                    python -m build "${pkg}" || break
                 done
    ```
3. Install the Python packages:
    ```sh
    (docker)🐍 $ pip --disable-pip-version-check --no-cache-dir \
                        install --no-deps python/*/dist/*.whl
    ```
4. Run the IoT3 Core SDK tests:
    ```sh
    (docker)🐍 ./python/iot3/tests/test-iot3-core
    (docker)🐍 ./python/iot3/tests/test-iot3-core-all
    (docker)🐍 ./python/iot3/tests/test-iot3-core-mqtt
    (docker)🐍 ./python/iot3/tests/test-iot3-core-otel
    (docker)🐍 ./python/iot3/tests/test-iot3-mobility
    (docker)🐍 ./python/iot3/tests/test-iot3-mobility-gnss
    (docker)🐍 ./python/iot3/tests/test-iot3-mobility-message
    ```
5. ...

Expected results
-------

1. The test environment is ready
2. The packages were all built successfully
3. The packages were all installed successfully
4. The tests all succeed:
5. ...
