import logging

from gpsd import connect, get_current, GpsResponse, NoFixError


class GeoPosition:
    def __init__(self):
        try:
            connect()
            self.connected = True
        except Exception as error:
            logging.warning(f"a gps init error occurs:{error}")

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
            except NoFixError as error:
                logging.warning(error)
            except UserWarning as error:
                logging.warning(error)
            except Exception as error:
                logging.warning(f"a gps position error occurs:{error}")
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
                    return (
                        lon,
                        lat,
                        movement.get("speed"),
                        altitude,
                        movement.get("track"),
                    )
                else:
                    logging.info("no location available")
            except NoFixError as error:
                logging.warning(error)
            except UserWarning as error:
                logging.warning(error)
            except Exception as error:
                logging.warning(f"a gps position error occurs:{error}")
        return None, None, None, None, None
