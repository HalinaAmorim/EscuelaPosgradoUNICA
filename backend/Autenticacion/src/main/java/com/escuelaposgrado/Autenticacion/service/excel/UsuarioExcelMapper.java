package com.escuelaposgrado.Autenticacion.service.excel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

import com.escuelaposgrado.Autenticacion.dto.request.RegistroRequest;
import com.escuelaposgrado.Autenticacion.dto.response.UsuarioResponse;
import com.escuelaposgrado.Autenticacion.exception.ExcelProcessingException;
import com.escuelaposgrado.Autenticacion.model.enums.Role;

/**
 * Mapeo fila Excel ↔ RegistroRequest / UsuarioResponse.
 * Extrae Feature Envy de ExcelService hacia un componente cohesivo.
 */
public final class UsuarioExcelMapper {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private UsuarioExcelMapper() {
    }

    public static RegistroRequest toRegistroRequest(Row row) {
        String username = cell(row, UsuarioImportColumns.USERNAME);
        String email = cell(row, UsuarioImportColumns.EMAIL);
        String password = cell(row, UsuarioImportColumns.PASSWORD);
        String nombres = cell(row, UsuarioImportColumns.NOMBRES);
        String apellidos = cell(row, UsuarioImportColumns.APELLIDOS);
        String dni = cell(row, UsuarioImportColumns.DNI);
        String roleStr = cell(row, UsuarioImportColumns.ROL);

        requireMandatoryFields(username, email, password, nombres, apellidos, dni, roleStr);

        RegistroRequest request = new RegistroRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setNombres(nombres);
        request.setApellidos(apellidos);
        request.setDni(dni);
        request.setTelefono(cell(row, UsuarioImportColumns.TELEFONO));
        request.setDireccion(cell(row, UsuarioImportColumns.DIRECCION));
        request.setRole(parseRole(roleStr));
        request.setCodigoEstudiante(cell(row, UsuarioImportColumns.CODIGO_ESTUDIANTE));
        request.setCodigoDocente(cell(row, UsuarioImportColumns.CODIGO_DOCENTE));
        request.setEspecialidad(cell(row, UsuarioImportColumns.ESPECIALIDAD));
        request.setProgramaInteres(cell(row, UsuarioImportColumns.PROGRAMA_INTERES));
        return request;
    }

    public static void writeUsuarioRow(Row row, UsuarioResponse usuario, CellStyle dataStyle) {
        ExcelWorkbookSupport.setCellValue(row, 0, String.valueOf(usuario.getId()), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 1, usuario.getUsername(), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 2, usuario.getEmail(), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 3, usuario.getNombres(), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 4, usuario.getApellidos(), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 5, usuario.getDni(), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 6, usuario.getTelefono(), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 7, usuario.getDireccion(), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 8, usuario.getRole().toString(), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 9, Boolean.TRUE.equals(usuario.getActivo()) ? "SÍ" : "NO", dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 10, usuario.getCodigoEstudiante(), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 11, usuario.getCodigoDocente(), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 12, usuario.getEspecialidad(), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 13, usuario.getProgramaInteres(), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 14, formatDateTime(usuario.getFechaCreacion()), dataStyle);
        ExcelWorkbookSupport.setCellValue(row, 15, formatDateTime(usuario.getUltimoAcceso()), dataStyle);
    }

    private static String cell(Row row, int column) {
        return ExcelWorkbookSupport.getCellValueAsString(row.getCell(column));
    }

    private static void requireMandatoryFields(String... values) {
        for (String value : values) {
            if (value == null || value.isEmpty()) {
                throw new ExcelProcessingException("Campos obligatorios vacíos");
            }
        }
    }

    private static Role parseRole(String roleStr) {
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ExcelProcessingException("Rol inválido: " + roleStr, e);
        }
    }

    private static String formatDateTime(LocalDateTime value) {
        return value != null ? value.format(DATE_TIME_FORMAT) : "";
    }
}
