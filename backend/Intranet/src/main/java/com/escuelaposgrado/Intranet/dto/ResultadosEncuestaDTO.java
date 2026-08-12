package com.escuelaposgrado.Intranet.dto;


import java.util.Map;

public class ResultadosEncuestaDTO {

    private Long encuestaId;
    private String encuestaTitulo;
    private Long totalRespuestas;

    private Map<Long, EstadisticasPreguntaDTO>
            estadisticasPorPregunta;

    public Long getEncuestaId() {
        return encuestaId;
    }

    public void setEncuestaId(Long encuestaId) {
        this.encuestaId = encuestaId;
    }

    public String getEncuestaTitulo() {
        return encuestaTitulo;
    }

    public void setEncuestaTitulo(
            String encuestaTitulo) {

        this.encuestaTitulo = encuestaTitulo;
    }

    public Long getTotalRespuestas() {
        return totalRespuestas;
    }

    public void setTotalRespuestas(
            Long totalRespuestas) {

        this.totalRespuestas = totalRespuestas;
    }

    public Map<Long, EstadisticasPreguntaDTO>
    getEstadisticasPorPregunta() {

        return estadisticasPorPregunta;
    }

    public void setEstadisticasPorPregunta(
            Map<Long, EstadisticasPreguntaDTO>
                    estadisticasPorPregunta) {

        this.estadisticasPorPregunta =
                estadisticasPorPregunta;
    }
}