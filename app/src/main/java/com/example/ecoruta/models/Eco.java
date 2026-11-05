package com.example.ecoruta.models;

public class Eco {
    private final String id;
    private final String accion;
    private final String categoria;
    private final String fecha;
    private final String impacto;
    private final String descripcion;

    public Eco(String id, String accion, String categoria, String fecha, String impacto, String descripcion) {
        this.id = id;
        this.accion = accion;
        this.categoria = categoria;
        this.fecha = fecha;
        this.impacto = impacto;
        this.descripcion = descripcion;
    }

    public String getId() { return id; }
    public String getAccion() { return accion; }
    public String getCategoria() { return categoria; }
    public String getFecha() { return fecha; }
    public String getImpacto() { return impacto; }
    public String getDescripcion() { return descripcion; }
}
