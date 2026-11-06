package com.example.ecoruta.models;

public class EcoCategory {
    private final String id;
    private final String nombre;
    private final String descripcion;
    private final String icono;

    public EcoCategory(String id, String nombre, String descripcion, String icono) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getIcono() { return icono; }
}
