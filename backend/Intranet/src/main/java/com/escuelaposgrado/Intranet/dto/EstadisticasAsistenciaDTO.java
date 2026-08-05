package com.escuelaposgrado.Intranet.dto;

/**
 * DTO para estadísticas de asistencia de un estudiante
 */
public class EstadisticasAsistenciaDTO {

    private Long estudianteId;
    private String estudianteNombre;
    private Long totalPresente;
    private Long totalAusente;
    private Long totalTardanza;
    private Long totalJustificado;
    private Long totalClases;
    private Double porcentajeAsistencia;

    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }

    public String getEstudianteNombre() { return estudianteNombre; }
    public void setEstudianteNombre(String estudianteNombre) { this.estudianteNombre = estudianteNombre; }

    public Long getTotalPresente() { return totalPresente; }
    public void setTotalPresente(Long totalPresente) { this.totalPresente = totalPresente; }

    public Long getTotalAusente() { return totalAusente; }
    public void setTotalAusente(Long totalAusente) { this.totalAusente = totalAusente; }

    public Long getTotalTardanza() { return totalTardanza; }
    public void setTotalTardanza(Long totalTardanza) { this.totalTardanza = totalTardanza; }

    public Long getTotalJustificado() { return totalJustificado; }
    public void setTotalJustificado(Long totalJustificado) { this.totalJustificado = totalJustificado; }

    public Long getTotalClases() { return totalClases; }
    public void setTotalClases(Long totalClases) { this.totalClases = totalClases; }

    public Double getPorcentajeAsistencia() { return porcentajeAsistencia; }
    public void setPorcentajeAsistencia(Double porcentajeAsistencia) { this.porcentajeAsistencia = porcentajeAsistencia; }
}
