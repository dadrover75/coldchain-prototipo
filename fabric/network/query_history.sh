#!/bin/bash
. ./set-env.sh

# Validar que se haya pasado un parámetro
if [ -z "$1" ]; then
  echo "Uso: $0 <deviceId>"
  exit 1
fi

DEVICE_ID="$1"

peer chaincode query \
  -C mychannel \
  -n chaincode-temp \
  -c "{\"function\":\"GetHistorialLectura\",\"Args\":[\"$DEVICE_ID\"]}"
