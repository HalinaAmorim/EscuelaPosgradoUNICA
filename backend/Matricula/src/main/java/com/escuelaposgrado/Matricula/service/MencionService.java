package com.escuelaposgrado.Matricula.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Matricula.dto.request.MencionRequest;
import com.escuelaposgrado.Matricula.dto.response.MencionResponse;
import com.escuelaposgrado.Matricula.dto.response.nested.ProgramaEstudioBasicResponse;
import com.escuelaposgrado.Matricula.exception.BadRequestException;
import com.escuelaposgrado.Matricula.exception.ResourceNotFoundException;
import com.escuelaposgrado.Matricula.model.entity.Mencion;
import com.escuelaposgrado.Matricula.model.entity.ProgramaEstudio;
import com.escuelaposgrado.Matricula.repository.MencionRepository;
import com.escuelaposgrado.Matricula.repository.ProgramaEstudioRepository;

/**
 * Servicio para gestionar las operaciones CRUD de Menciones
 */
@Service
@Transactional
public class MencionService {

    private static final String MSG_MENCION_NO_ENCONTRADA = "Mención no encontrada con ID: ";
    private static final String MSG_PROGRAMA_NO_ENCONTRADO = "Programa de estudio no encontrado con ID: ";
    private static final String MSG_NOMBRE_DUPLICADO = "Ya existe una mención con el nombre: ";
    private static final String MSG_CODIGO_DUPLICADO = "Ya existe una mención con el código: ";

    private final MencionRepository mencionRepository;
    private final ProgramaEstudioRepository programaEstudioRepository;

    public MencionService(MencionRepository mencionRepository,
                          ProgramaEstudioRepository programaEstudioRepository) {
        this.mencionRepository = mencionRepository;
        this.programaEstudioRepository = programaEstudioRepository;
    }

    @Transactional(readOnly = true)
    public List<MencionResponse> findAll() {
        return mapAll(mencionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<MencionResponse> findAllActive() {
        return mapAll(mencionRepository.findByActivoTrueOrderByNombreAsc());
    }

    @Transactional(readOnly = true)
    public List<MencionResponse> findByProgramaEstudioId(Long programaEstudioId) {
        return mapAll(mencionRepository.findByProgramaEstudioIdAndActivoTrueOrderByNombreAsc(programaEstudioId));
    }

    @Transactional(readOnly = true)
    public List<MencionResponse> findAvailableByProgramaEstudioId(Long programaEstudioId) {
        return mapAll(mencionRepository
                .findByProgramaEstudioIdAndActivoTrueAndDisponibleTrueOrderByNombreAsc(programaEstudioId));
    }

    @Transactional(readOnly = true)
    public MencionResponse findById(Long id) {
        return convertToResponse(findMencionOrThrow(id));
    }

    public MencionResponse create(MencionRequest request) {
        ProgramaEstudio programaEstudio = resolveActivePrograma(
                request.getProgramaEstudioId(),
                "No se puede crear la mención en un programa de estudio inactivo");
        assertUniqueNombreYCodigo(request.getNombre(), request.getCodigo(), null);

        Mencion mencion = new Mencion();
        applyRequestData(mencion, request, programaEstudio);
        mencion.setActivo(true);
        mencion.setDisponible(true);

        return convertToResponse(mencionRepository.save(mencion));
    }

    public MencionResponse update(Long id, MencionRequest request) {
        Mencion mencion = findMencionOrThrow(id);
        ProgramaEstudio programaEstudio = resolveActivePrograma(
                request.getProgramaEstudioId(),
                "No se puede asignar la mención a un programa de estudio inactivo");
        assertUniqueNombreYCodigo(request.getNombre(), request.getCodigo(), id);

        applyRequestData(mencion, request, programaEstudio);
        return convertToResponse(mencionRepository.save(mencion));
    }

    public MencionResponse toggleActive(Long id) {
        Mencion mencion = findMencionOrThrow(id);
        mencion.setActivo(!mencion.getActivo());
        return convertToResponse(mencionRepository.save(mencion));
    }

    public MencionResponse toggleDisponible(Long id) {
        Mencion mencion = findMencionOrThrow(id);
        mencion.setDisponible(!mencion.getDisponible());
        return convertToResponse(mencionRepository.save(mencion));
    }

    public void delete(Long id) {
        Mencion mencion = findMencionOrThrow(id);
        mencion.setActivo(false);
        mencion.setDisponible(false);
        mencionRepository.save(mencion);
    }

    private ProgramaEstudio resolveActivePrograma(Long programaEstudioId, String inactiveMessage) {
        ProgramaEstudio programaEstudio = programaEstudioRepository.findById(programaEstudioId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_PROGRAMA_NO_ENCONTRADO + programaEstudioId));
        if (!programaEstudio.getActivo()) {
            throw new BadRequestException(inactiveMessage);
        }
        return programaEstudio;
    }

    private void assertUniqueNombreYCodigo(String nombre, String codigo, Long excludeId) {
        assertNotTaken(mencionRepository.findByNombre(nombre), excludeId, MSG_NOMBRE_DUPLICADO + nombre);
        assertNotTaken(mencionRepository.findByCodigo(codigo), excludeId, MSG_CODIGO_DUPLICADO + codigo);
    }

    private void assertNotTaken(Optional<Mencion> existing, Long excludeId, String message) {
        Predicate<Mencion> isOther = mencion -> excludeId == null || !mencion.getId().equals(excludeId);
        if (existing.filter(isOther).isPresent()) {
            throw new BadRequestException(message);
        }
    }

    private void applyRequestData(Mencion mencion, MencionRequest request, ProgramaEstudio programaEstudio) {
        mencion.setNombre(request.getNombre());
        mencion.setCodigo(request.getCodigo());
        mencion.setDescripcion(request.getDescripcion());
        mencion.setRequisitos(request.getRequisitos());
        mencion.setProgramaEstudio(programaEstudio);
    }

    private Mencion findMencionOrThrow(Long id) {
        return mencionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_MENCION_NO_ENCONTRADA + id));
    }

    private List<MencionResponse> mapAll(List<Mencion> menciones) {
        return menciones.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private MencionResponse convertToResponse(Mencion mencion) {
        MencionResponse response = new MencionResponse();
        response.setId(mencion.getId());
        response.setNombre(mencion.getNombre());
        response.setCodigo(mencion.getCodigo());
        response.setActivo(mencion.getActivo());
        response.setDisponible(mencion.getDisponible());
        response.setDescripcion(mencion.getDescripcion());
        response.setRequisitos(mencion.getRequisitos());
        response.setFechaCreacion(mencion.getFechaCreacion());
        response.setFechaActualizacion(mencion.getFechaActualizacion());
        response.setProgramaEstudio(toProgramaBasic(mencion.getProgramaEstudio()));
        return response;
    }

    private ProgramaEstudioBasicResponse toProgramaBasic(ProgramaEstudio programa) {
        if (programa == null) {
            return null;
        }
        ProgramaEstudioBasicResponse programaBasic = new ProgramaEstudioBasicResponse();
        programaBasic.setId(programa.getId());
        programaBasic.setNombre(programa.getNombre());
        programaBasic.setCodigo(programa.getCodigo());
        return programaBasic;
    }
}
