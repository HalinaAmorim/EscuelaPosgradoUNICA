package com.escuelaposgrado.Intranet.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Intranet.dto.EncuestaDTO;
import com.escuelaposgrado.Intranet.dto.EstadisticasPreguntaDTO;
import com.escuelaposgrado.Intranet.dto.PreguntaEncuestaDTO;
import com.escuelaposgrado.Intranet.dto.ResultadosEncuestaDTO;
import com.escuelaposgrado.Intranet.model.Encuesta;
import com.escuelaposgrado.Intranet.model.Materia;
import com.escuelaposgrado.Intranet.model.PreguntaEncuesta;
import com.escuelaposgrado.Intranet.model.RespuestaEncuesta;
import com.escuelaposgrado.Intranet.model.TipoEncuesta;
import com.escuelaposgrado.Intranet.model.TipoPregunta;
import com.escuelaposgrado.Intranet.model.Usuario;
import com.escuelaposgrado.Intranet.repository.EncuestaRepository;
import com.escuelaposgrado.Intranet.repository.MateriaRepository;
import com.escuelaposgrado.Intranet.repository.PreguntaEncuestaRepository;
import com.escuelaposgrado.Intranet.repository.RespuestaEncuestaRepository;
import com.escuelaposgrado.Intranet.repository.UsuarioRepository;
import com.escuelaposgrado.Intranet.service.exception.RecursoNaoEncontradoException;

@Service
@Transactional
public class EncuestaService {

    private static final String ENCUESTA_NO_ENCONTRADA =
            "Encuesta no encontrada";

    private static final String MATERIA_NO_ENCONTRADA =
            "Materia no encontrada";

    private static final String USUARIO_NO_ENCONTRADO =
            "Usuario no encontrado";

    private static final String PREGUNTA_NO_ENCONTRADA =
            "Pregunta no encontrada";

    private final EncuestaRepository encuestaRepository;
    private final PreguntaEncuestaRepository preguntaRepository;
    private final RespuestaEncuestaRepository respuestaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MateriaRepository materiaRepository;

    public EncuestaService(
            EncuestaRepository encuestaRepository,
            PreguntaEncuestaRepository preguntaRepository,
            RespuestaEncuestaRepository respuestaRepository,
            UsuarioRepository usuarioRepository,
            MateriaRepository materiaRepository) {

        this.encuestaRepository = encuestaRepository;
        this.preguntaRepository = preguntaRepository;
        this.respuestaRepository = respuestaRepository;
        this.usuarioRepository = usuarioRepository;
        this.materiaRepository = materiaRepository;
    }

    /**
     * Crear nueva encuesta.
     */
    public EncuestaDTO crearEncuesta(
            EncuestaDTO encuestaDTO,
            String creadoPor) {

        Encuesta encuesta = new Encuesta();

        encuesta.setTitulo(encuestaDTO.getTitulo());
        encuesta.setDescripcion(encuestaDTO.getDescripcion());
        encuesta.setTipo(
                TipoEncuesta.valueOf(encuestaDTO.getTipo())
        );
        encuesta.setFechaInicio(
                encuestaDTO.getFechaInicio()
        );
        encuesta.setFechaFin(
                encuestaDTO.getFechaFin()
        );

        encuesta.setActiva(
                encuestaDTO.getActiva() != null
                        ? encuestaDTO.getActiva()
                        : true
        );

        encuesta.setAnonima(
                encuestaDTO.getAnonima() != null
                        ? encuestaDTO.getAnonima()
                        : false
        );

        encuesta.setCreadoPor(creadoPor);

        asociarMateria(
                encuesta,
                encuestaDTO.getMateriaId()
        );

        return convertirADTO(
                encuestaRepository.save(encuesta)
        );
    }

    /**
     * Actualizar encuesta existente.
     */
    public EncuestaDTO actualizarEncuesta(
            Long id,
            EncuestaDTO encuestaDTO) {

        Encuesta encuesta = buscarEncuesta(id);

        encuesta.setTitulo(encuestaDTO.getTitulo());
        encuesta.setDescripcion(encuestaDTO.getDescripcion());
        encuesta.setTipo(
                TipoEncuesta.valueOf(encuestaDTO.getTipo())
        );
        encuesta.setFechaInicio(
                encuestaDTO.getFechaInicio()
        );
        encuesta.setFechaFin(
                encuestaDTO.getFechaFin()
        );
        encuesta.setActiva(encuestaDTO.getActiva());
        encuesta.setAnonima(encuestaDTO.getAnonima());

        return convertirADTO(
                encuestaRepository.save(encuesta)
        );
    }

    /**
     * Agregar pregunta a encuesta.
     */
    public PreguntaEncuestaDTO agregarPregunta(
            Long encuestaId,
            PreguntaEncuestaDTO preguntaDTO) {

        Encuesta encuesta = buscarEncuesta(encuestaId);

        PreguntaEncuesta pregunta =
                new PreguntaEncuesta();

        pregunta.setEncuesta(encuesta);
        pregunta.setTexto(preguntaDTO.getTexto());
        pregunta.setTipo(
                TipoPregunta.valueOf(
                        preguntaDTO.getTipo()
                )
        );
        pregunta.setOrden(
                preguntaDTO.getOrden()
        );

        pregunta.setObligatoria(
                preguntaDTO.getObligatoria() != null
                        ? preguntaDTO.getObligatoria()
                        : true
        );

        pregunta.setOpciones(
                preguntaDTO.getOpciones()
        );

        return convertirPreguntaADTO(
                preguntaRepository.save(pregunta)
        );
    }

    /**
     * Responder encuesta.
     */
    public void responderEncuesta(
            Long encuestaId,
            Long usuarioId,
            Map<Long, String> respuestas) {

        Encuesta encuesta =
                buscarEncuesta(encuestaId);

        Usuario usuario =
                buscarUsuario(usuarioId);

        validarEncuestaParaRespuesta(
                encuesta,
                usuario
        );

        respuestas.forEach(
                (preguntaId, respuesta) ->
                        guardarRespuesta(
                                encuesta,
                                usuario,
                                preguntaId,
                                respuesta
                        )
        );
    }

    /**
     * Obtener encuestas activas.
     */
    @Transactional(readOnly = true)
    public List<EncuestaDTO> obtenerEncuestasActivas() {

        return encuestaRepository
                .findEncuestasActivas()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener encuestas por tipo.
     */
    @Transactional(readOnly = true)
    public List<EncuestaDTO> obtenerEncuestasPorTipo(
            String tipo) {

        return encuestaRepository
                .findByTipo(
                        TipoEncuesta.valueOf(tipo)
                )
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener encuestas por materia.
     */
    @Transactional(readOnly = true)
    public List<EncuestaDTO> obtenerEncuestasPorMateria(
            Long materiaId) {

        Materia materia =
                buscarMateria(materiaId);

        return encuestaRepository
                .findByMateria(materia)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener preguntas de una encuesta.
     */
    @Transactional(readOnly = true)
    public List<PreguntaEncuestaDTO> obtenerPreguntasEncuesta(
            Long encuestaId) {

        Encuesta encuesta =
                buscarEncuesta(encuestaId);

        return preguntaRepository
                .findByEncuestaOrderByOrden(encuesta)
                .stream()
                .map(this::convertirPreguntaADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener resultados de encuesta.
     */
    @Transactional(readOnly = true)
    public ResultadosEncuestaDTO obtenerResultadosEncuesta(
            Long encuestaId) {

        Encuesta encuesta =
                buscarEncuesta(encuestaId);

        List<RespuestaEncuesta> respuestas =
                respuestaRepository.findByEncuesta(encuesta);

        List<PreguntaEncuesta> preguntas =
                preguntaRepository
                        .findByEncuestaOrderByOrden(encuesta);

        Map<Long, List<RespuestaEncuesta>>
                respuestasPorPregunta =
                respuestas.stream()
                        .collect(
                                Collectors.groupingBy(
                                        respuesta ->
                                                respuesta
                                                        .getPregunta()
                                                        .getId()
                                )
                        );

        Map<Long, EstadisticasPreguntaDTO>
                estadisticas =
                preguntas.stream()
                        .collect(
                                Collectors.toMap(
                                        PreguntaEncuesta::getId,
                                        pregunta ->
                                                crearEstadisticas(
                                                        pregunta,
                                                        respuestasPorPregunta
                                                                .getOrDefault(
                                                                        pregunta.getId(),
                                                                        List.of()
                                                                )
                                                )
                                )
                        );

        ResultadosEncuestaDTO resultados =
                new ResultadosEncuestaDTO();

        resultados.setEncuestaId(
                encuesta.getId()
        );

        resultados.setEncuestaTitulo(
                encuesta.getTitulo()
        );

        resultados.setTotalRespuestas(
                (long) respuestas.size()
        );

        resultados.setEstadisticasPorPregunta(
                estadisticas
        );

        return resultados;
    }

    /**
     * Activar/Desactivar encuesta.
     */
    public void cambiarEstadoEncuesta(
            Long id,
            Boolean activa) {

        Encuesta encuesta =
                buscarEncuesta(id);

        encuesta.setActiva(
                Boolean.TRUE.equals(activa)
        );

        encuestaRepository.save(encuesta);
    }

    /**
     * Eliminar encuesta.
     */
    public void eliminarEncuesta(Long id) {

        if (!encuestaRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException(
                    ENCUESTA_NO_ENCONTRADA
            );
        }

        encuestaRepository.deleteById(id);
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private Encuesta buscarEncuesta(Long id) {

        return encuestaRepository
                .findById(id)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                ENCUESTA_NO_ENCONTRADA
                        )
                );
    }

    private Usuario buscarUsuario(Long id) {

        return usuarioRepository
                .findById(id)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                USUARIO_NO_ENCONTRADO
                        )
                );
    }

    private Materia buscarMateria(Long id) {

        return materiaRepository
                .findById(id)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                MATERIA_NO_ENCONTRADA
                        )
                );
    }

    private PreguntaEncuesta buscarPregunta(Long id) {

        return preguntaRepository
                .findById(id)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                PREGUNTA_NO_ENCONTRADA
                        )
                );
    }

    private void asociarMateria(
            Encuesta encuesta,
            Long materiaId) {

        if (materiaId == null) {
            return;
        }

        encuesta.setMateria(
                buscarMateria(materiaId)
        );
    }

    private void validarEncuestaParaRespuesta(
            Encuesta encuesta,
            Usuario usuario) {

        if (!Boolean.TRUE.equals(
                encuesta.getActiva())) {

            throw new IllegalStateException(
                    "La encuesta no está activa"
            );
        }

        validarPeriodo(encuesta);

        if (!Boolean.TRUE.equals(
                encuesta.getAnonima())
                && respuestaRepository
                        .existsByEncuestaAndUsuario(
                                encuesta,
                                usuario)) {

            throw new IllegalStateException(
                    "Ya has respondido esta encuesta"
            );
        }
    }

    private void validarPeriodo(
            Encuesta encuesta) {

        LocalDate hoy = LocalDate.now();

        boolean fueraDelPeriodo =
                hoy.isBefore(
                        encuesta.getFechaInicio()
                )
                || hoy.isAfter(
                        encuesta.getFechaFin()
                );

        if (fueraDelPeriodo) {
            throw new IllegalStateException(
                    "La encuesta no está en el periodo válido"
            );
        }
    }

    private void guardarRespuesta(
            Encuesta encuesta,
            Usuario usuario,
            Long preguntaId,
            String valor) {

        PreguntaEncuesta pregunta =
                buscarPregunta(preguntaId);

        if (!pregunta
                .getEncuesta()
                .getId()
                .equals(encuesta.getId())) {

            throw new IllegalArgumentException(
                    "La pregunta no pertenece a esta encuesta"
            );
        }

        RespuestaEncuesta respuesta =
                new RespuestaEncuesta();

        respuesta.setEncuesta(encuesta);
        respuesta.setPregunta(pregunta);
        respuesta.setRespuesta(valor);

        if (!Boolean.TRUE.equals(
                encuesta.getAnonima())) {

            respuesta.setUsuario(usuario);
        }

        respuestaRepository.save(respuesta);
    }

    private EstadisticasPreguntaDTO crearEstadisticas(
            PreguntaEncuesta pregunta,
            List<RespuestaEncuesta> respuestas) {

        EstadisticasPreguntaDTO estadisticas =
                new EstadisticasPreguntaDTO();

        estadisticas.setPreguntaId(
                pregunta.getId()
        );

        estadisticas.setPreguntaTexto(
                pregunta.getTexto()
        );

        estadisticas.setTipoPregunta(
                pregunta.getTipo().name()
        );

        estadisticas.setTotalRespuestas(
                (long) respuestas.size()
        );

        estadisticas.setConteoRespuestas(
                respuestas.stream()
                        .collect(
                                Collectors.groupingBy(
                                        RespuestaEncuesta::getRespuesta,
                                        Collectors.counting()
                                )
                        )
        );

        return estadisticas;
    }

    private EncuestaDTO convertirADTO(
            Encuesta encuesta) {

        EncuestaDTO dto =
                new EncuestaDTO();

        dto.setId(encuesta.getId());
        dto.setTitulo(encuesta.getTitulo());
        dto.setDescripcion(
                encuesta.getDescripcion()
        );
        dto.setTipo(
                encuesta.getTipo().name()
        );
        dto.setFechaInicio(
                encuesta.getFechaInicio()
        );
        dto.setFechaFin(
                encuesta.getFechaFin()
        );
        dto.setActiva(
                encuesta.getActiva()
        );
        dto.setAnonima(
                encuesta.getAnonima()
        );
        dto.setCreadoPor(
                encuesta.getCreadoPor()
        );

        if (encuesta.getMateria() != null) {

            dto.setMateriaId(
                    encuesta.getMateria().getId()
            );

            dto.setMateriaNombre(
                    encuesta.getMateria().getNombre()
            );
        }

        return dto;
    }

    private PreguntaEncuestaDTO convertirPreguntaADTO(
            PreguntaEncuesta pregunta) {

        PreguntaEncuestaDTO dto =
                new PreguntaEncuestaDTO();

        dto.setId(
                pregunta.getId()
        );

        dto.setEncuestaId(
                pregunta.getEncuesta().getId()
        );

        dto.setTexto(
                pregunta.getTexto()
        );

        dto.setTipo(
                pregunta.getTipo().name()
        );

        dto.setOrden(
                pregunta.getOrden()
        );

        dto.setObligatoria(
                pregunta.getObligatoria()
        );

        dto.setOpciones(
                pregunta.getOpciones()
        );

        return dto;
    }
}