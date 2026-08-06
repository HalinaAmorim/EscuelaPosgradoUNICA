package com.escuelaposgrado.Intranet.service;

import com.escuelaposgrado.Intranet.dto.AsistenciaDTO;
import com.escuelaposgrado.Intranet.dto.EstadisticasAsistenciaDTO;
import com.escuelaposgrado.Intranet.exception.BadRequestException;
import com.escuelaposgrado.Intranet.exception.ResourceNotFoundException;
import com.escuelaposgrado.Intranet.model.Asistencia;
import com.escuelaposgrado.Intranet.model.EstadoAsistencia;
import com.escuelaposgrado.Intranet.model.Materia;
import com.escuelaposgrado.Intranet.model.Usuario;
import com.escuelaposgrado.Intranet.repository.AsistenciaRepository;
import com.escuelaposgrado.Intranet.repository.MateriaRepository;
import com.escuelaposgrado.Intranet.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de aplicación para asistencias académicas.
 */
@Service
@Transactional
public class AsistenciaService {

    private static final String MSG_ESTUDIANTE_NO_ENCONTRADO = "Estudiante no encontrado";
    private static final String MSG_MATERIA_NO_ENCONTRADA = "Materia no encontrada";
    private static final String MSG_ASISTENCIA_NO_ENCONTRADA = "Asistencia no encontrada";
    private static final String MSG_ASISTENCIA_DUPLICADA =
            "Ya existe un registro de asistencia para esta fecha";

    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MateriaRepository materiaRepository;

    public AsistenciaService(AsistenciaRepository asistenciaRepository,
                             UsuarioRepository usuarioRepository,
                             MateriaRepository materiaRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.materiaRepository = materiaRepository;
    }

    public AsistenciaDTO registrarAsistencia(AsistenciaDTO asistenciaDTO, String registradoPor) {
        Usuario estudiante = findEstudiante(asistenciaDTO.getEstudianteId());
        Materia materia = findMateria(asistenciaDTO.getMateriaId());
        assertNoDuplicateAttendance(estudiante, materia, asistenciaDTO.getFecha());

        Asistencia asistencia = new Asistencia();
        applyCreateData(asistencia, asistenciaDTO, estudiante, materia, registradoPor);
        return toDto(asistenciaRepository.save(asistencia));
    }

    public AsistenciaDTO actualizarAsistencia(Long id, AsistenciaDTO asistenciaDTO) {
        Asistencia asistencia = findAsistencia(id);
        applyUpdateData(asistencia, asistenciaDTO);
        return toDto(asistenciaRepository.save(asistencia));
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerAsistenciasPorEstudiante(Long estudianteId) {
        Usuario estudiante = findEstudiante(estudianteId);
        return mapAll(asistenciaRepository.findByEstudiante(estudiante));
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerAsistenciasPorMateria(Long materiaId) {
        Materia materia = findMateria(materiaId);
        return mapAll(asistenciaRepository.findByMateria(materia));
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerAsistenciasPorEstudianteYMateria(Long estudianteId, Long materiaId) {
        Usuario estudiante = findEstudiante(estudianteId);
        Materia materia = findMateria(materiaId);
        return mapAll(asistenciaRepository.findByEstudianteAndMateria(estudiante, materia));
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerAsistenciasPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return mapAll(asistenciaRepository.findByFechaBetween(fechaInicio, fechaFin));
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerAsistenciasDelDia(LocalDate fecha) {
        return mapAll(asistenciaRepository.findAsistenciasDelDia(fecha));
    }

    @Transactional(readOnly = true)
    public Double calcularPorcentajeAsistencia(Long estudianteId, Long materiaId) {
        Usuario estudiante = findEstudiante(estudianteId);
        Materia materia = findMateria(materiaId);

        long total = asistenciaRepository.countTotalAsistencias(estudiante, materia);
        if (total == 0) {
            return 0.0;
        }
        long presentes = asistenciaRepository.countAsistenciasPresente(estudiante, materia);
        return (presentes * 100.0) / total;
    }

    @Transactional(readOnly = true)
    public EstadisticasAsistenciaDTO obtenerEstadisticasEstudiante(Long estudianteId) {
        Usuario estudiante = findEstudiante(estudianteId);

        long totalPresente = countByEstado(estudiante, EstadoAsistencia.PRESENTE);
        long totalAusente = countByEstado(estudiante, EstadoAsistencia.AUSENTE);
        long totalTardanza = countByEstado(estudiante, EstadoAsistencia.TARDANZA);
        long totalJustificado = countByEstado(estudiante, EstadoAsistencia.JUSTIFICADO);
        long total = totalPresente + totalAusente + totalTardanza + totalJustificado;

        EstadisticasAsistenciaDTO estadisticas = new EstadisticasAsistenciaDTO();
        estadisticas.setEstudianteId(estudianteId);
        estadisticas.setEstudianteNombre(estudiante.getNombreCompleto());
        estadisticas.setTotalPresente(totalPresente);
        estadisticas.setTotalAusente(totalAusente);
        estadisticas.setTotalTardanza(totalTardanza);
        estadisticas.setTotalJustificado(totalJustificado);
        estadisticas.setTotalClases(total);
        estadisticas.setPorcentajeAsistencia(total > 0 ? (totalPresente * 100.0) / total : 0.0);
        return estadisticas;
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerReporteAsistencia(Long materiaId, LocalDate fecha) {
        Materia materia = findMateria(materiaId);
        return mapAll(asistenciaRepository.findReporteAsistenciaPorMateriaYFecha(materia, fecha));
    }

    public void eliminarAsistencia(Long id) {
        if (!asistenciaRepository.existsById(id)) {
            throw new ResourceNotFoundException(MSG_ASISTENCIA_NO_ENCONTRADA);
        }
        asistenciaRepository.deleteById(id);
    }

    private void assertNoDuplicateAttendance(Usuario estudiante, Materia materia, LocalDate fecha) {
        if (asistenciaRepository.existsByEstudianteAndMateriaAndFecha(estudiante, materia, fecha)) {
            throw new BadRequestException(MSG_ASISTENCIA_DUPLICADA);
        }
    }

    private long countByEstado(Usuario estudiante, EstadoAsistencia estado) {
        return asistenciaRepository.countByEstudianteAndEstado(estudiante, estado);
    }

    private void applyCreateData(Asistencia asistencia, AsistenciaDTO dto,
                                 Usuario estudiante, Materia materia, String registradoPor) {
        asistencia.setEstudiante(estudiante);
        asistencia.setMateria(materia);
        asistencia.setFecha(dto.getFecha());
        applyUpdateData(asistencia, dto);
        asistencia.setRegistradoPor(registradoPor);
    }

    private void applyUpdateData(Asistencia asistencia, AsistenciaDTO dto) {
        asistencia.setHoraEntrada(dto.getHoraEntrada());
        asistencia.setHoraSalida(dto.getHoraSalida());
        asistencia.setEstado(EstadoAsistencia.valueOf(dto.getEstado()));
        asistencia.setObservaciones(dto.getObservaciones());
        asistencia.setHorasAcademicas(dto.getHorasAcademicas());
    }

    private List<AsistenciaDTO> mapAll(List<Asistencia> asistencias) {
        return asistencias.stream().map(this::toDto).toList();
    }

    private Usuario findEstudiante(Long estudianteId) {
        return usuarioRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_ESTUDIANTE_NO_ENCONTRADO));
    }

    private Materia findMateria(Long materiaId) {
        return materiaRepository.findById(materiaId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_MATERIA_NO_ENCONTRADA));
    }

    private Asistencia findAsistencia(Long id) {
        return asistenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_ASISTENCIA_NO_ENCONTRADA));
    }

    private AsistenciaDTO toDto(Asistencia asistencia) {
        AsistenciaDTO dto = new AsistenciaDTO();
        dto.setId(asistencia.getId());
        dto.setEstudianteId(asistencia.getEstudiante().getId());
        dto.setMateriaId(asistencia.getMateria().getId());
        dto.setFecha(asistencia.getFecha());
        dto.setHoraEntrada(asistencia.getHoraEntrada());
        dto.setHoraSalida(asistencia.getHoraSalida());
        dto.setEstado(asistencia.getEstado().name());
        dto.setObservaciones(asistencia.getObservaciones());
        dto.setHorasAcademicas(asistencia.getHorasAcademicas());
        dto.setEstudianteNombre(asistencia.getEstudiante().getNombreCompleto());
        dto.setMateriaNombre(asistencia.getMateria().getNombre());
        dto.setRegistradoPor(asistencia.getRegistradoPor());
        return dto;
    }
}
