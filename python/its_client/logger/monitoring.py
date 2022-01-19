import logging


def monitore_cam(
    vehicle_id: str,
    direction: str,
    station_id: int,
    generation_delta_time: int,
    latitude: float,
    longitude: float,
    timestamp: int,
    partner: str,
    root_queue: str,
):
    logging.getLogger("obu.monitoring").info(
        f"{vehicle_id} cam {direction} {partner}/{root_queue}"
        f" {station_id}/{generation_delta_time}/lat:{latitude}/lng:{longitude} at {timestamp} "
    )


def monitore_denm(
    vehicle_id: str,
    station_id: int,
    originating_station_id: int,
    sequence_number: int,
    reference_time: int,
    detection_time: int,
    latitude: float,
    longitude: float,
    timestamp: int,
    partner: str,
    root_queue: str,
    sender: str,
):
    logging.getLogger("obu.monitoring").info(
        f"{vehicle_id} denm received_on {partner}/{root_queue}/{sender}"
        f" {station_id}/{originating_station_id}/{sequence_number}/{reference_time}/{detection_time}"
        f"/lat:{latitude}/lng:{longitude} at {timestamp}"
    )
