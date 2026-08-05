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

import org.springframework.beans.factory.annotation.Autowired;
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
 * Servicio para la gestión de calificaciones
 */
@Service
@Transactional
public class CalificacionService {

    private static final String MSG_ESTUDIANTE_NO_ENCONTRADO = "Estudiante no encontrado";
    private static final String MSG_MATERIA_NO_ENCONTRADA = "Materia no encontrada";
    private static final String MSG_CALIFICACION_NO_ENCONTRADA = "Calificación no encontrada";
    private static final ZoneId ZONE_LIMA = ZoneId.of("America/Lima");

    @Autowired
    private CalificacionRepository calificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    public CalificacionDTO registrarCalificacion(CalificacionDTO calificacionDTO, String registradoPor) {
        Usuario estudiante = findEstudiante(calificacionDTO.getEstudianteId());
        Materia materia = findMateria(calificacionDTO.getMateriaId());

        if (calificacionRepository.existsByEstudianteAndMateriaAndTipoEvaluacionAndFechaEvaluacion(
                estudiante, materia, TipoEvaluacion.valueOf(calificacionDTO.getTipoEvaluacion()),
                calificacionDTO.getFechaEvaluacion())) {
            throw new BadRequestException("Ya existe una calificación de este tipo para esta fecha");
        }

        Calificacion calificacion = new Calificacion();
        aplicarDatosCalificacion(calificacion, calificacionDTO, estudiante, materia);
        calificacion.setRegistradoPor(registradoPor);

        return convertirADTO(calificacionRepository.save(calificacion));
    }

    public CalificacionDTO actualizarCalificacion(Long id, CalificacionDTO calificacionDTO) {
        Calificacion calificacion = findCalificacion(id);
        aplicarDatosCalificacion(calificacion, calificacionDTO, calificacion.getEstudiante(), calificacion.getMateria());
        return convertirADTO(calificacionRepository.save(calificacion));
    }

    public CalificacionDTO corregirCalificacion(Long id, BigDecimal nuevaNota, String motivoCorreccion, String corregidoPor) {
        Calificacion calificacion = findCalificacion(id);

        String observacionesActuales = calificacion.getObservaciones() != null ? calificacion.getObservaciones() : "";
        String historialCorreccion = String.format(
                "[CORRECCIÓN %s] Nota anterior: %s -> Nueva nota: %s. Motivo: %s. Corregido por: %s%n%s",
                LocalDate.now(ZONE_LIMA), calificacion.getNota(), nuevaNota, motivoCorreccion, corregidoPor, observacionesActuales);

        calificacion.setNota(nuevaNota);
        calificacion.setObservaciones(historialCorreccion);

        return convertirADTO(calificacionRepository.save(calificacion));
    }

    @Transactional(readOnly = true)
    public List<CalificacionDTO> obtenerCalificacionesPorEstudiante(Long estudianteId) {
        Usuario estudiante = findEstudiante(estudianteId);
        return calificacionRepository.findByEstudiante(estudiante).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CalificacionDTO> obtenerCalificacionesPorMateria(Long materiaId) {
        Materia materia = findMateria(materiaId);
        return calificacionRepository.findByMateria(materia).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CalificacionDTO> obtenerCalificacionesPorEstudianteYMateria(Long estudianteId, Long materiaId) {
        Usuario estudiante = findEstudiante(estudianteId);
        Materia materia = findMateria(materiaId);
        return calificacionRepository.findByEstudianteAndMateria(estudiante, materia).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CalificacionDTO> obtenerCalificacionesPorCicloYAnio(String ciclo, Integer anio) {
        return calificacionRepository.findByCicloAndAnio(Ciclo.valueOf(ciclo), anio).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularPromedioEstudianteMateria(Long estudianteId, Long materiaId) {
        Usuario estudiante = findEstudiante(estudianteId);
        Materia materia = findMateria(materiaId);
        BigDecimal promedio = calificacionRepository.calcularPromedioEstudianteMateria(estudiante, materia);
        return promedio != null ? promedio.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularPromedioPonderado(Long estudianteId, Long materiaId) {
        Usuario estudiante = findEstudiante(estudianteId);
        Materia materia = findMateria(materiaId);
        List<Calificacion> calificaciones = calificacionRepository.findByEstudianteAndMateria(estudiante, materia);

        if (calificaciones.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sumaNotas = BigDecimal.ZERO;
        BigDecimal sumaPesos = BigDecimal.ZERO;

        for (Calificacion calificacion : calificaciones) {
            BigDecimal peso = calificacion.getPeso() != null ? calificacion.getPeso() : BigDecimal.ONE;
            sumaNotas = sumaNotas.add(calificacion.getNota().multiply(peso));
            sumaPesos = sumaPesos.add(peso);
        }

        if (sumaPesos.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return sumaNotas.divide(sumaPesos, 2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public EstadisticasCalificacionDTO obtenerEstadisticasMateria(Long materiaId) {
        Materia materia = findMateria(materiaId);

        BigDecimal promedio = calificacionRepository.calcularPromedioMateria(materia);
        BigDecimal notaMaxima = calificacionRepository.obtenerNotaMaximaMateria(materia);
        BigDecimal notaMinima = calificacionRepository.obtenerNotaMinimaMateria(materia);
        Long totalCalificaciones = calificacionRepository.countCalificacionesMateria(materia);

        EstadisticasCalificacionDTO estadisticas = new EstadisticasCalificacionDTO();
        estadisticas.setMateriaId(materiaId);
        estadisticas.setMateriaNombre(materia.getNombre());
        estadisticas.setPromedio(promedio != null ? promedio.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        estadisticas.setNotaMaxima(notaMaxima != null ? notaMaxima : BigDecimal.ZERO);
        estadisticas.setNotaMinima(notaMinima != null ? notaMinima : BigDecimal.ZERO);
        estadisticas.setTotalCalificaciones(totalCalificaciones);
        return estadisticas;
    }

    @Transactional(readOnly = true)
    public List<RankingEstudianteDTO> obtenerRankingEstudiantes(Long materiaId, Integer limite) {
        Materia materia = findMateria(materiaId);
        Map<Usuario, List<Calificacion>> porEstudiante = calificacionRepository.findByMateria(materia).stream()
                .collect(Collectors.groupingBy(Calificacion::getEstudiante));

        List<RankingEstudianteDTO> ranking = new ArrayList<>();
        for (Map.Entry<Usuario, List<Calificacion>> entry : porEstudiante.entrySet()) {
            List<Calificacion> calificacionesEstudiante = entry.getValue();
            BigDecimal promedio = calificacionesEstudiante.stream()
                    .map(Calificacion::getNota)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(calificacionesEstudiante.size()), 2, RoundingMode.HALF_UP);

            RankingEstudianteDTO rankingDTO = new RankingEstudianteDTO();
            rankingDTO.setEstudianteId(entry.getKey().getId());
            rankingDTO.setEstudianteNombre(entry.getKey().getNombreCompleto());
            rankingDTO.setPromedio(promedio);
            ranking.add(rankingDTO);
        }

        ranking.sort(Comparator.comparing(RankingEstudianteDTO::getPromedio).reversed());
        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setPosicion(i + 1);
        }

        int maxResultados = limite != null ? limite : 10;
        return ranking.stream().limit(maxResultados).toList();
    }

    @Transactional(readOnly = true)
    public List<CalificacionDTO> obtenerCalificacionesPendientes() {
        return calificacionRepository.findCalificacionesPendientes().stream()
                .map(this::convertirADTO)
                .toList();
    }

    public void eliminarCalificacion(Long id) {
        if (!calificacionRepository.existsById(id)) {
            throw new ResourceNotFoundException(MSG_CALIFICACION_NO_ENCONTRADA);
        }
        calificacionRepository.deleteById(id);
    }

    private void aplicarDatosCalificacion(Calificacion calificacion, CalificacionDTO dto,
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

    private CalificacionDTO convertirADTO(Calificacion calificacion) {
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
