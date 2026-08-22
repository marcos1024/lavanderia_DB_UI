package com.lavanderia.persistencia;

import com.lavanderia.modelo.Pedido;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementacion en memoria de PedidoRepositorio.
 * En la proxima sesion se creara RepositorioPostgres implementando la MISMA interfaz:
 * gracias a DIP, el resto del sistema no cambia una sola linea.
 */
public class RepositorioEnMemoria implements PedidoRepositorio {
    private final Map<Integer, Pedido> datos = new LinkedHashMap<>();

    @Override
    public void guardar(Pedido pedido) {

        datos.put(pedido.getId(), pedido);
    }

    @Override
    public Optional<Pedido> buscarPorId(int id) {

        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Pedido> listar() {

        return new ArrayList<>(datos.values());
    }
}
