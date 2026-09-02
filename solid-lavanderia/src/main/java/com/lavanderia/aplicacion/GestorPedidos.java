package com.lavanderia.aplicacion;

import com.lavanderia.modelo.Pedido;
import com.lavanderia.notificacion.Notificador;
import com.lavanderia.persistencia.PedidoRepositorio;
import com.lavanderia.servicio.CalculadoraTotal;

import java.util.List;

/**
 * Logica de aplicacion y fachada (Facade) que orquesta los casos de uso de pedidos.
 * DIP: depende de las abstracciones PedidoRepositorio y Notificador.
 * Centraliza la interaccion para que la capa de UI no conozca directamente la persistencia.
 */
public class GestorPedidos {
    private final PedidoRepositorio repositorio;
    private final CalculadoraTotal calculadora;
    private Notificador notificador;

    public GestorPedidos(PedidoRepositorio repositorio, Notificador notificador) {
        this(repositorio, notificador, new CalculadoraTotal());
    }

    public GestorPedidos(PedidoRepositorio repositorio,
                         Notificador notificador,
                         CalculadoraTotal calculadora) {
        this.repositorio = repositorio;
        this.notificador = notificador;
        this.calculadora = (calculadora != null) ? calculadora : new CalculadoraTotal();
    }

    public double registrarPedido(Pedido pedido) {
        repositorio.guardar(pedido);
        double total = calculadora.calcular(pedido);
        if (notificador != null) {
            notificador.enviar(pedido.getCliente(),
                    "Tu pedido #" + pedido.getId() + " fue registrado. Total: $" + total);
        }
        return total;
    }

    public List<Pedido> listarPedidos() {
        return repositorio.listar();
    }

    public int siguientePedidoId() {
        return repositorio.listar().stream().mapToInt(Pedido::getId).max().orElse(0) + 1;
    }

    public int siguienteClienteId() {
        return repositorio.listar().stream().mapToInt(p -> p.getCliente().getId()).max().orElse(0) + 1;
    }

    public void setNotificador(Notificador notificador) {
        this.notificador = notificador;
    }

    public PedidoRepositorio getRepositorio() {
        return repositorio;
    }

    public CalculadoraTotal getCalculadora() {
        return calculadora;
    }
}
