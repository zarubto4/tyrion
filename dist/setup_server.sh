#!/usr/bin/env bash

# Should be used when deploying server to a new machine

echo " == Setup new Tyrion server =="
echo "This script will take you through the installation process."

while [[ -z $ACCESS_TOKEN ]] ; do
    read -p "Enter the GitHub access token to download the release of the server: " ACCESS_TOKEN
done

while [[ -z $LATEST || ($LATEST != "Y" && $LATEST != "N") ]] ; do
    read -p "Would you like to download the latest release? [Y/N] " LATEST
done


if [ $LATEST == "N" ]; then
    while [[ -z $TAG ]] ; do
        read -p "Enter the release tag name to download: " TAG
    done
    DOWNLOAD_URL="https://api.github.com/repos/ByzanceIoT/tyrion/releases/tags/$TAG"
else
    DOWNLOAD_URL="https://api.github.com/repos/ByzanceIoT/tyrion/releases/latest"
fi

python ./download_release_asset.py $DOWNLOAD_URL $ACCESS_TOKEN

# Exit if python script failed
if [ $? -ne 0 ] ; then
    echo " !! Download failed !!"
    exit 1
fi

# Exit if file dist.zip not found
if ! [ -e ./dist.zip ] ; then
    echo " !! Could not find downloaded 'dist.zip' file !!"
    exit 1
fi

unzip ./dist.zip

echo "TYRIONROOT=\"$(pwd)\"" >> /etc/environment

# Add startup job to cron table
crontab -l > ./TEMPCRON
echo '@reboot $TYRIONROOT/start.sh' >> ./TEMPCRON

CURRENTSERVER=$(ls | grep -m 1 -E "^v([0-9]*)\.([0-9]*)\.([0-9]*)(-(alpha|beta)(\.[0-9]*){0,3})?$")

echo $CURRENTSERVER > CURRENTSERVER

while [[ -z $SECRETKEY ]] ; do
    read -p "Enter the application secret: " SECRETKEY
done

echo "SECRETKEY=\"$SECRETKEY\"" >> /etc/environment

while [[ -z $SECURED || ($SECURED != "Y" && $SECURED != "N") ]] ; do
    read -p "Will the server use an SSL certificate? [Y/N] " SECURED
done

echo "SECURED=\"$SECURED\"" >> /etc/environment

if [ $SECURED == "Y" ]; then
    while [[ -z $DOMAIN ]] ; do
        read -p "Enter the server domain name (without protocol): " DOMAIN
    done

    echo "CERTPATH=\"/etc/letsencrypt/live/$DOMAIN\"" >> /etc/environment

    while [[ -z $CERTPASS ]] ; do
        read -p "Enter the password for the certificate: " $CERTPASS
    done

    echo "CERTPASS=\"$CERTPASS\"" >> /etc/environment

    while [[ -z $CERTNOW || ($CERTNOW != "Y" && $CERTNOW != "N") ]] ; do
        read -p "Would you like to obtain the certificate right now? [Y/N] " CERTNOW
    done

    if [ $CERTNOW == "Y" ]; then
        # TODO cd to server folder
        ./start_http.sh
        sleep 30
        letsencrypt certonly --webroot -w -d $DOMAIN # TODO determine current version

    fi

    echo '0 3 * * $TYRIONROOT/start.sh' >> ./TEMPCRON
fi


# Install new crontab
crontab ./TEMPCRON
rm -rf ./TEMPCRON

while [[ -z $RESTART || ($RESTART != "Y" && $RESTART != "N") ]] ; do
    read -p "Restart is required for changes to take effect. Restart now? [Y/N] " RESTART
done

if [ $RESTART == "Y" ]; then
    reboot
else
    echo "Restart the machine manually, when you are done."
fi

# TODO setup cron etc. [Lexa]