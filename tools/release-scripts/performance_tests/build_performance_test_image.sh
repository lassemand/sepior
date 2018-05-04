#!/bin/bash

set -e

if [ -z "$1" ]; then
  echo "Missing tag."
  exit 1
fi

base_directory="$(git rev-parse --show-toplevel)"
script_directory="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
working_directory="$(mktemp -d)"

finish() {
  exit_code="$?"
  cd "${base_directory}" || exit 1
  rm -rf "${working_directory}"
  exit "${exit_code}"
}
trap finish EXIT

#
# Set Docker variables.
#

docker_tag="$1"
docker_image="java-crypto-performance-tests"
docker_registry="docker.sepior.net:5000"
docker_repo_image_tag="${docker_registry}/${docker_image}:${docker_tag}"

#
# Configure working directory. 
#

cd "${working_directory}" || exit 1

cp ${base_directory}/target/*.jar .
cp "${script_directory}/run_performance_tests.sh" .
cp "${script_directory}/Dockerfile" .

#
# Build and push Docker image.
#

docker build --rm=false -t "${docker_repo_image_tag}" .
docker login -u "$DOCKER_USER" -e sepiorinfrastructure@sepior.net -p "$DOCKER_PASSWORD" "${docker_registry}"
docker push "${docker_repo_image_tag}"
