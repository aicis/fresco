#!/bin/sh

PASS=testpass
TRUST=truststore
for INDEX in `seq $1` 
do 
    ALIAS=P$INDEX
    STORE=keystore$INDEX
    keytool -genkeypair -alias $ALIAS -keyalg RSA -validity 600 -keystore $STORE -dname "CN=$ALIAS, OU=TLSTest, O=FRESCO, C=DK" -storepass $PASS -ext BC=ca:false
    keytool -export -alias $ALIAS -keystore $STORE -rfc -file $ALIAS.cer -storepass $PASS
    keytool -import -alias $ALIAS-cert -file $ALIAS.cer -keystore truststore -storepass $PASS -noprompt
done

keytool -genkeypair -alias badman -keyalg RSA -validity 600 -keystore keystore-bad -dname "CN=BADMAN, OU=TLSTest, O=FRESCO, C=DK" -storepass $PASS
