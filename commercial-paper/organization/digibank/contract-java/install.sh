#!/bin/bash

cd /Users/balazsvarga/blockchain/fabric-samples/commercial-paper/organization/digibank/contract-java
./gradlew clean build

cd /Users/balazsvarga/blockchain/fabric-samples/commercial-paper/organization/digibank
source ./digibank.sh
peer lifecycle chaincode package cp.tar.gz --lang java --path ./contract-java --label commit_2

export PACKAGE_ID=`peer lifecycle chaincode install cp.tar.gz | tail -c 74 | head -c 73`

peer lifecycle chaincode approveformyorg --orderer localhost:7050 --ordererTLSHostnameOverride orderer.example.com \
--channelID mychannel --name newcommit -v 0 --sequence 1 --tls --cafile $ORDERER_CA --package-id $PACKAGE_ID

