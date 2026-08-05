package com.escuelaposgrado.Intranet.dto;


import java.util.Map;

public class EstadisticasPreguntaDTO {

    private Long preguntaId;
    private String preguntaTexto;
    private String tipoPregunta;
    private Long totalRespuestas;
    private Map<String, Long> conteoRespuestas;

    public Long getPreguntaId() {
        return preguntaId;
    }

    public void setPreguntaId(Long preguntaId) {
        this.preguntaId = preguntaId;
    }

    public String getPreguntaTexto() {
        return preguntaTexto;
    }

    public void setPreguntaTexto(
            String preguntaTexto) {

        this.preguntaTexto = preguntaTexto;
    }

    public String getTipoPregunta() {
        return tipoPregunta;
    }

    public void setTipoPregunta(
            String tipoPregunta) {

        this.tipoPregunta = tipoPregunta;
    }

    public Long getTotalRespuestas() {
        return totalRespuestas;
    }

    public void setTotalRespuestas(
            Long totalRespuestas) {

        this.totalRespuestas = totalRespuestas;
    }

    public Map<String, Long>
    getConteoRespuestas() {

        return conteoRespuestas;
    }

    public void setConteoRespuestas(
            Map<String, Long> conteoRespuestas) {

        this.conteoRespuestas =
                conteoRespuestas;
    }
}