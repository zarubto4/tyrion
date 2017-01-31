#!/bin/bash

cd ../../..

usr/src/app/certbot-auto renew

cd /etc/letsencrypt/live/tyrion.byzance.cz

openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -out cert_and_key.p12 -CAfile chain.pem -caname root -passout pass:RNpL2FfUp8s8MxUg

keytool -importkeystore -srcstorepass RNpL2FfUp8s8MxUg -destkeystore keyStore.jks -srckeystore cert_and_key.p12 -srcstoretype PKCS12 -storepass RNpL2FfUp8s8MxUg


