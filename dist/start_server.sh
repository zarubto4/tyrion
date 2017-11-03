#!/usr/bin/env bash

if [ -e ./RUNNING_PID ]; then
    echo " == Found previous PID - trying to kill it =="
    kill $(cat ./RUNNING_PID)
fi

echo " == Starting new instance of Tyrion =="
chmod +x ./bin/tyrion
./bin/tyrion 2>&1 >> server.log &