package com.escuelaposgrado.Intranet.exception;

import com.escuelaposgrado.Intranet.dto.MensajeResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MensajeResponse> handleRuntimeException(
            RuntimeException e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MensajeResponse("Error: " + e.getMessage()));
    }
}