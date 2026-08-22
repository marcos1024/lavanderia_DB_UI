package com.lavanderia.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * SRP: representa un pedido (un cliente y la lista de servicios que pidio).
 * Solo agrupa datos; no sabe calcular ni persistir.
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

    public int getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public List<ServicioLavado> getServicios() { return servicios; }
}
