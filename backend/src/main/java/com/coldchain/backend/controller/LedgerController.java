package com.coldchain.backend.controller;

import com.coldchain.backend.service.FabricGatewayService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@ConditionalOnProperty(value = "fabric.gateway.enabled", havingValue = "true", matchIfMissing = true)
@RestController
@RequestMapping("/ledger")
public class LedgerController {

    private final FabricGatewayService fabricGatewayService;

    public LedgerController(FabricGatewayService fabricGatewayService) {
        this.fabricGatewayService = fabricGatewayService;
    }

    @GetMapping("/lectura/{id}")
    public String getLectura(@PathVariable("id") String id) {
        return fabricGatewayService.obtenerLectura(id);
    }

    @GetMapping("/lectura/{id}/historial")
    public String getHistorial(@PathVariable("id") String id) {
        return fabricGatewayService.obtenerHistorialLectura(id);
    }

    @GetMapping("/estado")
    public String getEstado() {
        return fabricGatewayService.obtenerEstadoContrato();
    }

    @PostMapping("/reset")
    public String resetEstado() {
        return fabricGatewayService.resetEstadoContrato();
    }

    @GetMapping("/health")
    public String health() {
        return fabricGatewayService.isReady() ? "READY" : "NOT_READY";
    }
}
