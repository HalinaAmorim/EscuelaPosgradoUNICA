package com.escuelaposgrado.Autenticacion.exception;

/**
 * Excepción de dominio para errores de importación/exportación Excel.
 */
public class ExcelProcessingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExcelProcessingException(String message) {
        super(message);
    }

    public ExcelProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ExcelProcessingException withMessage(String message) {
        return new ExcelProcessingException(message);
    }

    public static ExcelProcessingException withCause(String message, Throwable cause) {
        return new ExcelProcessingException(message, cause);
    }
}
