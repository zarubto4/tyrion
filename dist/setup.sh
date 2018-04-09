#!/usr/bin/env bash

# Should be used when deploying server to a new machine

echo ""
echo " _____  _  _   ____    ___   ____   _  _      ___   ___   _____  _  _   ____ "
echo ")__ __() () ( /  _ \  )_ _( / __ \ ) \/ (    (  _( ) __( )__ __() () ( )  _)\\"
echo "  | |  '.  /  )  ' /  _| |_ ))__(( |  \ |    _) \  | _)    | |  | \/ | | '__/"
echo "  )_(   /_(   |_()_\ )_____(\____/ )_()_(   )____) )___(   )_(  )____( )_(   "
echo ""

function check_cmd {
    if ! command -v $1 &>/dev/null; then
        local DOINSTALL
        while [[ -z $DOINSTALL || ($DOINSTALL != "Y" && $DOINSTALL != "N") ]] ; do
            read -p "Python is required for the installation and none was detected. Would you like to install it? [Y/N] " DOINSTALL
        done
        if [ $DOINSTALL == "Y" ]; then
            apt-get --yes --force-yes install $1
        else
            echo " !! Manually install $1 and then run the script again !!"
            exit 1
        fi
        apt-get --yes --force-yes install $1
    fi
}

check_cmd python

if ! command -v python &>/dev/null; then

    while [[ -z $INSTALLPYTHON || ($INSTALLPYTHON != "Y" && $INSTALLPYTHON != "N") ]] ; do
        read -p "Python is required for the installation and none was detected. Would you like to install it? [Y/N] " INSTALLPYTHON
    done

    if [ $INSTALLPYTHON == "Y" ]; then
        apt-get --yes --force-yes install python
        apt-get --yes --force-yes install pip
        pip install requests
    else
        echo " !! Manually install python and then run the script again !!"
        exit 1
    fi

else
    python -c "import requests"

    if ! command -v pip &>/dev/null; then
        apt-get --yes --force-yes install pip
    fi

    pip install requests
fi

echo " == Setup new Tyrion server =="

# Create the installation folder
mkdir tyrion
cd ./tyrion

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

# python ./download_release_asset.py $DOWNLOAD_URL $ACCESS_TOKEN

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

headers2 = {'Authorization': 'token ' + config['api_key'], 'Accept': 'application/octet-stream'}
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

unzip ./dist.zip

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

while [[ -z $SECRETKEY ]] ; do
    read -p "Enter a strong application secret: " SECRETKEY
done

echo "SECRETKEY=\"$SECRETKEY\"" >> /etc/environment

while [[ -z $DATABASEURL ]] ; do
    read -p "Enter an URL of a database which the server will be connecting to in format 'jdbc:postgresql://{url}:{port}/{name}': " DATABASEURL
done

echo "DATABASEURL=\"$DATABASEURL\"" >> /etc/environment

while [[ -z $DATABASEUSR ]] ; do
    read -p "Enter the database username which the server will use to log in: " DATABASEUSR
done

echo "DATABASEUSR=\"$DATABASEUSR\"" >> /etc/environment

while [[ -z $DATABASEPASS ]] ; do
    read -p "Enter the database password which the server will use to log in: " DATABASEPASS
done

echo "DATABASEPASS=\"$DATABASEPASS\"" >> /etc/environment

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
        # Obtain certificate
        letsencrypt certonly --standalone --preferred-challenges http -d $DOMAIN

        # Convert certificate to Java Key Store format, so server can use it
        openssl pkcs12 -export -in $CERTPATH/fullchain.pem -inkey $CERTPATH/privkey.pem -out $CERTPATH/cert_and_key.p12 -CAfile $CERTPATH/chain.pem -caname root -passout pass:$CERTPASS
        keytool -importkeystore -srcstorepass $CERTPASS -destkeystore $CERTPATH/keyStore.jks -srckeystore $CERTPATH/cert_and_key.p12 -srcstoretype PKCS12 -storepass $CERTPASS
    fi

    # Add CRON job for automatic certificate renewal
    echo '0 3 * * * letsencrypt renew --deploy-hook $TYRIONROOT/reload_certificate.sh' >> ./TEMPCRON
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