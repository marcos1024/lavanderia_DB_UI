package com.lavanderia.modelo;

public class LavadoEnSeco implements ServicioLavado {
    @Override public double precio() { return 500.0; }
    @Override public String descripcion() { return "Lavado en seco"; }
}
