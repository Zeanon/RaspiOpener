#!/usr/bin/bash

if [ -d "RaspiOpener" ]; then
  rm -r RaspiOpener
fi

git clone $1