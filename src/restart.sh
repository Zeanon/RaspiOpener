#!/bin/bash

sleep 1

if [ -a "RaspiOpener/build/libs/RaspiOpener.jar" ]; then
  mv RaspiOpener.jar RaspiOpener-old.jar
  mv RaspiOpener/build/libs/RaspiOpener.jar .
fi

# 6. Start jar
java -jar RaspiOpener.jar $1

# 7. Check on error code, when 101 do the following
if [ $? -eq 101 ]; then
    # 8. Remove current jar
    rm RaspiOpener.jar

    # 9. Copy old jar to current
    cp RaspiOpener-old.jar RaspiOpener.jar

    # 10. Start old jar
    java -jar RaspiOpener.jar $1
fi