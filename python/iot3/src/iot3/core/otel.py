# Software Name: iot3
# SPDX-FileCopyrightText: Copyright (c) 2024 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

"""This module implements a lightweight, very limited subset of the
OpenTelemetry protocol. Only Spans (for traces) and the JSON-over-HTTP
exporter are supported, both with limitations.
"""

import base64
import contextlib
import enum
import json
import queue
import random
import requests
import threading
import time
import zlib
from typing import Optional, Self


class SpanKind(enum.IntEnum):
    # We need those values to *exactly* match those defined in the spec,
    # so we must not use enum.auto(). This is an enum.IntEnum (rather
    # than just an enum.Enum) because we want them to be comparable to
    # an actual int() (in case a caller needs to test it against a span
    # kind from another implementation).
    INTERNAL = 1
    SERVER = 2
    CLIENT = 3
    PRODUCER = 4
    CONSUMER = 5


class SpanStatus(enum.IntEnum):
    # We need those values to *exactly* match those defined in the spec,
    # so we must not use enum.auto(). This is an enum.IntEnum (rather
    # than just an enum.Enum) because we want them to be comparable to
    # an actual int() (in case a caller needs to test it against a span
    # status from another implementation).
    UNSET = 0
    OK = 1
    ERROR = 2


class Compression(enum.StrEnum):
    NONE = enum.auto()
    GZIP = enum.auto()


class Auth(enum.StrEnum):
    NONE = enum.auto()
    BASIC = enum.auto()
    DIGEST = enum.auto()


class Otel(threading.Thread):
    """Lightweight implementation of an OpenTelemetry exporter.

    This is only able to export spans as application/json, to an HTTP
    OTLP collector.
    """

    def __init__(
        self,
        *,
        service_name: str,
        endpoint: str,
        auth: Auth = Auth.NONE,
        username: Optional[str] = None,
        password: Optional[str] = None,
        batch_period: Optional[float] = None,
        max_backlog: int = 1023,
        compression: Compression = Compression.NONE,
    ):
        """
        Simple span exporter

        Exports spans to an OpenTelemetry collector, using JSON over HTTP.

        Spans are not sent right away after being finalised (closed); instead,
        when max_backlog spans have been accumulated, they are sent to the OTLP
        collector as a batch; this is also the maximum number of spans that
        are kept if they can't be exported to the collector, for a later export
        tentative.

        If batch_period is specified, spans that have been accumulated so far
        are sent to the OTLP collector, even if there is not max_backlog spans
        accumulated yet.

        Note: failure to call stop() before terminating, risk losing any
        pending spans not yet exported.

        :param service_name: The name of the service.
        :param endpoint: The URL to the OTLP collector (without the trailing
                         /v1/traces).
        :param auth: Type of HTTP authentication to employ.
        :param username: Username to use for authentication to the collector.
        :param password: Password to use for authentication to the collector.
        :param batch_period: Send spans every so often, in seconds. There is no
                             default, which means no periodic send (only backlog
                             based).
        :param max_backlog: Send spans as soon as that many have accumulated.
                            The default is 1023.
        :param compression: Type of compression, if any, to use to compress
                            the payload; the default is no compression.
        """

        self.service_name = service_name
        self.endpoint = endpoint
        if self.endpoint.endswith("/"):
            self.endpoint = self.endpoint[:-1]

        if auth == Auth.NONE:
            self.auth = None
        else:
            if not username and not password:
                raise ValueError(f"{auth.name} needs both username and password")
            if auth == Auth.BASIC:
                self.auth = (username, password)
            else:
                self.auth = requests.auth.HTTPDigestAuth(username, password)

        self.batch_period = batch_period
        self.max_backlog = max_backlog
        self.compression = compression

        self.spans = list()
        self.shutdown = False
        self.tls = threading.local()
        # Create a queue twice the required size, so it can still be filled a
        # bit while we are trying to push the messages to the OTLP collector.
        self.queue = queue.Queue(maxsize=2 * self.max_backlog)

        super().__init__(
            name="otel-client",
            target=self._run,
            daemon=True,
        )

    def export_span(self, *, span: "Span"):
        """Enqeue the given span for exporting.

        The span will be exported, subject to batch_period and max_backlog
        as passed to __init__().

        :param span: The span to export.

        Note: when the queue is full, spans are dropped, so this function
        is not blocking.
        """
        # Note: if queued after stop(), span will ultimately be ignored
        try:
            self.queue.put(span, block=False)
        except queue.Full:
            # When we can send events, we can trace that situation...
            pass

    @contextlib.contextmanager
    def span(self, *args, **kwargs) -> "Span":
        """Context manager that wraps a Span().

        span() has the same signature as Span().

        span() automatically makes the new span a child of the current
        span, if any, makes the new span the current span, finalises and
        exports the new span, and then restore the previous current
        span.

        If the parent_span argument is not specified, span() automatically
        handles the parent-child relationship of the new span and any
        current span, if any. If parent_span is specified and is a Span(),
        this is used as the span to use as a parent.
        """

        try:
            current_span = self.tls.spans[-1]
        except AttributeError:
            self.tls.spans = list()
        except IndexError:
            pass
        else:
            if "parent_span" not in kwargs:
                kwargs["parent_span"] = current_span

        s = Span(*args, **kwargs)
        try:
            self.tls.spans.append(s)
            yield s
        finally:
            s.finalise()
            self.export_span(span=s)
            self.tls.spans.pop()

    @staticmethod
    @contextlib.contextmanager
    def noexport_span(*args, **kwargs) -> "Span":
        """Context manager that provides a span that is not exported

        This simplifies coding when no telemetry is needed, by providing
        a way to write the code only once.

        See otel.span() and Span() for signature.
        """
        try:
            yield NoExportSpan(*args, **kwargs)
        finally:
            pass

    def stop(self):
        """Stop the exporter thread; export pending spans, if any."""
        self.shutdown = True
        self.queue.put(Otel._Quit())
        self.join()
        # Last chance to send any pending span
        self._send()

    class _Quit:
        pass

    def _run(self):
        while True:
            if self.batch_period:
                # This does not provide a perfect next-expiration delay for
                # a precise period, but over the long run, that will make
                # for slightly jittered expiration delays, all more or less
                # close to the ideal expiration delay. Which is good enough.
                timeout = self.batch_period - (time.time() % self.batch_period)
            else:
                timeout = None

            try:
                span = self.queue.get(timeout=timeout)
            except queue.Empty:
                # queue.Empty is only raised when we timed out waiting on the
                # queue, which means we have a batch_preiod, and so it's time
                # to push the spans.
                self._send()
                continue

            if type(span) is Otel._Quit:
                return
            self.spans.append(span)
            # Opportunistically try to drain the queue, but do not stay stuck
            # if it fills faster than we can empty it...
            try:
                for i in range(2 * self.max_backlog):
                    span = self.queue.get(block=False)
                    if type(span) is Otel._Quit:
                        return
                    self.spans.append(span)
            except queue.Empty:
                pass

            if len(self.spans) >= self.max_backlog:
                self._send()

    def _send(self):
        if not self.spans:
            return

        report = {
            "resourceSpans": [
                {
                    "resource": {
                        "attributes": [
                            {
                                "key": "service.name",
                                "value": {
                                    "stringValue": self.service_name,
                                },
                            },
                        ],
                    },
                    "scopeSpans": [
                        {
                            "scope": {
                                "name": "report",
                            },
                            "spans": [],
                        }
                    ],
                },
            ],
        }
        report["resourceSpans"][0]["scopeSpans"][0]["spans"] = [
            span.to_dict() for span in self.spans
        ]

        data = json.dumps(report, separators=(",", ":"))
        headers = {
            "Content-Type": "application/json",
        }
        if self.compression == Compression.GZIP:
            headers["Content-Encoding"] = self.compression.value
            # zlib.compress() is faster than gzip.compress();
            # 31 = 15 + 16: window size is 2^15 + output has a gzip header.
            # ==> https://docs.python.org/3/library/gzip.html#gzip.compress
            # ==> https://docs.python.org/3/library/zlib.html#zlib.compress
            data = zlib.compress(data.encode(), level=9, wbits=31)

        try:
            r = requests.post(
                self.endpoint + "/v1/traces",
                auth=self.auth,
                headers=headers,
                data=data,
            )
        except:
            # Failed to send spans, keep them until next try
            pass
        else:
            # if we could send the spans, start afresh
            if r.ok:
                self.spans = list()
        finally:
            # In any case, only keep a limited backlog
            self.spans = self.spans[-self.max_backlog :]


class Span:
    """Implements a minimalist span.

    Prefer using the Otel.span() context manager, as it will
    automatically handle the parent/child relationship, and
    finalise and export the span.
    """

    _ATTRIBUTES_MAPPING = {
        bool: {"type": "boolValue", "encode": lambda x: x},
        bytes: {
            "type": "bytesValue",
            "encode": lambda x: base64.b64encode(x).decode("ascii"),
        },
        float: {"type": "doubleValue", "encode": lambda x: x},
        int: {"type": "intValue", "encode": lambda x: x},
        str: {"type": "stringValue", "encode": lambda x: x},
    }

    def __init__(
        self,
        *,
        name: str,
        kind: Optional[SpanKind] = SpanKind.INTERNAL,
        parent_span: Optional[Self] = None,
        span_links: Optional[list[Self | str]] = None,
    ):
        """Basic span.

        :param name: Name of the span.
        :param kind: Kind of span.
        :param parent_span: Make the new span a child of parent_span.
        :param span_links: Link the new span to the spans in this list; each
                           item in the list can be either a Span(), or an str()
                           representing a traceparent.
        """
        self.name = name
        self.kind = kind
        self.start_time = time.time()
        if parent_span:
            self.trace_id = parent_span.trace_id
            self.parent_id = parent_span.span_id
        else:
            self.trace_id = random.randbytes(16).hex()
            self.parent_id = None
        self.span_id = random.randbytes(8).hex()
        self.attributes = dict()
        self.links = list()
        if span_links:
            for span in span_links:
                self.add_link(link=span)
        self.status_code = SpanStatus.UNSET
        self.set_attribute(key="iot3.core.sdk_language", value="python")
        self.status_message = None

    def finalise(self):
        """Finalise the span (i.e. set end time)."""
        self.end_time = time.time()

    def set_attribute(self, *, key, value):
        """Set or add an atribute

        Only bool, int, float, and string values are accepted.

        :param key: The key (aka name) of the attribute.
        :param value: The value of the attribute.
        """
        if type(value) not in Span._ATTRIBUTES_MAPPING:
            raise ValueError(
                f"{value!r} is not one of: {', '.join([k.__name__ for k in Span._ATTRIBUTES_MAPPING])}",
            )
        self.attributes[key] = value

    def add_link(
        self,
        *,
        link: Self | str,
    ):
        """Add a link from this span to the specified traceparent.

        :param link: The Span() or the str() of the traceparent to link to.
        """
        if type(link) is Span:
            trace_id = link.trace_id
            span_id = link.span_id
        else:
            trace_id = link.split("-")[1]
            span_id = link.split("-")[2]
        self.links.append(
            {
                "trace_id": trace_id,
                "span_id": span_id,
            }
        )

    def set_status(
        self,
        *,
        status_code: SpanStatus,
        status_message: Optional[str] = None,
    ):
        self.status_code = status_code
        self.status_message = status_message

    def to_dict(self):
        """Convert the span to a dict with key valid to the OTLP spec."""
        # The spec at: https://opentelemetry.io/docs/specs/otlp/#json-protobuf-encoding
        # is ambiguous. It states:
        #   """
        #   The keys of JSON objects are field names converted to lowerCamelCase.
        #   Original field names are not valid to use as keys for JSON objects.
        #   For example, this is a valid JSON representation of a Resource:
        #       { "attributes": {...}, "droppedAttributesCount": 123 },
        #   and this is NOT a valid representation:
        #       { "attributes": {...}, "dropped_attributes_count": 123 }.
        #   """
        # It is not obivous whether that applies when the data is sent as
        # application/json, or as application/x-protobuf, or both.
        # However, when tested against the official Docker image (as of 2024-07-12)
        # otel/opentelemetry-collector:latest, snake_case identifiers are *also*
        # accepted when sent as application/json.
        #
        # The start and end UNIX timestamp are to be expressed as an integral
        # number os nanoseconds serialized as a string. Again, passing an int()
        # or a str() both work with the official collector.
        #
        # Let's try to match the spec for now, until proven wrong...
        span = {
            "name": self.name,
            "traceId": self.trace_id,
            "spanId": self.span_id,
            "kind": self.kind.value,
            "parentSpanId": self.parent_id,
            "startTimeUnixNano": str(int(self.start_time * 1_000_000_000)),
            "endTimeUnixNano": str(int(self.end_time * 1_000_000_000)),
        }
        if self.attributes:
            span["attributes"] = list()
            for attr in self.attributes:
                attr_value = self.attributes[attr]
                attr_map = Span._ATTRIBUTES_MAPPING[type(attr_value)]
                span["attributes"].append(
                    {
                        "key": attr,
                        "value": {
                            attr_map["type"]: attr_map["encode"](attr_value),
                        },
                    },
                )
        if self.links:
            span["links"] = list()
            for link in self.links:
                span["links"].append(
                    {
                        "traceId": link["trace_id"],
                        "spanId": link["span_id"],
                    },
                )
        if self.status_code != SpanStatus.UNSET:
            span["status"] = {"code": self.status_code.value}
            if self.status_code == SpanStatus.ERROR and self.status_message:
                span["status"]["message"] = self.status_message
        return span

    def to_traceparent(self):
        """Return a traceparent that refers to the span."""
        return f"00-{self.trace_id}-{self.span_id}-00"


class NoExportSpan(Span):
    """A fake span when no telemetry is used"""

    def to_dict(self):
        return None

    def to_traceparent(self):
        return None
