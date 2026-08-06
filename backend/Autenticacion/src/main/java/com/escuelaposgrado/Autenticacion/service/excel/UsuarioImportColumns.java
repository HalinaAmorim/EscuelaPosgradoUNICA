package com.escuelaposgrado.Autenticacion.service.excel;

/**
 * Índices de columnas del Excel de importación de usuarios.
 * Elimina magic numbers dispersos en el mapeo de filas.
 */
public final class UsuarioImportColumns {

    public static final int USERNAME = 0;
    public static final int EMAIL = 1;
    public static final int PASSWORD = 2;
    public static final int NOMBRES = 3;
    public static final int APELLIDOS = 4;
    public static final int DNI = 5;
    public static final int TELEFONO = 6;
    public static final int DIRECCION = 7;
    public static final int ROL = 8;
    public static final int CODIGO_ESTUDIANTE = 9;
    public static final int CODIGO_DOCENTE = 10;
    public static final int ESPECIALIDAD = 11;
    public static final int PROGRAMA_INTERES = 12;

    /** Columnas básicas usadas para detectar filas vacías. */
    public static final int BASIC_COLUMN_COUNT = 9;

    private UsuarioImportColumns() {
    }
}
