#!/bin/bash

cmd="./restart.sh $*"
echo -n "$cmd"
screen -dmS RaspiOpener bash -c "$cmd"
echo -n "END!"