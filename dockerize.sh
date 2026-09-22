#!/bin/bash

#
# builds the Docker image for this project
#

set -euo pipefail

./gradlew build -x check

docker build -t sample-coupon-service:latest .
