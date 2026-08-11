package com.escuelaposgrado.Autenticacion.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.escuelaposgrado.Autenticacion.config.CorsOrigins;
import com.escuelaposgrado.Autenticacion.dto.response.MessageResponse;
import com.escuelaposgrado.Autenticacion.dto.response.UsuarioResponse;
import com.escuelaposgrado.Autenticacion.service.AuthService;
import com.escuelaposgrado.Autenticacion.service.ExcelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoints administrativos de importación/exportación Excel.
 * Mantiene las mismas rutas {@code /api/admin/...} del contrato existente.
 */
@Tag(name = "Administracion", description = "Endpoints exclusivos para administradores del sistema")
@CrossOrigin(origins = {CorsOrigins.LOCALHOST, CorsOrigins.LOCALHOST_IP},
             allowCredentials = "true", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminExcelController {

    private static final String EXCEL_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter EXPORT_FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final AuthService authService;
    private final ExcelService excelService;

    public AdminExcelController(AuthService authService, ExcelService excelService) {
        this.authService = authService;
        this.excelService = excelService;
    }

    @Operation(
            summary = "Exportar usuarios a Excel",
            description = "Exporta todos los usuarios del sistema a un archivo Excel",
            security = @SecurityRequirement(name = "bearerAuth"),
            tags = {"Administracion"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo Excel generado exitosamente",
                    content = @Content(mediaType = EXCEL_MEDIA_TYPE)),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT inválido",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Prohibido - Se requiere rol ADMIN",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/usuarios/exportar-excel")
    public ResponseEntity<InputStreamResource> exportarUsuariosExcel() {
        try {
            List<UsuarioResponse> usuarios = authService.getAllUsuariosIncluyendoInactivos();
            ByteArrayInputStream in = excelService.exportUsuariosToExcel(usuarios);
            return excelAttachment(
                    "usuarios_" + LocalDateTime.now().format(EXPORT_FILE_TIMESTAMP) + ".xlsx", in);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "Importar usuarios desde Excel",
            description = "Importa usuarios al sistema desde un archivo Excel. El archivo debe seguir el formato de la plantilla.",
            security = @SecurityRequirement(name = "bearerAuth"),
            tags = {"Administracion"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Importación completada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = @ExampleObject(
                                    name = "Importación exitosa",
                                    value = """
                                            {
                                              "message": "Importación completada.\\nUsuarios creados exitosamente: 5\\nErrores encontrados: 1",
                                              "success": true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Archivo inválido o formato incorrecto",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT inválido",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Prohibido - Se requiere rol ADMIN",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/usuarios/importar-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> importarUsuariosExcel(
            @Parameter(description = "Archivo Excel con los usuarios a importar", required = true)
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Debe seleccionar un archivo", false));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("El archivo debe ser de formato Excel (.xlsx)", false));
        }

        try {
            return ResponseEntity.ok(excelService.importUsuariosFromExcel(file));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al procesar el archivo: " + e.getMessage(), false));
        }
    }

    @Operation(
            summary = "Descargar plantilla Excel",
            description = "Descarga una plantilla de Excel con el formato correcto para importar usuarios",
            security = @SecurityRequirement(name = "bearerAuth"),
            tags = {"Administracion"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plantilla Excel generada exitosamente",
                    content = @Content(mediaType = EXCEL_MEDIA_TYPE)),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT inválido",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Prohibido - Se requiere rol ADMIN",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/usuarios/plantilla-excel")
    public ResponseEntity<InputStreamResource> descargarPlantillaExcel() {
        try {
            return excelAttachment("plantilla_usuarios.xlsx", excelService.generateExcelTemplate());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<InputStreamResource> excelAttachment(String filename, ByteArrayInputStream content) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE))
                .body(new InputStreamResource(content));
    }
}
