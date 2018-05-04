#!/bin/bash

# Get version.
if [ "$1" = "previous_version" ]; then
  prev="^1"
  shift 1
fi

if [ -z "$1" ]; then
  commit="HEAD"
else
  commit="$1"
fi
version="$(git describe --tags "${commit}${prev}")"
meta="$(git rev-parse --short "${commit}${prev}")"

# Abort if version or meta is empty.
[ -z "${version}" ] || [ -z "${meta}" ] && exit 1

# Release tag.
regex="^[0-9]+(\.[0-9]+)*$"
if [[ "${version}" =~ ${regex} ]]; then
  echo "${version}"
  exit 0
fi

regex="^[0-9]+(\.[0-9]+)*-[0-9]+-.+"
if ! [[ "${version}" =~ ${regex} ]]; then
  echo "Unrecognized tag: ${version}" >&2
  exit 1
fi

tokens=(${version//-/ })

# shellcheck disable=SC2016
version="$(echo "${tokens[0]}" | sed -r 's/^([0-9]+\.)?([0-9]+\.)?([0-9]+)(-|$)/echo \1\2$((\3+1))\4/ge')"
count="$(printf "%04x" "${tokens[1]}")"

echo "${version}-RC-${count}-${meta}"
