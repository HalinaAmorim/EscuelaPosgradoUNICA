package com.escuelaposgrado.Autenticacion.service.excel;

/**
 * Contenido estático de plantillas/export Excel (separado de la orquestación).
 */
public final class ExcelTemplateContent {

    public static final String[] EXPORT_HEADERS = {
        "ID", "Username", "Email", "Nombres", "Apellidos", "DNI", "Teléfono",
        "Dirección", "Rol", "Activo", "Código Estudiante", "Código Docente",
        "Especialidad", "Programa de Interés", "Fecha Creación", "Último Acceso"
    };

    public static final String[] TEMPLATE_HEADERS = {
        "Username", "Email", "Password", "Nombres", "Apellidos", "DNI", "Teléfono",
        "Dirección", "Rol", "Código Estudiante", "Código Docente",
        "Especialidad", "Programa de Interés"
    };

    public static final String[] TEMPLATE_EXAMPLE = {
        "jperez", "jperez@email.com", "password123", "Juan", "Pérez", "12345678",
        "987654321", "Av. Principal 123", "ALUMNO", "E001", "", "", "Maestría en Sistemas"
    };

    public static final String[] IMPORT_INSTRUCTIONS = {
        "CAMPOS OBLIGATORIOS:",
        "- Username: Nombre de usuario único",
        "- Email: Correo electrónico válido",
        "- Password: Contraseña (mínimo 6 caracteres)",
        "- Nombres: Nombres del usuario",
        "- Apellidos: Apellidos del usuario",
        "- DNI: Documento de identidad (8 dígitos)",
        "- Rol: ADMIN, DOCENTE, ALUMNO, COORDINADOR, POSTULANTE",
        "",
        "CAMPOS OPCIONALES:",
        "- Teléfono: Número de contacto",
        "- Dirección: Dirección de residencia",
        "- Código Estudiante: Solo para ALUMNO y POSTULANTE",
        "- Código Docente: Solo para DOCENTE y COORDINADOR",
        "- Especialidad: Para DOCENTE y COORDINADOR",
        "- Programa de Interés: Para POSTULANTE",
        "",
        "ROLES Y CAMPOS ESPECÍFICOS:",
        "   - ADMIN: No requiere campos específicos adicionales",
        "   - ALUMNO: Puede tener Código Estudiante",
        "   - POSTULANTE: Puede tener Código Estudiante y Programa de Interés",
        "   - DOCENTE: Puede tener Código Docente y Especialidad",
        "   - COORDINADOR: Puede tener Código Docente y Especialidad"
    };

    private ExcelTemplateContent() {
    }
}
