#!/usr/bin/env bash

###
### Helper script to enable me to debug builds on certain targets without having to
### physically install them on my machine.
###

if [ $# -lt 1 ]; then
  echo "USAGE: $0 <java version | image reference> [flags to pass to Maven]"
  exit 1
fi

set -e
set -x

if echo "${1}" | grep -qE '^[0-9]+$'; then
  image="openjdk:${1}-alpine"
else
  image="${1}"
fi

this_dir="$(dirname "$(readlink -f "${0}")")"
user_id="$(id -u "${USER}")"
group_id="$(id -g "${USER}")"
shift 1

docker run \
  --rm \
  -u "${user_id}:${group_id}" \
  -v "${this_dir}:${this_dir}" \
  -w "${this_dir}" \
  "${image}" \
  "${this_dir}/mvnw" $@
