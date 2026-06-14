package com.TPI.Programacion.IV.Model;

import java.time.LocalDateTime;

public class Puja {
    private Long id;
    private int monto;
    private LocalDateTime fechaPuja;

    public Puja() {}

    public Puja(Long id, int monto, LocalDateTime fechaPuja) {
        this.id = id;
        this.monto = monto;
        this.fechaPuja = fechaPuja;
    }

    public void validadMontoMinimo(Puja ultimaPuja) {
        System.out.println("Validando monto mínimo contra última puja");
    }

    public void validadMontoMinimo(int incrementoMinimo) {
        System.out.println("Validando monto mínimo contra incremento");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMonto() {
        return monto;
    }

    public void setMonto(int monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaPuja() {
        return fechaPuja;
    }

    public void setFechaPuja(LocalDateTime fechaPuja) {
        this.fechaPuja = fechaPuja;
    }
}