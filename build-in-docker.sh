#!/usr/bin/env sh

###
### Helper script to enable me to debug builds on certain targets without having to
### physically install them on my machine.
###

if [ $# -lt 1 ]; then
  echo "USAGE: $0 <java version | image reference> [flags to pass to Maven]"
  exit 1
fi

fullpath() {
  readlink -f $@
}

javaversion=$1
rootdir=$(fullpath $(dirname $(fullpath ${0})))
if echo "${javaversion}" | grep -qE "^[0-9]+$"; then
  image=openjdk:${javaversion}-alpine
else
  image=${1}
fi

shift 1

echo "Using image ${image}"

set -x
docker run \
  -u $(id -u ${USER}):$(id -g ${USER}) \
  --rm \
  -w ${rootdir} \
  -v ${rootdir}:${rootdir} \
  ${image} \
  ${rootdir}/mvnw $@


