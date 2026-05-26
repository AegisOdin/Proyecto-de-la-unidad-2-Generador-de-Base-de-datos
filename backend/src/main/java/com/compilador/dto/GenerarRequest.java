package com.compilador.dto;

import com.compilador.model.Tabla;

public class GenerarRequest {
    private String baseDatos;
    private Tabla tabla;

    public String getBaseDatos() { return baseDatos; }
    public void setBaseDatos(String baseDatos) { this.baseDatos = baseDatos; }
    public Tabla getTabla() { return tabla; }
    public void setTabla(Tabla tabla) { this.tabla = tabla; }
}
