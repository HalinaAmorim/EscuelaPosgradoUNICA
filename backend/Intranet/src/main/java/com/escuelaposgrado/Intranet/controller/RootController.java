package com.escuelaposgrado.Intranet.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.escuelaposgrado.Intranet.dto.response.ApiInfoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador raíz para información básica de la API de Intranet.
 */
@Tag(
    name = "🏠 API Info",
    description = "Información general de la API de Intranet"
)
@RestController
@RequestMapping("/")
public class RootController {

    @Operation(
        summary = "📋 Información de la API",
        description = "Endpoint público que proporciona información básica sobre la API de Intranet"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Información obtenida correctamente"
        )
    })
    @GetMapping
    public ResponseEntity<ApiInfoResponse> getApiInfo() {

        Map<String, String> links = Map.of(
            "health", "/health/status",
            "info", "/health/info",
            "ping", "/health/ping",
            "swagger", "/swagger-ui.html",
            "api-docs", "/v3/api-docs"
        );

        Map<String, String> endpoints = Map.of(
            "authentication", "/api/auth",
            "users", "/api/usuarios",
            "attendance", "/api/asistencias",
            "grades", "/api/calificaciones",
            "surveys", "/api/encuestas"
        );

        ApiInfoResponse apiInfo = new ApiInfoResponse(
            "API Intranet - Escuela de Posgrado UNICA",
            "1.0.0",
            "API REST para el sistema de intranet académica",
            LocalDateTime.now(),
            8081,
            "running",
            links,
            endpoints
        );

        return ResponseEntity.ok(apiInfo);
    }
}