'use strict';

const { Contract } = require('fabric-contract-api');

const MIN_TEMP = 2;
const MAX_TEMP = 8;

class TempValidatorContract extends Contract {

    async InitLedger(ctx) {
        console.log('Ledger initialized');
        // Inicializar estado global si no existe
        const estadoBytes = await ctx.stub.getState("EstadoContrato");
        if (!estadoBytes || estadoBytes.length === 0) {
            await ctx.stub.putState("EstadoContrato", Buffer.from("success"));
        }
    }

    async ValidarLectura(ctx, lecturaID, lecturaJSON) {
        const lectura = JSON.parse(lecturaJSON);

        // Guardar la lectura (aunque la temperatura falle, se registra siempre)
        await ctx.stub.putState(lecturaID, Buffer.from(lecturaJSON));

        // Validar temperatura
        const tempValida = lectura.temperature >= MIN_TEMP && lectura.temperature <= MAX_TEMP;

        // Obtener estado actual del contrato
        let estadoContratoBytes = await ctx.stub.getState("EstadoContrato");
        let estadoContrato = estadoContratoBytes && estadoContratoBytes.length > 0
            ? estadoContratoBytes.toString()
            : "success";

        // Actualizar estado global del contrato según reglas
        if (!tempValida && estadoContrato === "success") {
            await ctx.stub.putState("EstadoContrato", Buffer.from("fail"));
            estadoContrato = "fail";
        }
        // Si ya está en fail y llega un nuevo fail, se mantiene igual
        // Si la temperatura es válida y el estado está en fail, no se cambia automáticamente

        return `Lectura ${lecturaID} validada. Estado contrato: ${estadoContrato}`;
    }

    async GetLectura(ctx, lecturaID) {
        const lecturaBytes = await ctx.stub.getState(lecturaID);
        if (!lecturaBytes || lecturaBytes.length === 0) {
            throw new Error(`La lectura con ID ${lecturaID} no existe`);
        }
        return lecturaBytes.toString();
    }

    async GetHistorialLectura(ctx, lecturaID) {
        const iterator = await ctx.stub.getHistoryForKey(lecturaID);
        const resultados = [];
        while (true) {
            const res = await iterator.next();
            if (res.value && res.value.value.toString()) {
                resultados.push({
                    txId: res.value.txId,
                    timestamp: res.value.timestamp,
                    isDelete: res.value.isDelete,
                    value: res.value.value.toString('utf8')
                });
            }
            if (res.done) {
                await iterator.close();
                break;
            }
        }
        return JSON.stringify(resultados);
    }

    async GetEstadoContrato(ctx) {
        const estadoBytes = await ctx.stub.getState("EstadoContrato");
        if (!estadoBytes || estadoBytes.length === 0) {
            return "success"; // estado por defecto si no existe
        }
        return estadoBytes.toString();
    }

    // Opcional: función para resetear estado a success
    async ResetEstadoContrato(ctx) {
        await ctx.stub.putState("EstadoContrato", Buffer.from("success"));
        return "Estado contrato reseteado a success";
    }
}

module.exports.contracts = [TempValidatorContract];
