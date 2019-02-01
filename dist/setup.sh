#!/usr/bin/env bash

# Should be used when deploying server to a new machine

echo ""
echo " _____  _  _   ____    ___   ____   _  _      ___   ___   _____  _  _   ____ "
echo ")__ __() () ( /  _ \  )_ _( / __ \ ) \/ (    (  _( ) __( )__ __() () ( )  _)\\"
echo "  | |  '.  /  )  ' /  _| |_ ))__(( |  \ |    _) \  | _)    | |  | \/ | | '__/"
echo "  )_(   /_(   |_()_\ )_____(\____/ )_()_(   )____) )___(   )_(  )____( )_(   "
echo ""

echo "This script will take you through the installation process."

function check_cmd {
    if ! command -v $1 &>/dev/null; then
        local DOINSTALL
        while [[ -z $DOINSTALL || ($DOINSTALL != "Y" && $DOINSTALL != "N") ]] ; do
            read -p "$1 is required for the installation and none was detected. Would you like to install it? [Y/N] " DOINSTALL
        done
        if [ $DOINSTALL == "Y" ]; then

            if [ $1 == "certbot" ]; then
                add-apt-repository --yes ppa:certbot/certbot
                apt-get update
            fi

            if [ $1 == "pip" ]; then
                apt-get --yes --force-yes install python-pip
            else
                apt-get --yes --force-yes install $1
            fi
        else
            echo " !! Manually install $1 and then run the script again. !!"
            exit 1
        fi
    fi
}

function prompt {
    local PROMPTRESULT
    while [[ -z $PROMPTRESULT ]] ; do
        read -p "$1" PROMPTRESULT
    done

    echo "$PROMPTRESULT"
}

function prompt_yn {
    local PROMPTRESULT
    while [[ -z $PROMPTRESULT || ($PROMPTRESULT != "Y" && $PROMPTRESULT != "N") ]] ; do
        read -p "$1" PROMPTRESULT
    done

    echo "$PROMPTRESULT"
}

function prompt_save {
    local PROMPTRESULT=$(prompt "$2")
    echo "$1=\"$PROMPTRESULT\"" >> /etc/environment
    echo "$PROMPTRESULT"
}

echo "Checking prerequisites."

check_cmd python
check_cmd dos2unix
check_cmd unzip

# Check if requests module is installed
python -c "import requests"

# Exit if python script failed
if [ $? -ne 0 ] ; then
    check_cmd pip

    DOINSTALLREQUESTS=$(prompt_yn "'requests' python module is required for the installation and none was detected. Would you like to install it? [Y/N] ")
    if [ $DOINSTALLREQUESTS == "Y" ]; then
        pip install requests
    else
        echo " !! Manually install 'requests' python module (run 'pip install requests') and then run the script again. !!"
        exit 1
    fi
fi

echo "Prerequisites are met, everything is good to go."

# Create the installation folder
mkdir tyrion
cd ./tyrion

ACCESS_TOKEN=$(prompt "Enter the GitHub access token to download the release of the server: ")
LATEST=$(prompt_yn "Would you like to download the latest release? [Y/N] ")

if [ $LATEST == "N" ]; then
    while [[ -z $TAG ]] ; do
        read -p "Enter the release tag name to download: " TAG
    done
    DOWNLOAD_URL="https://api.github.com/repos/ByzanceIoT/tyrion/releases/tags/$TAG"
else
    DOWNLOAD_URL="https://api.github.com/repos/ByzanceIoT/tyrion/releases/latest"
fi

echo "Downloading (may take a while) ..."

# Python script for downloading
python - $DOWNLOAD_URL $ACCESS_TOKEN <<END
import sys
import requests

if len(sys.argv) < 3:
    print 'Missing arguments, correct syntax: download_release_asset.py <release_url> <access_token>'
    sys.exit(1)

headers = {'Authorization': 'token ' + sys.argv[2]}
response = requests.get(sys.argv[1], headers = headers)

if response.status_code != 200:
     print 'Request was unsuccessful'
     print response.text
     sys.exit(1)

release = response.json()

if len(release['assets']) == 0:
     print 'Release does not contain any asset'
     sys.exit(1)

for index, asset in enumerate(release['assets']):
     if asset['name'] == 'dist.zip':
         asset_url = asset['url']
         break
     elif index == len(release['assets']) - 1:
         print 'Cannot found any asset named dist.zip'
         sys.exit(1)

headers2 = {'Authorization': 'token ' + sys.argv[2], 'Accept': 'application/octet-stream'}
package = requests.get(asset_url, headers = headers2)

with open('dist.zip', 'w') as fd:
     for chunk in package.iter_content(chunk_size=128):
         fd.write(chunk)

sys.exit(0)
END

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

echo "Downloaded successfully. Unzipping ..."

unzip -q ./dist.zip

echo "Unzipped successfully."

# Add current working directory (installation directory) to environment variable
echo "TYRIONROOT=\"$(pwd)\"" >> /etc/environment

# Copy current user's crontab to temporary file, so it can be modified
crontab -l > ./TEMPCRON

# Add startup job to cron table
echo '@reboot $TYRIONROOT/start.sh' >> ./TEMPCRON

# Find the downloaded unzipped server folder. (e.g. v1.25.456-beta) If more than one folder matching following RegEx is found, first occurrence is used.
CURRENTSERVER=$(ls | grep -m 1 -E "^v([0-9]*)\.([0-9]*)\.([0-9]*)(-(alpha|beta)(\.[0-9]*){0,3})?$") # TODO prompt for the folder if there are more options

echo $CURRENTSERVER > CURRENTSERVER

# Copy necessary files from the server folder to installation directory
cp ./$CURRENTSERVER/start.sh ./start.sh
cp ./$CURRENTSERVER/reload_certificate.sh ./reload_certificate.sh

chmod +x ./start.sh
chmod +x ./reload_certificate.sh

while [[ -z $SERVERMODE || ($SERVERMODE != "DEVELOPER" && $SERVERMODE != "STAGE" && $SERVERMODE != "PRODUCTION") ]] ; do
    read -p "Choose server mode? [DEVELOPER/STAGE/PRODUCTION] " SERVERMODE
done

echo "SERVERMODE=\"$SERVERMODE\"" >> /etc/environment

SECRETKEY=$(prompt_save "SECRETKEY" "Enter a strong application secret: ")
DATABASEURL=$(prompt_save "DATABASEURL" "Enter an URL of a database which the server will be connecting to in format 'jdbc:postgresql://{url}:{port}/{name}': ")
DATABASEUSR=$(prompt_save "DATABASEUSR" "Enter the database username which the server will use to log in: ")
DATABASEPASS=$(prompt_save "DATABASEPASS" "Enter the database password which the server will use to log in: ")

SECURED=$(prompt_yn "Will the server use an SSL certificate? [Y/N] ")
echo "SECURED=\"$SECURED\"" >> /etc/environment

if [ $SECURED == "Y" ]; then

    check_cmd certbot

    DOMAIN=$(prompt_save "DOMAIN" "Enter the server domain name (without protocol): ")
    echo "CERTPATH=\"/etc/letsencrypt/live/$DOMAIN\"" >> /etc/environment

    CERTPASS=$(prompt_save "CERTPASS" "Enter the password for the certificate: ")

    CERTNOW=$(prompt_yn "Would you like to obtain the certificate right now? [Y/N] ")

    if [ $CERTNOW == "Y" ]; then
        # Obtain certificate
        certbot certonly --standalone --preferred-challenges http -d $DOMAIN

        # Convert certificate to Java Key Store format, so server can use it
        openssl pkcs12 -export -in $CERTPATH/fullchain.pem -inkey $CERTPATH/privkey.pem -out $CERTPATH/cert_and_key.p12 -CAfile $CERTPATH/chain.pem -caname root -passout pass:$CERTPASS
        keytool -importkeystore -srcstorepass $CERTPASS -destkeystore $CERTPATH/keyStore.jks -srckeystore $CERTPATH/cert_and_key.p12 -srcstoretype PKCS12 -storepass $CERTPASS
    fi

    # Add CRON job for automatic certificate renewal
    echo '0 3 * * * certbot renew --deploy-hook $TYRIONROOT/reload_certificate.sh' >> ./TEMPCRON
fi


# Install new crontab
crontab ./TEMPCRON
rm -rf ./TEMPCRON

RESTART=$(prompt_yn "Restart is required for changes to take effect. Restart now? [Y/N] ")

if [ $RESTART == "Y" ]; then
    reboot
else
    echo "Restart the machine manually, when you are done."
fi