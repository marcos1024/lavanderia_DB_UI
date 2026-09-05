package com.lavanderia.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SRP: representa un pedido (un cliente y la lista de servicios que pidio).
 * Information Expert (GRASP): calcula de forma autonoma su monto total a partir de sus servicios.
 */
public class Pedido {
    private final int id;
    private final Cliente cliente;
    private final List<ServicioLavado> servicios = new ArrayList<>();

    public Pedido(int id, Cliente cliente) {
        this.id = id;
        this.cliente = cliente;
    }

    public void agregarServicio(ServicioLavado servicio) {
        servicios.add(servicio);
    }

    public double calcularTotal() {
        return servicios.stream().mapToDouble(ServicioLavado::precio).sum();
    }

    public int getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<ServicioLavado> getServicios() {
        return Collections.unmodifiableList(servicios);
    }
}
