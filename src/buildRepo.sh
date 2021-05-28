#!/usr/bin/bash

cd RaspiOpener || exit 1
chmod u+x gradlew
./gradlew build
cd ..