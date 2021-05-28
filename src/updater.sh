#!/usr/bin/bash

# Needs to be detached in a screen like 'screen -dm ./updater.sh'

sleep 1

# 1. Download git repo
git clone $1

# 2. Build via gradle
cd RaspiOpener
chmod u+x gradlew
./gradlew build --no-daemon

# 3. Move current jar to somewhere else
cd ..
mv RaspiOpener.jar RaspiOpener-old.jar

# 4. Move builded jar to current location
mv RaspiOpener/build/libs/RaspiOpener.jar .

# 5. Remove git dir
rm -rf RaspiOpener

# 6. Start jar
java -jar RaspiOpener.jar $2

# 7. Check on error code, when 101 do the following
if [ $? -eq 101 ]; then
    # 8. Remove current jar
    rm RaspiOpener.jar

    # 9. Copy old jar to current
    cp RaspiOpener-old.jar RaspiOpener.jar

    # 10. Start old jar
    java -jar RaspiOpener.jar $2
fi