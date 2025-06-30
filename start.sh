#!/bin/bash

set -e

echo "🚀 [1/5] Levantando red de Hyperledger Fabric..."
cd "$(dirname "$0")/fabric/network"

export PATH=$PWD/bin:$PATH

./network.sh down
./network.sh up createChannel -ca -c mychannel

echo "⏳ [2/5] Esperando 5 segundos para que la red se estabilice..."
sleep 5

echo "📦 [3/5] Desplegando chaincode..."
./redeploy-chaincode.sh

cd "$(dirname "$0")"  # Volver a la raíz del proyecto

echo "🚀 [4/5] Levantando sistema principal (MQTT, backend, frontend)..."
docker-compose up -d

echo "🔧 [5/5] Compilando y ejecutando simulador..."
cd simulator
mvn clean package -DskipTests
java -jar target/simulator-*-jar-with-dependencies.jar &

cd ..

echo "✅ Todo el sistema fue levantado correctamente."
