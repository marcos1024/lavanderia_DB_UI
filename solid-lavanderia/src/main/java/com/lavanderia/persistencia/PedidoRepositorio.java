package com.lavanderia.persistencia;

import com.lavanderia.modelo.Pedido;
import java.util.List;
import java.util.Optional;

/**
 * ISP: interfaz pequena y enfocada solo en persistir pedidos.
 * DIP: la logica de alto nivel dependera de esta abstraccion, no de una base concreta.
 */
public interface PedidoRepositorio {
    void guardar(Pedido pedido);
    Optional<Pedido> buscarPorId(int id);
    List<Pedido> listar();
}
