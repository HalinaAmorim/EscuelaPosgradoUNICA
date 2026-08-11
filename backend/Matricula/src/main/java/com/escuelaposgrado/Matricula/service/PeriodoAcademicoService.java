package com.escuelaposgrado.Matricula.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Matricula.dto.request.PeriodoAcademicoRequest;
import com.escuelaposgrado.Matricula.dto.response.PeriodoAcademicoResponse;
import com.escuelaposgrado.Matricula.exception.BadRequestException;
import com.escuelaposgrado.Matricula.exception.ResourceNotFoundException;
import com.escuelaposgrado.Matricula.model.entity.PeriodoAcademico;
import com.escuelaposgrado.Matricula.repository.PeriodoAcademicoRepository;

/**
 * Servicio para la gestión de Períodos Académicos
 */
@Service
@Transactional
public class PeriodoAcademicoService {

    private static final String MSG_PERIODO_NO_ENCONTRADO = "Período académico no encontrado con ID: ";
    private static final String MSG_CODIGO_DUPLICADO = "Ya existe un período académico con el código: ";
    private static final String MSG_NOMBRE_DUPLICADO = "Ya existe un período académico con el nombre: ";
    private static final String MSG_FECHAS_INVALIDAS =
            "La fecha de inicio no puede ser posterior a la fecha de fin";
    private static final String MSG_FECHAS_MATRICULA_INVALIDAS =
            "La fecha de inicio de matrícula no puede ser posterior a la fecha de fin de matrícula";

    private final PeriodoAcademicoRepository periodoAcademicoRepository;

    public PeriodoAcademicoService(PeriodoAcademicoRepository periodoAcademicoRepository) {
        this.periodoAcademicoRepository = periodoAcademicoRepository;
    }

    @Transactional(readOnly = true)
    public List<PeriodoAcademicoResponse> findAll() {
        return mapAll(periodoAcademicoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<PeriodoAcademicoResponse> findActivos() {
        return mapAll(periodoAcademicoRepository.findByActivoTrueOrderByFechaCreacionDesc());
    }

    @Transactional(readOnly = true)
    public PeriodoAcademicoResponse findById(Long id) {
        return convertToResponse(findPeriodoOrThrow(id));
    }

    public PeriodoAcademicoResponse create(PeriodoAcademicoRequest request) {
        String codigo = resolveCodigo(request);
        assertUniqueCodigoYNombre(codigo, request.getNombre(), null);
        assertUniqueAnioSemestre(request);
        assertValidDateRanges(request);

        PeriodoAcademico periodo = new PeriodoAcademico();
        applyRequestData(periodo, request, codigo);

        return convertToResponse(periodoAcademicoRepository.save(periodo));
    }

    public PeriodoAcademicoResponse update(Long id, PeriodoAcademicoRequest request) {
        PeriodoAcademico periodo = findPeriodoOrThrow(id);
        String codigo = resolveCodigo(request);
        assertUniqueCodigoYNombre(codigo, request.getNombre(), id);
        assertValidDateRanges(request);

        applyRequestData(periodo, request, codigo);
        return convertToResponse(periodoAcademicoRepository.save(periodo));
    }

    public void delete(Long id) {
        PeriodoAcademico periodo = findPeriodoOrThrow(id);
        periodo.setActivo(false);
        periodoAcademicoRepository.save(periodo);
    }

    public PeriodoAcademicoResponse toggleHabilitado(Long id) {
        PeriodoAcademico periodo = findPeriodoOrThrow(id);
        periodo.setHabilitado(!periodo.getHabilitado());
        return convertToResponse(periodoAcademicoRepository.save(periodo));
    }

    public PeriodoAcademicoResponse reactivar(Long id) {
        PeriodoAcademico periodo = findPeriodoOrThrow(id);
        periodo.setActivo(true);
        periodo.setHabilitado(false);
        return convertToResponse(periodoAcademicoRepository.save(periodo));
    }

    @Transactional(readOnly = true)
    public List<PeriodoAcademicoResponse> findHabilitados() {
        return mapAll(periodoAcademicoRepository.findByHabilitadoTrueOrderByFechaCreacionDesc());
    }

    private String resolveCodigo(PeriodoAcademicoRequest request) {
        if (request.getCodigo() != null && !request.getCodigo().trim().isEmpty()) {
            return request.getCodigo().trim();
        }
        return request.getAnio() + "-" + request.getSemestre();
    }

    private void assertUniqueCodigoYNombre(String codigo, String nombre, Long excludeId) {
        assertNotTaken(periodoAcademicoRepository.findByCodigo(codigo), excludeId, MSG_CODIGO_DUPLICADO + codigo);
        assertNotTaken(periodoAcademicoRepository.findByNombre(nombre), excludeId, MSG_NOMBRE_DUPLICADO + nombre);
    }

    private void assertUniqueAnioSemestre(PeriodoAcademicoRequest request) {
        if (periodoAcademicoRepository.findByAnioAndSemestre(request.getAnio(), request.getSemestre()).isPresent()) {
            throw new BadRequestException(
                    "Ya existe un período académico para " + request.getAnio() + " - " + request.getSemestre());
        }
    }

    private void assertValidDateRanges(PeriodoAcademicoRequest request) {
        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new BadRequestException(MSG_FECHAS_INVALIDAS);
        }
        if (request.getFechaInicioMatricula().isAfter(request.getFechaFinMatricula())) {
            throw new BadRequestException(MSG_FECHAS_MATRICULA_INVALIDAS);
        }
    }

    private void assertNotTaken(Optional<PeriodoAcademico> existing, Long excludeId, String message) {
        Predicate<PeriodoAcademico> isOther = periodo -> excludeId == null || !periodo.getId().equals(excludeId);
        if (existing.filter(isOther).isPresent()) {
            throw new BadRequestException(message);
        }
    }

    private void applyRequestData(PeriodoAcademico periodo, PeriodoAcademicoRequest request, String codigo) {
        periodo.setCodigo(codigo);
        periodo.setNombre(request.getNombre());
        periodo.setAnio(request.getAnio());
        periodo.setSemestre(request.getSemestre());
        periodo.setFechaInicio(request.getFechaInicio());
        periodo.setFechaFin(request.getFechaFin());
        periodo.setFechaInicioMatricula(request.getFechaInicioMatricula());
        periodo.setFechaFinMatricula(request.getFechaFinMatricula());
        periodo.setHabilitado(request.getHabilitado());
        periodo.setDescripcion(request.getDescripcion());
    }

    private PeriodoAcademico findPeriodoOrThrow(Long id) {
        return periodoAcademicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_PERIODO_NO_ENCONTRADO + id));
    }

    private List<PeriodoAcademicoResponse> mapAll(List<PeriodoAcademico> periodos) {
        return periodos.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private PeriodoAcademicoResponse convertToResponse(PeriodoAcademico periodo) {
        return new PeriodoAcademicoResponse(
                periodo.getId(),
                periodo.getCodigo(),
                periodo.getNombre(),
                periodo.getAnio(),
                periodo.getSemestre(),
                periodo.getFechaInicio(),
                periodo.getFechaFin(),
                periodo.getFechaInicioMatricula(),
                periodo.getFechaFinMatricula(),
                periodo.getActivo(),
                periodo.getHabilitado(),
                periodo.getDescripcion(),
                periodo.getFechaCreacion(),
                periodo.getFechaActualizacion()
        );
    }
}
