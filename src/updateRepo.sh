#!/usr/bin/bash

if [ ! -d "RaspiOpener" ]; then
  git clone $1
fi

cd RaspiOpener
git update