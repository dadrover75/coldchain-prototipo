#!/bin/bash
. ./set-env.sh

# === Configuración de Fabric para evitar errores con core.yaml ===
export FABRIC_CFG_PATH=$PWD/../config

# === Ejecutar query al chaincode ===
peer chaincode query \
  -C mychannel \
  -n chaincode-temp \
  -c '{"function":"GetEstadoContrato","Args":[]}'
