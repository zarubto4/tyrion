#!/usr/bin/env bash

cd $TYRIONROOT

CURRENTSERVER=$(cat ./CURRENTSERVER)

echo " == Starting server version $CURRENTSERVER from CURRENTSERVER file =="

cd ./$CURRENTSERVER

if [[ -v SECURED && $SECURED == "Y" ]] ; then
    chmod +x ./start_https.sh
    ./start_https.sh
else
    chmod +x ./start_http.sh
    ./start_http.sh
fi