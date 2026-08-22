package com.lavanderia.modelo;

public class LavadoBasico implements ServicioLavado {
    @Override public double precio() { return 200.0; }
    @Override public String descripcion() { return "Lavado basico"; }
}
