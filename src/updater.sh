#!/usr/bin/bash

# Needs to be detached in a screen like 'screen -dm ./updater.sh'

sleep 10

# 1. Download git repo
# 2. Build via gradle
# 3. Move current jar to somewhere else
# 4. Move builded jar to current location
# 5. Remove git dir
# 6. Start jar
# 7. Check on error code, when 101 do the following
# 8. Remove current jar
# 9. Copy old jar to current
# 10. Start old jar

$?
