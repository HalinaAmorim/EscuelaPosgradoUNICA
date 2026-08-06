package com.escuelaposgrado.Matricula.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Matricula.dto.request.TurnoMatriculaRequest;
import com.escuelaposgrado.Matricula.dto.response.TurnoMatriculaResponse;
import com.escuelaposgrado.Matricula.dto.response.nested.PeriodoAcademicoBasicResponse;
import com.escuelaposgrado.Matricula.dto.response.nested.ProgramaEstudioBasicResponse;
import com.escuelaposgrado.Matricula.exception.BadRequestException;
import com.escuelaposgrado.Matricula.exception.ResourceNotFoundException;
import com.escuelaposgrado.Matricula.model.entity.PeriodoAcademico;
import com.escuelaposgrado.Matricula.model.entity.ProgramaEstudio;
import com.escuelaposgrado.Matricula.model.entity.TurnoMatricula;
import com.escuelaposgrado.Matricula.repository.PeriodoAcademicoRepository;
import com.escuelaposgrado.Matricula.repository.ProgramaEstudioRepository;
import com.escuelaposgrado.Matricula.repository.TurnoMatriculaRepository;

/**
 * Servicio de aplicación para Turnos de Matrícula.
 */
@Service
@Transactional
public class TurnoMatriculaService {

    private static final String MSG_TURNO_NO_ENCONTRADO = "Turno de matrícula no encontrado con ID: ";
    private static final String MSG_PERIODO_NO_ENCONTRADO = "Período académico no encontrado con ID: ";
    private static final String MSG_PROGRAMA_NO_ENCONTRADO = "Programa de estudio no encontrado con ID: ";
    private static final String MSG_CODIGO_DUPLICADO = "Ya existe un turno con el código: ";
    private static final String MSG_NOMBRE_DUPLICADO = "Ya existe un turno con el nombre: ";
    private static final String MSG_FECHAS_INVALIDAS =
            "La fecha de inicio debe ser anterior a la fecha de fin";

    private final TurnoMatriculaRepository turnoMatriculaRepository;
    private final PeriodoAcademicoRepository periodoAcademicoRepository;
    private final ProgramaEstudioRepository programaEstudioRepository;

    public TurnoMatriculaService(TurnoMatriculaRepository turnoMatriculaRepository,
                                 PeriodoAcademicoRepository periodoAcademicoRepository,
                                 ProgramaEstudioRepository programaEstudioRepository) {
        this.turnoMatriculaRepository = turnoMatriculaRepository;
        this.periodoAcademicoRepository = periodoAcademicoRepository;
        this.programaEstudioRepository = programaEstudioRepository;
    }

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findAll() {
        return mapAll(turnoMatriculaRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findAllActive() {
        return mapAll(turnoMatriculaRepository.findByActivoTrueOrderByOrdenTurnoAsc());
    }

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findAllEnabled() {
        return mapAll(turnoMatriculaRepository.findByActivoTrueAndHabilitadoTrueOrderByOrdenTurnoAsc());
    }

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findByPeriodoAcademico(Long periodoId) {
        return mapAll(turnoMatriculaRepository.findByPeriodoAcademicoIdAndActivoTrueOrderByOrdenTurnoAsc(periodoId));
    }

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findByProgramaEstudio(Long programaId) {
        return mapAll(turnoMatriculaRepository.findByProgramaEstudioIdAndActivoTrueOrderByOrdenTurnoAsc(programaId));
    }

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findByPeriodoAndPrograma(Long periodoId, Long programaId) {
        return mapAll(turnoMatriculaRepository
                .findByPeriodoAcademicoIdAndProgramaEstudioIdAndActivoTrueOrderByOrdenTurnoAsc(periodoId, programaId));
    }

    @Transactional(readOnly = true)
    public TurnoMatriculaResponse findById(Long id) {
        return toResponse(findTurnoOrThrow(id));
    }

    public TurnoMatriculaResponse create(TurnoMatriculaRequest request) {
        RelatedEntities related = resolveActiveRelatedEntities(
                request,
                "No se puede crear el turno en un período académico inactivo",
                "No se puede crear el turno para un programa de estudio inactivo");
        assertUniqueCodigoYNombre(request.getCodigo(), request.getNombre(), null);
        assertValidDateRange(request);

        TurnoMatricula turno = new TurnoMatricula();
        applyRequestData(turno, request, related);
        turno.setHabilitado(Boolean.TRUE.equals(request.getHabilitado()));
        turno.setActivo(true);

        return toResponse(turnoMatriculaRepository.save(turno));
    }

    public TurnoMatriculaResponse update(Long id, TurnoMatriculaRequest request) {
        TurnoMatricula turno = findTurnoOrThrow(id);
        RelatedEntities related = resolveActiveRelatedEntities(
                request,
                "No se puede asignar el turno a un período académico inactivo",
                "No se puede asignar el turno a un programa de estudio inactivo");
        assertUniqueCodigoYNombre(request.getCodigo(), request.getNombre(), id);
        assertValidDateRange(request);

        applyRequestData(turno, request, related);
        if (request.getHabilitado() != null) {
            turno.setHabilitado(request.getHabilitado());
        }

        return toResponse(turnoMatriculaRepository.save(turno));
    }

    public TurnoMatriculaResponse toggleActive(Long id) {
        TurnoMatricula turno = findTurnoOrThrow(id);
        turno.setActivo(!Boolean.TRUE.equals(turno.getActivo()));
        return toResponse(turnoMatriculaRepository.save(turno));
    }

    public TurnoMatriculaResponse toggleEnabled(Long id) {
        TurnoMatricula turno = findTurnoOrThrow(id);
        turno.setHabilitado(!Boolean.TRUE.equals(turno.getHabilitado()));
        return toResponse(turnoMatriculaRepository.save(turno));
    }

    public void delete(Long id) {
        TurnoMatricula turno = findTurnoOrThrow(id);
        turno.setActivo(false);
        turnoMatriculaRepository.save(turno);
    }

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findByNombreContaining(String nombre) {
        return mapAll(turnoMatriculaRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre));
    }

    private RelatedEntities resolveActiveRelatedEntities(TurnoMatriculaRequest request,
                                                         String inactivePeriodMessage,
                                                         String inactiveProgramMessage) {
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(request.getPeriodoAcademicoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        MSG_PERIODO_NO_ENCONTRADO + request.getPeriodoAcademicoId()));
        if (!Boolean.TRUE.equals(periodo.getActivo())) {
            throw new BadRequestException(inactivePeriodMessage);
        }

        ProgramaEstudio programa = programaEstudioRepository.findById(request.getProgramaEstudioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        MSG_PROGRAMA_NO_ENCONTRADO + request.getProgramaEstudioId()));
        if (!Boolean.TRUE.equals(programa.getActivo())) {
            throw new BadRequestException(inactiveProgramMessage);
        }

        return new RelatedEntities(periodo, programa);
    }

    private void assertUniqueCodigoYNombre(String codigo, String nombre, Long excludeId) {
        assertNotTaken(turnoMatriculaRepository.findByCodigo(codigo), excludeId, MSG_CODIGO_DUPLICADO + codigo);
        assertNotTaken(turnoMatriculaRepository.findByNombre(nombre), excludeId, MSG_NOMBRE_DUPLICADO + nombre);
    }

    private void assertNotTaken(Optional<TurnoMatricula> existing, Long excludeId, String message) {
        Predicate<TurnoMatricula> isOther = turno -> excludeId == null || !turno.getId().equals(excludeId);
        if (existing.filter(isOther).isPresent()) {
            throw new BadRequestException(message);
        }
    }

    private void assertValidDateRange(TurnoMatriculaRequest request) {
        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new BadRequestException(MSG_FECHAS_INVALIDAS);
        }
    }

    private void applyRequestData(TurnoMatricula turno, TurnoMatriculaRequest request, RelatedEntities related) {
        turno.setNombre(request.getNombre());
        turno.setCodigo(request.getCodigo());
        turno.setFechaInicio(request.getFechaInicio());
        turno.setFechaFin(request.getFechaFin());
        turno.setOrdenTurno(request.getOrdenTurno());
        turno.setDescripcion(request.getDescripcion());
        turno.setRequisitos(request.getRequisitos());
        turno.setPeriodoAcademico(related.periodo());
        turno.setProgramaEstudio(related.programa());
    }

    private TurnoMatricula findTurnoOrThrow(Long id) {
        return turnoMatriculaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_TURNO_NO_ENCONTRADO + id));
    }

    private List<TurnoMatriculaResponse> mapAll(List<TurnoMatricula> turnos) {
        return turnos.stream().map(this::toResponse).toList();
    }

    private TurnoMatriculaResponse toResponse(TurnoMatricula turno) {
        TurnoMatriculaResponse response = new TurnoMatriculaResponse();
        response.setId(turno.getId());
        response.setNombre(turno.getNombre());
        response.setCodigo(turno.getCodigo());
        response.setFechaInicio(turno.getFechaInicio());
        response.setFechaFin(turno.getFechaFin());
        response.setOrdenTurno(turno.getOrdenTurno());
        response.setActivo(turno.getActivo());
        response.setHabilitado(turno.getHabilitado());
        response.setDescripcion(turno.getDescripcion());
        response.setRequisitos(turno.getRequisitos());
        response.setFechaCreacion(turno.getFechaCreacion());
        response.setFechaActualizacion(turno.getFechaActualizacion());
        response.setPeriodoAcademico(toPeriodoBasic(turno.getPeriodoAcademico()));
        response.setProgramaEstudio(toProgramaBasic(turno.getProgramaEstudio()));
        return response;
    }

    private PeriodoAcademicoBasicResponse toPeriodoBasic(PeriodoAcademico periodo) {
        if (periodo == null) {
            return null;
        }
        PeriodoAcademicoBasicResponse basic = new PeriodoAcademicoBasicResponse();
        basic.setId(periodo.getId());
        basic.setNombre(periodo.getNombre());
        basic.setCodigo(periodo.getCodigo());
        return basic;
    }

    private ProgramaEstudioBasicResponse toProgramaBasic(ProgramaEstudio programa) {
        if (programa == null) {
            return null;
        }
        ProgramaEstudioBasicResponse basic = new ProgramaEstudioBasicResponse();
        basic.setId(programa.getId());
        basic.setNombre(programa.getNombre());
        basic.setCodigo(programa.getCodigo());
        return basic;
    }

    private record RelatedEntities(PeriodoAcademico periodo, ProgramaEstudio programa) {}
}
