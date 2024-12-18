its-vehicle
===========

The _its-vehicle_ application generates [ITS CAM messages
](/schema/cam/cam_schema_1-1-3.json), and listens for any kind of [ITS
message](/schema/) in its vicinity [\*]. It feeds traces of such
exchanges back to an OpenTelemetry collector, to compute round-trip
times of messages in the ITS infra.

[\*] received messages are not acted upon; they are only monitored to
provide OpenTelemetry traces.

Configuration
-------------

See the `its-vehicle.cfg` file for an example configuration file.
