#!/bin/bash

# -----------------------
# Configuración general
# -----------------------
CHANNEL_NAME="mychannel"
CC_NAME="chaincode-temp"
CC_PATH="../../chaincode/temp-validator/javascript"
CC_LANG="node"
ORDERER_CA="$PWD/organizations/ordererOrganizations/example.com/tlsca/tlsca.example.com-cert.pem"
PEER0_ORG1_CA="$PWD/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt"
PEER0_ORG2_CA="$PWD/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt"

# -----------------------
# Funciones para setear entorno
# -----------------------
setEnvOrg1() {
  export CORE_PEER_TLS_ENABLED=true
  export CORE_PEER_LOCALMSPID="Org1MSP"
  export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_ORG1_CA
  export CORE_PEER_MSPCONFIGPATH="$PWD/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp"
  export CORE_PEER_ADDRESS=localhost:7051
}

setEnvOrg2() {
  export CORE_PEER_TLS_ENABLED=true
  export CORE_PEER_LOCALMSPID="Org2MSP"
  export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_ORG2_CA
  export CORE_PEER_MSPCONFIGPATH="$PWD/organizations/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp"
  export CORE_PEER_ADDRESS=localhost:9051
}

# -----------------------
# Paso 1: calcular próxima versión y secuencia
# -----------------------
echo "🔍 Obteniendo secuencia actual..."
setEnvOrg1
RAW_COMMITTED=$(peer lifecycle chaincode querycommitted -C $CHANNEL_NAME -n $CC_NAME)

CURRENT_SEQ=$(echo "$RAW_COMMITTED" | grep -Eo "Sequence: [0-9]+" | head -n1 | awk '{print $2}')

if [ -z "$CURRENT_SEQ" ]; then
  CURRENT_SEQ=0
fi

NEXT_SEQ=$((CURRENT_SEQ + 1))
VERSION="1.$NEXT_SEQ"

echo "📦 Secuencia actual: $CURRENT_SEQ"
echo "📦 Nueva secuencia: $NEXT_SEQ"
echo "📦 Nueva versión: $VERSION"
# -----------------------
# Paso 2: empaquetar
# -----------------------
echo "📦 Limpiando paquetes anteriores..."
rm -f ${CC_NAME}.tar.gz

echo "📦 Empaquetando chaincode..."
peer lifecycle chaincode package ${CC_NAME}.tar.gz --path $CC_PATH --lang $CC_LANG --label ${CC_NAME}_$VERSION
if [ $? -ne 0 ]; then echo "❌ Error al empaquetar"; exit 1; fi

# -----------------------
# Paso 3: instalar en ambos peers
# -----------------------
echo "📥 Instalando en Org1..."
setEnvOrg1
peer lifecycle chaincode queryinstalled | grep ${CC_NAME}_$VERSION > /dev/null
if [ $? -eq 0 ]; then
  echo "✅ Ya está instalado en Org1, se omite."
else
  peer lifecycle chaincode install ${CC_NAME}.tar.gz
  if [ $? -ne 0 ]; then echo "❌ Error al instalar en Org1"; exit 1; fi
fi

echo "📥 Instalando en Org2..."
setEnvOrg2
peer lifecycle chaincode queryinstalled | grep ${CC_NAME}_$VERSION > /dev/null
if [ $? -eq 0 ]; then
  echo "✅ Ya está instalado en Org2, se omite."
else
  peer lifecycle chaincode install ${CC_NAME}.tar.gz
  if [ $? -ne 0 ]; then echo "❌ Error al instalar en Org2"; exit 1; fi
fi

# -----------------------
# Paso 4: obtener Package ID
# -----------------------
echo "📦 Obteniendo Package ID..."
setEnvOrg1
PACKAGE_ID=$(peer lifecycle chaincode queryinstalled | grep ${CC_NAME}_$VERSION | awk '{print $3}' | sed 's/,$//')
echo "📦 Package ID: $PACKAGE_ID"
if [ -z "$PACKAGE_ID" ]; then echo "❌ No se encontró Package ID"; exit 1; fi

# -----------------------
# Paso 5: aprobar
# -----------------------
echo "✅ Aprobando en Org1..."
setEnvOrg1
peer lifecycle chaincode approveformyorg -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com \
  --tls --cafile $ORDERER_CA --channelID $CHANNEL_NAME --name $CC_NAME \
  --version $VERSION --package-id $PACKAGE_ID --sequence $NEXT_SEQ
if [ $? -ne 0 ]; then echo "❌ Error al aprobar en Org1"; exit 1; fi

echo "✅ Aprobando en Org2..."
setEnvOrg2
peer lifecycle chaincode approveformyorg -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com \
  --tls --cafile $ORDERER_CA --channelID $CHANNEL_NAME --name $CC_NAME \
  --version $VERSION --package-id $PACKAGE_ID --sequence $NEXT_SEQ
if [ $? -ne 0 ]; then echo "❌ Error al aprobar en Org2"; exit 1; fi

# -----------------------
# Paso 6: checkcommitreadiness
# -----------------------
echo "🔎 Verificando readiness..."
setEnvOrg1
peer lifecycle chaincode checkcommitreadiness \
  --channelID $CHANNEL_NAME --name $CC_NAME --version $VERSION --sequence $NEXT_SEQ --output json
echo "✔️ Si ambas orgs están en true, podés continuar."

# -----------------------
# Paso 7: commit
# -----------------------
echo "🚀 Commitiendo definición..."
peer lifecycle chaincode commit -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com \
  --tls --cafile $ORDERER_CA --channelID $CHANNEL_NAME --name $CC_NAME --version $VERSION --sequence $NEXT_SEQ \
  --peerAddresses localhost:7051 --tlsRootCertFiles $PEER0_ORG1_CA \
  --peerAddresses localhost:9051 --tlsRootCertFiles $PEER0_ORG2_CA
if [ $? -ne 0 ]; then echo "❌ Error en el commit del chaincode"; exit 1; fi

# -----------------------
# Paso 8: Confirmar despliegue
# -----------------------
echo "📋 Consultando chaincode desplegado..."
setEnvOrg1
peer lifecycle chaincode querycommitted -C $CHANNEL_NAME -n $CC_NAME

echo "✅ Redeploy completado exitosamente."
