#!/bin/bash
set -e -u -o pipefail

if [ $# -lt 1 ]; then
  echo 'version is required'
  exit 1
fi

VERSION=$1
ARCH=amd64

if [ $# -ge 2 ]; then
  ARCH=$2
fi

docker build . -t midjourney-proxy:${VERSION}

docker tag midjourney-proxy:${VERSION} novicezk/midjourney-proxy-${ARCH}:${VERSION}
docker push novicezk/midjourney-proxy-${ARCH}:${VERSION}