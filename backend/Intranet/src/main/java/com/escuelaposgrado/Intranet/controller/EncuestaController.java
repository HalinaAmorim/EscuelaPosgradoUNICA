package com.escuelaposgrado.Intranet.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.escuelaposgrado.Intranet.dto.EncuestaDTO;
import com.escuelaposgrado.Intranet.dto.PreguntaEncuestaDTO;
import com.escuelaposgrado.Intranet.dto.UsuarioDTO;
import com.escuelaposgrado.Intranet.security.jwt.JwtUtils;
import com.escuelaposgrado.Intranet.service.EncuestaService;
import com.escuelaposgrado.Intranet.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de encuestas.
 */
@RestController
@RequestMapping("/api/encuestas")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EncuestaController {

    @Autowired
    private EncuestaService encuestaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Crear nueva encuesta.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDINADOR')")
    public ResponseEntity<EncuestaDTO> crearEncuesta(
            @Valid @RequestBody EncuestaDTO encuestaDTO,
            @RequestHeader("Authorization") String token) {

        String jwt = token.substring(7);
        String username = jwtUtils.getUserNameFromJwtToken(jwt);

        EncuestaDTO nuevaEncuesta = encuestaService.crearEncuesta(encuestaDTO, username);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevaEncuesta);
    }

    /**
     * Actualizar encuesta.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDINADOR')")
    public ResponseEntity<EncuestaDTO> actualizarEncuesta(
            @PathVariable Long id,
            @Valid @RequestBody EncuestaDTO encuestaDTO) {

        EncuestaDTO encuestaActualizada = encuestaService.actualizarEncuesta(id, encuestaDTO);

        return ResponseEntity.ok(encuestaActualizada);
    }

    /**
     * Agregar pregunta a encuesta.
     */
    @PostMapping("/{encuestaId}/preguntas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDINADOR')")
    public ResponseEntity<PreguntaEncuestaDTO> agregarPregunta(
            @PathVariable Long encuestaId,
            @Valid @RequestBody PreguntaEncuestaDTO preguntaDTO) {

        PreguntaEncuestaDTO nuevaPregunta = encuestaService.agregarPregunta(encuestaId, preguntaDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevaPregunta);
    }

    /**
     * Responder encuesta.
     */
    @PostMapping("/{encuestaId}/responder")
    @PreAuthorize("hasRole('ALUMNO') or hasRole('DOCENTE')")
    public ResponseEntity<MensajeResponse> responderEncuesta(
            @PathVariable Long encuestaId,
            @RequestBody Map<Long, String> respuestas,
            @RequestHeader("Authorization") String token) {

        String jwt = token.substring(7);
        String username = jwtUtils.getUserNameFromJwtToken(jwt);

        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(username);

        Long usuarioId = usuario.getId();

        encuestaService.responderEncuesta(
                encuestaId,
                usuarioId,
                respuestas);

        return ResponseEntity.ok(
                new MensajeResponse(
                        "Encuesta respondida correctamente"));
    }

    /**
     * Obtener encuestas activas.
     */
    @GetMapping("/activas")
    @PreAuthorize("hasRole('ALUMNO') or hasRole('DOCENTE') or hasRole('COORDINADOR') or hasRole('ADMIN')")
    public ResponseEntity<List<EncuestaDTO>> obtenerEncuestasActivas() {

        List<EncuestaDTO> encuestas = encuestaService.obtenerEncuestasActivas();

        return ResponseEntity.ok(encuestas);
    }

    /**
     * Obtener encuestas por tipo.
     */
    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDINADOR') or hasRole('DOCENTE')")
    public ResponseEntity<List<EncuestaDTO>> obtenerEncuestasPorTipo(
            @PathVariable String tipo) {

        List<EncuestaDTO> encuestas = encuestaService.obtenerEncuestasPorTipo(tipo);

        return ResponseEntity.ok(encuestas);
    }

    /**
     * Obtener encuestas por materia.
     */
    @GetMapping("/materia/{materiaId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDINADOR') or hasRole('DOCENTE')")
    public ResponseEntity<List<EncuestaDTO>> obtenerEncuestasPorMateria(
            @PathVariable Long materiaId) {

        List<EncuestaDTO> encuestas = encuestaService.obtenerEncuestasPorMateria(materiaId);

        return ResponseEntity.ok(encuestas);
    }

    /**
     * Obtener preguntas de una encuesta.
     */
    @GetMapping("/{encuestaId}/preguntas")
    @PreAuthorize("hasRole('ALUMNO') or hasRole('DOCENTE') or hasRole('COORDINADOR') or hasRole('ADMIN')")
    public ResponseEntity<List<PreguntaEncuestaDTO>> obtenerPreguntasEncuesta(
            @PathVariable Long encuestaId) {

        List<PreguntaEncuestaDTO> preguntas = encuestaService.obtenerPreguntasEncuesta(encuestaId);

        return ResponseEntity.ok(preguntas);
    }

    /**
     * Obtener resultados de encuesta.
     */
    @GetMapping("/{encuestaId}/resultados")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDINADOR')")
    public ResponseEntity<?> obtenerResultadosEncuesta(
            @PathVariable Long encuestaId) {

        var resultados = encuestaService.obtenerResultadosEncuesta(encuestaId);

        return ResponseEntity.ok(resultados);
    }

    /**
     * Activar/Desactivar encuesta.
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDINADOR')")
    public ResponseEntity<MensajeResponse> cambiarEstadoEncuesta(
            @PathVariable Long id,
            @RequestParam Boolean activa) {

        encuestaService.cambiarEstadoEncuesta(id, activa);

        String mensaje = activa
                ? "Encuesta activada"
                : "Encuesta desactivada";

        return ResponseEntity.ok(
                new MensajeResponse(mensaje + " correctamente"));
    }

    /**
     * Obtener todas las encuestas.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDINADOR')")
    public ResponseEntity<List<EncuestaDTO>> obtenerTodasLasEncuestas() {

        List<EncuestaDTO> encuestas = encuestaService.obtenerEncuestasActivas();

        return ResponseEntity.ok(encuestas);
    }

    /**
     * Eliminar encuesta.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeResponse> eliminarEncuesta(
            @PathVariable Long id) {

        encuestaService.eliminarEncuesta(id);

        return ResponseEntity.ok(
                new MensajeResponse(
                        "Encuesta eliminada correctamente"));
    }
}