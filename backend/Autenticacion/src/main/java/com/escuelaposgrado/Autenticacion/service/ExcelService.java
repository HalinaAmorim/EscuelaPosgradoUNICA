package com.escuelaposgrado.Autenticacion.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.escuelaposgrado.Autenticacion.dto.request.RegistroRequest;
import com.escuelaposgrado.Autenticacion.dto.response.MessageResponse;
import com.escuelaposgrado.Autenticacion.dto.response.UsuarioResponse;
import com.escuelaposgrado.Autenticacion.exception.ExcelProcessingException;
import com.escuelaposgrado.Autenticacion.service.excel.ExcelTemplateContent;
import com.escuelaposgrado.Autenticacion.service.excel.ExcelWorkbookSupport;
import com.escuelaposgrado.Autenticacion.service.excel.UsuarioExcelMapper;
import com.escuelaposgrado.Autenticacion.service.excel.UsuarioImportColumns;

/**
 * Orquesta importación/exportación Excel de usuarios.
 */
@Service
public class ExcelService {

    private static final String FILA_PREFIX = "Fila ";
    private static final String XLSX_EXTENSION = ".xlsx";
    private static final String SHEET_USUARIOS = "Usuarios";
    private static final String SHEET_PLANTILLA = "Plantilla Usuarios";
    private static final String SHEET_INSTRUCCIONES = "Instrucciones";

    private final AuthService authService;

    public ExcelService(AuthService authService) {
        this.authService = authService;
    }

    public ByteArrayInputStream exportUsuariosToExcel(List<UsuarioResponse> usuarios) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_USUARIOS);
            CellStyle headerStyle = ExcelWorkbookSupport.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelWorkbookSupport.createDataStyle(workbook);

            ExcelWorkbookSupport.writeHeaderRow(sheet, ExcelTemplateContent.EXPORT_HEADERS, headerStyle);

            int rowIdx = 1;
            for (UsuarioResponse usuario : usuarios) {
                UsuarioExcelMapper.writeUsuarioRow(sheet.createRow(rowIdx++), usuario, dataStyle);
            }

            ExcelWorkbookSupport.autoSizeColumns(sheet, ExcelTemplateContent.EXPORT_HEADERS.length);
            return toInputStream(workbook);
        }
    }

    public MessageResponse importUsuariosFromExcel(MultipartFile file) {
        MessageResponse validationError = validateImportFile(file);
        if (validationError != null) {
            return validationError;
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
            Sheet sheet = workbook.createSheet(SHEET_PLANTILLA);
            CellStyle headerStyle = ExcelWorkbookSupport.createHeaderStyle(workbook);
            CellStyle exampleStyle = ExcelWorkbookSupport.createDataStyle(workbook);

            ExcelWorkbookSupport.writeHeaderRow(sheet, ExcelTemplateContent.TEMPLATE_HEADERS, headerStyle);
            writeExampleRow(sheet.createRow(1), exampleStyle);
            ExcelWorkbookSupport.autoSizeColumns(sheet, ExcelTemplateContent.TEMPLATE_HEADERS.length);
            addInstructionsSheet(workbook);
            return toInputStream(workbook);
        }
    }

    private MessageResponse validateImportFile(MultipartFile file) {
        if (file.isEmpty()) {
            return new MessageResponse("El archivo está vacío", false);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(XLSX_EXTENSION)) {
            return new MessageResponse("El archivo debe ser formato .xlsx", false);
        }
        return null;
    }

    private void processImportSheet(Sheet sheet, List<String> exitosos, List<String> errores) {
        Iterator<Row> rows = sheet.iterator();
        if (rows.hasNext()) {
            rows.next();
        }

        int filaActual = 2;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            if (!ExcelWorkbookSupport.isEmptyRow(currentRow, UsuarioImportColumns.BASIC_COLUMN_COUNT)) {
                processImportRow(currentRow, filaActual, exitosos, errores);
            }
            filaActual++;
        }
    }

    private void processImportRow(Row row, int filaActual, List<String> exitosos, List<String> errores) {
        try {
            RegistroRequest registroRequest = UsuarioExcelMapper.toRegistroRequest(row);
            MessageResponse response = authService.registro(registroRequest);
            if (response.isSuccess()) {
                exitosos.add(filaLabel(filaActual, registroRequest.getUsername()));
            } else {
                errores.add(filaLabel(filaActual, response.getMessage()));
            }
        } catch (ExcelProcessingException e) {
            errores.add(filaLabel(filaActual, e.getMessage()));
        } catch (RuntimeException e) {
            errores.add(filaLabel(filaActual, "Error al procesar - " + e.getMessage()));
        }
    }

    private String filaLabel(int filaActual, String detail) {
        return FILA_PREFIX + filaActual + ": " + detail;
    }

    private MessageResponse buildImportResponse(List<String> exitosos, List<String> errores) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Importación completada.").append(System.lineSeparator());
        mensaje.append("Usuarios importados exitosamente: ").append(exitosos.size()).append(System.lineSeparator());
        mensaje.append("Errores encontrados: ").append(errores.size()).append(System.lineSeparator());
        appendSection(mensaje, "Exitosos:", exitosos);
        appendSection(mensaje, "Errores:", errores);
        return new MessageResponse(mensaje.toString(), errores.isEmpty());
    }

    private void appendSection(StringBuilder mensaje, String title, List<String> items) {
        if (items.isEmpty()) {
            return;
        }
        mensaje.append(System.lineSeparator()).append(title).append(System.lineSeparator());
        items.forEach(msg -> mensaje.append("  - ").append(msg).append(System.lineSeparator()));
    }

    private void writeExampleRow(Row exampleRow, CellStyle exampleStyle) {
        for (int i = 0; i < ExcelTemplateContent.TEMPLATE_EXAMPLE.length; i++) {
            ExcelWorkbookSupport.setCellValue(
                    exampleRow, i, ExcelTemplateContent.TEMPLATE_EXAMPLE[i], exampleStyle);
        }
    }

    private ByteArrayInputStream toInputStream(Workbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addInstructionsSheet(Workbook workbook) {
        Sheet instructionsSheet = workbook.createSheet(SHEET_INSTRUCCIONES);
        CellStyle headerStyle = ExcelWorkbookSupport.createHeaderStyle(workbook);
        CellStyle normalStyle = ExcelWorkbookSupport.createDataStyle(workbook);

        int rowNum = 0;
        Row titleRow = instructionsSheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("INSTRUCCIONES PARA IMPORTACIÓN DE USUARIOS");
        titleCell.setCellStyle(headerStyle);
        rowNum++;

        for (String instruction : ExcelTemplateContent.IMPORT_INSTRUCTIONS) {
            Row row = instructionsSheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(instruction);
            cell.setCellStyle(normalStyle);
        }
        instructionsSheet.autoSizeColumn(0);
    }
}
