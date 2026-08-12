package com.escuelaposgrado.Autenticacion.controller;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.escuelaposgrado.Autenticacion.config.CorsOrigins;
import com.escuelaposgrado.Autenticacion.dto.response.MessageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Health checks e información del microservicio.
 */
@Tag(name = "Salud del Sistema", description = "Endpoints para verificar el estado y salud del microservicio")
@CrossOrigin(origins = {CorsOrigins.LOCALHOST, CorsOrigins.LOCALHOST_IP},
             allowCredentials = "true", maxAge = 3600)
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;
    private final BuildProperties buildProperties;

    public HealthController(DataSource dataSource, ObjectProvider<BuildProperties> buildPropertiesProvider) {
        this.dataSource = dataSource;
        this.buildProperties = buildPropertiesProvider.getIfAvailable();
    }

    @Operation(
            summary = "Estado básico del servicio",
            description = "Verifica que el microservicio esté funcionando correctamente",
            tags = {"Salud del Sistema"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio funcionando correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = @ExampleObject(
                                    name = "Servicio activo",
                                    value = """
                                            {
                                              "message": "Microservicio de Autenticación - ACTIVO",
                                              "success": true
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/status")
    public ResponseEntity<MessageResponse> getStatus() {
        return ResponseEntity.ok(new MessageResponse("Microservicio de Autenticación - ACTIVO"));
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        try {
            health.put("database", isDatabaseUp() ? "UP" : "DOWN");
            health.put("service", "Autenticación");
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());
            putBuildInfo(health);
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Microservicio de Autenticación");
        info.put("description", "Sistema de autenticación y autorización para la Escuela de Posgrado UNICA");
        info.put("version", "1.0.0");
        info.put("institution", "Universidad Nacional San Luis Gonzaga de Ica");
        info.put("java_version", System.getProperty("java.version"));
        info.put("spring_profiles", System.getProperty("spring.profiles.active", "default"));
        return ResponseEntity.ok(info);
    }

    private boolean isDatabaseUp() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        }
    }

    private void putBuildInfo(Map<String, Object> health) {
        if (buildProperties == null) {
            return;
        }
        health.put("version", buildProperties.getVersion());
        health.put("buildTime", buildProperties.getTime());
    }
}
