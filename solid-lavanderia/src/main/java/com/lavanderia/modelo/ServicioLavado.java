package com.lavanderia.modelo;

/**
 * Abstraccion de un tipo de servicio de la lavanderia.
 * OCP: para sumar un servicio nuevo se crea una clase que implemente esta interfaz;
 * el codigo existente no se modifica.
 */
public interface ServicioLavado {
    double precio();
    String descripcion();
}
