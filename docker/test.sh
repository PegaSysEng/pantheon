#!/bin/bash

export GOSS_PATH=tests/goss-linux-amd64
export GOSS_OPTS="$GOSS_OPTS --format junit"
export GOSS_FILES_STRATEGY=cp
DOCKER_IMAGE=$1
DOCKER_FILE="${2:-$PWD/Dockerfile}"

i=0

# Checks on the Dockerfile
GOSS_FILES_PATH=tests/00 \
bash tests/dgoss dockerfile $DOCKER_FILE \
> ./reports/00.xml || i=`expr $i + 1`
# fail fast if we dont pass static checks
if [[ $i != 0 ]]; then exit $i; fi

# Test for normal startup with ports opened
GOSS_FILES_PATH=tests/01 \
bash tests/dgoss \
run $DOCKER_IMAGE \
--network=dev \
--p2p-host=0.0.0.0 \
--rpc-http-enabled \
--rpc-ws-enabled \
--graphql-http-enabled \
> ./reports/01.xml || i=`expr $i + 1`

exit $i
