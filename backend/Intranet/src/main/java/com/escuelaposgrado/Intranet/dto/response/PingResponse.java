package com.escuelaposgrado.Intranet.dto.response;


public class PingResponse {

    private String message;
    private String service;
    private String timestamp;

    public PingResponse(
            String message,
            String service,
            String timestamp) {

        this.message = message;
        this.service = service;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getService() {
        return service;
    }

    public String getTimestamp() {
        return timestamp;
    }
}