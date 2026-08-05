package com.escuelaposgrado.Intranet.service.exception;

public class UsuarioNotFoundException
        extends RuntimeException {

    public UsuarioNotFoundException(String mensagem) {
        super(mensagem);
    }
}