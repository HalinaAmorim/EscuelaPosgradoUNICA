package com.escuelaposgrado.Intranet.controller;

import java.time.LocalDateTime;
import java.util.Map;

public class ApiInfoResponse {

    private String name;
    private String version;
    private String description;
    private LocalDateTime timestamp;
    private int port;
    private String status;
    private Map<String, String> links;
    private Map<String, String> mainEndpoints;

    public ApiInfoResponse(
            String name,
            String version,
            String description,
            LocalDateTime timestamp,
            int port,
            String status,
            Map<String, String> links,
            Map<String, String> mainEndpoints) {

        this.name = name;
        this.version = version;
        this.description = description;
        this.timestamp = timestamp;
        this.port = port;
        this.status = status;
        this.links = links;
        this.mainEndpoints = mainEndpoints;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getPort() {
        return port;
    }

    public String getStatus() {
        return status;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public Map<String, String> getMainEndpoints() {
        return mainEndpoints;
    }
}
