package com.escuelaposgrado.Intranet.dto;

import java.math.BigDecimal;

/**
 * DTO para estadísticas de calificaciones por materia
 */
public class EstadisticasCalificacionDTO {

    private Long materiaId;
    private String materiaNombre;
    private BigDecimal promedio;
    private BigDecimal notaMaxima;
    private BigDecimal notaMinima;
    private Long totalCalificaciones;

    public Long getMateriaId() { return materiaId; }
    public void setMateriaId(Long materiaId) { this.materiaId = materiaId; }

    public String getMateriaNombre() { return materiaNombre; }
    public void setMateriaNombre(String materiaNombre) { this.materiaNombre = materiaNombre; }

    public BigDecimal getPromedio() { return promedio; }
    public void setPromedio(BigDecimal promedio) { this.promedio = promedio; }

    public BigDecimal getNotaMaxima() { return notaMaxima; }
    public void setNotaMaxima(BigDecimal notaMaxima) { this.notaMaxima = notaMaxima; }

    public BigDecimal getNotaMinima() { return notaMinima; }
    public void setNotaMinima(BigDecimal notaMinima) { this.notaMinima = notaMinima; }

    public Long getTotalCalificaciones() { return totalCalificaciones; }
    public void setTotalCalificaciones(Long totalCalificaciones) { this.totalCalificaciones = totalCalificaciones; }
}
