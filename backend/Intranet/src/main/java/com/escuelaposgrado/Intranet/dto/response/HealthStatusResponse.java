package com.escuelaposgrado.Intranet.controller;

import java.time.LocalDateTime;

public class HealthStatusResponse {

    private String service;
    private String status;
    private LocalDateTime timestamp;
    private String version;
    private String description;
    private int port;
    private String[] endpoints;

    public HealthStatusResponse(
            String service,
            String status,
            LocalDateTime timestamp,
            String version,
            String description,
            int port,
            String[] endpoints) {

        this.service = service;
        this.status = status;
        this.timestamp = timestamp;
        this.version = version;
        this.description = description;
        this.port = port;
        this.endpoints = endpoints;
    }

    public String getService() {
        return service;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public int getPort() {
        return port;
    }

    public String[] getEndpoints() {
        return endpoints;
    }
}