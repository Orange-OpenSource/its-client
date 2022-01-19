from setuptools import setup, find_packages

import its_client

setup(
    name="its_client",
    version=its_client.__version__,
    author="Frederic GARDES",
    author_email="frederic(dot)gardes(at)orange(dot)com",
    maintainer="Frederic GARDES",
    maintainer_email="frederic(dot)gardes(at)orange(dot)com",
    keywords="network, its, vehicle, mqtt, etsi",
    classifiers=[
        "Development Status :: 3 - Alpha",
        "Intended Audience :: Telecommunications Industry",
        "License :: OSI Approved :: MIT License",
        "Topic :: Communications",
        "Topic :: Software Development :: Embedded Systems",
    ],
    url="https://github.com/Orange-OpenSource/its-client",
    download_url="https://pypi.org/project/its-client",
    packages=find_packages(exclude=["*.tests.*"]),
    packages_dir={"": "its_client"},
    description="The Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) Python packages based on the "
    "[JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription.",
    long_description="The Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) Python packages based on "
    "the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification "
    "transcription. It permits to connect on ITS platform as a Passenger car (station type 5).",
    license="MIT",
    platforms="LINUX",
    install_requires=[
        "gpsd-py3==0.3.0",
        "paho-mqtt==1.6.1",
        "pyGeoTile==1.0.6",
        "pytest==6.2.5",
    ],
    data_files=[("config", ["its_client.cfg"])],
)
