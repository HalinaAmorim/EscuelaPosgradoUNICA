package com.escuelaposgrado.Matricula.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.escuelaposgrado.Matricula.dto.common.MessageResponse;
import com.escuelaposgrado.Matricula.dto.request.PeriodoAcademicoRequest;
import com.escuelaposgrado.Matricula.dto.response.PeriodoAcademicoResponse;
import com.escuelaposgrado.Matricula.service.PeriodoAcademicoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de Períodos Académicos
 */
@Tag(name = "📅 Períodos Académicos", 
     description = "Gestión de períodos académicos para habilitar procesos de matrícula")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/periodos-academicos")
public class PeriodoAcademicoController {

    private final PeriodoAcademicoService periodoAcademicoService;

    public PeriodoAcademicoController(PeriodoAcademicoService periodoAcademicoService) {
        this.periodoAcademicoService = periodoAcademicoService;
    }

    @Operation(
        summary = "📋 Listar todos los períodos académicos",
        description = "Obtiene todos los períodos académicos del sistema (activos e inactivos)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso prohibido")
    })
    @GetMapping
    public ResponseEntity<List<PeriodoAcademicoResponse>> getAllPeriodos() {
        List<PeriodoAcademicoResponse> periodos = periodoAcademicoService.findAll();
        return ResponseEntity.ok(periodos);
    }

    @Operation(
        summary = "📋 Listar períodos académicos activos",
        description = "Obtiene solo los períodos académicos activos del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso prohibido")
    })
    @GetMapping("/activos")
    public ResponseEntity<List<PeriodoAcademicoResponse>> getPeriodosActivos() {
        List<PeriodoAcademicoResponse> periodos = periodoAcademicoService.findActivos();
        return ResponseEntity.ok(periodos);
    }

    @Operation(
        summary = "🔍 Obtener período académico por ID",
        description = "Busca un período académico específico por su identificador"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Período encontrado"),
        @ApiResponse(responseCode = "404", description = "Período no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PeriodoAcademicoResponse> getPeriodoById(
            @Parameter(description = "ID del período académico", required = true)
            @PathVariable Long id) {
        PeriodoAcademicoResponse periodo = periodoAcademicoService.findById(id);
        return ResponseEntity.ok(periodo);
    }

    @Operation(
        summary = "📅 Obtener períodos habilitados para matrícula",
        description = "Lista todos los períodos académicos actualmente habilitados para el proceso de matrícula"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/habilitados")
    public ResponseEntity<List<PeriodoAcademicoResponse>> getPeriodosHabilitados() {
        List<PeriodoAcademicoResponse> periodos = periodoAcademicoService.findHabilitados();
        return ResponseEntity.ok(periodos);
    }

    @Operation(
        summary = "➕ Crear nuevo período académico",
        description = "Crea un nuevo período académico en el sistema. Requiere rol ADMIN o COORDINADOR."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Período creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso prohibido")
    })
    @PostMapping
    public ResponseEntity<PeriodoAcademicoResponse> createPeriodo(
            @Parameter(description = "Datos del período académico", required = true)
            @Valid @RequestBody PeriodoAcademicoRequest request) {
        PeriodoAcademicoResponse periodo = periodoAcademicoService.create(request);
        return new ResponseEntity<>(periodo, HttpStatus.CREATED);
    }

    @Operation(
        summary = "✏️ Actualizar período académico",
        description = "Actualiza los datos de un período académico existente. Requiere rol ADMIN o COORDINADOR."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Período actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Período no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PeriodoAcademicoResponse> updatePeriodo(
            @Parameter(description = "ID del período académico", required = true)
            @PathVariable Long id,
            @Parameter(description = "Datos actualizados del período", required = true)
            @Valid @RequestBody PeriodoAcademicoRequest request) {
        PeriodoAcademicoResponse periodo = periodoAcademicoService.update(id, request);
        return ResponseEntity.ok(periodo);
    }

    @Operation(
        summary = "🗑️ Eliminar período académico",
        description = "Realiza eliminación lógica de un período académico. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Período eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Período no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso prohibido")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deletePeriodo(
            @Parameter(description = "ID del período académico", required = true)
            @PathVariable Long id) {
        periodoAcademicoService.delete(id);
        return ResponseEntity.ok(MessageResponse.success("Período académico eliminado exitosamente"));
    }

    @Operation(
        summary = "🔄 Habilitar/Deshabilitar período para matrícula",
        description = "Cambia el estado de habilitación de un período académico para el proceso de matrícula"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Período no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, 
                 allowCredentials = "true")
    @PostMapping("/periodos-toggle")
    public ResponseEntity<PeriodoAcademicoResponse> toggleHabilitado(
            @Parameter(description = "ID del período académico", required = true)
            @RequestParam Long id) {
        PeriodoAcademicoResponse periodo = periodoAcademicoService.toggleHabilitado(id);
        return ResponseEntity.ok(periodo);
    }

    @Operation(
        summary = "♻️ Reactivar período académico",
        description = "Reactiva un período académico previamente desactivado (eliminado lógicamente)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Período reactivado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Período no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso prohibido")
    })
    @CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, 
                 allowCredentials = "true")
    @PostMapping("/periodos-reactivar")
    public ResponseEntity<PeriodoAcademicoResponse> reactivarPeriodo(
            @Parameter(description = "ID del período académico", required = true)
            @RequestParam Long id) {
        PeriodoAcademicoResponse periodo = periodoAcademicoService.reactivar(id);
        return ResponseEntity.ok(periodo);
    }
}
