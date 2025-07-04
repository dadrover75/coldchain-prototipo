#!/bin/bash
. ./set-env.sh

# === Invocar función ResetEstadoContrato ===
peer chaincode invoke \
  -o orderer.example.com:7050 \
  --ordererTLSHostnameOverride orderer.example.com \
  --tls \
  --cafile "$PWD/organizations/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem" \
  -C mychannel \
  -n chaincode-temp \
  --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles "$CORE_PEER_TLS_ROOTCERT_FILE" \
  --peerAddresses peer0.org2.example.com:9051 --tlsRootCertFiles "$PWD/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
  -c '{"function":"ResetEstadoContrato","Args":[]}'
