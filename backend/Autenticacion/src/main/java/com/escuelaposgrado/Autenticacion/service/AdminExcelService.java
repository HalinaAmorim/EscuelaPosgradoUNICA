package com.escuelaposgrado.Autenticacion.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.escuelaposgrado.Autenticacion.dto.response.MessageResponse;
import com.escuelaposgrado.Autenticacion.dto.response.UsuarioResponse;

@Service
public class AdminExcelService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final ZoneId ZONE =
            ZoneId.systemDefault();

    @Autowired
    private ExcelService excelService;

    @Autowired
    private AuthService authService;

    public ResponseEntity<InputStreamResource> exportarUsuarios()
            throws IOException {

        List<UsuarioResponse> usuarios =
                authService.getAllUsuariosIncluyendoInactivos();

        ByteArrayInputStream in =
                excelService.exportUsuariosToExcel(usuarios);

        HttpHeaders headers = new HttpHeaders();

        headers.add(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=usuarios_"
                        + LocalDateTime.now(ZONE).format(FORMATTER)
                        + ".xlsx"
        );

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    public ResponseEntity<MessageResponse> importarUsuarios(MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(
                            "Debe seleccionar un archivo",
                            false));
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.endsWith(".xlsx")) {

            return ResponseEntity.badRequest()
                    .body(new MessageResponse(
                            "El archivo debe ser .xlsx",
                            false));
        }

        try {

            return ResponseEntity.ok(
                    excelService.importUsuariosFromExcel(file));

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body(new MessageResponse(
                            e.getMessage(),
                            false));
        }

    }

    public ResponseEntity<InputStreamResource> plantilla() {

        try {

            ByteArrayInputStream in =
                    excelService.generateExcelTemplate();

            HttpHeaders headers = new HttpHeaders();

            headers.add(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=plantilla_usuarios.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));

        } catch (Exception e) {

            return ResponseEntity.internalServerError().build();

        }

    }

}