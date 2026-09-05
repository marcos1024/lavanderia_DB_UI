package com.lavanderia.ui;

import com.lavanderia.aplicacion.GestorPedidos;
import com.lavanderia.modelo.Cliente;
import com.lavanderia.modelo.Pedido;
import com.lavanderia.modelo.ServicioLavado;
import com.lavanderia.modelo.TipoServicio;
import com.lavanderia.notificacion.NotificadorSwing;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Formv3 extends JFrame {
    private JPanel contentPane;
    private JTextField txtClienteId;
    private JTextField txtClienteNombre;
    private JTextField txtClienteEmail;
    private JCheckBox chkLavadoBasico;
    private JCheckBox chkLavadoSeco;
    private JCheckBox chkPlanchado;
    private JLabel lblTotal;
    private JButton btnAgregarPedido;
    private JButton btnActualizarLista;
    private JScrollPane scrollTabla;
    private JTable tablaPedidos;
    private JTextArea txtLog;
    private JButton btnSalir;


    // El gestor es quien sabe guardar/recuperar pedidos y avisar al cliente.
    private final GestorPedidos gestor;
    // Modelo de la tabla
    private DefaultTableModel tableModel;

    public Formv3(GestorPedidos gestor) {
        super("Lavanderia - Agenda de pedidos");
        this.gestor = gestor;
        // El log de la ventana muestra los avisos que genera el gestor al registrar un pedido.
        this.gestor.setNotificador(new NotificadorSwing(this::log));

        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        inicializarTabla();

        btnAgregarPedido.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                registrarPedido();
            }
        });
        btnActualizarLista.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                recuperarDatos();
            }
        });
        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                System.exit(0);
            }
        });

        // Al abrir la ventana ya se propone el proximo ID y se trae la agenda guardada.
        txtClienteId.setText(String.valueOf(gestor.siguienteClienteId()));
        recuperarDatos();

        // Tamano fijo
        setSize(700, 650);
        setResizable(false); //No permite modificar el tamaño del form
        setLocationRelativeTo(null);

        // Los tres checkboxes comparten el mismo listener: cada vez que se
        // tilda/destilda uno, se vuelve a sumar el total estimado.
        ActionListener recalcularTotal = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarTotalEstimado();
            }
        };
        chkLavadoBasico.addActionListener(recalcularTotal);
        chkLavadoSeco.addActionListener(recalcularTotal);
        chkPlanchado.addActionListener(recalcularTotal);
    }

    /** Suma el precio de los servicios tildados y lo muestra en lblTotal. */
    private void actualizarTotalEstimado() {
        double total = 0.0;
        if (chkLavadoBasico.isSelected()) total += TipoServicio.LAVADO_BASICO.precio();
        if (chkLavadoSeco.isSelected()) total += TipoServicio.LAVADO_EN_SECO.precio();
        if (chkPlanchado.isSelected()) total += TipoServicio.PLANCHADO.precio();
        lblTotal.setText(String.format("$%.2f", total));
    }

    /** Deja la tabla lista con las columnas de la agenda y sin filas editables a mano. */
    private void inicializarTabla() {
        tableModel = new DefaultTableModel(new Object[]{"Pedido", "Cliente", "Servicios", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPedidos.setModel(tableModel);

        // Con ventana de tamano fijo no siempre entran todas las filas: se deja
        // la barra de scroll siempre visible para que quede claro que hay que
        // bajar para ver el resto de los pedidos.
        //setSize(700, 650);
        //setResizable(false);
        scrollTabla.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    /**
     * Recupera los datos de la agenda: le pide al gestor la lista de pedidos
     * guardados
     */
    private void recuperarDatos() {
        tableModel.setRowCount(0);
        for (Pedido pedido : gestor.listarPedidos()) {
            tableModel.addRow(new Object[]{
                    pedido.getId(),
                    pedido.getCliente().getNombre(),
                    describirServicios(pedido),
                    String.format("$%.2f", pedido.calcularTotal())
            });
        }
    }

    /** Junta las descripciones de los servicios de un pedido en un solo texto separado por comas. */
    private String describirServicios(Pedido pedido) {
        StringBuilder servicios = new StringBuilder();
        for (ServicioLavado servicio : pedido.getServicios()) {
            if (!servicios.isEmpty()) servicios.append(", ");
            servicios.append(servicio.descripcion());
        }
        return servicios.toString();
    }

    /** Escribe un mensaje en el area de log de la ventana. */
    private void log(String mensaje) {
        txtLog.append(mensaje + System.lineSeparator());
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    /** Lee los datos cargados en el formulario, arma el pedido y lo registra a traves del gestor. */
    private void registrarPedido() {
        String nombre = txtClienteNombre.getText().trim();
        String email = txtClienteEmail.getText().trim();

        // Validaciones minimas antes de armar el pedido.
        if (nombre.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa nombre y email del cliente.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!chkLavadoBasico.isSelected() && !chkLavadoSeco.isSelected() && !chkPlanchado.isSelected()) {
            JOptionPane.showMessageDialog(this, "Selecciona al menos un servicio.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int clienteId;
        try {
            clienteId = Integer.parseInt(txtClienteId.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El ID de cliente debe ser numerico.",
                    "Datos invalidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Se arma el pedido con los servicios que esten tildados.
        Cliente cliente = new Cliente(clienteId, nombre, email);
        Pedido pedido = new Pedido(gestor.siguientePedidoId(), cliente);
        if (chkLavadoBasico.isSelected()) pedido.agregarServicio(TipoServicio.LAVADO_BASICO);
        if (chkLavadoSeco.isSelected()) pedido.agregarServicio(TipoServicio.LAVADO_EN_SECO);
        if (chkPlanchado.isSelected()) pedido.agregarServicio(TipoServicio.PLANCHADO);

        try {
            gestor.registrarPedido(pedido);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo registrar el pedido:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        limpiarFormulario();
        // Sin esto el pedido se guarda pero la tabla sigue mostrando lo viejo.
        recuperarDatos();
    }

    /** Deja el formulario en blanco y listo para el siguiente pedido. */
    private void limpiarFormulario() {
        txtClienteNombre.setText("");
        txtClienteEmail.setText("");
        chkLavadoBasico.setSelected(false);
        chkLavadoSeco.setSelected(false);
        chkPlanchado.setSelected(false);
        txtClienteId.setText(String.valueOf(gestor.siguienteClienteId()));
        actualizarTotalEstimado();
    }
}
