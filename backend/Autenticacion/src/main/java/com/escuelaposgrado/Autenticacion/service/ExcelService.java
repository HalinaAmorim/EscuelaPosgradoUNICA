package com.escuelaposgrado.Autenticacion.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.escuelaposgrado.Autenticacion.dto.request.RegistroRequest;
import com.escuelaposgrado.Autenticacion.dto.response.MessageResponse;
import com.escuelaposgrado.Autenticacion.dto.response.UsuarioResponse;
import com.escuelaposgrado.Autenticacion.exception.ExcelProcessingException;
import com.escuelaposgrado.Autenticacion.model.enums.Role;

/**
 * Servicio para manejar importación y exportación de usuarios en formato Excel.
 */
@Service
public class ExcelService {

    private static final String FILA_PREFIX = "Fila ";
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String[] EXPORT_HEADERS = {
        "ID", "Username", "Email", "Nombres", "Apellidos", "DNI", "Teléfono",
        "Dirección", "Rol", "Activo", "Código Estudiante", "Código Docente",
        "Especialidad", "Programa de Interés", "Fecha Creación", "Último Acceso"
    };

    private static final String[] TEMPLATE_HEADERS = {
        "Username", "Email", "Password", "Nombres", "Apellidos", "DNI", "Teléfono",
        "Dirección", "Rol", "Código Estudiante", "Código Docente",
        "Especialidad", "Programa de Interés"
    };

    @Autowired
    private AuthService authService;

    public ByteArrayInputStream exportUsuariosToExcel(List<UsuarioResponse> usuarios) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Usuarios");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            writeHeaderRow(sheet, EXPORT_HEADERS, headerStyle);

            int rowIdx = 1;
            for (UsuarioResponse usuario : usuarios) {
                writeUsuarioRow(sheet.createRow(rowIdx++), usuario, dataStyle);
            }

            autoSizeColumns(sheet, EXPORT_HEADERS.length);
            return toInputStream(workbook);
        }
    }

    public MessageResponse importUsuariosFromExcel(MultipartFile file) {
        if (file.isEmpty()) {
            return new MessageResponse("El archivo está vacío", false);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            return new MessageResponse("El archivo debe ser formato .xlsx", false);
        }

        List<String> errores = new ArrayList<>();
        List<String> exitosos = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            processImportSheet(workbook.getSheetAt(0), exitosos, errores);
        } catch (IOException e) {
            return new MessageResponse("Error al procesar el archivo Excel: " + e.getMessage(), false);
        }

        return buildImportResponse(exitosos, errores);
    }

    public ByteArrayInputStream generateExcelTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Plantilla Usuarios");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle exampleStyle = createDataStyle(workbook);

            writeHeaderRow(sheet, TEMPLATE_HEADERS, headerStyle);

            String[] exampleData = {
                "jperez", "jperez@email.com", "password123", "Juan", "Pérez", "12345678",
                "987654321", "Av. Principal 123", "ALUMNO", "E001", "", "", "Maestría en Sistemas"
            };
            Row exampleRow = sheet.createRow(1);
            for (int i = 0; i < exampleData.length; i++) {
                setCellValue(exampleRow, i, exampleData[i], exampleStyle);
            }

            autoSizeColumns(sheet, TEMPLATE_HEADERS.length);
            addInstructionsSheet(workbook);
            return toInputStream(workbook);
        }
    }

    private void processImportSheet(Sheet sheet, List<String> exitosos, List<String> errores) {
        Iterator<Row> rows = sheet.iterator();
        if (rows.hasNext()) {
            rows.next();
        }

        int filaActual = 2;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            if (!isEmptyRow(currentRow)) {
                processImportRow(currentRow, filaActual, exitosos, errores);
            }
            filaActual++;
        }
    }

    private void processImportRow(Row row, int filaActual, List<String> exitosos, List<String> errores) {
        try {
            RegistroRequest registroRequest = mapRowToRegistroRequest(row);
            MessageResponse response = authService.registro(registroRequest);
            if (response.isSuccess()) {
                exitosos.add(FILA_PREFIX + filaActual + ": " + registroRequest.getUsername());
            } else {
                errores.add(FILA_PREFIX + filaActual + ": " + response.getMessage());
            }
        } catch (ExcelProcessingException e) {
            errores.add(FILA_PREFIX + filaActual + ": " + e.getMessage());
        } catch (RuntimeException e) {
            errores.add(FILA_PREFIX + filaActual + ": Error al procesar - " + e.getMessage());
        }
    }

    private MessageResponse buildImportResponse(List<String> exitosos, List<String> errores) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Importación completada.").append(System.lineSeparator());
        mensaje.append("Usuarios importados exitosamente: ").append(exitosos.size()).append(System.lineSeparator());
        mensaje.append("Errores encontrados: ").append(errores.size()).append(System.lineSeparator());

        if (!exitosos.isEmpty()) {
            mensaje.append(System.lineSeparator()).append("Exitosos:").append(System.lineSeparator());
            exitosos.forEach(msg -> mensaje.append("  - ").append(msg).append(System.lineSeparator()));
        }
        if (!errores.isEmpty()) {
            mensaje.append(System.lineSeparator()).append("Errores:").append(System.lineSeparator());
            errores.forEach(msg -> mensaje.append("  - ").append(msg).append(System.lineSeparator()));
        }

        return new MessageResponse(mensaje.toString(), errores.isEmpty());
    }

    private void writeUsuarioRow(Row row, UsuarioResponse usuario, CellStyle dataStyle) {
        setCellValue(row, 0, String.valueOf(usuario.getId()), dataStyle);
        setCellValue(row, 1, usuario.getUsername(), dataStyle);
        setCellValue(row, 2, usuario.getEmail(), dataStyle);
        setCellValue(row, 3, usuario.getNombres(), dataStyle);
        setCellValue(row, 4, usuario.getApellidos(), dataStyle);
        setCellValue(row, 5, usuario.getDni(), dataStyle);
        setCellValue(row, 6, usuario.getTelefono(), dataStyle);
        setCellValue(row, 7, usuario.getDireccion(), dataStyle);
        setCellValue(row, 8, usuario.getRole().toString(), dataStyle);
        setCellValue(row, 9, Boolean.TRUE.equals(usuario.getActivo()) ? "SÍ" : "NO", dataStyle);
        setCellValue(row, 10, usuario.getCodigoEstudiante(), dataStyle);
        setCellValue(row, 11, usuario.getCodigoDocente(), dataStyle);
        setCellValue(row, 12, usuario.getEspecialidad(), dataStyle);
        setCellValue(row, 13, usuario.getProgramaInteres(), dataStyle);
        setCellValue(row, 14, formatDateTime(usuario.getFechaCreacion()), dataStyle);
        setCellValue(row, 15, formatDateTime(usuario.getUltimoAcceso()), dataStyle);
    }

    private String formatDateTime(java.time.LocalDateTime value) {
        return value != null ? value.format(DATE_TIME_FORMAT) : "";
    }

    private RegistroRequest mapRowToRegistroRequest(Row row) {
        String username = getCellValueAsString(row.getCell(0));
        String email = getCellValueAsString(row.getCell(1));
        String password = getCellValueAsString(row.getCell(2));
        String nombres = getCellValueAsString(row.getCell(3));
        String apellidos = getCellValueAsString(row.getCell(4));
        String dni = getCellValueAsString(row.getCell(5));
        String roleStr = getCellValueAsString(row.getCell(8));

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()
                || nombres.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || roleStr.isEmpty()) {
            throw new ExcelProcessingException("Campos obligatorios vacíos");
        }

        Role role = parseRole(roleStr);

        RegistroRequest request = new RegistroRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setNombres(nombres);
        request.setApellidos(apellidos);
        request.setDni(dni);
        request.setTelefono(getCellValueAsString(row.getCell(6)));
        request.setDireccion(getCellValueAsString(row.getCell(7)));
        request.setRole(role);
        request.setCodigoEstudiante(getCellValueAsString(row.getCell(9)));
        request.setCodigoDocente(getCellValueAsString(row.getCell(10)));
        request.setEspecialidad(getCellValueAsString(row.getCell(11)));
        request.setProgramaInteres(getCellValueAsString(row.getCell(12)));
        return request;
    }

    private Role parseRole(String roleStr) {
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ExcelProcessingException("Rol inválido: " + roleStr, e);
        }
    }

    private void writeHeaderRow(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private ByteArrayInputStream toInputStream(Workbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < 9; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !getCellValueAsString(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private void setCellValue(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void addInstructionsSheet(Workbook workbook) {
        Sheet instructionsSheet = workbook.createSheet("Instrucciones");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle normalStyle = createDataStyle(workbook);

        int rowNum = 0;
        Row titleRow = instructionsSheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("INSTRUCCIONES PARA IMPORTACIÓN DE USUARIOS");
        titleCell.setCellStyle(headerStyle);
        rowNum++;

        String[] instructions = {
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

        for (String instruction : instructions) {
            Row row = instructionsSheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(instruction);
            cell.setCellStyle(normalStyle);
        }
        instructionsSheet.autoSizeColumn(0);
    }
}
