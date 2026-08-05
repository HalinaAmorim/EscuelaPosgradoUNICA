package com.escuelaposgrado.Intranet.service.exception;


public class CodigoJaExisteException
        extends RuntimeException {

    public CodigoJaExisteException(String codigo) {
        super("O código já existe: " + codigo);
    }
}