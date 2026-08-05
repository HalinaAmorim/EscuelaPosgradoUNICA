package com.escuelaposgrado.Matricula.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
 * Servicio para gestionar las operaciones CRUD de Turnos de Matrícula
 */
@Service
@Transactional
public class TurnoMatriculaService {

    private static final String MSG_TURNO_NO_ENCONTRADO = "Turno de matrícula no encontrado con ID: ";

    @Autowired
    private TurnoMatriculaRepository turnoMatriculaRepository;

    @Autowired
    private PeriodoAcademicoRepository periodoAcademicoRepository;

    @Autowired
    private ProgramaEstudioRepository programaEstudioRepository;

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findAll() {
        return mapToResponse(turnoMatriculaRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findAllActive() {
        return mapToResponse(turnoMatriculaRepository.findByActivoTrueOrderByOrdenTurnoAsc());
    }

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findAllEnabled() {
        return mapToResponse(turnoMatriculaRepository.findByActivoTrueAndHabilitadoTrueOrderByOrdenTurnoAsc());
    }

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findByPeriodoAcademico(Long periodoId) {
        return mapToResponse(
                turnoMatriculaRepository.findByPeriodoAcademicoIdAndActivoTrueOrderByOrdenTurnoAsc(periodoId));
    }

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findByProgramaEstudio(Long programaId) {
        return mapToResponse(
                turnoMatriculaRepository.findByProgramaEstudioIdAndActivoTrueOrderByOrdenTurnoAsc(programaId));
    }

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findByPeriodoAndPrograma(Long periodoId, Long programaId) {
        return mapToResponse(
                turnoMatriculaRepository.findByPeriodoAcademicoIdAndProgramaEstudioIdAndActivoTrueOrderByOrdenTurnoAsc(
                        periodoId, programaId));
    }

    @Transactional(readOnly = true)
    public TurnoMatriculaResponse findById(Long id) {
        return convertToResponse(findTurnoOrThrow(id));
    }

    public TurnoMatriculaResponse create(TurnoMatriculaRequest request) {
        ValidatedRefs refs = validateRequestRefs(request);
        validateUniqueCodigoYNombre(request.getCodigo(), request.getNombre(), null);
        validateFechas(request);

        TurnoMatricula turno = new TurnoMatricula();
        applyRequestToEntity(turno, request, refs.periodo(), refs.programa());
        turno.setHabilitado(Boolean.TRUE.equals(request.getHabilitado()));
        turno.setActivo(true);

        return convertToResponse(turnoMatriculaRepository.save(turno));
    }

    public TurnoMatriculaResponse update(Long id, TurnoMatriculaRequest request) {
        TurnoMatricula turno = findTurnoOrThrow(id);
        ValidatedRefs refs = validateRequestRefs(request);
        validateUniqueCodigoYNombre(request.getCodigo(), request.getNombre(), id);
        validateFechas(request);

        applyRequestToEntity(turno, request, refs.periodo(), refs.programa());
        if (request.getHabilitado() != null) {
            turno.setHabilitado(request.getHabilitado());
        }

        return convertToResponse(turnoMatriculaRepository.save(turno));
    }

    public TurnoMatriculaResponse toggleActive(Long id) {
        TurnoMatricula turno = findTurnoOrThrow(id);
        turno.setActivo(!Boolean.TRUE.equals(turno.getActivo()));
        return convertToResponse(turnoMatriculaRepository.save(turno));
    }

    public TurnoMatriculaResponse toggleEnabled(Long id) {
        TurnoMatricula turno = findTurnoOrThrow(id);
        turno.setHabilitado(!Boolean.TRUE.equals(turno.getHabilitado()));
        return convertToResponse(turnoMatriculaRepository.save(turno));
    }

    public void delete(Long id) {
        TurnoMatricula turno = findTurnoOrThrow(id);
        turno.setActivo(false);
        turnoMatriculaRepository.save(turno);
    }

    @Transactional(readOnly = true)
    public List<TurnoMatriculaResponse> findByNombreContaining(String nombre) {
        return mapToResponse(turnoMatriculaRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre));
    }

    private ValidatedRefs validateRequestRefs(TurnoMatriculaRequest request) {
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(request.getPeriodoAcademicoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Período académico no encontrado con ID: " + request.getPeriodoAcademicoId()));

        if (!Boolean.TRUE.equals(periodo.getActivo())) {
            throw new BadRequestException("No se puede usar un período académico inactivo");
        }

        ProgramaEstudio programa = programaEstudioRepository.findById(request.getProgramaEstudioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Programa de estudio no encontrado con ID: " + request.getProgramaEstudioId()));

        if (!Boolean.TRUE.equals(programa.getActivo())) {
            throw new BadRequestException("No se puede usar un programa de estudio inactivo");
        }

        return new ValidatedRefs(periodo, programa);
    }

    private void validateUniqueCodigoYNombre(String codigo, String nombre, Long excludeId) {
        Optional<TurnoMatricula> porCodigo = turnoMatriculaRepository.findByCodigo(codigo);
        if (porCodigo.isPresent() && (excludeId == null || !porCodigo.get().getId().equals(excludeId))) {
            throw new BadRequestException("Ya existe un turno con el código: " + codigo);
        }

        Optional<TurnoMatricula> porNombre = turnoMatriculaRepository.findByNombre(nombre);
        if (porNombre.isPresent() && (excludeId == null || !porNombre.get().getId().equals(excludeId))) {
            throw new BadRequestException("Ya existe un turno con el nombre: " + nombre);
        }
    }

    private void validateFechas(TurnoMatriculaRequest request) {
        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new BadRequestException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
    }

    private void applyRequestToEntity(TurnoMatricula turno, TurnoMatriculaRequest request,
                                      PeriodoAcademico periodo, ProgramaEstudio programa) {
        turno.setNombre(request.getNombre());
        turno.setCodigo(request.getCodigo());
        turno.setFechaInicio(request.getFechaInicio());
        turno.setFechaFin(request.getFechaFin());
        turno.setOrdenTurno(request.getOrdenTurno());
        turno.setDescripcion(request.getDescripcion());
        turno.setRequisitos(request.getRequisitos());
        turno.setPeriodoAcademico(periodo);
        turno.setProgramaEstudio(programa);
    }

    private TurnoMatricula findTurnoOrThrow(Long id) {
        return turnoMatriculaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_TURNO_NO_ENCONTRADO + id));
    }

    private List<TurnoMatriculaResponse> mapToResponse(List<TurnoMatricula> turnos) {
        return turnos.stream().map(this::convertToResponse).toList();
    }

    private TurnoMatriculaResponse convertToResponse(TurnoMatricula turno) {
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

        if (turno.getPeriodoAcademico() != null) {
            PeriodoAcademicoBasicResponse periodoBasic = new PeriodoAcademicoBasicResponse();
            periodoBasic.setId(turno.getPeriodoAcademico().getId());
            periodoBasic.setNombre(turno.getPeriodoAcademico().getNombre());
            periodoBasic.setCodigo(turno.getPeriodoAcademico().getCodigo());
            response.setPeriodoAcademico(periodoBasic);
        }

        if (turno.getProgramaEstudio() != null) {
            ProgramaEstudioBasicResponse programaBasic = new ProgramaEstudioBasicResponse();
            programaBasic.setId(turno.getProgramaEstudio().getId());
            programaBasic.setNombre(turno.getProgramaEstudio().getNombre());
            programaBasic.setCodigo(turno.getProgramaEstudio().getCodigo());
            response.setProgramaEstudio(programaBasic);
        }

        return response;
    }

    private record ValidatedRefs(PeriodoAcademico periodo, ProgramaEstudio programa) {}
}
