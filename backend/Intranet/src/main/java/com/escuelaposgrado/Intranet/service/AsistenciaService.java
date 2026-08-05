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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para la gestión de asistencias
 */
@Service
@Transactional
public class AsistenciaService {

    private static final String MSG_ESTUDIANTE_NO_ENCONTRADO = "Estudiante no encontrado";
    private static final String MSG_MATERIA_NO_ENCONTRADA = "Materia no encontrada";
    private static final String MSG_ASISTENCIA_NO_ENCONTRADA = "Asistencia no encontrada";

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    public AsistenciaDTO registrarAsistencia(AsistenciaDTO asistenciaDTO, String registradoPor) {
        Usuario estudiante = findEstudiante(asistenciaDTO.getEstudianteId());
        Materia materia = findMateria(asistenciaDTO.getMateriaId());

        if (asistenciaRepository.existsByEstudianteAndMateriaAndFecha(
                estudiante, materia, asistenciaDTO.getFecha())) {
            throw new BadRequestException("Ya existe un registro de asistencia para esta fecha");
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setEstudiante(estudiante);
        asistencia.setMateria(materia);
        asistencia.setFecha(asistenciaDTO.getFecha());
        asistencia.setHoraEntrada(asistenciaDTO.getHoraEntrada());
        asistencia.setHoraSalida(asistenciaDTO.getHoraSalida());
        asistencia.setEstado(EstadoAsistencia.valueOf(asistenciaDTO.getEstado()));
        asistencia.setObservaciones(asistenciaDTO.getObservaciones());
        asistencia.setHorasAcademicas(asistenciaDTO.getHorasAcademicas());
        asistencia.setRegistradoPor(registradoPor);

        return convertirADTO(asistenciaRepository.save(asistencia));
    }

    public AsistenciaDTO actualizarAsistencia(Long id, AsistenciaDTO asistenciaDTO) {
        Asistencia asistencia = findAsistencia(id);
        asistencia.setHoraEntrada(asistenciaDTO.getHoraEntrada());
        asistencia.setHoraSalida(asistenciaDTO.getHoraSalida());
        asistencia.setEstado(EstadoAsistencia.valueOf(asistenciaDTO.getEstado()));
        asistencia.setObservaciones(asistenciaDTO.getObservaciones());
        asistencia.setHorasAcademicas(asistenciaDTO.getHorasAcademicas());
        return convertirADTO(asistenciaRepository.save(asistencia));
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerAsistenciasPorEstudiante(Long estudianteId) {
        Usuario estudiante = findEstudiante(estudianteId);
        return asistenciaRepository.findByEstudiante(estudiante).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerAsistenciasPorMateria(Long materiaId) {
        Materia materia = findMateria(materiaId);
        return asistenciaRepository.findByMateria(materia).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerAsistenciasPorEstudianteYMateria(Long estudianteId, Long materiaId) {
        Usuario estudiante = findEstudiante(estudianteId);
        Materia materia = findMateria(materiaId);
        return asistenciaRepository.findByEstudianteAndMateria(estudiante, materia).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerAsistenciasPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return asistenciaRepository.findByFechaBetween(fechaInicio, fechaFin).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerAsistenciasDelDia(LocalDate fecha) {
        return asistenciaRepository.findAsistenciasDelDia(fecha).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Double calcularPorcentajeAsistencia(Long estudianteId, Long materiaId) {
        Usuario estudiante = findEstudiante(estudianteId);
        Materia materia = findMateria(materiaId);

        long totalAsistencias = asistenciaRepository.countTotalAsistencias(estudiante, materia);
        long asistenciasPresente = asistenciaRepository.countAsistenciasPresente(estudiante, materia);

        if (totalAsistencias == 0) {
            return 0.0;
        }
        return (asistenciasPresente * 100.0) / totalAsistencias;
    }

    @Transactional(readOnly = true)
    public EstadisticasAsistenciaDTO obtenerEstadisticasEstudiante(Long estudianteId) {
        Usuario estudiante = findEstudiante(estudianteId);

        long totalPresente = asistenciaRepository.countByEstudianteAndEstado(estudiante, EstadoAsistencia.PRESENTE);
        long totalAusente = asistenciaRepository.countByEstudianteAndEstado(estudiante, EstadoAsistencia.AUSENTE);
        long totalTardanza = asistenciaRepository.countByEstudianteAndEstado(estudiante, EstadoAsistencia.TARDANZA);
        long totalJustificado = asistenciaRepository.countByEstudianteAndEstado(estudiante, EstadoAsistencia.JUSTIFICADO);

        long total = totalPresente + totalAusente + totalTardanza + totalJustificado;
        double porcentaje = total > 0 ? (totalPresente * 100.0) / total : 0.0;

        EstadisticasAsistenciaDTO estadisticas = new EstadisticasAsistenciaDTO();
        estadisticas.setEstudianteId(estudianteId);
        estadisticas.setEstudianteNombre(estudiante.getNombreCompleto());
        estadisticas.setTotalPresente(totalPresente);
        estadisticas.setTotalAusente(totalAusente);
        estadisticas.setTotalTardanza(totalTardanza);
        estadisticas.setTotalJustificado(totalJustificado);
        estadisticas.setTotalClases(total);
        estadisticas.setPorcentajeAsistencia(porcentaje);
        return estadisticas;
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDTO> obtenerReporteAsistencia(Long materiaId, LocalDate fecha) {
        Materia materia = findMateria(materiaId);
        return asistenciaRepository.findReporteAsistenciaPorMateriaYFecha(materia, fecha).stream()
                .map(this::convertirADTO)
                .toList();
    }

    public void eliminarAsistencia(Long id) {
        if (!asistenciaRepository.existsById(id)) {
            throw new ResourceNotFoundException(MSG_ASISTENCIA_NO_ENCONTRADA);
        }
        asistenciaRepository.deleteById(id);
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

    private AsistenciaDTO convertirADTO(Asistencia asistencia) {
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
