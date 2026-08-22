package com.lavanderia.modelo;

public class Planchado implements ServicioLavado {
    @Override public double precio() { return 150.0; }
    @Override public String descripcion() { return "Planchado"; }
}
