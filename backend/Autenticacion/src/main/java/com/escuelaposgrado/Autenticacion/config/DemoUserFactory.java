package com.escuelaposgrado.Autenticacion.config;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.escuelaposgrado.Autenticacion.model.entity.Usuario;
import com.escuelaposgrado.Autenticacion.model.enums.Role;

/**
 * Factory de usuarios demo para inicialización (elimina duplicación en DataInitializer).
 */
final class DemoUserFactory {

    private DemoUserFactory() {
    }

    static Usuario admin(PasswordEncoder encoder) {
        return base("admin", "admin@unica.edu.pe", "admin123", "Administrador", "del", "Sistema",
                Role.ADMIN, encoder);
    }

    static Usuario docente(PasswordEncoder encoder) {
        Usuario docente = base("docente.demo", "docente.demo@unica.edu.pe", "docente123",
                "María Elena", "García", "Rodríguez", Role.DOCENTE, encoder);
        docente.setCodigoDocente("DOC001");
        docente.setEspecialidad("Ingeniería de Sistemas");
        docente.setDni("12345678");
        docente.setTelefono("956123456");
        return docente;
    }

    static Usuario coordinador(PasswordEncoder encoder) {
        Usuario coordinador = base("coordinador.demo", "coordinador.demo@unica.edu.pe", "coordinador123",
                "Carlos Antonio", "Mendoza", "Silva", Role.COORDINADOR, encoder);
        coordinador.setCodigoDocente("COORD001");
        coordinador.setEspecialidad("Gestión Académica");
        coordinador.setDni("87654321");
        coordinador.setTelefono("956654321");
        return coordinador;
    }

    static Usuario alumno(PasswordEncoder encoder) {
        Usuario alumno = base("alumno.demo", "alumno.demo@unica.edu.pe", "alumno123",
                "Ana Sofía", "López", "Fernández", Role.ALUMNO, encoder);
        alumno.setCodigoEstudiante("EST2024001");
        alumno.setDni("11223344");
        alumno.setTelefono("956111222");
        return alumno;
    }

    static Usuario postulante(PasswordEncoder encoder) {
        Usuario postulante = base("postulante.demo", "postulante.demo@gmail.com", "postulante123",
                "Luis Miguel", "Vargas", "Torres", Role.POSTULANTE, encoder);
        postulante.setCodigoEstudiante("POST2024001");
        postulante.setProgramaInteres("Maestría en Ingeniería de Sistemas");
        postulante.setDni("55667788");
        postulante.setTelefono("956333444");
        return postulante;
    }

    private static Usuario base(
            String username,
            String email,
            String rawPassword,
            String nombres,
            String apellidoPaterno,
            String apellidoMaterno,
            Role role,
            PasswordEncoder encoder) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPassword(encoder.encode(rawPassword));
        usuario.setNombres(nombres);
        usuario.setApellidoPaterno(apellidoPaterno);
        usuario.setApellidoMaterno(apellidoMaterno);
        usuario.setRole(role);
        usuario.setActivo(true);
        return usuario;
    }
}
