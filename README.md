
# 🧊 ColdChain Prototipo - IoT + Blockchain

Este proyecto es un sistema completo de trazabilidad de la cadena de frío basado en tecnologías modernas: IoT para la captura de datos en tiempo real, WebSockets para la visualización, y **Hyperledger Fabric** para asegurar integridad e inmutabilidad mediante **smart contracts**.

## Tecnologías utilizadas

- **Java + Spring Boot** (backend)
- **React** (frontend)
- **PostgreSQL** (almacenamiento de lecturas)
- **Mosquitto MQTT** (broker de sensores)
- **Hyperledger Fabric** (blockchain privada)
- **Maven** (simulador de dispositivos)
- **Docker + Docker Compose** (orquestación)

---

## Arquitectura

```text
[Simulador IoT (Java)]
   │
   ▼ (MQTT: topic `sensors/mock`)
[Broker Mosquitto]
   │
   ▼
[Backend Spring Boot]
   ├─► Almacena en PostgreSQL
   ├─► Envía a dashboard vía WebSocket
   └─► Ejecuta Smart Contract (Hyperledger Fabric)
               │
               ▼
[Ledger privado - Fabric]
````

---

## Instrucciones de arranque

1. **Clona el repositorio**:

```bash
git clone https://github.com/dadrover75/coldchain-prototipo
cd coldchain-prototipo
```

2. **Dale permisos al script de arranque (una sola vez)**:

```bash
chmod +x start.sh
```

3. **Ejecutá el sistema completo**:

```bash
./start.sh
```

Este script realiza lo siguiente:

* \[1/5] Levanta la red de **Hyperledger Fabric**.
* \[2/5] Espera unos segundos para su estabilización.
* \[3/5] Despliega el **Chaincode** y lo aprueban las organizaciones.
* \[4/5] Inicia los servicios: `postgres`, `mosquitto`, `backend`, `frontend`.
* \[5/5] Compila y lanza el simulador de sensores que publica datos MQTT.

---

## Visualización

Una vez iniciado todo correctamente, abrí tu navegador en:

```
http://localhost:80
```

Allí verás un **dashboard en tiempo real** con:

* Tarjetas por cada dispositivo con la **temperatura actual**.
* Al hacer clic, podés ver el **historial gráfico** de lecturas.

---

## ¿Qué sucede por detrás?

Cada lectura del sensor:

1. Se recibe por MQTT en el backend.
2. Se valida a través del **smart contract** en Fabric.
3. Se **almacena inmutablemente en el ledger** como una transacción.
4. Se guarda también en la base de datos relacional (PostgreSQL).
5. Se envía en tiempo real por WebSocket al frontend.

---

## Simulador de sensores

El proyecto incluye un módulo `simulator` (Java + Maven) que:

* Publica valores de temperatura en el tópico `device/temperature`.
* Emula el comportamiento de sensores físicos para testing.

Podés ejecutarlo de forma independiente:

```bash
cd simulator
mvn clean package -DskipTests
java -jar target/simulator-*-jar-with-dependencies.jar
```

---

## Requisitos previos

* Docker y Docker Compose instalados.
* Java 17 y Maven instalados si querés correr el simulador fuera de contenedor.
* 4GB de RAM libres para levantar la red Fabric sin problemas.

---

## Próximos pasos

El sistema está diseñado para ser modular y escalable. Algunas mejoras planificadas incluyen:

* Integración de **alertas** por temperatura fuera de rango.
* Implementación de un **SDK de Hyperledger Fabric** en Java dentro del backend para invocar contratos directamente por GPRC, mejorando la integración y eliminando la dependencia de scripts.
* Mejoras en la **interfaz de usuario** para una experiencia más intuitiva.
* Login de usuarios y gestión de roles para mayor seguridad.
* Gestion de **dispositivos**  desde el frontend.
* Modulo de **reportes** para generar estadísticas de temperatura por dispositivo.

---

## Autora

Proyecto desarrollado por **Diana Adrover** para trazabilidad en la cadena de frío con enfoque en automatización agrícola e integración de datos.

---
