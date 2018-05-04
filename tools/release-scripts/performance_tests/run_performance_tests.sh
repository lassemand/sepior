#!/bin/bash

set -e

if [ ! -d "$1" ]; then
  echo "Usage: $0 <jar directory>"
  exit 1
fi
jar_directory="$1"

echo "Running performance tests."

java -cp "${jar_directory}/*" com.sepior.crypto.performance.tests.DecryptionPerformanceTest output.json
java -cp "${jar_directory}/*" com.sepior.crypto.performance.tests.EnryptionPerformanceTest output.json
java -cp "${jar_directory}/*" com.sepior.crypto.performance.tests.ReencryptionPerformanceTest output.json
