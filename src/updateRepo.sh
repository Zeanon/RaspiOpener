#!/bin/bash

if [ ! -d "RaspiOpener" ]; then
  git clone $1
else
  cd RaspiOpener
  git pull
fi
