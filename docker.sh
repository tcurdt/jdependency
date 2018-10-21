#!/bin/sh

MVN=3.5.4
JDKS="jdk-9-slim jdk-10-slim jdk-11-slim"

for JDK in $JDKS; do
  echo "testing ${MVN}-${JDK}"

  docker run -it --rm --name jdependency-${JDK} \
    -v "$(pwd)":/usr/src/mymaven \
    -w /usr/src/mymaven \
    maven:${MVN}-${JDK} \
    mvn clean test

done

