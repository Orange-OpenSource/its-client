from __future__ import annotations
import its_iqm.authority.file as _file
import its_iqm.authority.http as _http
import its_iqm.authority.mqtt as _mqtt
import its_iqm.iqm


class Authority:
    def __new__(
        cls,
        instance_id: str,
        cfg: dict,
        update_cb: Callable[[its_iqm.iqm.IQM, dict], None],
    ):
        authority_type = cfg["type"]
        if authority_type == "file":
            return _file.Authority(instance_id, cfg, update_cb)
        if authority_type == "http":
            return _http.Authority(instance_id, cfg, update_cb)
        if authority_type == "mqtt":
            return _mqtt.Authority(instance_id, cfg, update_cb)
        raise ValueError(f"unknown central authority type {authority_type}")

    def __init__(self, *args, **kwargs):
        raise RuntimeError("Trying to initialise an Authority demuxer...")


__all__ = [
    "Authority",
]
