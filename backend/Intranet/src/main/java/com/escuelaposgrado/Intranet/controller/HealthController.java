package com.escuelaposgrado.Intranet.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para verificación de salud del microservicio de Intranet.
 */
@Tag(
    name = "🏥 Health Check",
    description = "Endpoints para verificar el estado del microservicio de Intranet"
)
@RestController
@RequestMapping("/health")
public class HealthController {

    @Operation(
        summary = "🩺 Verificar estado del servicio",
        description = "Endpoint público para verificar que el microservicio de Intranet está funcionando correctamente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Servicio funcionando correctamente"
        )
    })
    @GetMapping("/status")
    public ResponseEntity<HealthStatusResponse> getStatus() {

        HealthStatusResponse status = new HealthStatusResponse(
            "Microservicio de Intranet",
            "UP",
            LocalDateTime.now(),
            "1.0.0",
            "Sistema de intranet para la Escuela de Posgrado UNICA",
            8081,
            new String[]{
                "/api/auth",
                "/api/usuarios",
                "/api/asistencias",
                "/api/calificaciones",
                "/api/encuestas",
                "/health"
            }
        );

        return ResponseEntity.ok(status);
    }

    @Operation(
        summary = "📊 Información detallada del servicio",
        description = "Proporciona información detallada sobre el microservicio de Intranet y sus capacidades"
    )
    @GetMapping("/info")
    public ResponseEntity<HealthInfoResponse> getInfo() {

        Map<String, String> features = Map.of(
            "authentication", "JWT Token-based",
            "database", "PostgreSQL",
            "security", "Spring Security",
            "documentation", "Swagger/OpenAPI 3"
        );

        Map<String, String> endpoints = Map.of(
            "auth", "/api/auth - Autenticación y autorización",
            "users", "/api/usuarios - Gestión de usuarios",
            "attendance", "/api/asistencias - Control de asistencia",
            "grades", "/api/calificaciones - Gestión de calificaciones",
            "surveys", "/api/encuestas - Sistema de encuestas",
            "health", "/health - Estado del servicio",
            "swagger", "/swagger-ui.html - Documentación API"
        );

        HealthInfoResponse info = new HealthInfoResponse(
            "Intranet Escuela de Posgrado UNICA",
            "Microservicio para gestión de intranet académica",
            "1.0.0",
            LocalDateTime.now(),
            features,
            endpoints
        );

        return ResponseEntity.ok(info);
    }

    /**
     * Endpoint para verificar conectividad básica.
     */
    @Operation(
        summary = "🔗 Test de conectividad",
        description = "Endpoint simple para verificar que el servicio responde"
    )
    @GetMapping("/ping")
    public ResponseEntity<PingResponse> ping() {

        PingResponse response = new PingResponse(
            "pong",
            "Intranet",
            LocalDateTime.now().toString()
        );

        return ResponseEntity.ok(response);
    }
}