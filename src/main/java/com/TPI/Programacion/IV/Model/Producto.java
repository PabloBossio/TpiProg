package com.TPI.Programacion.IV.Model;

public class Producto {
    private Long id;
    private String nombre;
    private String descripcion;

    public Producto() {}

    public Producto(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public void crearProducto() {
        System.out.println("Producto creado.");
    }

    public void editarProducto() {
        System.out.println("Producto editado.");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
