#!/usr/bin/env bash

###
### Helper script to enable me to debug builds on certain targets without having to
### physically install them on my machine.
###
### This is also used by the CI to enable the use of pre-release JDK images, and to
### enable being able to replicate builds from CI on your local machine.
###

if [ $# -lt 1 ]; then
  echo "USAGE: $0 <java version | image reference> [flags to pass to Maven]"
  exit 1
fi

set -e

if echo "${1}" | grep -qE '^[0-9]+$'; then
  image="openjdk:${1}"
else
  image="${1}"
fi

this_dir="$(dirname "$(readlink -f "${0}")")"
user_id="$(id -u "${USER}")"
group_id="$(id -g "${USER}")"
shift 1

echo -e "\e[1;32m[IMAGE]:\e[0;32m ${image}\e[0m"
echo -e "\e[1;32m[CWD]:\e[0;32m ${this_dir}\e[0m"
echo -e "\e[1;32m[USER ID]:\e[0;32m ${user_id}\e[0m"
echo -e "\e[1;32m[GROUP ID]:\e[0;32m ${group_id}\e[0m"
echo -e "\e[1;32m[USER ENTRY]:\e[0;32m $(whoami)\e[0m"
echo -e "\e[1;32m[SYSTEM]:\e[0;32m $(uname -a)\e[0m"
echo -e "\e[1;32m[COMMAND]:\e[0;32m ./mvnw $@\e[0m"
echo -en "\e[1;32m[DOCKER VERSION]:\e[0;32m $(docker --version | xargs)\e[0m"

docker run \
    --rm \
    -u "${user_id}:${group_id}" \
    -v "${this_dir}:${this_dir}" \
    -w "${this_dir}" \
    "${image}" \
    "${this_dir}/mvnw" $@ \
    2>&1 \
  | while read line; do echo -e "\e[1;36m[DOCKER]\e[0m ${line}"; done
  
