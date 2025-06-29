#!/bin/bash
. ./set-env.sh

# Definir las variables para los certificados TLS de los peers
PEER0_ORG1_CA="$PWD/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt"
PEER0_ORG2_CA="$PWD/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt"
ORDERER_CA="$PWD/organizations/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem"

# Ejecutar invoke con temperatura válida
peer chaincode invoke \
  -o localhost:7050 \
  --ordererTLSHostnameOverride orderer.example.com \
  --tls \
  --cafile "$ORDERER_CA" \
  -C mychannel \
  -n chaincode-temp \
  --peerAddresses localhost:7051 --tlsRootCertFiles "$PEER0_ORG1_CA" \
  --peerAddresses localhost:9051 --tlsRootCertFiles "$PEER0_ORG2_CA" \
  -c '{"function":"ValidarLectura","Args":["lectura1", "{\"temperatura\":7}"]}'
