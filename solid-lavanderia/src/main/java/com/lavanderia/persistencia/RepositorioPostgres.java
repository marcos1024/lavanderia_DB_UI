package com.lavanderia.persistencia;

import com.lavanderia.modelo.Cliente;
import com.lavanderia.modelo.LavadoBasico;
import com.lavanderia.modelo.LavadoEnSeco;
import com.lavanderia.modelo.Pedido;
import com.lavanderia.modelo.Planchado;
import com.lavanderia.modelo.ServicioLavado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementacion de PedidoRepositorio contra PostgreSQL.
 * DIP: implementa la misma interfaz que RepositorioEnMemoria, asi que
 * GestorPedidos y el resto de la app no cambian una linea al usar esta clase.
 */
public class RepositorioPostgres implements PedidoRepositorio {

    @Override
    public void guardar(Pedido pedido) {
        String upsertCliente = "INSERT INTO clientes(id, nombre, email) VALUES (?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre, email = EXCLUDED.email";
        String upsertPedido = "INSERT INTO pedidos(id, cliente_id) VALUES (?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET cliente_id = EXCLUDED.cliente_id";
        String deleteServicios = "DELETE FROM pedido_servicios WHERE pedido_id = ?";
        String insertServicio = "INSERT INTO pedido_servicios(pedido_id, tipo, descripcion, precio) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                Cliente cliente = pedido.getCliente();
                try (PreparedStatement ps = con.prepareStatement(upsertCliente)) {
                    ps.setInt(1, cliente.getId());
                    ps.setString(2, cliente.getNombre());
                    ps.setString(3, cliente.getEmail());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(upsertPedido)) {
                    ps.setInt(1, pedido.getId());
                    ps.setInt(2, cliente.getId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(deleteServicios)) {
                    ps.setInt(1, pedido.getId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(insertServicio)) {
                    for (ServicioLavado servicio : pedido.getServicios()) {
                        ps.setInt(1, pedido.getId());
                        ps.setString(2, servicio.getClass().getSimpleName());
                        ps.setString(3, servicio.descripcion());
                        ps.setDouble(4, servicio.precio());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el pedido #" + pedido.getId(), e);
        }
    }

    @Override
    public Optional<Pedido> buscarPorId(int id) {
        String sql = "SELECT p.id AS pedido_id, c.id AS cliente_id, c.nombre, c.email, ps.tipo " +
                "FROM pedidos p " +
                "JOIN clientes c ON c.id = p.cliente_id " +
                "LEFT JOIN pedido_servicios ps ON ps.pedido_id = p.id " +
                "WHERE p.id = ? " +
                "ORDER BY ps.id";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                Pedido pedido = null;
                while (rs.next()) {
                    if (pedido == null) {
                        pedido = crearPedidoDesdeFila(rs);
                    }
                    agregarServicioDesdeFila(pedido, rs);
                }
                return Optional.ofNullable(pedido);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el pedido #" + id, e);
        }
    }

    @Override
    public List<Pedido> listar() {
        String sql = "SELECT p.id AS pedido_id, c.id AS cliente_id, c.nombre, c.email, ps.tipo " +
                "FROM pedidos p " +
                "JOIN clientes c ON c.id = p.cliente_id " +
                "LEFT JOIN pedido_servicios ps ON ps.pedido_id = p.id " +
                "ORDER BY p.id, ps.id";
        Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int pedidoId = rs.getInt("pedido_id");
                Pedido pedido = pedidos.get(pedidoId);
                if (pedido == null) {
                    pedido = crearPedidoDesdeFila(rs);
                    pedidos.put(pedidoId, pedido);
                }
                agregarServicioDesdeFila(pedido, rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los pedidos", e);
        }
        return new ArrayList<>(pedidos.values());
    }

    private Pedido crearPedidoDesdeFila(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(rs.getInt("cliente_id"), rs.getString("nombre"), rs.getString("email"));
        return new Pedido(rs.getInt("pedido_id"), cliente);
    }

    private void agregarServicioDesdeFila(Pedido pedido, ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo");
        if (tipo != null) {
            pedido.agregarServicio(crearServicio(tipo));
        }
    }

    private ServicioLavado crearServicio(String tipo) {
        return switch (tipo) {
            case "LavadoBasico" -> new LavadoBasico();
            case "LavadoEnSeco" -> new LavadoEnSeco();
            case "Planchado" -> new Planchado();
            default -> throw new IllegalArgumentException("Tipo de servicio desconocido: " + tipo);
        };
    }
}
