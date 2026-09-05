package com.lavanderia.modelo;

/**
 * OCP / Strategy: agrupa los tipos de servicio disponibles con su descripcion y precio.
 * Reemplaza multiples clases individuales por un enum fuertemente tipado que
 * implementa ServicioLavado.
 */
public enum TipoServicio implements ServicioLavado {
    LAVADO_BASICO("Lavado basico", 200.0),
    LAVADO_EN_SECO("Lavado en seco", 500.0),
    PLANCHADO("Planchado", 150.0);

    private final String descripcion;
    private final double precio;

    TipoServicio(String descripcion, double precio) {
        this.descripcion = descripcion;
        this.precio = precio;
    }

    @Override
    public double precio() {
        return precio;
    }

    @Override
    public String descripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion + " ($" + String.format("%.2f", precio) + ")";
    }
}
