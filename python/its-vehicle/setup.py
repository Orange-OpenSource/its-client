# Software Name: its-vehicle
# SPDX-FileCopyrightText: Copyright (c) 2023 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>


from setuptools import setup, find_packages

setup(
    name="its_vehicle",
    version="0.0.0",
    author="Yann E. MORIN",
    author_email="yann(dot)morin(at)orange(dot)com",
    maintainer="Yann E. MORIN",
    maintainer_email="yann(dot)morin(at)orange(dot)com",
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
    include_package_data=True,
    package_data={
        "its_vehicle": ["its_vehicle.cfg"],
    },
    description="The Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) Python packages based on the "
    "[JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription.",
    long_description="The Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) Python packages based on "
    "the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification "
    "transcription. It permits to connect on ITS platform as a Passenger car (station type 5).",
    license="MIT",
    platforms="LINUX",
    install_requires=[
        "iot3",
        "its-quadkeys",
        "linuxfd==1.5",
    ],
    entry_points={"console_scripts": ["its-vehicle = its_vehicle.main:main"]},
)
