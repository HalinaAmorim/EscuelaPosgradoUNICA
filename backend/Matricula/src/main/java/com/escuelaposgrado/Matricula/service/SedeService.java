package com.escuelaposgrado.Matricula.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Matricula.dto.request.SedeRequest;
import com.escuelaposgrado.Matricula.dto.response.SedeResponse;
import com.escuelaposgrado.Matricula.exception.BadRequestException;
import com.escuelaposgrado.Matricula.exception.ResourceNotFoundException;
import com.escuelaposgrado.Matricula.model.entity.Sede;
import com.escuelaposgrado.Matricula.repository.SedeRepository;

/**
 * Servicio para gestionar las operaciones CRUD de Sedes
 */
@Service
@Transactional
public class SedeService {

    private static final String MSG_SEDE_NO_ENCONTRADA = "Sede no encontrada con ID: ";
    private static final String MSG_NOMBRE_DUPLICADO = "Ya existe una sede con el nombre: ";
    private static final String MSG_CODIGO_DUPLICADO = "Ya existe una sede con el código: ";

    private final SedeRepository sedeRepository;

    public SedeService(SedeRepository sedeRepository) {
        this.sedeRepository = sedeRepository;
    }

    @Transactional(readOnly = true)
    public List<SedeResponse> findAll() {
        return mapAll(sedeRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<SedeResponse> findAllActive() {
        return mapAll(sedeRepository.findByActivoTrue());
    }

    @Transactional(readOnly = true)
    public SedeResponse findById(Long id) {
        return convertToResponse(findSedeOrThrow(id));
    }

    public SedeResponse create(SedeRequest request) {
        if (sedeRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BadRequestException(MSG_NOMBRE_DUPLICADO + request.getNombre());
        }
        if (sedeRepository.existsByCodigo(request.getCodigo())) {
            throw new BadRequestException(MSG_CODIGO_DUPLICADO + request.getCodigo());
        }

        Sede sede = new Sede();
        applyRequestData(sede, request);
        sede.setActivo(true);

        return convertToResponse(sedeRepository.save(sede));
    }

    public SedeResponse update(Long id, SedeRequest request) {
        Sede sede = findSedeOrThrow(id);
        assertUniqueNombreYCodigo(request.getNombre(), request.getCodigo(), id);
        applyRequestData(sede, request);
        return convertToResponse(sedeRepository.save(sede));
    }

    public SedeResponse toggleActive(Long id) {
        Sede sede = findSedeOrThrow(id);
        sede.setActivo(!sede.getActivo());
        return convertToResponse(sedeRepository.save(sede));
    }

    public void delete(Long id) {
        Sede sede = findSedeOrThrow(id);
        sede.setActivo(false);
        sedeRepository.save(sede);
    }

    @Transactional(readOnly = true)
    public List<SedeResponse> findByNombreContaining(String nombre) {
        return mapAll(sedeRepository.findByNombreContainingIgnoreCase(nombre));
    }

    private void assertUniqueNombreYCodigo(String nombre, String codigo, Long excludeId) {
        assertNotTaken(sedeRepository.findByNombreIgnoreCase(nombre), excludeId, MSG_NOMBRE_DUPLICADO + nombre);
        assertNotTaken(sedeRepository.findByCodigo(codigo), excludeId, MSG_CODIGO_DUPLICADO + codigo);
    }

    private void assertNotTaken(Optional<Sede> existing, Long excludeId, String message) {
        Predicate<Sede> isOther = sede -> excludeId == null || !sede.getId().equals(excludeId);
        if (existing.filter(isOther).isPresent()) {
            throw new BadRequestException(message);
        }
    }

    private void applyRequestData(Sede sede, SedeRequest request) {
        sede.setNombre(request.getNombre());
        sede.setCodigo(request.getCodigo());
        sede.setDireccion(request.getDireccion());
        sede.setTelefono(request.getTelefono());
        sede.setEmail(request.getEmail());
    }

    private Sede findSedeOrThrow(Long id) {
        return sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_SEDE_NO_ENCONTRADA + id));
    }

    private List<SedeResponse> mapAll(List<Sede> sedes) {
        return sedes.stream().map(this::convertToResponse).toList();
    }

    private SedeResponse convertToResponse(Sede sede) {
        SedeResponse response = new SedeResponse();
        response.setId(sede.getId());
        response.setNombre(sede.getNombre());
        response.setCodigo(sede.getCodigo());
        response.setDireccion(sede.getDireccion());
        response.setTelefono(sede.getTelefono());
        response.setEmail(sede.getEmail());
        response.setActivo(sede.getActivo());
        response.setFechaCreacion(sede.getFechaCreacion());
        response.setFechaActualizacion(sede.getFechaActualizacion());
        return response;
    }
}
