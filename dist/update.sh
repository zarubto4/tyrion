#!/bin/bash

if [ $# -eq 0 ] ; then
    echo " !! No arguments supplied! Usage: update_server.sh <version> !!"
    exit 1
fi

# Go one level below current instance
cd ..

# Set vars
NEWSERVER=$1
CURRENTSERVER=$(cat CURRENTSERVER)
OLDSERVER=$(cat OLDSERVER)

# Unzip new package
unzip -q "dist.zip"

if [ ! -e ./$CURRENTSERVER/RUNNING_PID ] ; then
    >&2 echo " !! Current PID is missing !!"
    exit 1
fi

# Stop previous instance and remove previous pid
echo " == Killing previous instance with pid: $(cat ./$CURRENTSERVER/RUNNING_PID) =="
(kill $(cat ./$CURRENTSERVER/RUNNING_PID) && rm -rf ./$CURRENTSERVER/RUNNING_PID && echo " == Previous instance stopped ==") &

# Go into new server
cd ./$NEWSERVER

# Add execute permission
chmod +x ./bin/tyrion
chmod +x ./start_http.sh
chmod +x ./start_https.sh
chmod +x ./update.sh

dos2unix ./start_http.sh
dos2unix ./start_https.sh
dos2unix ./update.sh

# Run instance of new verion
echo " == Starting new server =="

if [[ -v SECURED && $SECURED == "Y" ]] ; then
    ./start_https.sh
    TESTURL="https://$DOMAIN:443"
else
    ./start_http.sh
    TESTURL="http://0.0.0.0:80"
fi

# Go one level below current instance
cd ..

# Wait 30s for server to start
echo " == Sleeping for 30s to wait for server to start =="
sleep 30

# Then request it
RESPONSE=$(curl -w "%{http_code}" -o "" --silent --connect-timeout 60 "$TESTURL")
case $RESPONSE in
        200|201|400|404)
                echo " == Server started properly =="
                # Replace values in files and remove old server and dist.zip
                echo "$CURRENTSERVER" > ./OLDSERVER && echo "$NEWSERVER" > ./CURRENTSERVER && rm -rf ./dist.zip && rm -rf ./$OLDSERVER
                ;;
        *)
                echo " !! Server did not start or is in fault state !!"

                # If server started but is in fault state - stopping it
                if [ -e ./$NEWSERVER/RUNNING_PID ] ; then
                    echo " == Killing non functioning instance with pid: $(cat ./$NEWSERVER/RUNNING_PID) =="
                    kill $(cat ./$NEWSERVER/RUNNING_PID)
                fi

                # Run instance of last version
                echo " == Starting previous server =="
                ./start.sh

                # Post to Slack
                echo " == Posting to Slack, that error occurred during update =="
                curl -X POST --data-urlencode "payload={\"text\": \"Failed to update server to version $NEWSERVER\"}" "https://hooks.slack.com/services/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"

                # Clean up non functioning server
                echo " == Cleaning after unsuccessful update =="
                # rm -rf ./$NEWSERVER && rm -rf ./dist.zip
                exit 1
                ;;
esac
