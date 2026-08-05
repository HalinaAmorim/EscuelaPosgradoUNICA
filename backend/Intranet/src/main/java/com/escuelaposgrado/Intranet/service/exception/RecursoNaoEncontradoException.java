package com.escuelaposgrado.Intranet.service.exception;


public class RecursoNaoEncontradoException
        extends RuntimeException {

    public RecursoNaoEncontradoException(
            String message) {

        super(message);
    }
}