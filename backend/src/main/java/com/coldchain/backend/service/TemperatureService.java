package com.coldchain.backend.service;

import org.springframework.stereotype.Service;

@Service
public class TemperatureService {

    public String validarStatus(Double temperature) {
        if (temperature == null) return "fail";

        if (temperature < -5 || temperature > 10) {
            return "fail";
        } else if ((temperature > -5 && temperature < -2) || (temperature > 7 && temperature < 10)) {
            return "warning";
        } else if (temperature >= -2 && temperature <= 7) {
            return "ok";
        }
        return "fail";
    }
}
