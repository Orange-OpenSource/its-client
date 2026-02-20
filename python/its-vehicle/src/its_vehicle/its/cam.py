import datetime
import hashlib
import json

from iot3.mobility.gnss import compute_position_confidence_ellipse
from its_vehicle.gpsd import GNSSReport
from . import ETSI, SI2ETSI


class CooperativeAwarenessMessage:
    def __init__(
        self,
        *,
        uuid: str,
        gnss_report: GNSSReport,
    ):
        confidence_ellipse = compute_position_confidence_ellipse(gnss_report)

        self.cam = dict(
            {
                "message_type": "cam",
                "source_uuid": uuid,
                "timestamp": (
                    SI2ETSI.seconds(
                        datetime.datetime.now(datetime.timezone.utc).timestamp(),
                        SI2ETSI.MILLI_SECOND,
                        0,
                    )
                ),
                "version": "2.4.0",
                "message": {
                    "protocol_version": 1,
                    "station_id": self.station_id(uuid),
                    "generation_delta_time": (
                        ETSI.generation_delta_time(gnss_report.timestamp)
                    ),
                    "basic_container": {
                        "station_type": 5,
                        "reference_position": {
                            "latitude": SI2ETSI.degrees(
                                gnss_report.latitude,
                                SI2ETSI.DECI_MICRO_DEGREE,
                                900000001,
                            ),
                            "longitude": SI2ETSI.degrees(
                                gnss_report.longitude,
                                SI2ETSI.DECI_MICRO_DEGREE,
                                1800000001,
                            ),
                            # Position confidence ellipse with RTK precision
                            "position_confidence_ellipse": confidence_ellipse,
                            "altitude": {
                                "value": SI2ETSI.meters(
                                    gnss_report.altitude,
                                    SI2ETSI.CENTI_METER,
                                    800001,
                                ),
                                "confidence": 1,  # altitude confidence
                            },
                        },
                    },
                    "high_frequency_container": {
                        "basic_vehicle_container_high_frequency": {
                            "heading": {
                                "value": SI2ETSI.degrees(
                                    gnss_report.track,
                                    SI2ETSI.DECI_DEGREE,
                                    3601,
                                ),
                                "confidence": 2,
                            },
                            "speed": {
                                "value": SI2ETSI.meters_per_second(
                                    gnss_report.speed,
                                    SI2ETSI.CENTI_METER_PER_SECOND,
                                    16383,
                                ),
                                "confidence": 3,
                            },
                            "longitudinal_acceleration": {
                                "value": SI2ETSI.meters_per_second_second(
                                    gnss_report.acceleration,
                                    SI2ETSI.DECI_METER_PER_SECOND_SECOND,
                                    161,
                                ),
                                "confidence": 102,
                            },
                            "drive_direction": 0,
                            "vehicle_length": {
                                "value": 40,
                                "confidence": 0,
                            },
                            "vehicle_width": 20,
                            "curvature": {
                                "value": 1023,  # unavailable
                                "confidence": 7,  # unavailable
                            },
                            "curvature_calculation_mode": 2,  # unavailable
                            "yaw_rate": {
                                "value": 32767,  # unavailable
                                "confidence": 8,  # unavailable
                            },
                        },
                    },
                },
            },
        )

    @staticmethod
    def station_id(uuid: str) -> int:
        # Generate station ID from UUID hash (first 6 hex chars)
        return int(
            hashlib.sha256(uuid.encode()).hexdigest()[:6],
            16,
        )

    def to_json(self) -> str:
        # Return the densest possible JSON sentence
        return json.dumps(self.cam, separators=(",", ":"))
