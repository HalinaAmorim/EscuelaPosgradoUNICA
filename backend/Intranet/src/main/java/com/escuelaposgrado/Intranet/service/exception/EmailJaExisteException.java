package com.escuelaposgrado.Intranet.service.exception;


public class EmailJaExisteException
        extends RuntimeException {

    public EmailJaExisteException(String email) {
        super("O email já existe: " + email);
    }
}
