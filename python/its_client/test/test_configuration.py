import os
import unittest

from its_client import configuration


def _create_args(
    broker_host=None,
    broker_port=None,
    broker_tls_port=None,
    broker_username=None,
    broker_password=None,
    broker_client_id=None,
    position_static=None,
    log_default_level=None,
) -> [str]:
    args = ["--config-path", os.path.dirname(os.path.realpath(__file__))]
    if broker_host is not None:
        args.append("--mqtt-host")
        args.append(broker_host)
    if broker_port is not None:
        args.append("--mqtt-port")
        args.append(str(broker_port))
    if broker_tls_port is not None:
        args.append("--mqtt-tls-port")
        args.append(str(broker_tls_port))
    if broker_username is not None:
        args.append("--mqtt-username")
        args.append(broker_username)
    if broker_password is not None:
        args.append("--mqtt-password")
        args.append(broker_password)
    if broker_client_id is not None:
        args.append("--mqtt-client-id")
        args.append(broker_client_id)
    if position_static is not None and position_static:
        args.append("--static")
    if log_default_level is not None:
        args.append("--log-level")
        args.append(log_default_level)
    return args


class TestConfiguration(unittest.TestCase):
    def setUp(self):
        self.config = None

    def tearDown(self):
        pass

    def test_from_file(self):
        args = ["--config-path", os.path.dirname(os.path.realpath(__file__))]
        self.config = configuration.build(args)
        self._check_parameter_list()

    def test_from_parameter(self):
        broker_host = "parameter_host"
        broker_port = 19
        broker_tls_port = 89
        broker_username = "parameter_user"
        broker_password = "parameter_password"
        broker_client_id = "parameter_client_id"
        log_default_level = "CRITICAL"
        args = _create_args(
            broker_host=broker_host,
            broker_port=broker_port,
            broker_tls_port=broker_tls_port,
            broker_username=broker_username,
            broker_password=broker_password,
            broker_client_id=broker_client_id,
            log_default_level=log_default_level,
        )
        self.config = configuration.build(args)
        self._check_parameter_list(
            broker_host=broker_host,
            broker_port=broker_port,
            broker_tls_port=broker_tls_port,
            broker_username=broker_username,
            broker_password=broker_password,
            broker_client_id=broker_client_id,
            position_static=False,
            position_latitude=44.50779,
            position_longitude=2.209381,
            position_heading=130.7275,
            position_speed=0.103,
            log_default_level=log_default_level,
        )

    def test_broker_host_from_parameter(self):
        broker_host = "unique_host"
        args = _create_args(broker_host=broker_host)
        self.config = configuration.build(args)
        self._check_parameter_list(broker_host=broker_host)

    def test_broker_port_from_parameter(self):
        broker_port = 20
        args = _create_args(broker_port=broker_port)
        self.config = configuration.build(args)
        self._check_parameter_list(broker_port=broker_port)

    def test_broker_tls_port_from_parameter(self):
        broker_tls_port = 90
        args = _create_args(broker_tls_port=broker_tls_port)
        self.config = configuration.build(args)
        self._check_parameter_list(broker_tls_port=broker_tls_port)

    def test_broker_username_from_parameter(self):
        broker_username = "unique_name"
        args = _create_args(broker_username=broker_username)
        self.config = configuration.build(args)
        self._check_parameter_list(broker_username=broker_username)

    def test_broker_password_from_parameter(self):
        broker_password = "unique_password"
        args = _create_args(broker_password=broker_password)
        self.config = configuration.build(args)
        self._check_parameter_list(broker_password=broker_password)

    def test_broker_client_id_from_parameter(self):
        broker_client_id = "unique_client_id"
        args = _create_args(broker_client_id=broker_client_id)
        self.config = configuration.build(args)
        self._check_parameter_list(broker_client_id=broker_client_id)

    def test_log_default_level_from_parameter(self):
        log_default_level = "WARNING"
        args = _create_args(log_default_level=log_default_level)
        self.config = configuration.build(args)
        self._check_parameter_list(log_default_level=log_default_level)

    def _check_parameter_list(
        self,
        broker_host="test_host",
        broker_port=18,
        broker_tls_port=88,
        broker_username="test_user",
        broker_password="test_password",
        broker_client_id="test_client_id",
        position_static=False,
        position_latitude=44.50779,
        position_longitude=2.209381,
        position_heading=130.7275,
        position_speed=0.103,
        log_default_level="DEBUG",
    ):
        self.assertEqual(self.config.get("broker", "host"), broker_host)
        self.assertEqual(self.config.getint("broker", "port"), broker_port)
        self.assertEqual(self.config.getint("broker", "tls_port"), broker_tls_port)
        self.assertEqual(self.config.get("broker", "username"), broker_username)
        self.assertEqual(self.config.get("broker", "password"), broker_password)
        self.assertEqual(self.config.get("broker", "client_id"), broker_client_id)
        self.assertEqual(self.config.getboolean("position", "static"), position_static)
        self.assertEqual(
            self.config.getfloat("position", "latitude"), position_latitude
        )
        self.assertEqual(
            self.config.getfloat("position", "longitude"), position_longitude
        )
        self.assertEqual(self.config.getfloat("position", "heading"), position_heading)
        self.assertEqual(self.config.getfloat("position", "speed"), position_speed)
        self.assertEqual(self.config.get("log", "default_level"), log_default_level)
