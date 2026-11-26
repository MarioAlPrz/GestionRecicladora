package greentrack.ui;

import greentrack.dao.UbicacionDAO;
import greentrack.model.Ubicacion;
import greentrack.util.SesionUsuario;
import greentrack.util.Validaciones;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana para la gestión de ubicaciones y rutas del sistema GreenTrack.
 */
public class VentanaRutas extends JDialog {

    private static final Color COLOR_CAFE = new Color(74, 44, 23);
    private static final Color COLOR_AMARILLO = new Color(204, 153, 51);
    private static final Color COLOR_VERDE = new Color(46, 125, 50);
    private static final Color COLOR_BLANCO = Color.WHITE;

    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JTextField txtReferencia;
    private JCheckBox chkActiva;

    private JButton btnGuardar;
    private JButton btnLimpiar;
    private JButton btnAlternarEstado;

    private JTable tblUbicaciones;
    private DefaultTableModel modeloTabla;

    private final UbicacionDAO ubicacionDAO = new UbicacionDAO();
    private Integer ubicacionSeleccionadaId = null;
    private final boolean editable;

    public VentanaRutas(Window owner, boolean editable) {
        super(owner, editable ? "Gestión de Rutas y Ubicaciones" : "Consulta de Estado de Rutas", ModalityType.APPLICATION_MODAL);
        this.editable = editable && SesionUsuario.puedeModificar();

        setSize(880, 560);
        setMinimumSize(new Dimension(780, 480));
        setLocationRelativeTo(owner);

        inicializarComponentes();
        cargarUbicaciones();
        setVisible(true);
    }

    public VentanaRutas(Window owner) {
        this(owner, true);
    }

    public VentanaRutas() {
        this(null, true);
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(16, 16)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_CAFE, 0, getHeight(), COLOR_AMARILLO);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelPrincipal.setBorder(new EmptyBorder(16, 20, 20, 20));

        JLabel lblTitulo = new JLabel(editable ? "Administración de Ubicaciones y Zonas de Reciclaje" : "Consulta de Rutas y Zonas");
        lblTitulo.setForeground(COLOR_BLANCO);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setOpaque(false);
        split.setDividerLocation(340);

        // Panel Izquierdo: Formulario
        JPanel panelForm = crearTarjetaPanel(new BorderLayout(0, 12), new EmptyBorder(16, 16, 16, 16));
        JLabel lblSub = new JLabel("Datos de la Ubicación / Zona");
        lblSub.setForeground(COLOR_BLANCO);
        lblSub.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelForm.add(lblSub, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(0, 1, 6, 8));
        campos.setOpaque(false);

        campos.add(crearLabel("Nombre de la Zona / Ubicación:"));
        txtNombre = new JTextField();
        campos.add(txtNombre);

        campos.add(crearLabel("Descripción:"));
        txtDescripcion = new JTextField();
        campos.add(txtDescripcion);

        campos.add(crearLabel("Referencia Física / Dirección:"));
        txtReferencia = new JTextField();
        campos.add(txtReferencia);

        chkActiva = new JCheckBox("Ubicación activa");
        chkActiva.setSelected(true);
        chkActiva.setOpaque(false);
        chkActiva.setForeground(COLOR_BLANCO);
        chkActiva.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campos.add(chkActiva);

        panelForm.add(campos, BorderLayout.CENTER);

        JPanel panelBtnForm = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelBtnForm.setOpaque(false);

        btnGuardar = crearBoton("Guardar", COLOR_VERDE, 110, 36);
        btnGuardar.setEnabled(editable);
        btnGuardar.addActionListener(e -> guardarUbicacion());

        btnLimpiar = crearBoton("Limpiar", new Color(120, 120, 120), 100, 36);
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBtnForm.add(btnGuardar);
        panelBtnForm.add(btnLimpiar);
        panelForm.add(panelBtnForm, BorderLayout.SOUTH);

        split.setLeftComponent(panelForm);

        // Panel Derecho: Tabla
        JPanel panelTabla = crearTarjetaPanel(new BorderLayout(0, 10), new EmptyBorder(16, 16, 16, 16));

        String[] columnas = { "ID", "Nombre", "Descripción", "Referencia", "Estado" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblUbicaciones = new JTable(modeloTabla);
        tblUbicaciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUbicaciones.setRowHeight(24);
        tblUbicaciones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblUbicaciones.getSelectedRow() != -1) {
                cargarSeleccion();
            }
        });

        panelTabla.add(new JScrollPane(tblUbicaciones), BorderLayout.CENTER);

        JPanel panelInferiorTabla = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelInferiorTabla.setOpaque(false);

        btnAlternarEstado = crearBoton("Cambiar Estado", COLOR_AMARILLO.darker(), 140, 34);
        btnAlternarEstado.setEnabled(editable);
        btnAlternarEstado.addActionListener(e -> alternarEstado());

        panelInferiorTabla.add(btnAlternarEstado);
        panelTabla.add(panelInferiorTabla, BorderLayout.SOUTH);

        split.setRightComponent(panelTabla);

        panelPrincipal.add(split, BorderLayout.CENTER);
        setContentPane(panelPrincipal);
    }

    private void cargarUbicaciones() {
        SwingWorker<List<Ubicacion>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Ubicacion> doInBackground() {
                return ubicacionDAO.listarTodas();
            }

            @Override
            protected void done() {
                try {
                    List<Ubicacion> lista = get();
                    modeloTabla.setRowCount(0);
                    for (Ubicacion u : lista) {
                        modeloTabla.addRow(new Object[] {
                            u.getId(),
                            u.getNombre(),
                            u.getDescripcion(),
                            u.getReferencia(),
                            u.isEstado() ? "ACTIVO" : "INACTIVO"
                        });
                    }
                } catch (Exception ex) {
                    Validaciones.mostrarError(VentanaRutas.this, "Error al cargar ubicaciones: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void cargarSeleccion() {
        int fila = tblUbicaciones.getSelectedRow();
        if (fila == -1) return;

        int id = (int) modeloTabla.getValueAt(fila, 0);
        Ubicacion u = ubicacionDAO.buscarPorId(id);
        if (u != null) {
            ubicacionSeleccionadaId = u.getId();
            txtNombre.setText(u.getNombre());
            txtDescripcion.setText(u.getDescripcion());
            txtReferencia.setText(u.getReferencia());
            chkActiva.setSelected(u.isEstado());
            btnGuardar.setText("Actualizar");
        }
    }

    private void guardarUbicacion() {
        if (!editable) return;

        String nombre = txtNombre.getText().trim();
        String desc = txtDescripcion.getText().trim();
        String ref = txtReferencia.getText().trim();
        boolean estado = chkActiva.isSelected();

        if (Validaciones.esTextoVacio(nombre)) {
            Validaciones.mostrarError(this, "El nombre de la ubicación es obligatorio.");
            return;
        }

        if (ubicacionSeleccionadaId == null) {
            if (ubicacionDAO.existeNombre(nombre)) {
                Validaciones.mostrarError(this, "Ya existe una ubicación con ese nombre.");
                return;
            }
            Ubicacion nueva = new Ubicacion(0, nombre, desc, ref, estado);
            boolean exito = ubicacionDAO.insertar(nueva);
            if (exito) {
                Validaciones.mostrarExito(this, "Ubicación registrada exitosamente.");
                limpiarFormulario();
                cargarUbicaciones();
            } else {
                Validaciones.mostrarError(this, "Error al registrar la ubicación.");
            }
        } else {
            if (ubicacionDAO.existeNombre(nombre, ubicacionSeleccionadaId)) {
                Validaciones.mostrarError(this, "El nombre ya pertenece a otra ubicación existente.");
                return;
            }
            Ubicacion actual = new Ubicacion(ubicacionSeleccionadaId, nombre, desc, ref, estado);
            boolean exito = ubicacionDAO.actualizar(actual);
            if (exito) {
                Validaciones.mostrarExito(this, "Ubicación actualizada correctamente.");
                limpiarFormulario();
                cargarUbicaciones();
            } else {
                Validaciones.mostrarError(this, "Error al actualizar la ubicación.");
            }
        }
    }

    private void alternarEstado() {
        if (!editable) return;
        int fila = tblUbicaciones.getSelectedRow();
        if (fila == -1) {
            Validaciones.mostrarError(this, "Seleccione una ubicación de la tabla.");
            return;
        }

        int id = (int) modeloTabla.getValueAt(fila, 0);
        String estadoStr = (String) modeloTabla.getValueAt(fila, 4);
        boolean nuevoEstado = !"ACTIVO".equalsIgnoreCase(estadoStr);

        if (!nuevoEstado && ubicacionDAO.tieneContenedoresAsociados(id)) {
            boolean continuar = Validaciones.confirmar(this,
                    "Esta ubicación tiene contenedores vinculados.\n" +
                    "¿Desea desactivarla de todos modos?");
            if (!continuar) return;
        }

        boolean exito = ubicacionDAO.cambiarEstado(id, nuevoEstado);
        if (exito) {
            Validaciones.mostrarExito(this, "Estado actualizado correctamente.");
            limpiarFormulario();
            cargarUbicaciones();
        } else {
            Validaciones.mostrarError(this, "Error al actualizar estado.");
        }
    }

    private void limpiarFormulario() {
        ubicacionSeleccionadaId = null;
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtReferencia.setText("");
        chkActiva.setSelected(true);
        btnGuardar.setText("Guardar");
        tblUbicaciones.clearSelection();
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(COLOR_BLANCO);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return lbl;
    }

    private JPanel crearTarjetaPanel(LayoutManager layout, EmptyBorder padding) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 36, 18, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(padding);
        return panel;
    }

    private JButton crearBoton(String texto, Color fondo, int ancho, int alto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? fondo.darker() : (getModel().isRollover() ? fondo.brighter() : fondo));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(COLOR_BLANCO);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(ancho, alto));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
