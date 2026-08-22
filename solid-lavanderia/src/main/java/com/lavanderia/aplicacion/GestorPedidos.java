package com.lavanderia.aplicacion;

import com.lavanderia.modelo.Pedido;
import com.lavanderia.notificacion.Notificador;
import com.lavanderia.persistencia.PedidoRepositorio;
import com.lavanderia.servicio.CalculadoraTotal;

/**
 * Logica de alto nivel que orquesta el registro de un pedido.
 * DIP: depende de las abstracciones PedidoRepositorio y Notificador, nunca de sus
 * implementaciones concretas. Las recibe por el constructor (inyeccion de dependencias).
 */
public class GestorPedidos {
    private final PedidoRepositorio repositorio;
    private final Notificador notificador;
    private final CalculadoraTotal calculadora;

    public GestorPedidos(PedidoRepositorio repositorio,
                         Notificador notificador,
                         CalculadoraTotal calculadora) {
        this.repositorio = repositorio;
        this.notificador = notificador;
        this.calculadora = calculadora;
    }

    public double registrarPedido(Pedido pedido) {
        repositorio.guardar(pedido);
        double total = calculadora.calcular(pedido);
        notificador.enviar(pedido.getCliente(),
                "Tu pedido #" + pedido.getId() + " fue registrado. Total: $" + total);
        return total;
    }
}
