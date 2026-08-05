package com.escuelaposgrado.Autenticacion.exception;

/**
 * Excepción de dominio para errores de importación/exportación Excel.
 */
public class ExcelProcessingException extends RuntimeException {

    public ExcelProcessingException(String message) {
        super(message);
    }

    public ExcelProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
