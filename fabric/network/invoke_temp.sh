#!/bin/bash
. ./set-env.sh

# Variables para certificados TLS
ROOTDIR=$(cd "$(dirname "$0")" && pwd)
PEER0_ORG1_CA="$ROOTDIR/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt"
PEER0_ORG2_CA="$ROOTDIR/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt"
ORDERER_CA="$ROOTDIR/organizations/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem"

# Parámetros recibidos
DEVICE_ID=$1
TIMESTAMP=$2
TEMPERATURE=$3

# Validar entrada
if [ -z "$DEVICE_ID" ] || [ -z "$TIMESTAMP" ] || [ -z "$TEMPERATURE" ]; then
  echo "Uso: $0 <deviceId> <timestamp> <temperature>"
  exit 1
fi

# Crear JSON interno como string escapado
ARGS_JSON="{\\\"timestamp\\\":\\\"$TIMESTAMP\\\",\\\"temperature\\\":$TEMPERATURE}"

# Invocar chaincode
ppeer chaincode invoke \
   -o orderer.example.com:7050 \
   --ordererTLSHostnameOverride orderer.example.com \
   --tls \
   --cafile "$ORDERER_CA" \
   -C mychannel \
   -n chaincode-temp \
   --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles "$PEER0_ORG1_CA" \
   --peerAddresses peer0.org2.example.com:9051 --tlsRootCertFiles "$PEER0_ORG2_CA" \
   -c "{\"function\":\"ValidarLectura\",\"Args\":[\"$DEVICE_ID\", \"$ARGS_JSON\"]}"
