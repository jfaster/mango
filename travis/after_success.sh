#!/bin/bash

if [ $TRAVIS_JDK_VERSION == "openjdk8" ]; then
  mvn  clean test jacoco:report coveralls:report -DrepoToken=vtcV2hf6u7qBowa7Jvp1ymNA6H2ZPGh6r
fi
