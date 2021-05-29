#!/bin/bash

# release
# beta
# dev

branch=""

cd RaspiOpener
case $1 in
  release)
    branch="master"
    ;;
  beta)
    branch="beta"
    ;;
  dev)
    branch="dev"
    ;;
  *)
    branch="master"
    ;;
esac

branches=$(git branch)
if [[ $branches == *"$branch"* ]]; then
  branch=branch
else
  branch="master"
fi

git checkout "$branch"
cd ..