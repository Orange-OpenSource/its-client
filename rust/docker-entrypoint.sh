#!/bin/bash

export RUST_LOG=ERROR
./its-client --outdir ${1}
code=0
echo "::set-output name=return_code::${code}"
exit ${code}