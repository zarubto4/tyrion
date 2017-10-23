#!/usr/bin/env bash

# Go one level below current instance
cd ..

# Set vars
NEWSERVER=$1
CURRENTSERVER=$(cat ./CURRENTSERVER)
OLDSERVER=$(cat ./OLDSERVER)

# Unzip new package
unzip "dist.zip"

# Stop previous instance
kill $(cat ./$CURRENTSERVER/RUNNING_PID)

# Add execute permission
chmod +x ./$NEWSERVER/bin/tyrion

# Run instance of new verion
./$NEWSERVER/bin/tyrion 2>&1 >> ./$NEWSERVER/server.log &

# Replace values in files and remove old server
cat $CURRENTSERVER > ./OLDSERVER && cat $NEWSERVER > ./CURRENTSERVER && rm -rf ./$OLDSERVER
