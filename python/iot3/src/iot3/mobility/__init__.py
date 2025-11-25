# Software Name: IoT3 Mobility
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import copy
import its_quadkeys
import json
import time

from typing import Any, Callable, Optional, TypeAlias

from .. import core

from . import etsi
from .cam import CAM
from .cpm import CPM
from .denm import DENM
from .gnss import GNSSReport


"""
This module provides a high-level IoT3 Mobility client with a very simple API.

This simple API consists in a few functions and a type annotation:

* Functions:
  * start(): start the IoT3 Mobility client
  * send_position():
  * send_alert(): send an alert
  * stop(): stop the IoT3 Mobility client

* Type annotations:
  * AlertCallbackType: the type annotation of a function called when a
    message is received; parameters will be passed as keyword arguments;
    to be future proof, such a function should be declared as:
        def my_alert_callback(
            *_args,
            data: typing.Any,
            location: iot3.mobility.gnss.GNSSReport,
            cause: iot3.mobility.denm.DENM.Cause,
            **_kwargs,
        ) -> None:
            ...
"""


sample_bootstrap_config = dict(core.sample_bootstrap_config)
sample_config = dict(core.sample_config)
sample_config.update(
    {
        "uuid": "1234",
        "station_type": etsi.Message.StationType.unknown,
        "namespace": "default",
        "report_depth": 22,
        "roi_depth": 15,
    }
)

# Simple type annotation for now, as doing a proper type annotation for
# a callable with *args and **kwargs is non trivial, and even though
# python 3.13 will make that slightly easier. it's not there yet...
AlertCallbackType: TypeAlias = Callable[..., None]


def bootstrap(
    *,
    ue_id: str,
    role: str,
    service_name: str,
    bootstrap_config: dict,
    station_type: Optional[etsi.Message.StationType] = etsi.Message.StationType.unknown,
    # FIXME: Shouldn't namespace come from the bootstrap, in fact?
    namespace: Optional[str] = "default",
) -> dict:
    config = core.bootstrap(
        ue_id=ue_id,
        role=role,
        service_name=service_name,
        bootstrap_config=bootstrap_config,
    )

    config.update(
        {
            "uuid": config["mqtt"]["client_id"],
            "station_type": station_type,
            "namespace": namespace,
            # FIXME: Should those be configurable? Should they come from the
            # bootstrap, if so?
            "report_depth": 22,
            "roi_depth": 15,
        },
    )

    return config


def start(
    *,
    config: dict,
    alert_callback: Optional[AlertCallbackType] = None,
    cb_data: Any = None,
):
    """Start the IoT3 Mobility SDK

    :param config: The SDK configuration
    :param alert_callback: The function to call upon reception of a message;
        that function should only expect and accept keyword arguments (kwargs).
        To be future proof, such a function should be declared as:
            def my_alert_callback(
                *,
                data: typing.Any,
                location: iot3.mobility.gnss.GNSSReport,
                detection_time: float,
                cause: iot3.mobility.denm.DENM.Cause,
                **kwargs,
            ) -> None:
                ...
        Currently defined kwargs that are always present are:
          - data: the value passed as msg_cb_data (below)
          - location: contains the location of the alert
          - detection_time: the time of detection of the event
          - cause: the cause for the DENM
        Additional kwargs may be present if conditions are met:
          - subcause: a DENM.SubCause.Any, the sub-cause for the DENM, if
            available in the received message
    :param cb_data: The data to use when calling alert_callback(), above.
    """
    global _mobility

    if _mobility is not None:
        raise RuntimeError("IoT3 Mobility SDK already initialised.")

    _mobility = copy.deepcopy(config)
    _mobility["topic_template_send"] = (
        f"{config['namespace']}/inQueue/v2x/{{msg_type}}/{{source_uuid}}/{{quadkey}}"
    )
    _mobility["topic_template_recv"] = (
        f"{config['namespace']}/outQueue/v2x/{{msg_type}}/{{source_uuid}}/{{quadkey}}"
    )

    def _msg_cb(data, topic, payload):
        try:
            msg = message_from_json(msg_json=payload)
        except Exception as e:
            # Can't make it a known message, ignore
            return
        if msg.msg_type == "denm":
            kwargs = dict()
            if msg.subcause is not None:
                kwargs["subcause"] = msg.subcause
            alert_callback(
                data=data,
                location=GNSSReport(
                    latitude=msg.latitude,
                    longitude=msg.longitude,
                    altitude=msg.altitude,
                ),
                cause=msg.cause,
                detection_time=msg.detection_time,
                **kwargs,
            )

    core.start(
        config=config,
        message_callback=_msg_cb if alert_callback else None,
        callback_data=cb_data,
    )


def stop():
    """Stop the IoT3 Mobility SDK."""
    global _mobility

    if _mobility is None:
        raise RuntimeError("IoT3 mobility SDK not initialised.")

    core.stop()

    _mobility = None


def send_position(
    *,
    latitude: Optional[float] = None,
    longitude: Optional[float] = None,
    altitude: Optional[float] = None,
    heading: Optional[float] = None,
    speed: Optional[float] = None,
    acceleration: Optional[float] = None,
    measurement_time: Optional[float] = None,
    gnss_report: Optional[GNSSReport] = None,
):
    """Send the current position.

    :param latitude: latitude, in degrees; positive is North
    :param longitude: longitude, in degrees; positive is East
    :param altitude: altitude, in meters
    :param heading: heading from true North (geographic North), in
                    degrees; 0.0 is toward North, 90.0 is toward
                    East, 180.0 is toward South, 270.0 (or -90.0)
                    is toward West
    :param speed: speed over ground (horizontal speed), in m/s
    :param acceleration: longitudinal acceleration (horizontal), in
                         m/s²
    :param measurement_time: UNIX timestamp the GNSS leasurement were
                             made (see below).
    :param gnss_report: an iot3.mobility.gnss.GNSSReport object that
                        encapsulates the other parameters (see below).

    All parameters are optional, but latitude and longitude are
    required, either as discrete parameters, or encapsulated in
    gnss_report.

    Heading is toward the true North, aka the geographical North, not
    toward the magnetic North.

    Measurement time is the timestamp at which the GNSS measurement was
    done, not when calling this function; usually, measurement_time is
    provided by the GNSS device.

    Location data can be passed either as individual parameters, or as
    gnss_report, an aggregated object of type iot3.mobility.gnss.GNSSReport;
    if gnss_report is passed, all other location parameters are ignored.

    The following three examples are equivalent:

        send_position(latitude=43.635, longitude=-1.375)
        send_position(iot3.mobility.gnss.GNSSReport(
            latitude=43.635,
            longitude=-1.375,
        ))
        send_position(iot3.mobility.gnss.GNSSReport(
            latitude_r=0.7616,
            longitude_r=-0.024,
        ))
    """
    global _mobility

    if _mobility is None:
        raise RuntimeError("IoT3 mobility SDK not initialised.")

    now = time.time()

    if gnss_report is None:
        gnss_report = GNSSReport(
            time=measurement_time,
            latitude=latitude,
            longitude=longitude,
            altitude=altitude,
            true_heading=heading,
            speed=speed,
            acceleration=acceleration,
        )

    if gnss_report.latitude is None:
        raise RuntimeError("No valid latitude provided")
    if gnss_report.longitude is None:
        raise RuntimeError("No valid longitude provided")

    # Create the CAM early, to get better timestamping
    cam = CAM(
        uuid=_mobility["uuid"],
        station_type=_mobility["station_type"],
        gnss_report=gnss_report,
    )

    # Before reporting current location, update RoI, to not miss any message
    quadkey = its_quadkeys.QuadKey(
        (
            latitude,
            longitude,
            _mobility["roi_depth"],
        ),
    )
    roi = quadkey.neighbours(as_zone=True)
    roi.add(quadkey)
    core.subscribe(
        topics=list(
            map(
                lambda qk: (
                    _mobility["topic_template_recv"].format(
                        msg_type="denm",
                        source_uuid="+",
                        quadkey=qk.to_str(separator="/"),
                    )
                    + "/#"
                ),
                roi,
            )
        )
    )

    core.publish(
        topic=cam.topic(
            template=_mobility["topic_template_send"],
            depth=_mobility["report_depth"],
        ),
        payload=cam.to_json(),
    )


def send_alert(
    *,
    latitude: Optional[float] = None,
    longitude: Optional[float] = None,
    altitude: Optional[float] = None,
    gnss_report: Optional[GNSSReport] = None,
    cause: Optional[DENM.Cause] = DENM.Cause.dangerousSituation,
):
    """Send an alert.

    :param latitude: latitude, in degrees; positive is North
    :param longitude: longitude, in degrees; positive is East
    :param altitude: altitude, in meters

    All parameters are optional, but latitude and longitude are
    required, either as discrete parameters, or encapsulated in
    gnss_report.

    Location data can be passed either as individual parameters, or as
    gnss_report, an aggregated object of type iot3.mobility.gnss.GNSSReport;
    if gnss_report is passed, all other location parameters are ignored.
    """
    global _mobility

    if _mobility is None:
        raise RuntimeError("IoT3 mobility SDK not initialised.")

    if gnss_report is None:
        gnss_report = GNSSReport(
            latitude=latitude,
            longitude=longitude,
            altitude=altitude,
        )

    if gnss_report.latitude is None:
        raise RuntimeError("No valid latitude provided")
    if gnss_report.longitude is None:
        raise RuntimeError("No valid longitude provided")

    denm = DENM(
        uuid=_mobility["uuid"],
        gnss_report=gnss_report,
        cause=cause,
    )

    core.publish(
        topic=denm.topic(
            template=_mobility["topic_template_send"],
            depth=_mobility["report_depth"],
        ),
        payload=denm.to_json(),
    )


def message_from_json(
    *,
    msg_json: str,
) -> etsi.Message:
    """Create a new ITS message from a json sentence"""
    _MSG_TYPE_CLASS = {
        "cam": CAM,
        "cpm": CPM,
        "denm": DENM,
    }
    # We should validate msg_json here before loading it,
    # but we need to know the type of message first
    try:
        msg_py_obj = json.loads(msg_json)
    except json.decoder.JSONDecodeError:
        raise RuntimeError("Not a known ITS message")
    try:
        try:
            cls = _MSG_TYPE_CLASS[msg_py_obj["message_type"]]
        except KeyError:
            # Old, legacy, pre-2.x messages
            cls = _MSG_TYPE_CLASS[msg_py_obj["type"]]
        source_uuid = msg_py_obj["source_uuid"]
    except (KeyError, TypeError):
        raise RuntimeError("Not a known ITS message")
    # Create a raw ITS mesage
    msg = cls(uuid=source_uuid, gnss_report=GNSSReport())
    # And now just use the origianl message content
    msg._message = msg_py_obj

    return msg


_mobility = None
