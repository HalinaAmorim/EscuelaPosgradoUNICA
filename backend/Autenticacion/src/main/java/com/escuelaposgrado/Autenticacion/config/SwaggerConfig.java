package com.escuelaposgrado.Autenticacion.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * OpenAPI del microservicio de autenticación.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo local")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")));
    }

    private Info apiInfo() {
        return new Info()
                .title("Microservicio de Autenticacion - Escuela de Posgrado UNICA")
                .description("""
                        Autenticacion y autorizacion JWT para la Escuela de Posgrado UNICA.

                        Roles: ADMIN, COORDINADOR, DOCENTE, ALUMNO, POSTULANTE.

                        Header: Authorization: Bearer <jwt-token>
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Escuela de Posgrado UNICA")
                        .email("posgrado@unica.edu.pe")
                        .url("https://www.unica.edu.pe/posgrado"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
