package com.escuelaposgrado.Autenticacion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.escuelaposgrado.Autenticacion.dto.request.RegistroRequest;
import com.escuelaposgrado.Autenticacion.model.enums.Role;
import com.escuelaposgrado.Autenticacion.repository.UsuarioRepository;

@Service
public class UsuarioValidationService {

    @Autowired
    private UsuarioRepository repository;

    /**
     * Valida os campos únicos de um usuário antes do cadastro.
     *
     * @return mensagem de erro ou null caso esteja tudo válido.
     */
    public String validateUniqueFields(RegistroRequest request) {

        String error;

        error = validarUsername(request.getUsername());
        if (error != null) {
            return error;
        }

        error = validarEmail(request.getEmail());
        if (error != null) {
            return error;
        }

        error = validarCodigoEstudiante(
                request.getRole(),
                request.getCodigoEstudiante());

        if (error != null) {
            return error;
        }

        error = validarCodigoDocente(
                request.getRole(),
                request.getCodigoDocente());

        if (error != null) {
            return error;
        }

        return validarDni(request.getDni());
    }

    private String validarUsername(String username) {

        if (repository.existsByUsername(username)) {
            return "El nombre de usuario ya está en uso!";
        }

        return null;
    }

    private String validarEmail(String email) {

        if (repository.existsByEmail(email)) {
            return "El email ya está en uso!";
        }

        return null;
    }

    private String validarCodigoEstudiante(Role role, String codigo) {

        if ((role == Role.ALUMNO || role == Role.POSTULANTE)
                && codigo != null
                && !codigo.trim().isEmpty()
                && repository.findByCodigoEstudiante(codigo).isPresent()) {

            return "El código de estudiante ya está en uso!";
        }

        return null;
    }

    private String validarCodigoDocente(Role role, String codigo) {

        if ((role == Role.DOCENTE || role == Role.COORDINADOR)
                && codigo != null
                && !codigo.trim().isEmpty()
                && repository.findByCodigoDocente(codigo).isPresent()) {

            return "El código de docente ya está en uso!";
        }

        return null;
    }

    private String validarDni(String dni) {

        if (dni != null
                && !dni.trim().isEmpty()
                && repository.findByDni(dni).isPresent()) {

            return "El DNI ya está registrado!";
        }

        return null;
    }

}