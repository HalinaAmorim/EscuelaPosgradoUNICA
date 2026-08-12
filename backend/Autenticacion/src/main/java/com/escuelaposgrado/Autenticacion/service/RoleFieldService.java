package com.escuelaposgrado.Autenticacion.service;

import org.springframework.stereotype.Service;

import com.escuelaposgrado.Autenticacion.dto.request.ActualizarUsuarioAdminRequest;
import com.escuelaposgrado.Autenticacion.dto.request.RegistroRequest;
import com.escuelaposgrado.Autenticacion.model.entity.Usuario;
import com.escuelaposgrado.Autenticacion.model.enums.Role;

@Service
public class RoleFieldService {

    public void apply(Usuario usuario, RegistroRequest request) {

        applyRoleFields(
                usuario,
                request.getRole(),
                request.getCodigoEstudiante(),
                request.getCodigoDocente(),
                request.getEspecialidad(),
                request.getProgramaInteres());
    }

    public void apply(Usuario usuario, ActualizarUsuarioAdminRequest request) {

        applyRoleFields(
                usuario,
                request.getRole(),
                request.getCodigoEstudiante(),
                request.getCodigoDocente(),
                request.getEspecialidad(),
                request.getProgramaInteres());
    }

    private void applyRoleFields(
            Usuario usuario,
            Role role,
            String codigoEstudiante,
            String codigoDocente,
            String especialidad,
            String programaInteres) {

        limparCampos(usuario);

        if (role == Role.ALUMNO || role == Role.POSTULANTE) {

            preencherAluno(usuario, codigoEstudiante);

            if (role == Role.POSTULANTE) {
                usuario.setProgramaInteres(trim(programaInteres));
            }

            return;
        }

        if (role == Role.DOCENTE || role == Role.COORDINADOR) {

            preencherDocente(
                    usuario,
                    codigoDocente,
                    especialidad);
        }
    }

    private void limparCampos(Usuario usuario) {

        usuario.setCodigoEstudiante(null);
        usuario.setCodigoDocente(null);
        usuario.setEspecialidad(null);
        usuario.setProgramaInteres(null);
    }

    private void preencherAluno(
            Usuario usuario,
            String codigoEstudiante) {

        usuario.setCodigoEstudiante(trim(codigoEstudiante));
    }

    private void preencherDocente(
            Usuario usuario,
            String codigoDocente,
            String especialidad) {

        usuario.setCodigoDocente(trim(codigoDocente));
        usuario.setEspecialidad(trim(especialidad));
    }

    private String trim(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

}