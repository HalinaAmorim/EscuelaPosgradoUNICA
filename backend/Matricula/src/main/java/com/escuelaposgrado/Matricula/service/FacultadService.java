package com.escuelaposgrado.Matricula.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Matricula.dto.request.FacultadRequest;
import com.escuelaposgrado.Matricula.dto.response.FacultadResponse;
import com.escuelaposgrado.Matricula.exception.BadRequestException;
import com.escuelaposgrado.Matricula.exception.ResourceNotFoundException;
import com.escuelaposgrado.Matricula.model.entity.Facultad;
import com.escuelaposgrado.Matricula.repository.FacultadRepository;

/**
 * Servicio para gestionar las operaciones CRUD de Facultades
 */
@Service
@Transactional
public class FacultadService {

    private static final String MSG_FACULTAD_NO_ENCONTRADA = "Facultad no encontrada con ID: ";
    private static final String MSG_CODIGO_DUPLICADO = "Ya existe una facultad con el código ";
    private static final String MSG_NOMBRE_DUPLICADO = "Ya existe una facultad con el nombre ";

    private final FacultadRepository facultadRepository;

    public FacultadService(FacultadRepository facultadRepository) {
        this.facultadRepository = facultadRepository;
    }

    @Transactional(readOnly = true)
    public List<FacultadResponse> findAll() {
        return mapAll(facultadRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<FacultadResponse> findAllActive() {
        return mapAll(facultadRepository.findByActivoTrueOrderByNombreAsc());
    }

    @Transactional(readOnly = true)
    public FacultadResponse findById(Long id) {
        return convertToResponse(findFacultadOrThrow(id));
    }

    public FacultadResponse create(FacultadRequest request) {
        assertUniqueCodigoYNombre(request.getCodigo(), request.getNombre(), null);

        Facultad facultad = new Facultad();
        applyRequestData(facultad, request);
        facultad.setActivo(true);

        return convertToResponse(facultadRepository.save(facultad));
    }

    public FacultadResponse update(Long id, FacultadRequest request) {
        Facultad facultad = findFacultadOrThrow(id);
        assertUniqueCodigoYNombre(request.getCodigo(), request.getNombre(), id);
        applyRequestData(facultad, request);
        return convertToResponse(facultadRepository.save(facultad));
    }

    public FacultadResponse toggleActive(Long id) {
        Facultad facultad = findFacultadOrThrow(id);
        facultad.setActivo(!facultad.getActivo());
        return convertToResponse(facultadRepository.save(facultad));
    }

    public void delete(Long id) {
        Facultad facultad = findFacultadOrThrow(id);
        facultad.setActivo(false);
        facultadRepository.save(facultad);
    }

    @Transactional(readOnly = true)
    public List<FacultadResponse> findByNombreContaining(String nombre) {
        String nombreLower = nombre.toLowerCase();
        return facultadRepository.findAll().stream()
                .filter(facultad -> facultad.getNombre().toLowerCase().contains(nombreLower) && facultad.getActivo())
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FacultadResponse> findByDecanoContaining(String decano) {
        return mapAll(facultadRepository.findByDecanoIgnoreCaseContainingAndActivoTrueOrderByNombreAsc(decano));
    }

    @Transactional(readOnly = true)
    public List<FacultadResponse> findFacultadesConProgramasActivos() {
        return mapAll(facultadRepository.findFacultadesConProgramasActivos());
    }

    private void assertUniqueCodigoYNombre(String codigo, String nombre, Long excludeId) {
        assertNotTaken(facultadRepository.findByCodigo(codigo), excludeId, MSG_CODIGO_DUPLICADO + codigo);
        assertNotTaken(facultadRepository.findByNombre(nombre), excludeId, MSG_NOMBRE_DUPLICADO + nombre);
    }

    private void assertNotTaken(Optional<Facultad> existing, Long excludeId, String message) {
        Predicate<Facultad> isOther = facultad -> excludeId == null || !facultad.getId().equals(excludeId);
        if (existing.filter(isOther).isPresent()) {
            throw new BadRequestException(message);
        }
    }

    private void applyRequestData(Facultad facultad, FacultadRequest request) {
        facultad.setNombre(request.getNombre());
        facultad.setCodigo(request.getCodigo());
        facultad.setDescripcion(request.getDescripcion());
        facultad.setDecano(request.getDecano());
    }

    private Facultad findFacultadOrThrow(Long id) {
        return facultadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_FACULTAD_NO_ENCONTRADA + id));
    }

    private List<FacultadResponse> mapAll(List<Facultad> facultades) {
        return facultades.stream().map(this::convertToResponse).toList();
    }

    private FacultadResponse convertToResponse(Facultad facultad) {
        FacultadResponse response = new FacultadResponse();
        response.setId(facultad.getId());
        response.setNombre(facultad.getNombre());
        response.setCodigo(facultad.getCodigo());
        response.setDescripcion(facultad.getDescripcion());
        response.setDecano(facultad.getDecano());
        response.setActivo(facultad.getActivo());
        response.setFechaCreacion(facultad.getFechaCreacion());
        response.setFechaActualizacion(facultad.getFechaActualizacion());
        return response;
    }
}
