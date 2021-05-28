#!/usr/bin/bash

if [ -d "RaspiOpener" ]; then
  cd RaspiOpener
  originUrl=$(git remote get-url origin)
  cd ..
  if [ "$1" != "$originUrl" ]; then
    rm -r RaspiOpener
  fi
fi

git clone $1