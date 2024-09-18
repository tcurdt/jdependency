#!/bin/sh

TAGS="3.9-eclipse-temurin-21-alpine"

for TAG in $TAGS; do
  echo "testing ${TAG}"

  docker run -it --rm --name jdependency-${TAG} \
    -v "$(pwd)":/usr/src/mymaven \
    -w /usr/src/mymaven \
    maven:${TAG} \
    mvn clean test

done

