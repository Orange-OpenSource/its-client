# its-client

The Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) Python packages based on
the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription.

## Build

* Install Python 3 and pip

## Run

TODO add details on the logger levels

TODO add details on the log files

TODO add details on the arguments.

### Binary

* use the `python` command to execute

## Development

Build the wheel package:

```shell
$ python3 setup.py bdist_wheel
running bdist_wheel
running build
running build_py
running egg_info
writing manifest file 'its_client.egg-info/SOURCES.txt'
/home/kvmk8371/workspace/its-client/python/venv/lib/python3.8/site-packages/setuptools/command/install.py:34: SetuptoolsDeprecationWarning: setup.py install is deprecated. Use build and pip and other standards-based tools.
  warnings.warn(
running install
running install_lib
creating build/bdist.linux-x86_64/wheel
creating build/bdist.linux-x86_64/wheel/its_client
creating build/bdist.linux-x86_64/wheel/its_client/logger
creating build/bdist.linux-x86_64/wheel/its_client/mqtt
creating build/bdist.linux-x86_64/wheel/its_client/position
creating build/bdist.linux-x86_64/wheel/its_client/test
running install_egg_info
Copying its_client.egg-info to build/bdist.linux-x86_64/wheel/its_client-1.0.0-py3.8.egg-info
running install_scripts
$
```

### Suggested Environment

* Have PyCharm
