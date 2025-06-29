#!/bin/bash
. ./set-env.sh

# Ejecutar query
peer chaincode query \
  -C mychannel \
  -n chaincode-temp \
  -c '{"function":"GetLectura","Args":["123"]}'
