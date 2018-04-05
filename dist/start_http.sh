#!/usr/bin/env bash

if [ -e ./RUNNING_PID ] ; then
    echo " == Found previous PID - trying to kill it =="
    kill $(cat ./RUNNING_PID)
    rm -rf ./RUNNING_PID
fi

echo " == Starting new instance of Tyrion (http) =="
chmod +x ./bin/tyrion
./bin/tyrion -Dhttp.port=80 2>&1 >> server.log &