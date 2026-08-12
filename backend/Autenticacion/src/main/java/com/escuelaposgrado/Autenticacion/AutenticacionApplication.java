package com.escuelaposgrado.Autenticacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AutenticacionApplication {

    public static void main(String[] args) {
        createApplication().run(args);
    }

    static SpringApplication createApplication() {
        return new SpringApplication(AutenticacionApplication.class);
    }
}
