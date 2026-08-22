package com.lavanderia.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.lavanderia.aplicacion.GestorPedidos;
import com.lavanderia.modelo.Cliente;
import com.lavanderia.modelo.LavadoBasico;
import com.lavanderia.modelo.LavadoEnSeco;
import com.lavanderia.modelo.Pedido;
import com.lavanderia.modelo.Planchado;
import com.lavanderia.modelo.ServicioLavado;
import com.lavanderia.notificacion.Notificador;
import com.lavanderia.notificacion.NotificadorSwing;
import com.lavanderia.persistencia.PedidoRepositorio;
import com.lavanderia.servicio.CalculadoraTotal;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;

/**
 * Ventana principal. Editable con el GUI Designer de IntelliJ IDEA
 * (abrir LavanderiaFrame.form).
 * Solo arma pedidos y delega en GestorPedidos: no calcula totales ni
 * persiste directamente (SRP), y depende de las abstracciones
 * PedidoRepositorio/Notificador, no de Postgres ni de ningun detalle
 * concreto (DIP).
 */
public class LavanderiaFrame extends JFrame {
    private JPanel contentPane;
    private JTextField txtClienteId;
    private JTextField txtClienteNombre;
    private JTextField txtClienteEmail;
    private JCheckBox chkLavadoBasico;
    private JCheckBox chkLavadoSeco;
    private JCheckBox chkPlanchado;
    private JLabel lblTotal;
    private JButton btnRegistrar;
    private JButton btnRefrescar;
    private JTable tablaPedidos;
    private JTextArea txtLog;

    private final PedidoRepositorio repositorio;
    private final CalculadoraTotal calculadora;
    private final Notificador notificador;
    private final GestorPedidos gestor;
    private DefaultTableModel tableModel;

    public LavanderiaFrame(PedidoRepositorio repositorio, CalculadoraTotal calculadora) {
        super("Lavanderia - Gestion de pedidos");
        this.repositorio = repositorio;
        this.calculadora = calculadora;
        this.notificador = new NotificadorSwing(this::log);
        this.gestor = new GestorPedidos(repositorio, notificador, calculadora);

        $$$setupUI$$$();
        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(560, 600));

        tableModel = new DefaultTableModel(new Object[]{"Pedido", "Cliente", "Servicios", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPedidos.setModel(tableModel);

        ActionListener actualizarTotal = e -> actualizarTotalEstimado();
        chkLavadoBasico.addActionListener(actualizarTotal);
        chkLavadoSeco.addActionListener(actualizarTotal);
        chkPlanchado.addActionListener(actualizarTotal);

        btnRegistrar.addActionListener(e -> registrarPedido());
        btnRefrescar.addActionListener(e -> refrescarTabla());

        txtClienteId.setText(String.valueOf(siguienteClienteId()));
        refrescarTabla();

        pack();
        setLocationRelativeTo(null);
    }

    private void registrarPedido() {
        String nombre = txtClienteNombre.getText().trim();
        String email = txtClienteEmail.getText().trim();
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

        Cliente cliente = new Cliente(clienteId, nombre, email);
        Pedido pedido = new Pedido(siguientePedidoId(), cliente);
        if (chkLavadoBasico.isSelected())
            pedido.agregarServicio(new LavadoBasico());
        if (chkLavadoSeco.isSelected())
            pedido.agregarServicio(new LavadoEnSeco());
        if (chkPlanchado.isSelected())
            pedido.agregarServicio(new Planchado());

        try {
            gestor.registrarPedido(pedido);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo registrar el pedido:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        limpiarFormulario();
        refrescarTabla();
    }

    private void limpiarFormulario() {
        txtClienteNombre.setText("");
        txtClienteEmail.setText("");
        chkLavadoBasico.setSelected(false);
        chkLavadoSeco.setSelected(false);
        chkPlanchado.setSelected(false);
        actualizarTotalEstimado();
        txtClienteId.setText(String.valueOf(siguienteClienteId()));
    }

    private void actualizarTotalEstimado() {
        double total = 0.0;
        if (chkLavadoBasico.isSelected()) total += new LavadoBasico().precio();
        if (chkLavadoSeco.isSelected()) total += new LavadoEnSeco().precio();
        if (chkPlanchado.isSelected()) total += new Planchado().precio();
        lblTotal.setText(String.format("$%.2f", total));
    }

    private void refrescarTabla() {
        tableModel.setRowCount(0);
        for (Pedido pedido : repositorio.listar()) {
            StringBuilder servicios = new StringBuilder();
            for (ServicioLavado servicio : pedido.getServicios()) {
                if (servicios.length() > 0) servicios.append(", ");
                servicios.append(servicio.descripcion());
            }
            double total = calculadora.calcular(pedido);
            tableModel.addRow(new Object[]{
                    pedido.getId(),
                    pedido.getCliente().getNombre(),
                    servicios.toString(),
                    String.format("$%.2f", total)
            });
        }
    }

    private int siguientePedidoId() {
        return repositorio.listar().stream().mapToInt(Pedido::getId).max().orElse(0) + 1;
    }

    private int siguienteClienteId() {
        return repositorio.listar().stream().mapToInt(p -> p.getCliente().getId()).max().orElse(0) + 1;
    }

    private void log(String mensaje) {
        txtLog.append(mensaje + System.lineSeparator());
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(10, 2, new Insets(10, 10, 10, 10), -1, -1));

        final JLabel lblClienteId = new JLabel();
        lblClienteId.setText("ID Cliente:");
        contentPane.add(lblClienteId, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        txtClienteId = new JTextField();
        contentPane.add(txtClienteId, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(150, -1), null, 0, false));

        final JLabel lblNombre = new JLabel();
        lblNombre.setText("Nombre:");
        contentPane.add(lblNombre, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        txtClienteNombre = new JTextField();
        contentPane.add(txtClienteNombre, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(150, -1), null, 0, false));

        final JLabel lblEmail = new JLabel();
        lblEmail.setText("Email:");
        contentPane.add(lblEmail, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        txtClienteEmail = new JTextField();
        contentPane.add(txtClienteEmail, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(150, -1), null, 0, false));

        chkLavadoBasico = new JCheckBox();
        chkLavadoBasico.setText("Lavado básico ($200.00)");
        contentPane.add(chkLavadoBasico, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        chkLavadoSeco = new JCheckBox();
        chkLavadoSeco.setText("Lavado en seco ($500.00)");
        contentPane.add(chkLavadoSeco, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        chkPlanchado = new JCheckBox();
        chkPlanchado.setText("Planchado ($150.00)");
        contentPane.add(chkPlanchado, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        final JLabel lblTotalCaption = new JLabel();
        lblTotalCaption.setText("Total estimado:");
        contentPane.add(lblTotalCaption, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        lblTotal = new JLabel();
        lblTotal.setText("$0.00");
        contentPane.add(lblTotal, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        btnRegistrar = new JButton();
        btnRegistrar.setText("Registrar pedido");
        contentPane.add(btnRegistrar, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        btnRefrescar = new JButton();
        btnRefrescar.setText("Refrescar lista");
        contentPane.add(btnRefrescar, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        tablaPedidos = new JTable();
        final JScrollPane scrollPedidos = new JScrollPane();
        scrollPedidos.setViewportView(tablaPedidos);
        contentPane.add(scrollPedidos, new GridConstraints(8, 0, 1, 2, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(400, 150), null, 0, false));

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setRows(4);
        final JScrollPane scrollLog = new JScrollPane();
        scrollLog.setViewportView(txtLog);
        contentPane.add(scrollLog, new GridConstraints(9, 0, 1, 2, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(400, 80), null, 0, false));
    }

    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
