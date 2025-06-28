'use strict';

const { Contract } = require('fabric-contract-api');

class TempValidatorContract extends Contract {

    async InitLedger(ctx) {
        console.log('Ledger initialized');
    }

    async ValidarLectura(ctx, id, lecturaJSON) {
        const lectura = JSON.parse(lecturaJSON);

        // Validar temperatura
        if (lectura.temperatura < 2 || lectura.temperatura > 8) {
            throw new Error('Temperatura fuera de rango permitido');
        }

        // Guardar el estado en world state
        await ctx.stub.putState(id, Buffer.from(lecturaJSON));

        // Retornar algún resultado o simplemente éxito
        return `Lectura ${id} validada correctamente`;
    }
}

module.exports = TempValidatorContract;
