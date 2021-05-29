#!/bin/bash

if [ -d "RaspiOpener" ]; then
  cd RaspiOpener
  originUrl=$(git remote get-url origin)
  cd ..
  if [ "$1" != "$originUrl" ]; then
    rm -rf RaspiOpener
  else
    exit 0
  fi
fi

git clone $1