#!/bin/bash
set -e -u -o pipefail

VERSION=latest

if [ $# -ge 1 ]; then
  VERSION=$1
fi

echo "remove old manifest..."
docker manifest rm novicezk/midjourney-proxy:${VERSION}

echo "create manifest..."
docker manifest create novicezk/midjourney-proxy:${VERSION} novicezk/midjourney-proxy-amd64:${VERSION} novicezk/midjourney-proxy-arm64v8:${VERSION}

echo "annotate amd64..."
docker manifest annotate novicezk/midjourney-proxy:${VERSION} novicezk/midjourney-proxy-amd64:${VERSION} --os linux --arch amd64

echo "annotate arm64v8..."
docker manifest annotate novicezk/midjourney-proxy:${VERSION} novicezk/midjourney-proxy-arm64v8:${VERSION} --os linux --arch arm64 --variant v8

echo "push manifest..."
docker manifest push novicezk/midjourney-proxy:${VERSION}