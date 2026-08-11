package com.escuelaposgrado.Matricula.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Matricula.dto.request.ComisionUnidadPosgradoRequest;
import com.escuelaposgrado.Matricula.dto.response.ComisionUnidadPosgradoResponse;
import com.escuelaposgrado.Matricula.dto.response.nested.FacultadBasicResponse;
import com.escuelaposgrado.Matricula.exception.ResourceNotFoundException;
import com.escuelaposgrado.Matricula.model.entity.ComisionUnidadPosgrado;
import com.escuelaposgrado.Matricula.model.entity.Facultad;
import com.escuelaposgrado.Matricula.repository.ComisionUnidadPosgradoRepository;
import com.escuelaposgrado.Matricula.repository.FacultadRepository;

/**
 * Servicio para la gestión de ComisionUnidadPosgrado
 */
@Service
@Transactional
public class ComisionUnidadPosgradoService {

    private static final String MSG_COMISION_NO_ENCONTRADA = "ComisionUnidadPosgrado no encontrada con ID: ";
    private static final String MSG_FACULTAD_NO_ENCONTRADA = "Facultad no encontrada con ID: ";
    private static final String MSG_CODIGO_DUPLICADO = "Ya existe una comisión con el código: ";

    private final ComisionUnidadPosgradoRepository comisionRepository;
    private final FacultadRepository facultadRepository;

    public ComisionUnidadPosgradoService(ComisionUnidadPosgradoRepository comisionRepository,
                                         FacultadRepository facultadRepository) {
        this.comisionRepository = comisionRepository;
        this.facultadRepository = facultadRepository;
    }

    @Transactional(readOnly = true)
    public List<ComisionUnidadPosgradoResponse> findAll() {
        return mapAll(comisionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ComisionUnidadPosgradoResponse findById(Long id) {
        return convertToResponse(findComisionOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ComisionUnidadPosgradoResponse> findByFacultadId(Long facultadId) {
        return mapAll(comisionRepository.findByFacultadIdAndActivoTrueOrderByNombreAsc(facultadId));
    }

    @Transactional(readOnly = true)
    public List<ComisionUnidadPosgradoResponse> findByActivoTrue() {
        return mapAll(comisionRepository.findByActivoTrueOrderByNombreAsc());
    }

    @Transactional(readOnly = true)
    public List<ComisionUnidadPosgradoResponse> findByNombreContaining(String nombre) {
        String nombreLower = nombre.toLowerCase();
        return comisionRepository.findAll().stream()
                .filter(comision -> comision.getNombre().toLowerCase().contains(nombreLower))
                .map(this::convertToResponse)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ComisionUnidadPosgradoResponse create(ComisionUnidadPosgradoRequest request) {
        Facultad facultad = findFacultadOrThrow(request.getFacultadId());
        assertUniqueCodigo(request.getCodigo());

        ComisionUnidadPosgrado comision = new ComisionUnidadPosgrado();
        mapRequestToEntity(request, comision);
        comision.setFacultad(facultad);
        comision.setActivo(true);
        comision.setFechaCreacion(LocalDateTime.now());
        comision.setFechaActualizacion(LocalDateTime.now());

        return convertToResponse(comisionRepository.save(comision));
    }

    public ComisionUnidadPosgradoResponse update(Long id, ComisionUnidadPosgradoRequest request) {
        ComisionUnidadPosgrado existingComision = findComisionOrThrow(id);
        Facultad facultad = findFacultadOrThrow(request.getFacultadId());

        if (!existingComision.getCodigo().equals(request.getCodigo())) {
            assertUniqueCodigo(request.getCodigo());
        }

        mapRequestToEntity(request, existingComision);
        existingComision.setFacultad(facultad);
        existingComision.setFechaActualizacion(LocalDateTime.now());

        return convertToResponse(comisionRepository.save(existingComision));
    }

    public void delete(Long id) {
        comisionRepository.delete(findComisionOrThrow(id));
    }

    public ComisionUnidadPosgradoResponse toggleActivo(Long id, Boolean activo) {
        ComisionUnidadPosgrado comision = findComisionOrThrow(id);
        comision.setActivo(activo);
        comision.setFechaActualizacion(LocalDateTime.now());
        return convertToResponse(comisionRepository.save(comision));
    }

    private void assertUniqueCodigo(String codigo) {
        if (comisionRepository.findByCodigo(codigo).isPresent()) {
            throw new IllegalArgumentException(MSG_CODIGO_DUPLICADO + codigo);
        }
    }

    private ComisionUnidadPosgrado findComisionOrThrow(Long id) {
        return comisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_COMISION_NO_ENCONTRADA + id));
    }

    private Facultad findFacultadOrThrow(Long facultadId) {
        return facultadRepository.findById(facultadId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_FACULTAD_NO_ENCONTRADA + facultadId));
    }

    private List<ComisionUnidadPosgradoResponse> mapAll(List<ComisionUnidadPosgrado> comisiones) {
        return comisiones.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private ComisionUnidadPosgradoResponse convertToResponse(ComisionUnidadPosgrado comision) {
        ComisionUnidadPosgradoResponse response = new ComisionUnidadPosgradoResponse();
        response.setId(comision.getId());
        response.setNombre(comision.getNombre());
        response.setCodigo(comision.getCodigo());
        response.setTipo(comision.getTipo());
        response.setPresidente(comision.getPresidente());
        response.setSecretario(comision.getSecretario());
        response.setMiembros(comision.getMiembros());
        response.setActivo(comision.getActivo());
        response.setDescripcion(comision.getDescripcion());
        response.setFunciones(comision.getFunciones());
        response.setFechaInicioGestion(comision.getFechaInicioGestion());
        response.setFechaFinGestion(comision.getFechaFinGestion());
        response.setFechaCreacion(comision.getFechaCreacion());
        response.setFechaActualizacion(comision.getFechaActualizacion());
        response.setFacultad(toFacultadBasic(comision.getFacultad()));
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

    private void mapRequestToEntity(ComisionUnidadPosgradoRequest request, ComisionUnidadPosgrado comision) {
        comision.setNombre(request.getNombre());
        comision.setCodigo(request.getCodigo());
        comision.setTipo(request.getTipo());
        comision.setPresidente(request.getPresidente());
        comision.setSecretario(request.getSecretario());
        comision.setMiembros(request.getMiembros());
        comision.setDescripcion(request.getDescripcion());
        comision.setFunciones(request.getFunciones());
        comision.setFechaInicioGestion(request.getFechaInicioGestion());
        comision.setFechaFinGestion(request.getFechaFinGestion());
    }
}
