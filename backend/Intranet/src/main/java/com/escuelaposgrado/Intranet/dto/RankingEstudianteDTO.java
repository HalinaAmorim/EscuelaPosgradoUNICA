package com.escuelaposgrado.Intranet.dto;

import java.math.BigDecimal;

/**
 * DTO para ranking de estudiantes por promedio
 */
public class RankingEstudianteDTO {

    private Long estudianteId;
    private String estudianteNombre;
    private BigDecimal promedio;
    private Integer posicion;

    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }

    public String getEstudianteNombre() { return estudianteNombre; }
    public void setEstudianteNombre(String estudianteNombre) { this.estudianteNombre = estudianteNombre; }

    public BigDecimal getPromedio() { return promedio; }
    public void setPromedio(BigDecimal promedio) { this.promedio = promedio; }

    public Integer getPosicion() { return posicion; }
    public void setPosicion(Integer posicion) { this.posicion = posicion; }
}
