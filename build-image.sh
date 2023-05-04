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

CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

JAR_FILE_COUNT=$(find "${CURRENT_DIR}/target/" -maxdepth 1 -name '*.jar' | wc -l)
if [ $JAR_FILE_COUNT == 0  ]; then
    echo "jar file not found, please execute: mvn clean package"
    exit 1
fi

ls -l "${CURRENT_DIR}"/target/*.jar

(cd "${CURRENT_DIR}"; docker build . -t midjourney-proxy:${VERSION})

docker tag midjourney-proxy:${VERSION} novicezk/midjourney-proxy-${ARCH}:${VERSION}
docker push novicezk/midjourney-proxy-${ARCH}:${VERSION}