import logging


def record(
    json: str,
):
    logging.getLogger("obu.its").info(f"{json}")


def create_cam(
    json_cam: str,
):
    logging.getLogger("obu.creation").info(f"{json_cam}")
