package com.example.ecoruta.models;

public class Eco {
    private final String id;
    private final String nombre;
    private final String descripcion;
    private final String categoriaId;
    private final String categoriaNombre;
    private final String fecha;

    public Eco(String id, String nombre, String descripcion, String categoriaId, String categoriaNombre, String fecha) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.fecha = fecha;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoriaId() { return categoriaId; }
    public String getCategoriaNombre() { return categoriaNombre; }
    public String getFecha() { return fecha; }
}
