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
        "--mqtt-tls-port",
        "-T",
        type=int,
        help="TLS port of the MQTT broker",
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
    logger.log_setup(
        directory=config.get(section="log", option="directory"),
        log_level=config.get(section="log", option="default_level"),
    )
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
    if args.mqtt_tls_port is not None:
        config.set(section="broker", option="tls_port", value=str(args.mqtt_tls_port))
    if args.mqtt_username is not None:
        config.set(section="broker", option="username", value=args.mqtt_username)
    if args.mqtt_password is not None:
        config.set(section="broker", option="password", value=args.mqtt_password)

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
