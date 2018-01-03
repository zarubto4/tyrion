#!/usr/bin/env bash

if [ -v CERTPATH ] ; then
    echo " !! CERTPATH environment variable must be defined !!"
    exit 1
fi

if [ -v CERTPASS ] ; then
    echo " !! CERTPASS environment variable must be defined !!"
    exit 1
fi

if [ -e ./RUNNING_PID ] ; then
    echo " == Found previous PID - trying to kill it =="
    kill $(cat ./RUNNING_PID)
fi

echo " == Starting new instance of Tyrion =="
chmod +x ./bin/tyrion
./bin/tyrion -Dhttp.port=disabled -Dhttps.port=443 -Dplay.server.https.keyStore.path=$CERTPATH/keyStore.jks -Dplay.server.https.keyStore.password=$CERTPASS 2>&1 >> server.log &