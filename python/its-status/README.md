its-status
==========

The _its-status_ application generates [ITS status messages
](/schema/status/status_schema_1-2-0.json).

ITS status messages provide details on the status of the ITS instance
running _its-status_. Notably, it will report the hardware type, some of
the [os-release](https://www.freedesktop.org/software/systemd/man/os-release.html)
details, the CPU, RAM, and storage utilisation, as well as additional,
instance-specific status, like the time sources being used for time
synchronisation, the GNSS constellations in sight and the type of GNSS
fix, etc...

Configuration
-------------

See the `its-status.cfg` file for an example configuration file.
