package com.escuelaposgrado.Intranet.service;

import com.escuelaposgrado.Intranet.dto.CalificacionDTO;
import com.escuelaposgrado.Intranet.dto.EstadisticasCalificacionDTO;
import com.escuelaposgrado.Intranet.dto.RankingEstudianteDTO;
import com.escuelaposgrado.Intranet.exception.BadRequestException;
import com.escuelaposgrado.Intranet.exception.ResourceNotFoundException;
import com.escuelaposgrado.Intranet.model.Calificacion;
import com.escuelaposgrado.Intranet.model.Ciclo;
import com.escuelaposgrado.Intranet.model.Materia;
import com.escuelaposgrado.Intranet.model.TipoEvaluacion;
import com.escuelaposgrado.Intranet.model.Usuario;
import com.escuelaposgrado.Intranet.repository.CalificacionRepository;
import com.escuelaposgrado.Intranet.repository.MateriaRepository;
import com.escuelaposgrado.Intranet.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para calificaciones académicas.
 */
@Service
@Transactional
public class CalificacionService {

    private static final String MSG_ESTUDIANTE_NO_ENCONTRADO = "Estudiante no encontrado";
    private static final String MSG_MATERIA_NO_ENCONTRADA = "Materia no encontrada";
    private static final String MSG_CALIFICACION_NO_ENCONTRADA = "Calificación no encontrada";
    private static final String MSG_CALIFICACION_DUPLICADA =
            "Ya existe una calificación de este tipo para esta fecha";
    private static final ZoneId ZONE_LIMA = ZoneId.of("America/Lima");
    private static final int NOTA_SCALE = 2;
    private static final int DEFAULT_RANKING_LIMIT = 10;

    private final CalificacionRepository calificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final MateriaRepository materiaRepository;

    public CalificacionService(CalificacionRepository calificacionRepository,
                               UsuarioRepository usuarioRepository,
                               MateriaRepository materiaRepository) {
        this.calificacionRepository = calificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.materiaRepository = materiaRepository;
    }

    public CalificacionDTO registrarCalificacion(CalificacionDTO calificacionDTO, String registradoPor) {
        Usuario estudiante = findEstudiante(calificacionDTO.getEstudianteId());
        Materia materia = findMateria(calificacionDTO.getMateriaId());
        assertNoDuplicateEvaluation(estudiante, materia, calificacionDTO);

        Calificacion calificacion = new Calificacion();
        applyCalificacionData(calificacion, calificacionDTO, estudiante, materia);
        calificacion.setRegistradoPor(registradoPor);

        return toDto(calificacionRepository.save(calificacion));
    }

    public CalificacionDTO actualizarCalificacion(Long id, CalificacionDTO calificacionDTO) {
        Calificacion calificacion = findCalificacion(id);
        applyCalificacionData(calificacion, calificacionDTO, calificacion.getEstudiante(), calificacion.getMateria());
        return toDto(calificacionRepository.save(calificacion));
    }

    public CalificacionDTO corregirCalificacion(Long id, BigDecimal nuevaNota, String motivoCorreccion, String corregidoPor) {
        Calificacion calificacion = findCalificacion(id);
        calificacion.setObservaciones(buildCorrectionHistory(
                calificacion.getObservaciones(), calificacion.getNota(), nuevaNota, motivoCorreccion, corregidoPor));
        calificacion.setNota(nuevaNota);
        return toDto(calificacionRepository.save(calificacion));
    }

    @Transactional(readOnly = true)
    public List<CalificacionDTO> obtenerCalificacionesPorEstudiante(Long estudianteId) {
        Usuario estudiante = findEstudiante(estudianteId);
        return mapAll(calificacionRepository.findByEstudiante(estudiante));
    }

    @Transactional(readOnly = true)
    public List<CalificacionDTO> obtenerCalificacionesPorMateria(Long materiaId) {
        Materia materia = findMateria(materiaId);
        return mapAll(calificacionRepository.findByMateria(materia));
    }

    @Transactional(readOnly = true)
    public List<CalificacionDTO> obtenerCalificacionesPorEstudianteYMateria(Long estudianteId, Long materiaId) {
        Usuario estudiante = findEstudiante(estudianteId);
        Materia materia = findMateria(materiaId);
        return mapAll(calificacionRepository.findByEstudianteAndMateria(estudiante, materia));
    }

    @Transactional(readOnly = true)
    public List<CalificacionDTO> obtenerCalificacionesPorCicloYAnio(String ciclo, Integer anio) {
        return mapAll(calificacionRepository.findByCicloAndAnio(Ciclo.valueOf(ciclo), anio));
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularPromedioEstudianteMateria(Long estudianteId, Long materiaId) {
        Usuario estudiante = findEstudiante(estudianteId);
        Materia materia = findMateria(materiaId);
        return scaleOrZero(calificacionRepository.calcularPromedioEstudianteMateria(estudiante, materia));
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularPromedioPonderado(Long estudianteId, Long materiaId) {
        Usuario estudiante = findEstudiante(estudianteId);
        Materia materia = findMateria(materiaId);
        return computeWeightedAverage(calificacionRepository.findByEstudianteAndMateria(estudiante, materia));
    }

    @Transactional(readOnly = true)
    public EstadisticasCalificacionDTO obtenerEstadisticasMateria(Long materiaId) {
        Materia materia = findMateria(materiaId);

        EstadisticasCalificacionDTO estadisticas = new EstadisticasCalificacionDTO();
        estadisticas.setMateriaId(materiaId);
        estadisticas.setMateriaNombre(materia.getNombre());
        estadisticas.setPromedio(scaleOrZero(calificacionRepository.calcularPromedioMateria(materia)));
        estadisticas.setNotaMaxima(nullToZero(calificacionRepository.obtenerNotaMaximaMateria(materia)));
        estadisticas.setNotaMinima(nullToZero(calificacionRepository.obtenerNotaMinimaMateria(materia)));
        estadisticas.setTotalCalificaciones(calificacionRepository.countCalificacionesMateria(materia));
        return estadisticas;
    }

    @Transactional(readOnly = true)
    public List<RankingEstudianteDTO> obtenerRankingEstudiantes(Long materiaId, Integer limite) {
        Materia materia = findMateria(materiaId);
        Map<Usuario, List<Calificacion>> byStudent = calificacionRepository.findByMateria(materia).stream()
                .collect(Collectors.groupingBy(Calificacion::getEstudiante));

        List<RankingEstudianteDTO> ranking = byStudent.entrySet().stream()
                .map(entry -> toRankingEntry(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(RankingEstudianteDTO::getPromedio).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

        assignPositions(ranking);
        int maxResults = limite != null ? limite : DEFAULT_RANKING_LIMIT;
        return ranking.stream().limit(maxResults).toList();
    }

    @Transactional(readOnly = true)
    public List<CalificacionDTO> obtenerCalificacionesPendientes() {
        return mapAll(calificacionRepository.findCalificacionesPendientes());
    }

    public void eliminarCalificacion(Long id) {
        if (!calificacionRepository.existsById(id)) {
            throw new ResourceNotFoundException(MSG_CALIFICACION_NO_ENCONTRADA);
        }
        calificacionRepository.deleteById(id);
    }

    private void assertNoDuplicateEvaluation(Usuario estudiante, Materia materia, CalificacionDTO dto) {
        boolean exists = calificacionRepository.existsByEstudianteAndMateriaAndTipoEvaluacionAndFechaEvaluacion(
                estudiante,
                materia,
                TipoEvaluacion.valueOf(dto.getTipoEvaluacion()),
                dto.getFechaEvaluacion());
        if (exists) {
            throw new BadRequestException(MSG_CALIFICACION_DUPLICADA);
        }
    }

    private BigDecimal computeWeightedAverage(List<Calificacion> calificaciones) {
        if (calificaciones.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal weightSum = BigDecimal.ZERO;

        for (Calificacion calificacion : calificaciones) {
            BigDecimal weight = calificacion.getPeso() != null ? calificacion.getPeso() : BigDecimal.ONE;
            weightedSum = weightedSum.add(calificacion.getNota().multiply(weight));
            weightSum = weightSum.add(weight);
        }

        if (weightSum.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return weightedSum.divide(weightSum, NOTA_SCALE, RoundingMode.HALF_UP);
    }

    private RankingEstudianteDTO toRankingEntry(Usuario estudiante, List<Calificacion> grades) {
        BigDecimal average = grades.stream()
                .map(Calificacion::getNota)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(grades.size()), NOTA_SCALE, RoundingMode.HALF_UP);

        RankingEstudianteDTO rankingDTO = new RankingEstudianteDTO();
        rankingDTO.setEstudianteId(estudiante.getId());
        rankingDTO.setEstudianteNombre(estudiante.getNombreCompleto());
        rankingDTO.setPromedio(average);
        return rankingDTO;
    }

    private void assignPositions(List<RankingEstudianteDTO> ranking) {
        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setPosicion(i + 1);
        }
    }

    private String buildCorrectionHistory(String currentObservations,
                                          BigDecimal previousGrade,
                                          BigDecimal newGrade,
                                          String reason,
                                          String correctedBy) {
        String previousNotes = currentObservations != null ? currentObservations : "";
        return String.format(
                "[CORRECCIÓN %s] Nota anterior: %s -> Nueva nota: %s. Motivo: %s. Corregido por: %s%n%s",
                LocalDate.now(ZONE_LIMA), previousGrade, newGrade, reason, correctedBy, previousNotes);
    }

    private void applyCalificacionData(Calificacion calificacion, CalificacionDTO dto,
                                       Usuario estudiante, Materia materia) {
        calificacion.setEstudiante(estudiante);
        calificacion.setMateria(materia);
        calificacion.setNota(dto.getNota());
        calificacion.setTipoEvaluacion(TipoEvaluacion.valueOf(dto.getTipoEvaluacion()));
        calificacion.setFechaEvaluacion(dto.getFechaEvaluacion());
        calificacion.setPeso(dto.getPeso());
        calificacion.setObservaciones(dto.getObservaciones());
        calificacion.setCiclo(Ciclo.valueOf(dto.getCiclo()));
        calificacion.setAnio(dto.getAnio());
    }

    private List<CalificacionDTO> mapAll(List<Calificacion> calificaciones) {
        return calificaciones.stream().map(this::toDto).toList();
    }

    private BigDecimal scaleOrZero(BigDecimal value) {
        return value != null ? value.setScale(NOTA_SCALE, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Usuario findEstudiante(Long estudianteId) {
        return usuarioRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_ESTUDIANTE_NO_ENCONTRADO));
    }

    private Materia findMateria(Long materiaId) {
        return materiaRepository.findById(materiaId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_MATERIA_NO_ENCONTRADA));
    }

    private Calificacion findCalificacion(Long id) {
        return calificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_CALIFICACION_NO_ENCONTRADA));
    }

    private CalificacionDTO toDto(Calificacion calificacion) {
        CalificacionDTO dto = new CalificacionDTO();
        dto.setId(calificacion.getId());
        dto.setEstudianteId(calificacion.getEstudiante().getId());
        dto.setMateriaId(calificacion.getMateria().getId());
        dto.setNota(calificacion.getNota());
        dto.setTipoEvaluacion(calificacion.getTipoEvaluacion().name());
        dto.setFechaEvaluacion(calificacion.getFechaEvaluacion());
        dto.setPeso(calificacion.getPeso());
        dto.setObservaciones(calificacion.getObservaciones());
        dto.setCiclo(calificacion.getCiclo().name());
        dto.setAnio(calificacion.getAnio());
        dto.setEstudianteNombre(calificacion.getEstudiante().getNombreCompleto());
        dto.setMateriaNombre(calificacion.getMateria().getNombre());
        dto.setRegistradoPor(calificacion.getRegistradoPor());
        return dto;
    }
}
