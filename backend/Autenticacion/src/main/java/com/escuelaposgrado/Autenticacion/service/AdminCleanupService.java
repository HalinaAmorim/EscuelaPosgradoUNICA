package com.escuelaposgrado.Autenticacion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.escuelaposgrado.Autenticacion.dto.response.MessageResponse;

@Service
public class AdminCleanupService {

    @Autowired
    private DataCleanupService cleanupService;

    public ResponseEntity<MessageResponse> limpiarDuplicados() {

        try {

            if (!cleanupService.existenDuplicados()) {

                return ResponseEntity.ok(
                        new MessageResponse(
                                "No se encontraron registros duplicados"));

            }

            cleanupService.limpiarDuplicados();

            return ResponseEntity.ok(
                    new MessageResponse(
                            "Limpieza de duplicados completada exitosamente"));

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body(
                            new MessageResponse(
                                    "Error al limpiar duplicados: "
                                            + e.getMessage(),
                                    false));

        }

    }

}