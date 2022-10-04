# Software Name: its-status
# SPDX-FileCopyrightText: Copyright (c) 2022 Orange
# SPDX-License-Identifier: MIT
# Author: Yann E. MORIN <yann.morin@orange.com>

import subprocess


def run(cmd):
    try:
        return subprocess.run(cmd, capture_output=True)
    except FileNotFoundError:
        return subprocess.CompletedProcess(
            args=cmd,
            returncode=127,
            stdout=b'',
            stderr=f'{cmd[0]}: command not found'.encode()
        )
