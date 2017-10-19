#!/usr/bin/env bash

# Go one level below current instance
cd ../..

# Set vars
VERSION=$1
NEWSERVER="tyrion-$VERSION"
CURRENTSERVER=$(cat ./CURRENTSERVER)
OLDSERVER=$(cat ./OLDSERVER)

# Unzip new package
unzip "$NEWSERVER.zip"

# Stop previous instance
cat ./$CURRENTSERVER/RUNNING_PID | kill

# Run instance of new verion
./$NEWSERVER/bin/tyrion 2>&1 >> ./$NEWSERVER/server.log &

# Replace values in files and remove old server
cat $CURRENTSERVER > ./OLDSERVER && cat $NEWSERVER > ./CURRENTSERVER && rm -rf ./$OLDSERVER
