#!/bin/bash
set -e -u -o pipefail

VERSION=latest
ARCH=amd64

if [ $# -ge 1 ]; then
  VERSION=$1
fi

if [ $# -ge 2 ]; then
  ARCH=$2
fi

docker build . -t midjourney-proxy:${VERSION}

docker tag midjourney-proxy:${VERSION} novicezk/midjourney-proxy-${ARCH}:${VERSION}
docker push novicezk/midjourney-proxy-${ARCH}:${VERSION}