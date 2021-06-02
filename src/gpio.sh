#!/bin/bash

pin="36"

pathExport="/sys/class/gpio/export"
pathDirection="/sys/class/gpio$pin/direction"
pathValue="/sys/class/gpio$pin/value"

sudo echo "$pin" > $pathExport
sudo echo "out" > $pathDirection

sudo echo "1" > $pathValue
sleep $1
sudo echo "0" > $pathValue