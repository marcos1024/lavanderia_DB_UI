package com.lavanderia;

import com.lavanderia.persistencia.PedidoRepositorio;
import com.lavanderia.persistencia.RepositorioEnMemoria;
import com.lavanderia.persistencia.RepositorioPostgres;
import com.lavanderia.servicio.CalculadoraTotal;
import com.lavanderia.ui.LavanderiaFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // Raiz de composicion: aca se elige la implementacion concreta del
        // repositorio y se inyecta en el resto de la app (DIP). Si Postgres
        // no esta disponible (por ejemplo, db.properties sin configurar),
        // se carga a un repositorio en memoria para no bloquear la UI.
        PedidoRepositorio repositorio = crearRepositorio();
        CalculadoraTotal calculadora = new CalculadoraTotal();

        SwingUtilities.invokeLater(() -> {
            LavanderiaFrame frame = new LavanderiaFrame(repositorio, calculadora);
            frame.setVisible(true);
        });
    }

    private static PedidoRepositorio crearRepositorio() {
        RepositorioPostgres repositorioPostgres = new RepositorioPostgres();
        try {
            repositorioPostgres.listar();
            return repositorioPostgres;
        } catch (RuntimeException e) {
            String causa = (e.getCause() instanceof SQLException) ? e.getCause().getMessage() : e.getMessage();
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a PostgreSQL, se va a usar un repositorio en memoria.\n"
                            + "Revisa src/main/resources/db.properties.\n\nDetalle: " + causa,
                    "Sin conexion a la base de datos", JOptionPane.WARNING_MESSAGE);
            return new RepositorioEnMemoria();
        }
    }
}
