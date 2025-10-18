#!/bin/bash
set -e

echo "🚀 Deteniendo todos los contenedores..."
docker stop $(docker ps -aq) 2>/dev/null || true

echo "🧹 Borrando todos los contenedores..."
docker rm $(docker ps -aq) 2>/dev/null || true

echo "🧹 Borrando volúmenes de Docker..."
docker volume prune -f

echo "🧹 Borrando redes de Docker..."
docker network prune -f

echo "🧹 Borrando imágenes no usadas (opcional)..."
docker image prune -a -f

echo "🔧 Ajustando permisos de la carpeta 'fabric' para Docker..."
# Cambia propietario de la carpeta fabric a tu usuario
sudo chown -R $USER:$USER ~/coldchain-prototipo/fabric

# Permisos recursivos de lectura/escritura para usuario y lectura para grupo y otros
chmod -R u+rwX ~/coldchain-prototipo/fabric/network/organizations

# Ajuste específico para los archivos *_sk que Docker necesita leer
find ~/coldchain-prototipo/fabric/network/organizations -type f -name "*_sk" -exec chmod 644 {} \;

echo "🔓 Abriendo permisos completos para todos los archivos y carpetas dentro de 'fabric'..."
chmod -R 777 ~/coldchain-prototipo/fabric

echo "✅ Reset completo realizado. Ahora podés levantar la red con ./start.sh"

