# Software Name: its-client
# SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
# SPDX-License-Identifier: MIT License
#
# This software is distributed under the MIT license, see LICENSE.txt file for more details.
#
# Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
# Software description: This Intelligent Transportation Systems (ITS)
# [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org)
# [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project
# for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
import logging

from gpsd import connect, get_current, GpsResponse, NoFixError


class GeoPosition:
    def __init__(self):
        try:
            connect()
            self.connected = True
        except Exception as error:
            logging.error(f"a gps init error occurs:{error}")
            exit(3)

    def get_current_position(self, packet: GpsResponse = None):
        if self.connected:
            try:
                if packet is None:
                    packet = get_current()
                if packet is not None:
                    if packet.mode >= 2:
                        lat, lon = packet.position()
                        logging.debug(
                            f"location received: lon {str(lon)}, lat {str(lat)}"
                        )
                        return lon, lat
                    else:
                        logging.warning("no location available")
            except UserWarning as error:
                logging.warning(f"a gps user warning occurs:{error}")
            except NoFixError as error:
                logging.error(f"a no fix gps error occurs:{error}")
                exit(3)
            except Exception as error:
                logging.error(f"a gps error occurs:{error}")
                exit(3)
        return None, None

    def get_current_value(self):
        logging.debug(f"gps value calling")
        if self.connected:
            try:
                packet = get_current()
                lon, lat = self.get_current_position(packet)
                if packet.mode >= 3:
                    altitude = packet.altitude()
                    logging.debug("altitude received:" + str(altitude))
                    movement = packet.movement()
                    logging.debug("movement received:" + str(movement))
                    position_time = packet.get_time()
                    logging.debug("time received:" + str(position_time))
                    return (
                        lon,
                        lat,
                        movement.get("speed"),
                        altitude,
                        movement.get("track"),
                        position_time,
                    )
                else:
                    logging.info("no location available")
            except UserWarning as error:
                logging.warning(f"a gps user warning occurs:{error}")
            except NoFixError as error:
                logging.error(f"a no fix gps error occurs:{error}")
                exit(3)
            except Exception as error:
                logging.error(f"a gps error occurs:{error}")
                exit(3)
        return None, None, None, None, None, None
