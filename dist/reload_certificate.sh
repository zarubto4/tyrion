#!/usr/bin/env bash

if [ -v CERTPATH ] ; then
    echo " !! CERTPATH environment variable must be defined !!"
    exit 1
fi

if [ -v CERTPASS ] ; then
    echo " !! CERTPASS environment variable must be defined !!"
    exit 1
fi

openssl pkcs12 -export -in $CERTPATH/fullchain.pem -inkey $CERTPATH/privkey.pem -out $CERTPATH/cert_and_key.p12 -CAfile $CERTPATH/chain.pem -caname root -passout pass:$CERTPASS
keytool -importkeystore -srcstorepass $CERTPASS -destkeystore $CERTPATH/keyStore.jks -srckeystore $CERTPATH/cert_and_key.p12 -srcstoretype PKCS12 -storepass $CERTPASS

./start_server.sh