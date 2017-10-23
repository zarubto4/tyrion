#!/bin/bash

# Go one level below current instance
cd ..

# Set vars
NEWSERVER=$1
CURRENTSERVER=$(cat ./CURRENTSERVER)
OLDSERVER=$(cat ./OLDSERVER)

# Unzip new package
unzip "dist.zip"

# Stop previous instance
kill $(cat ./$CURRENTSERVER/RUNNING_PID) &

# Remove previous pid
rm -rf ./$CURRENTSERVER/RUNNING_PID

# Add execute permission
chmod +x ./$NEWSERVER/bin/tyrion

# Go into new server
cd $NEWSERVER

# Run instance of new verion
./bin/tyrion 2>&1 >> ./server.log &

# Go one level below current instance
cd ..

# Replace values in files and remove old server and dist.zip
echo "$CURRENTSERVER" > ./OLDSERVER && echo "$NEWSERVER" > ./CURRENTSERVER && rm -rf ./$OLDSERVER && rm -rf ./dist.zip