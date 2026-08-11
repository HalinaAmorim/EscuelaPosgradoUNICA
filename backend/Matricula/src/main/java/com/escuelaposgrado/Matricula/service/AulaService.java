package com.escuelaposgrado.Matricula.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Matricula.dto.request.AulaRequest;
import com.escuelaposgrado.Matricula.dto.response.AulaResponse;
import com.escuelaposgrado.Matricula.dto.response.nested.SedeBasicResponse;
import com.escuelaposgrado.Matricula.exception.BadRequestException;
import com.escuelaposgrado.Matricula.exception.ResourceNotFoundException;
import com.escuelaposgrado.Matricula.model.entity.Aula;
import com.escuelaposgrado.Matricula.model.entity.Sede;
import com.escuelaposgrado.Matricula.repository.AulaRepository;
import com.escuelaposgrado.Matricula.repository.SedeRepository;

/**
 * Servicio para gestionar las operaciones CRUD de Aulas
 */
@Service
@Transactional
public class AulaService {

    private static final String MSG_AULA_NO_ENCONTRADA = "Aula no encontrada con ID: ";
    private static final String MSG_SEDE_NO_ENCONTRADA = "Sede no encontrada con ID: ";
    private static final String MSG_CODIGO_DUPLICADO_EN_SEDE = "Ya existe un aula con el código ";

    private final AulaRepository aulaRepository;
    private final SedeRepository sedeRepository;

    public AulaService(AulaRepository aulaRepository, SedeRepository sedeRepository) {
        this.aulaRepository = aulaRepository;
        this.sedeRepository = sedeRepository;
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> findAll() {
        return mapAll(aulaRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> findAllActive() {
        return mapAll(aulaRepository.findByActivoTrueOrderByNombreAsc());
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> findBySedeId(Long sedeId) {
        return mapAll(aulaRepository.findBySedeIdAndActivoTrueOrderByNombreAsc(sedeId));
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> findActiveBySedeId(Long sedeId) {
        return mapAll(aulaRepository.findBySedeIdAndActivoTrueOrderByNombreAsc(sedeId));
    }

    @Transactional(readOnly = true)
    public AulaResponse findById(Long id) {
        return convertToResponse(findAulaOrThrow(id));
    }

    public AulaResponse create(AulaRequest request) {
        Sede sede = resolveActiveSede(request.getSedeId(), "No se puede crear el aula en una sede inactiva");
        assertUniqueCodigoEnSede(request.getCodigo(), request.getSedeId(), null);

        Aula aula = new Aula();
        applyRequestData(aula, request, sede);
        aula.setActivo(true);

        return convertToResponse(aulaRepository.save(aula));
    }

    public AulaResponse update(Long id, AulaRequest request) {
        Aula aula = findAulaOrThrow(id);
        Sede sede = resolveActiveSede(request.getSedeId(), "No se puede asignar el aula a una sede inactiva");
        assertUniqueCodigoEnSede(request.getCodigo(), request.getSedeId(), id);

        applyRequestData(aula, request, sede);
        return convertToResponse(aulaRepository.save(aula));
    }

    public AulaResponse toggleActive(Long id) {
        Aula aula = findAulaOrThrow(id);
        aula.setActivo(!aula.getActivo());
        return convertToResponse(aulaRepository.save(aula));
    }

    public void delete(Long id) {
        Aula aula = findAulaOrThrow(id);
        aula.setActivo(false);
        aulaRepository.save(aula);
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> findByNombreContaining(String nombre) {
        String nombreLower = nombre.toLowerCase();
        return aulaRepository.findAll().stream()
                .filter(aula -> aula.getNombre().toLowerCase().contains(nombreLower) && aula.getActivo())
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> findByCapacidadMinima(Integer capacidadMinima) {
        return mapAll(aulaRepository.findByCapacidadGreaterThanEqualAndActivoTrueOrderByCapacidadAsc(capacidadMinima));
    }

    private Sede resolveActiveSede(Long sedeId, String inactiveMessage) {
        Sede sede = sedeRepository.findById(sedeId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_SEDE_NO_ENCONTRADA + sedeId));
        if (!sede.getActivo()) {
            throw new BadRequestException(inactiveMessage);
        }
        return sede;
    }

    private void assertUniqueCodigoEnSede(String codigo, Long sedeId, Long excludeId) {
        Optional<Aula> existing = aulaRepository.findByCodigo(codigo);
        if (existing.isEmpty()) {
            return;
        }
        Aula aula = existing.get();
        boolean sameSede = aula.getSede().getId().equals(sedeId);
        boolean isOther = excludeId == null || !aula.getId().equals(excludeId);
        if (sameSede && isOther) {
            throw new BadRequestException(MSG_CODIGO_DUPLICADO_EN_SEDE + codigo + " en esta sede");
        }
    }

    private void applyRequestData(Aula aula, AulaRequest request, Sede sede) {
        aula.setNombre(request.getNombre());
        aula.setCodigo(request.getCodigo());
        aula.setCapacidad(request.getCapacidad());
        aula.setTipo(request.getTipo());
        aula.setEquipamiento(request.getEquipamiento());
        aula.setDescripcion(request.getDescripcion());
        aula.setSede(sede);
    }

    private Aula findAulaOrThrow(Long id) {
        return aulaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_AULA_NO_ENCONTRADA + id));
    }

    private List<AulaResponse> mapAll(List<Aula> aulas) {
        return aulas.stream().map(this::convertToResponse).toList();
    }

    private AulaResponse convertToResponse(Aula aula) {
        AulaResponse response = new AulaResponse();
        response.setId(aula.getId());
        response.setNombre(aula.getNombre());
        response.setCodigo(aula.getCodigo());
        response.setCapacidad(aula.getCapacidad());
        response.setTipo(aula.getTipo());
        response.setEquipamiento(aula.getEquipamiento());
        response.setDescripcion(aula.getDescripcion());
        response.setActivo(aula.getActivo());
        response.setFechaCreacion(aula.getFechaCreacion());
        response.setFechaActualizacion(aula.getFechaActualizacion());
        response.setSede(toSedeBasic(aula.getSede()));
        return response;
    }

    private SedeBasicResponse toSedeBasic(Sede sede) {
        if (sede == null) {
            return null;
        }
        SedeBasicResponse sedeBasic = new SedeBasicResponse();
        sedeBasic.setId(sede.getId());
        sedeBasic.setNombre(sede.getNombre());
        sedeBasic.setCodigo(sede.getCodigo());
        return sedeBasic;
    }
}
