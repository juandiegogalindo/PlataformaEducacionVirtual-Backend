package com.bitcriollo.plataforma.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of(
                "estado", "activo",
                "proyecto", "Plataforma de Educacion Virtual",
                "equipo", "Bit Criollo"
        );
    }

}
