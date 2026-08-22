package com.lavanderia.modelo;

/**
 * SRP: esta clase tiene una sola responsabilidad, guardar los datos de un cliente.
 * No calcula precios, no persiste, no notifica.
 */
public class Cliente {
    private final int id;
    private final String nombre;
    private final String email;

    public Cliente(int id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return nombre + " (" + email + ")";
    }
}
