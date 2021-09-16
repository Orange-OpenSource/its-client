#!/bin/bash

export RUST_LOG=INFO
./its-client $@
code=0
echo "::set-output name=return_code::${code}"
exit ${code}