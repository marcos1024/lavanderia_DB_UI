package com.lavanderia.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.lavanderia.aplicacion.GestorPedidos;
import com.lavanderia.modelo.Cliente;
import com.lavanderia.modelo.Pedido;
import com.lavanderia.modelo.ServicioLavado;
import com.lavanderia.modelo.TipoServicio;
import com.lavanderia.notificacion.NotificadorSwing;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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

        // Arma contentPane y el resto de los componentes "a mano" (no depende
        // de que IntelliJ instrumente el .form al compilar, asi que funciona
        // igual desde el IDE que desde mvn/consola).
        $$$setupUI$$$();
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
     * guardados (base de datos o memoria, segun este armado el GestorPedidos)
     * y repinta la tabla desde cero.
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
    }

    /**
     * Arma contentPane con todos los componentes, siguiendo el mismo layout
     * de grilla (11 filas x 4 columnas) definido en Formv3.form. Se escribe
     * a mano (en vez de dejarselo al GUI Designer) para que la ventana
     * funcione igual dentro de IntelliJ que corriendo por consola/Maven.
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(11, 4, new Insets(10, 10, 10, 10), -1, -1));

        final JLabel lblId = new JLabel();
        lblId.setText("ID Cliente:");
        contentPane.add(lblId, new GridConstraints(0, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));

        txtClienteId = new JTextField();
        contentPane.add(txtClienteId, new GridConstraints(0, 3, 1, 1, 8, 1, 6, 0, null, new Dimension(150, -1), null, 0, false));

        final JLabel lblNombre = new JLabel();
        lblNombre.setText("Nombre:");
        contentPane.add(lblNombre, new GridConstraints(1, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));

        txtClienteNombre = new JTextField();
        contentPane.add(txtClienteNombre, new GridConstraints(1, 3, 1, 1, 8, 1, 6, 0, null, new Dimension(150, -1), null, 0, false));

        final JLabel lblEmail = new JLabel();
        lblEmail.setText("Email:");
        contentPane.add(lblEmail, new GridConstraints(2, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));

        txtClienteEmail = new JTextField();
        contentPane.add(txtClienteEmail, new GridConstraints(2, 3, 1, 1, 8, 1, 6, 0, null, new Dimension(150, -1), null, 0, false));

        chkLavadoBasico = new JCheckBox();
        chkLavadoBasico.setText("Lavado básico ($200.00)");
        contentPane.add(chkLavadoBasico, new GridConstraints(3, 0, 1, 4, 8, 0, 3, 0, null, null, null, 0, false));

        chkLavadoSeco = new JCheckBox();
        chkLavadoSeco.setText("Lavado en seco ($500.00)");
        contentPane.add(chkLavadoSeco, new GridConstraints(4, 0, 1, 4, 8, 0, 3, 0, null, null, null, 0, false));

        chkPlanchado = new JCheckBox();
        chkPlanchado.setText("Planchado ($150.00)");
        contentPane.add(chkPlanchado, new GridConstraints(5, 0, 1, 4, 8, 0, 3, 0, null, null, null, 0, false));

        final JLabel lblTotalCaption = new JLabel();
        lblTotalCaption.setText("Total estimado:");
        contentPane.add(lblTotalCaption, new GridConstraints(6, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));

        lblTotal = new JLabel();
        lblTotal.setText("$0.00");
        contentPane.add(lblTotal, new GridConstraints(6, 3, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));

        btnAgregarPedido = new JButton();
        btnAgregarPedido.setText("Agregar pedido");
        contentPane.add(btnAgregarPedido, new GridConstraints(7, 0, 1, 1, 0, 1, 3, 0, null, null, null, 0, false));

        btnActualizarLista = new JButton();
        btnActualizarLista.setText("Actualizar lista");
        contentPane.add(btnActualizarLista, new GridConstraints(7, 3, 1, 1, 0, 1, 3, 0, null, null, null, 0, false));

        scrollTabla = new JScrollPane();
        scrollTabla.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        contentPane.add(scrollTabla, new GridConstraints(8, 0, 1, 4, 0, 3, 7, 7, null, new Dimension(400, 320), null, 0, false));

        tablaPedidos = new JTable();
        scrollTabla.setViewportView(tablaPedidos);

        final JScrollPane scrollLog = new JScrollPane();
        contentPane.add(scrollLog, new GridConstraints(9, 0, 1, 4, 0, 3, 7, 7, null, new Dimension(400, 80), null, 0, false));

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setRows(4);
        scrollLog.setViewportView(txtLog);

        final Spacer spacer1 = new Spacer();
        contentPane.add(spacer1, new GridConstraints(10, 1, 1, 1, 0, 1, 6, 1, null, null, null, 0, false));

        btnSalir = new JButton();
        btnSalir.setText("Salir");
        contentPane.add(btnSalir, new GridConstraints(10, 3, 1, 1, 0, 1, 3, 0, null, null, null, 0, false));
    }

    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
