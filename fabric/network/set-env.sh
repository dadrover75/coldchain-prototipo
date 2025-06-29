#!/bin/bash

# Obtener el path base desde donde está parado este script
ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

# Rutas relativas desde ROOT_DIR
export PATH="$ROOT_DIR/bin:$PATH"
export FABRIC_CFG_PATH="$ROOT_DIR/config"

# Variables específicas de Org1
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE="$ROOT_DIR/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt"
export CORE_PEER_MSPCONFIGPATH="$ROOT_DIR/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp"
export CORE_PEER_ADDRESS=peer0.org1.example.com:7051
