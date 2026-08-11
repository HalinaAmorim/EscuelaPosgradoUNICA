package com.escuelaposgrado.Matricula.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Matricula.dto.request.TasaPagoRequest;
import com.escuelaposgrado.Matricula.dto.response.TasaPagoResponse;
import com.escuelaposgrado.Matricula.dto.response.nested.ProgramaEstudioBasicResponse;
import com.escuelaposgrado.Matricula.exception.BadRequestException;
import com.escuelaposgrado.Matricula.exception.ResourceNotFoundException;
import com.escuelaposgrado.Matricula.model.entity.ProgramaEstudio;
import com.escuelaposgrado.Matricula.model.entity.TasaPago;
import com.escuelaposgrado.Matricula.repository.ProgramaEstudioRepository;
import com.escuelaposgrado.Matricula.repository.TasaPagoRepository;

/**
 * Servicio para gestionar las operaciones CRUD de TasasPago
 */
@Service
@Transactional
public class TasaPagoService {

    private static final String MSG_TASA_NO_ENCONTRADA = "Tasa de pago no encontrada con ID: ";
    private static final String MSG_PROGRAMA_NO_ENCONTRADO = "Programa de estudio no encontrado con ID: ";
    private static final String MSG_CODIGO_DUPLICADO = "Ya existe una tasa de pago con el código: ";
    private static final String DEFAULT_MONEDA = "PEN";

    private final TasaPagoRepository tasaPagoRepository;
    private final ProgramaEstudioRepository programaEstudioRepository;

    public TasaPagoService(TasaPagoRepository tasaPagoRepository,
                           ProgramaEstudioRepository programaEstudioRepository) {
        this.tasaPagoRepository = tasaPagoRepository;
        this.programaEstudioRepository = programaEstudioRepository;
    }

    @Transactional(readOnly = true)
    public List<TasaPagoResponse> findAll() {
        return mapAll(tasaPagoRepository.findByActivoTrueOrderByConceptoAsc());
    }

    @Transactional(readOnly = true)
    public List<TasaPagoResponse> findAllActive() {
        return mapAll(tasaPagoRepository.findByActivoTrueOrderByConceptoAsc());
    }

    @Transactional(readOnly = true)
    public List<TasaPagoResponse> findAllObligatory() {
        return mapAll(tasaPagoRepository.findByObligatorioTrueAndActivoTrueOrderByConceptoAsc());
    }

    @Transactional(readOnly = true)
    public List<TasaPagoResponse> findByProgramaEstudioId(Long programaEstudioId) {
        return mapAll(tasaPagoRepository.findByProgramaEstudioIdAndActivoTrueOrderByConceptoAsc(programaEstudioId));
    }

    @Transactional(readOnly = true)
    public List<TasaPagoResponse> findByTipo(String tipo) {
        return mapAll(tasaPagoRepository.findByTipoIgnoreCaseAndActivoTrueOrderByConceptoAsc(tipo));
    }

    @Transactional(readOnly = true)
    public List<String> findDistinctTipos() {
        return tasaPagoRepository.findDistinctTipos();
    }

    @Transactional(readOnly = true)
    public List<TasaPagoResponse> searchByConcepto(String concepto) {
        return mapAll(tasaPagoRepository.findByConceptoIgnoreCaseContainingAndActivoTrueOrderByConceptoAsc(concepto));
    }

    @Transactional(readOnly = true)
    public TasaPagoResponse findById(Long id) {
        return convertToResponse(findTasaOrThrow(id));
    }

    public TasaPagoResponse create(TasaPagoRequest request) {
        assertUniqueCodigo(request.getCodigo(), null);
        ProgramaEstudio programa = findProgramaOrThrow(request.getProgramaEstudioId());

        TasaPago tasa = new TasaPago();
        applyRequestData(tasa, request, programa);
        tasa.setActivo(true);

        return convertToResponse(tasaPagoRepository.save(tasa));
    }

    public TasaPagoResponse update(Long id, TasaPagoRequest request) {
        TasaPago tasa = findTasaOrThrow(id);
        assertUniqueCodigo(request.getCodigo(), id);
        ProgramaEstudio programa = findProgramaOrThrow(request.getProgramaEstudioId());

        applyRequestData(tasa, request, programa);
        return convertToResponse(tasaPagoRepository.save(tasa));
    }

    public void toggleActive(Long id) {
        TasaPago tasa = findTasaOrThrow(id);
        tasa.setActivo(!tasa.getActivo());
        tasaPagoRepository.save(tasa);
    }

    public void delete(Long id) {
        TasaPago tasa = findTasaOrThrow(id);
        tasa.setActivo(false);
        tasaPagoRepository.save(tasa);
    }

    private void assertUniqueCodigo(String codigo, Long excludeId) {
        assertNotTaken(tasaPagoRepository.findByCodigo(codigo), excludeId, MSG_CODIGO_DUPLICADO + codigo);
    }

    private void assertNotTaken(Optional<TasaPago> existing, Long excludeId, String message) {
        Predicate<TasaPago> isOther = tasa -> excludeId == null || !tasa.getId().equals(excludeId);
        if (existing.filter(isOther).isPresent()) {
            throw new BadRequestException(message);
        }
    }

    private void applyRequestData(TasaPago tasa, TasaPagoRequest request, ProgramaEstudio programa) {
        tasa.setConcepto(request.getConcepto());
        tasa.setCodigo(request.getCodigo());
        tasa.setMonto(request.getMonto());
        tasa.setMoneda(request.getMoneda() != null ? request.getMoneda() : DEFAULT_MONEDA);
        tasa.setTipo(request.getTipo());
        tasa.setObligatorio(request.getObligatorio() != null ? request.getObligatorio() : false);
        tasa.setDescripcion(request.getDescripcion());
        tasa.setFechaVigenciaInicio(request.getFechaVigenciaInicio());
        tasa.setFechaVigenciaFin(request.getFechaVigenciaFin());
        tasa.setProgramaEstudio(programa);
    }

    private TasaPago findTasaOrThrow(Long id) {
        return tasaPagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_TASA_NO_ENCONTRADA + id));
    }

    private ProgramaEstudio findProgramaOrThrow(Long programaEstudioId) {
        return programaEstudioRepository.findById(programaEstudioId)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_PROGRAMA_NO_ENCONTRADO + programaEstudioId));
    }

    private List<TasaPagoResponse> mapAll(List<TasaPago> tasas) {
        return tasas.stream().map(this::convertToResponse).toList();
    }

    private TasaPagoResponse convertToResponse(TasaPago tasa) {
        TasaPagoResponse response = new TasaPagoResponse();
        response.setId(tasa.getId());
        response.setConcepto(tasa.getConcepto());
        response.setCodigo(tasa.getCodigo());
        response.setMonto(tasa.getMonto());
        response.setMoneda(tasa.getMoneda());
        response.setTipo(tasa.getTipo());
        response.setActivo(tasa.getActivo());
        response.setObligatorio(tasa.getObligatorio());
        response.setDescripcion(tasa.getDescripcion());
        response.setFechaVigenciaInicio(tasa.getFechaVigenciaInicio());
        response.setFechaVigenciaFin(tasa.getFechaVigenciaFin());
        response.setFechaCreacion(tasa.getFechaCreacion());
        response.setFechaActualizacion(tasa.getFechaActualizacion());
        response.setProgramaEstudio(toProgramaBasic(tasa.getProgramaEstudio()));
        return response;
    }

    private ProgramaEstudioBasicResponse toProgramaBasic(ProgramaEstudio programa) {
        if (programa == null) {
            return null;
        }
        ProgramaEstudioBasicResponse programaResponse = new ProgramaEstudioBasicResponse();
        programaResponse.setId(programa.getId());
        programaResponse.setNombre(programa.getNombre());
        programaResponse.setCodigo(programa.getCodigo());
        return programaResponse;
    }
}
