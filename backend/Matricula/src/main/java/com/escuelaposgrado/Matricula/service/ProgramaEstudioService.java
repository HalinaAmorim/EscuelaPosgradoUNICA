package com.escuelaposgrado.Matricula.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Matricula.dto.request.ProgramaEstudioRequest;
import com.escuelaposgrado.Matricula.dto.response.ProgramaEstudioResponse;
import com.escuelaposgrado.Matricula.dto.response.nested.FacultadBasicResponse;
import com.escuelaposgrado.Matricula.exception.BadRequestException;
import com.escuelaposgrado.Matricula.exception.ResourceNotFoundException;
import com.escuelaposgrado.Matricula.model.entity.Facultad;
import com.escuelaposgrado.Matricula.model.entity.ProgramaEstudio;
import com.escuelaposgrado.Matricula.repository.FacultadRepository;
import com.escuelaposgrado.Matricula.repository.ProgramaEstudioRepository;

/**
 * Servicio para gestionar las operaciones CRUD de Programas de Estudio
 */
@Service
@Transactional
public class ProgramaEstudioService {

    private static final String MSG_PROGRAMA_NO_ENCONTRADO = "Programa de estudio no encontrado con ID: ";
    private static final String MSG_FACULTAD_NO_ENCONTRADA = "Facultad no encontrada con ID: ";
    private static final String MSG_CODIGO_DUPLICADO = "Ya existe un programa con el código: ";
    private static final String MSG_NOMBRE_DUPLICADO = "Ya existe un programa con el nombre: ";
    private static final String MSG_OTRO_CODIGO_DUPLICADO = "Ya existe otro programa con el código: ";
    private static final String MSG_OTRO_NOMBRE_DUPLICADO = "Ya existe otro programa con el nombre: ";
    private static final String MSG_TIENE_MENCIONES = "No se puede eliminar el programa porque tiene menciones asociadas";
    private static final String MSG_INACTIVO_NO_DISPONIBLE = "No se puede hacer disponible un programa inactivo";

    private final ProgramaEstudioRepository programaEstudioRepository;
    private final FacultadRepository facultadRepository;

    public ProgramaEstudioService(ProgramaEstudioRepository programaEstudioRepository,
                                  FacultadRepository facultadRepository) {
        this.programaEstudioRepository = programaEstudioRepository;
        this.facultadRepository = facultadRepository;
    }

    @Transactional(readOnly = true)
    public List<ProgramaEstudioResponse> findAll() {
        return mapAll(programaEstudioRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<ProgramaEstudioResponse> findAllActive() {
        return mapAll(programaEstudioRepository.findByActivoTrueOrderByNombreAsc());
    }

    @Transactional(readOnly = true)
    public List<ProgramaEstudioResponse> findAllAvailable() {
        return mapAll(programaEstudioRepository.findByActivoTrueAndDisponibleTrueOrderByNombreAsc());
    }

    @Transactional(readOnly = true)
    public List<ProgramaEstudioResponse> findByFacultadId(Long facultadId) {
        return mapAll(programaEstudioRepository.findByFacultadIdAndActivoTrueOrderByNombreAsc(facultadId));
    }

    @Transactional(readOnly = true)
    public List<ProgramaEstudioResponse> findByNivel(String nivel) {
        return mapAll(programaEstudioRepository.findByNivelIgnoreCaseAndActivoTrueOrderByNombreAsc(nivel));
    }

    @Transactional(readOnly = true)
    public List<ProgramaEstudioResponse> findByModalidad(String modalidad) {
        return mapAll(programaEstudioRepository.findByModalidadIgnoreCaseAndActivoTrueOrderByNombreAsc(modalidad));
    }

    @Transactional(readOnly = true)
    public ProgramaEstudioResponse findById(Long id) {
        return convertToResponse(findProgramaOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<String> findDistinctNiveles() {
        return programaEstudioRepository.findDistinctNiveles();
    }

    @Transactional(readOnly = true)
    public List<String> findDistinctModalidades() {
        return programaEstudioRepository.findDistinctModalidades();
    }

    public ProgramaEstudioResponse create(ProgramaEstudioRequest request) {
        Facultad facultad = resolveActiveFacultad(
                request.getFacultadId(),
                "No se puede crear el programa en una facultad inactiva");
        assertUniqueCodigoYNombre(
                request.getCodigo(),
                request.getNombre(),
                null,
                MSG_CODIGO_DUPLICADO,
                MSG_NOMBRE_DUPLICADO);

        ProgramaEstudio programa = new ProgramaEstudio();
        applyRequestData(programa, request, facultad);
        programa.setActivo(true);
        programa.setDisponible(true);

        return convertToResponse(programaEstudioRepository.save(programa));
    }

    public ProgramaEstudioResponse update(Long id, ProgramaEstudioRequest request) {
        ProgramaEstudio programa = findProgramaOrThrow(id);
        Facultad facultad = resolveActiveFacultad(
                request.getFacultadId(),
                "No se puede asignar el programa a una facultad inactiva");
        assertUniqueCodigoYNombre(
                request.getCodigo(),
                request.getNombre(),
                id,
                MSG_OTRO_CODIGO_DUPLICADO,
                MSG_OTRO_NOMBRE_DUPLICADO);

        applyRequestData(programa, request, facultad);
        return convertToResponse(programaEstudioRepository.save(programa));
    }

    public void toggleActive(Long id) {
        ProgramaEstudio programa = findProgramaOrThrow(id);
        programa.setActivo(!programa.getActivo());
        if (!programa.getActivo()) {
            programa.setDisponible(false);
        }
        programaEstudioRepository.save(programa);
    }

    public void toggleDisponible(Long id) {
        ProgramaEstudio programa = findProgramaOrThrow(id);
        if (!programa.getActivo() && !programa.getDisponible()) {
            throw new BadRequestException(MSG_INACTIVO_NO_DISPONIBLE);
        }
        programa.setDisponible(!programa.getDisponible());
        programaEstudioRepository.save(programa);
    }

    public void delete(Long id) {
        ProgramaEstudio programa = findProgramaOrThrow(id);
        if (programaEstudioRepository.countMencionesByPrograma(id) > 0) {
            throw new BadRequestException(MSG_TIENE_MENCIONES);
        }
        programaEstudioRepository.delete(programa);
    }

    private Facultad resolveActiveFacultad(Long facultadId, String inactiveMessage) {
        Facultad facultad = facultadRepository.findById(facultadId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_FACULTAD_NO_ENCONTRADA + facultadId));
        if (!facultad.getActivo()) {
            throw new BadRequestException(inactiveMessage);
        }
        return facultad;
    }

    private void assertUniqueCodigoYNombre(String codigo,
                                           String nombre,
                                           Long excludeId,
                                           String codigoMessagePrefix,
                                           String nombreMessagePrefix) {
        assertNotTaken(programaEstudioRepository.findByCodigo(codigo), excludeId, codigoMessagePrefix + codigo);
        assertNotTaken(programaEstudioRepository.findByNombre(nombre), excludeId, nombreMessagePrefix + nombre);
    }

    private void assertNotTaken(Optional<ProgramaEstudio> existing, Long excludeId, String message) {
        Predicate<ProgramaEstudio> isOther = programa -> excludeId == null || !programa.getId().equals(excludeId);
        if (existing.filter(isOther).isPresent()) {
            throw new BadRequestException(message);
        }
    }

    private void applyRequestData(ProgramaEstudio programa, ProgramaEstudioRequest request, Facultad facultad) {
        programa.setNombre(request.getNombre());
        programa.setCodigo(request.getCodigo());
        programa.setNivel(request.getNivel());
        programa.setModalidad(request.getModalidad());
        programa.setDuracionSemestres(request.getDuracionSemestres());
        programa.setCreditosTotales(request.getCreditosTotales());
        programa.setDescripcion(request.getDescripcion());
        programa.setObjetivos(request.getObjetivos());
        programa.setFacultad(facultad);
    }

    private ProgramaEstudio findProgramaOrThrow(Long id) {
        return programaEstudioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_PROGRAMA_NO_ENCONTRADO + id));
    }

    private List<ProgramaEstudioResponse> mapAll(List<ProgramaEstudio> programas) {
        return programas.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private ProgramaEstudioResponse convertToResponse(ProgramaEstudio programa) {
        ProgramaEstudioResponse response = new ProgramaEstudioResponse();
        response.setId(programa.getId());
        response.setNombre(programa.getNombre());
        response.setCodigo(programa.getCodigo());
        response.setNivel(programa.getNivel());
        response.setModalidad(programa.getModalidad());
        response.setDuracionSemestres(programa.getDuracionSemestres());
        response.setCreditosTotales(programa.getCreditosTotales());
        response.setActivo(programa.getActivo());
        response.setDisponible(programa.getDisponible());
        response.setDescripcion(programa.getDescripcion());
        response.setObjetivos(programa.getObjetivos());
        response.setFechaCreacion(programa.getFechaCreacion());
        response.setFechaActualizacion(programa.getFechaActualizacion());
        response.setFacultad(toFacultadBasic(programa.getFacultad()));
        return response;
    }

    private FacultadBasicResponse toFacultadBasic(Facultad facultad) {
        if (facultad == null) {
            return null;
        }
        FacultadBasicResponse facultadResponse = new FacultadBasicResponse();
        facultadResponse.setId(facultad.getId());
        facultadResponse.setNombre(facultad.getNombre());
        facultadResponse.setCodigo(facultad.getCodigo());
        return facultadResponse;
    }
}
