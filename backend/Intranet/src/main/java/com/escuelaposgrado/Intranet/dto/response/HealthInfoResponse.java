package com.escuelaposgrado.Intranet.controller;

import java.time.LocalDateTime;
import java.util.Map;

public class HealthInfoResponse {

    private String name;
    private String description;
    private String version;
    private LocalDateTime buildTime;
    private Map<String, String> features;
    private Map<String, String> endpoints;

    public HealthInfoResponse(
            String name,
            String description,
            String version,
            LocalDateTime buildTime,
            Map<String, String> features,
            Map<String, String> endpoints) {

        this.name = name;
        this.description = description;
        this.version = version;
        this.buildTime = buildTime;
        this.features = features;
        this.endpoints = endpoints;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public LocalDateTime getBuildTime() {
        return buildTime;
    }

    public Map<String, String> getFeatures() {
        return features;
    }

    public Map<String, String> getEndpoints() {
        return endpoints;
    }
}