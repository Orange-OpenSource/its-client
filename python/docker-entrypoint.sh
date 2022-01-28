#!/bin/bash

# Software Name: its-client
# SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
# SPDX-License-Identifier: MIT License
#
# This software is distributed under the MIT license, see LICENSE.txt.txt file for more details.
#
# Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
# Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

export PYTHONPATH="${PYTHONPATH}:/usr/local/lib/python3.8/site-packages/its_client"
python /usr/local/lib/python3.8/site-packages/its_client/main.py $@
code=0
echo "::set-output name=return_code::${code}"
exit ${code}