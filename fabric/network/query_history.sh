#!/bin/bash
. ./set-env.sh

peer chaincode query \
  -C mychannel \
  -n chaincode-temp \
  -c '{"function":"GetHistorialLectura","Args":["lectura1"]}'
