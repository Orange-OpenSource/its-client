#!/usr/bin/env python

# From: https://gist.github.com/zed/92df922103ac9deb1a05/f1864577a8a05bcc78d19512135d51bede2db3b7
# SPDX-FileCopyrightText: This file is in the public domain.
# See URL above, and doc-string below, for the (non-)license...
# Modified by: Yann E. MORIN <yann.morin@orange.com>

"""Get TAI-UTC difference in seconds for a given time using tzdata.

i.e., find the number of seconds that must be added to UTC to compute
TAI for any timestamp at or after the given time[1].

  >>> from datetime import datetime
  >>> import leapseconds
  >>> leapseconds.dTAI_UTC_from_utc(datetime(2005, 1, 1))
  datetime.timedelta(seconds=32)
  >>> leapseconds.utc_to_tai(datetime(2015, 7, 1))
  datetime.datetime(2015, 7, 1, 0, 0, 36)
  >>> leapseconds.tai_to_utc(datetime(2015, 7, 1, 0, 0, 36))
  datetime.datetime(2015, 7, 1, 0, 0)
  >>> leapseconds.tai_to_utc(datetime(2015, 7, 1, 0, 0, 35)) # leap second
  datetime.datetime(2015, 7, 1, 0, 0)
  >>> leapseconds.tai_to_utc(datetime(2015, 7, 1, 0, 0, 34))
  datetime.datetime(2015, 6, 30, 23, 59, 59)

Python 2.6+, Python 3, Jython, Pypy support.

#       COPYRIGHT STATUS OF THIS FILE
#       This file is in the public domain.

[1]: https://github.com/eggert/tz/blob/master/leap-seconds.list
[2]: https://github.com/eggert/tz/blob/master/tzfile.h
[3]: https://github.com/eggert/tz/blob/master/zic.c
[4]: https://datacenter.iers.org/data/16/bulletinc-070.txt
"""
from __future__ import with_statement

import time
from collections import namedtuple
from datetime import datetime, timedelta
from struct import Struct
from warnings import warn

__all__ = [
    "leapseconds",
    "LeapSecond",
    "dTAI_UTC_from_utc",
    "dTAI_UTC_from_tai",
    "tai_to_utc",
    "utc_to_tai",
    "gps_to_utc",
    "utc_to_gps",
    "tai_to_gps",
    "gps_to_tai",
]

__version__ = "0.4.0"

# from timezone/tzfile.h [2] (the file is in public domain)
"""
struct tzhead {
 char tzh_magic[4];  /* TZ_MAGIC */
 char tzh_version[1];  /* '\0' or '2' or '3' as of 2013 */
 char tzh_reserved[15]; /* reserved--must be zero */
 char tzh_ttisgmtcnt[4]; /* coded number of trans. time flags */
 char tzh_ttisstdcnt[4]; /* coded number of trans. time flags */
 char tzh_leapcnt[4];  /* coded number of leap seconds */
 char tzh_timecnt[4];  /* coded number of transition times */
 char tzh_typecnt[4];  /* coded number of local time types */
 char tzh_charcnt[4];  /* coded number of abbr. chars */
};

# from zic.c[3] (the file is in public domain)
convert(const int_fast32_t val, char *const buf)
{
        register int i;
        register int shift;
        unsigned char *const b = (unsigned char *) buf;

        for (i = 0, shift = 24; i < 4; ++i, shift -= 8)
                b[i] = val >> shift;
}
# val = 0x12345678
# (val >> 24) & 0xff, (val >> 16) & 0xff, (val >> 8) & 0xff, val & 0xff
# 0x12 0x34 0x56 0x78
# therefore "coded number" means big-endian 32-bit integer
"""

dTAI_GPS = timedelta(seconds=19)  # constant offset
NTP_EPOCH = datetime(1900, 1, 1)

LeapSecond = namedtuple("LeapSecond", "utc dTAI_UTC")  # tai = utc + dTAI_UTC
sentinel = LeapSecond(utc=datetime.max, dTAI_UTC=timedelta(0))

LeapSeconds = None
LastLoad = None
# Only reload once a day. In practice, even once a day is waaaayyyy too much...
ReloadDelay = 24 * 60 * 60


def _leapseconds_reset():
    global LeapSeconds
    global LastLoad

    LeapSeconds = None
    LastLoad = None


def leapseconds(
    tzfiles=[
        "/run/zoneinfo/leap-seconds.list",
        "/usr/share/zoneinfo/leap-seconds.list",
        "/usr/share/zoneinfo/right/UTC",
        "/usr/lib/zoneinfo/right/UTC",
    ],
    use_fallback=False,
):
    """Extract leap seconds from *tzfiles*.

    >>> _leapseconds_reset()
    >>> leapseconds()[0]
    LeapSecond(utc=datetime.datetime(1972, 1, 1, 0, 0), dTAI_UTC=datetime.timedelta(seconds=10))
    >>> _leapseconds_reset()
    >>> leapseconds(tzfiles=["non-existent"])[27]
    Traceback (most recent call last):
    ...
    ValueError: Unable to open any tzfile: ['non-existent']
    >>> _leapseconds_reset()
    >>> leapseconds(tzfiles=["non-existent"], use_fallback=True)[27]
    LeapSecond(utc=datetime.datetime(2017, 1, 1, 0, 0), dTAI_UTC=datetime.timedelta(seconds=37))
    """
    global LeapSeconds
    global LastLoad

    now = time.time()
    if (
        LeapSeconds  # neither None nor an empty list()
        and LastLoad is not None
        and now - LastLoad < ReloadDelay
    ):
        return LeapSeconds

    for filename in tzfiles:
        try:
            file = open(filename, "rb")
        except IOError:
            continue
        else:
            break
    else:  # no break
        if not use_fallback:
            raise ValueError("Unable to open any tzfile: %s" % (tzfiles,))
        else:
            # Don't store fallback as if it were actual data
            return _fallback()

    with file:
        header = Struct(">4s c 15x 6i")  # see struct tzhead above
        (magic, version, _, _, leapcnt, timecnt, typecnt, charcnt) = header.unpack_from(
            file.read(header.size)
        )
        if magic != "TZif".encode():
            # assume /usr/share/zoneinfo/leap-seconds.list like file
            file.seek(0)  # rewind
            LeapSeconds = leapseconds_from_listfile(file, now)
        else:
            if version not in "\x0023".encode():
                warn(
                    "Unsupported version %r in tzfile: %s" % (version, file.name),
                    RuntimeWarning,
                )
            if leapcnt == 0:
                raise ValueError("No leap seconds in tzfile: %s" % (file.name))

            """# from tzfile.h[2] (the file is in public domain)

             . . .header followed by. . .

             tzh_timecnt (char [4])s  coded transition times a la time(2)
             tzh_timecnt (unsigned char)s types of local time starting at above
             tzh_typecnt repetitions of
               one (char [4])  coded UT offset in seconds
               one (unsigned char) used to set tm_isdst
               one (unsigned char) that's an abbreviation list index
             tzh_charcnt (char)s  '\0'-terminated zone abbreviations
             tzh_leapcnt repetitions of
               one (char [4])  coded leap second transition times
               one (char [4])  total correction after above
            """
            file.read(timecnt * 5 + typecnt * 6 + charcnt)  # skip

            LeapSeconds = [LeapSecond(datetime(1972, 1, 1), timedelta(seconds=10))]
            nleap_seconds = 10
            tai_epoch_as_tai = datetime(1970, 1, 1, 0, 0, 10)
            buf = Struct(">2i")
            for _ in range(leapcnt):  # read leap seconds
                t, cnt = buf.unpack_from(file.read(buf.size))
                dTAI_UTC = nleap_seconds + cnt
                utc = tai_epoch_as_tai + timedelta(seconds=t - dTAI_UTC + 1)
                assert utc - datetime(utc.year, utc.month, utc.day) == timedelta(0)
                LeapSeconds.append(LeapSecond(utc, timedelta(seconds=dTAI_UTC)))

        LeapSeconds.append(sentinel)
        LastLoad = now
        return LeapSeconds


def leapseconds_from_listfile(file, now=None, comment="#".encode()):
    """Extract leap seconds from *file*

    See /usr/share/zoneinfo/leap-seconds.list
    """
    if now is None:
        now = time.time()
    result = []
    for line in file:
        if not line.startswith(comment):  # skip comments
            # ntp time, dtai, # day month year
            ntp_time, dtai = line.partition(comment)[0].split()
            utc = NTP_EPOCH + timedelta(seconds=int(ntp_time))
            result.append(LeapSecond(utc, timedelta(seconds=int(dtai))))
        elif line.startswith("#@".encode()):
            ntp_time = line[2:].strip()
            utc = NTP_EPOCH + timedelta(seconds=int(ntp_time))
            if utc.timestamp() < now:
                raise ValueError(
                    f"leap-seconds.list is out of date, expired on {utc.isoformat()}"
                )
    return result


def _fallback():
    """Leap seconds list if no tzfiles are available."""
    return [
        LeapSecond(utc=datetime(1972, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 10)),
        LeapSecond(utc=datetime(1972, 7, 1, 0, 0), dTAI_UTC=timedelta(0, 11)),
        LeapSecond(utc=datetime(1973, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 12)),
        LeapSecond(utc=datetime(1974, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 13)),
        LeapSecond(utc=datetime(1975, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 14)),
        LeapSecond(utc=datetime(1976, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 15)),
        LeapSecond(utc=datetime(1977, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 16)),
        LeapSecond(utc=datetime(1978, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 17)),
        LeapSecond(utc=datetime(1979, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 18)),
        LeapSecond(utc=datetime(1980, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 19)),
        LeapSecond(utc=datetime(1981, 7, 1, 0, 0), dTAI_UTC=timedelta(0, 20)),
        LeapSecond(utc=datetime(1982, 7, 1, 0, 0), dTAI_UTC=timedelta(0, 21)),
        LeapSecond(utc=datetime(1983, 7, 1, 0, 0), dTAI_UTC=timedelta(0, 22)),
        LeapSecond(utc=datetime(1985, 7, 1, 0, 0), dTAI_UTC=timedelta(0, 23)),
        LeapSecond(utc=datetime(1988, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 24)),
        LeapSecond(utc=datetime(1990, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 25)),
        LeapSecond(utc=datetime(1991, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 26)),
        LeapSecond(utc=datetime(1992, 7, 1, 0, 0), dTAI_UTC=timedelta(0, 27)),
        LeapSecond(utc=datetime(1993, 7, 1, 0, 0), dTAI_UTC=timedelta(0, 28)),
        LeapSecond(utc=datetime(1994, 7, 1, 0, 0), dTAI_UTC=timedelta(0, 29)),
        LeapSecond(utc=datetime(1996, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 30)),
        LeapSecond(utc=datetime(1997, 7, 1, 0, 0), dTAI_UTC=timedelta(0, 31)),
        LeapSecond(utc=datetime(1999, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 32)),
        LeapSecond(utc=datetime(2006, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 33)),
        LeapSecond(utc=datetime(2009, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 34)),
        LeapSecond(utc=datetime(2012, 7, 1, 0, 0), dTAI_UTC=timedelta(0, 35)),
        LeapSecond(utc=datetime(2015, 7, 1, 0, 0), dTAI_UTC=timedelta(0, 36)),
        LeapSecond(utc=datetime(2017, 1, 1, 0, 0), dTAI_UTC=timedelta(0, 37)),
        sentinel,
    ]


def dTAI_UTC_from_utc(utc_time):
    """TAI time = utc_time + dTAI_UTC_from_utc(utc_time)."""
    return _dTAI_UTC(utc_time, lambda ls: ls.utc)


def dTAI_UTC_from_tai(tai_time):
    """UTC time = tai_time - dTAI_UTC_from_tai(tai_time)."""
    return _dTAI_UTC(tai_time, lambda ls: ls.utc + ls.dTAI_UTC)


def _dTAI_UTC(time, leapsecond_to_time, leapseconds=leapseconds):
    """Get TAI-UTC difference in seconds for a given time.

    >>> from datetime import datetime, timedelta
    >>> _dTAI_UTC(datetime(1972, 1, 1), lambda ls: ls.utc)
    datetime.timedelta(seconds=10)
    >>> tai = lambda ls: ls.utc + ls.dTAI_UTC
    >>> _dTAI_UTC(datetime(2015, 7, 1, 0, 0, 34), tai)
    datetime.timedelta(seconds=35)
    >>> _dTAI_UTC(datetime(2015, 7, 1, 0, 0, 35), tai) # leap second
    datetime.timedelta(seconds=35)
    >>> _dTAI_UTC(datetime(2015, 7, 1, 0, 0, 36), tai)
    datetime.timedelta(seconds=36)

    Bulletin C 70 says "NO leap second will be introduced at the end
    of December 2025."[4] and therefore TAI-UTC is still 37s as of
    October 9th, 2025:

    >>> _dTAI_UTC(datetime(2025, 10, 9), lambda ls: ls.utc)
    datetime.timedelta(seconds=37)

    """
    leapseconds_list = leapseconds()
    transition_times = list(map(leapsecond_to_time, leapseconds_list))
    if time < transition_times[0]:
        raise ValueError(
            "Dates before %s are not supported, got %r" % (transition_times[0], time)
        )
    for i, (start, end) in enumerate(zip(transition_times, transition_times[1:])):
        if start <= time < end:
            return leapseconds_list[i].dTAI_UTC
    assert 0


def tai_to_utc(tai_time):
    """Convert TAI time given as datetime object to UTC time."""
    return tai_time - dTAI_UTC_from_tai(tai_time)


def utc_to_tai(utc_time):
    """Convert UTC time given as datetime object to TAI time."""
    return utc_time + dTAI_UTC_from_utc(utc_time)


def gps_to_utc(gps_time):
    """Convert GPS time given as datetime object to UTC time."""
    return tai_to_utc(gps_to_tai(gps_time))


def utc_to_gps(utc_time):
    """Convert UTC time given as datetime object to GPS time."""
    return tai_to_gps(utc_to_tai(utc_time))


def tai_to_gps(tai_time):
    """Convert TAI time given as datetime object to GPS time."""
    return tai_time - dTAI_GPS


def gps_to_tai(gps_time):
    """Convert GPS time given as datetime object to TAI time."""
    return gps_time + dTAI_GPS


if __name__ == "__main__":
    import sys

    if "--test" in sys.argv:
        import doctest

        doctest.testmod()

    else:
        import json

        assert all(
            ls.dTAI_UTC == timedelta(seconds=ls.dTAI_UTC.seconds)
            for ls in leapseconds()
        )  # ~+200 leap second until 2100
        print(
            json.dumps(
                [
                    dict(utc=t.utc, tai=t.utc + t.dTAI_UTC, dTAI_UTC=t.dTAI_UTC.seconds)
                    for t in leapseconds()
                ],
                default=str,
                indent=4,
                sort_keys=True,
            )
        )
