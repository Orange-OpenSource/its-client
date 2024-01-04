import argparse
import logging
import os
from configparser import ConfigParser

from its_client.logger import logger


def build(args=None) -> ConfigParser:
    # argument parser
    parser = argparse.ArgumentParser(formatter_class=argparse.RawTextHelpFormatter)
    parser.add_argument(
        "--mqtt-host",
        "-H",
        help="hostname of the MQTT broker",
    )
    parser.add_argument(
        "--mqtt-port",
        "-P",
        type=int,
        help="port of the MQTT broker",
    )
    parser.add_argument(
        "--mqtt-tls",
        action="store_true",
        default=None,  # store_true defaults to False, but we don't want a default
        help="use TLS when connecting to the MQTT broker",
    )
    parser.add_argument(
        "--mqtt-no-tls",
        action="store_false",
        dest="mqtt_tls",
        help="use TLS when connecting to the MQTT broker",
    )
    parser.add_argument(
        "--mqtt-username",
        "-u",
        help="username to use when connecting to the MQTT broker",
    )
    parser.add_argument(
        "--mqtt-password",
        "-p",
        help="password to use when connecting to the MQTT broker",
    )
    parser.add_argument(
        "--mqtt-client-id",
        help="identifier of MQTT client. This must be unique in the broker",
    )
    parser.add_argument(
        "--mqtt-root-pub",
        help="root of all MQTT published topics",
    )
    parser.add_argument(
        "--mqtt-root-sub",
        help="root of all MQTT subscribed topics",
    )
    parser.add_argument(
        "--mqtt-prefix",
        help="prefix to all MQTT topics, subscribed or published, after --mqtt-root",
    )
    parser.add_argument(
        "--mqtt-mirror-host",
        help="mirror all messages sent to and received from --mqtt-host to this broker",
    )
    parser.add_argument(
        "--mqtt-mirror-port",
        type=int,
        help="port of the mirror MQTT broker",
    )
    parser.add_argument(
        "--mqtt-mirror-tls",
        action="store_true",
        default=None,  # store_true defaults to False, but we don't want a default
        help="use TLS when connecting to the mirror MQTT broker",
    )
    parser.add_argument(
        "--mqtt-mirror-no-tls",
        action="store_false",
        dest="mqtt_mirror_tls",
        help="use TLS when connecting to the mirror MQTT broker",
    )
    parser.add_argument(
        "--mqtt-mirror-username",
        help="username to use when connecting to the mirror MQTT broker",
    )
    parser.add_argument(
        "--mqtt-mirror-password",
        help="password to use when connecting to the mirror MQTT broker",
    )
    parser.add_argument(
        "--mqtt-mirror-client-id",
        help=(
            "unique MQTT client ID on the mirror MQTT broker,"
            " if different from --mqtt-client-id."
        ),
    )
    parser.add_argument(
        "--mqtt-mirror-self",
        action="store_true",
        default=None,  # store_true defaults to False, but we don't want a default
        help="do forward self messages received back from main broker",
    )
    parser.add_argument(
        "--mqtt-mirror-no-self",
        action="store_false",
        dest="mqtt_mirror_self",
        help="don't forward self messages received back from main broker",
    )
    parser.add_argument(
        "--static",
        "-s",
        action="store_true",
        default=None,
        help="use a static position store in configuration instead of the gps daemon",
    )
    parser.add_argument(
        "--log-level",
        "-l",
        help="logging level: CRITICAL, ERROR, WARNING, INFO or DEBUG",
    )
    parser.add_argument(
        "--log-sending-max-bytes",
        type=int,
        help="rotate sending logfiles after max-bytes",
    )
    parser.add_argument(
        "--log-sending-max-files",
        type=int,
        help="keep the last max-files sending logfiles",
    )
    parser.add_argument(
        "--log-reception-max-bytes",
        type=int,
        help="rotate reception logfiles after max-bytes",
    )
    parser.add_argument(
        "--log-reception-max-files",
        type=int,
        help="keep the last max-files reception logfiles",
    )
    parser.add_argument(
        "--log-monitoring-max-bytes",
        type=int,
        help="rotate monitoring logfiles after max-bytes",
    )
    parser.add_argument(
        "--log-monitoring-max-files",
        type=int,
        help="keep the last max-files monitoring logfiles",
    )
    parser.add_argument(
        "--config-path",
        "-c",
        default=os.path.dirname(os.path.realpath(__file__)),
        help="path to the its_config.cfg file",
    )
    # parse the arguments mainly for the config path option
    args, unknown_arguments = parser.parse_known_args(args=args)

    # config parser
    config = ConfigParser(allow_no_value=True)
    config_file = f"{args.config_path}/its_client.cfg"
    try:
        # Load the configuration file
        with open(file=config_file) as file:
            config.read_file(file)
    except IOError:
        # if the user did not specify a config path and there is not a file
        # at the default path, we stop
        print(f"no {config_file} config file found")
        exit(1)

    # overwrite the configuration default values with the provided parameters
    if args.log_level is not None:
        config.set(section="log", option="default_level", value=args.log_level)
    # set up the logger with the configuration to be able to well log as soon as possible
    # this only initiliases the stdout logger for now
    logger.log_setup(log_level=config.get(section="log", option="default_level"))
    logging.info(f"config loaded from {config_file}")

    logging.info("argument configuration:")
    for key, value in vars(args).items():
        if key == "mqtt_password":
            value = "****"
        logging.info(f"{key}: {value}")
    logging.info("unknown arguments:")
    logging.info(unknown_arguments)

    if args.static is not None:
        config.set(section="position", option="static", value=str(args.static))
    if args.mqtt_client_id is not None:
        config.set(section="broker", option="client_id", value=args.mqtt_client_id)
    if args.mqtt_host is not None:
        config.set(section="broker", option="host", value=args.mqtt_host)
    if args.mqtt_port is not None:
        config.set(section="broker", option="port", value=str(args.mqtt_port))
    if args.mqtt_tls is not None:
        config.set(section="broker", option="tls", value=str(args.mqtt_tls))
    if args.mqtt_username is not None:
        config.set(section="broker", option="username", value=args.mqtt_username)
    if args.mqtt_password is not None:
        config.set(section="broker", option="password", value=args.mqtt_password)
    if args.mqtt_root_pub is not None:
        config.set(section="broker", option="root_pub", value=args.mqtt_root_pub)
    if args.mqtt_root_sub is not None:
        config.set(section="broker", option="root_sub", value=args.mqtt_root_sub)
    if args.mqtt_prefix is not None:
        config.set(section="broker", option="prefix", value=args.mqtt_prefix)

    if not config.has_section("mirror-broker"):
        config.add_section("mirror-broker")
    if args.mqtt_mirror_host is not None:
        config.set(section="mirror-broker", option="host", value=args.mqtt_mirror_host)
    if args.mqtt_mirror_port is not None:
        config.set(
            section="mirror-broker",
            option="port",
            value=str(args.mqtt_mirror_port),
        )
    if args.mqtt_mirror_tls is not None:
        config.set(
            section="mirror-broker",
            option="tls",
            value=str(args.mqtt_mirror_tls),
        )
    if args.mqtt_mirror_username is not None:
        config.set(
            section="mirror-broker",
            option="username",
            value=args.mqtt_mirror_username or "",
        )
    if args.mqtt_mirror_password is not None:
        config.set(
            section="mirror-broker",
            option="password",
            value=args.mqtt_mirror_password or "",
        )
    if args.mqtt_mirror_client_id is not None:
        config.set(
            section="mirror-broker",
            option="client_id",
            value=args.mqtt_mirror_client_id or "",
        )
    if args.mqtt_mirror_self is not None:
        config.set(
            section="mirror-broker",
            option="mirror-self",
            value=str(args.mqtt_mirror_self),
        )
    if config.get("mirror-broker", "host", fallback=None) is None:
        config.remove_section("mirror-broker")

    if not config.has_section("log.sending"):
        config.add_section("log.sending")
    if args.log_sending_max_bytes is not None:
        config.set(
            section="log.sending", option="max_bytes", value=args.log_sending_max_bytes
        )
    if args.log_sending_max_files is not None:
        config.set(
            section="log.sending", option="max_files", value=args.log_sending_max_files
        )

    if not config.has_section("log.reception"):
        config.add_section("log.reception")
    if args.log_reception_max_bytes is not None:
        config.set(
            section="log.reception",
            option="max_bytes",
            value=args.log_reception_max_bytes,
        )
    if args.log_reception_max_files is not None:
        config.set(
            section="log.reception",
            option="max_files",
            value=args.log_reception_max_files,
        )

    if not config.has_section("log.monitoring"):
        config.add_section("log.monitoring")
    if args.log_monitoring_max_bytes is not None:
        config.set(
            section="log.monitoring",
            option="max_bytes",
            value=args.log_monitoring_max_bytes,
        )
    if args.log_monitoring_max_files is not None:
        config.set(
            section="log.monitoring",
            option="max_files",
            value=args.log_monitoring_max_files,
        )

    # Configure the rest of the loggers
    logger.log_setup2(
        directory=config.get(section="log", option="directory"),
        log_level=config.get(section="log", option="default_level"),
        sending={
            "max_bytes": config.getint(section="log.sending", option="max_bytes"),
            "max_files": config.getint(section="log.sending", option="max_files"),
        },
        reception={
            "max_bytes": config.getint(section="log.reception", option="max_bytes"),
            "max_files": config.getint(section="log.reception", option="max_files"),
        },
        monitoring={
            "max_bytes": config.getint(section="log.monitoring", option="max_bytes"),
            "max_files": config.getint(section="log.monitoring", option="max_files"),
        },
    )

    # list all used contents
    logging.info("used configuration:")
    for section in config.sections():
        logging.info(f"section: {section}")
        for option in config.options(section):
            if option == "password":
                option_value = "****"
            else:
                option_value = config.get(section, option)
            logging.info("x %s:::%s:::%s" % (option, option_value, str(type(option))))
    return config
