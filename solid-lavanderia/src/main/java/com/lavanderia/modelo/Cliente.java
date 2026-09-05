package com.lavanderia.modelo;

/**
 * SRP: record inmutable que representa los datos de un cliente.
 * Simplifica la definicion eliminando boilerplate de atributos y constructores.
 */
public record Cliente(int id, String nombre, String email) {

    // Getters estilo JavaBean para compatibilidad
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return nombre + " (" + email + ")";
    }
}
