package greentrack.ui;

import greentrack.dao.TipoResiduoDAO;
import greentrack.model.TipoResiduo;
import greentrack.util.SesionUsuario;
import greentrack.util.Validaciones;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana para la gestión de tipos de residuo reciclables.
 */
public class VentanaTiposResiduo extends JDialog {

    private static final Color COLOR_CAFE = new Color(74, 44, 23);
    private static final Color COLOR_AMARILLO = new Color(204, 153, 51);
    private static final Color COLOR_VERDE = new Color(46, 125, 50);
    private static final Color COLOR_BLANCO = Color.WHITE;

    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JCheckBox chkActivo;

    private JButton btnGuardar;
    private JButton btnLimpiar;
    private JButton btnAlternarEstado;

    private JTable tblTipos;
    private DefaultTableModel modeloTabla;

    private final TipoResiduoDAO tipoDAO = new TipoResiduoDAO();
    private Integer tipoSeleccionadoId = null;
    private final boolean editable;

    public VentanaTiposResiduo(Window owner) {
        super(owner, "Tipos de Residuo - GreenTrack", ModalityType.APPLICATION_MODAL);
        this.editable = SesionUsuario.puedeModificar();

        setSize(850, 560);
        setMinimumSize(new Dimension(750, 480));
        setLocationRelativeTo(owner);

        inicializarComponentes();
        cargarTipos();
        setVisible(true);
    }

    public VentanaTiposResiduo() {
        this(null);
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

        JLabel lblTitulo = new JLabel("Catálogo de Tipos de Residuo");
        lblTitulo.setForeground(COLOR_BLANCO);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setOpaque(false);
        split.setDividerLocation(340);

        // Panel Izquierdo: Formulario
        JPanel panelForm = crearTarjetaPanel(new BorderLayout(0, 12), new EmptyBorder(16, 16, 16, 16));
        JLabel lblSub = new JLabel("Datos del Residuo");
        lblSub.setForeground(COLOR_BLANCO);
        lblSub.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelForm.add(lblSub, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(0, 1, 6, 8));
        campos.setOpaque(false);

        campos.add(crearLabel("Nombre del Material (ej: Plástico PET):"));
        txtNombre = new JTextField();
        campos.add(txtNombre);

        campos.add(crearLabel("Descripción:"));
        txtDescripcion = new JTextField();
        campos.add(txtDescripcion);

        chkActivo = new JCheckBox("Tipo de residuo activo");
        chkActivo.setSelected(true);
        chkActivo.setOpaque(false);
        chkActivo.setForeground(COLOR_BLANCO);
        chkActivo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        campos.add(chkActivo);

        panelForm.add(campos, BorderLayout.CENTER);

        JPanel panelBtnForm = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelBtnForm.setOpaque(false);

        btnGuardar = crearBoton("Guardar", COLOR_VERDE, 110, 36);
        btnGuardar.setEnabled(editable);
        btnGuardar.addActionListener(e -> guardarTipo());

        btnLimpiar = crearBoton("Limpiar", new Color(120, 120, 120), 100, 36);
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBtnForm.add(btnGuardar);
        panelBtnForm.add(btnLimpiar);
        panelForm.add(panelBtnForm, BorderLayout.SOUTH);

        split.setLeftComponent(panelForm);

        // Panel Derecho: Tabla
        JPanel panelTabla = crearTarjetaPanel(new BorderLayout(0, 10), new EmptyBorder(16, 16, 16, 16));

        String[] columnas = { "ID", "Nombre", "Descripción", "Estado" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblTipos = new JTable(modeloTabla);
        tblTipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTipos.setRowHeight(24);
        tblTipos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblTipos.getSelectedRow() != -1) {
                cargarSeleccion();
            }
        });

        panelTabla.add(new JScrollPane(tblTipos), BorderLayout.CENTER);

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

    private void cargarTipos() {
        SwingWorker<List<TipoResiduo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TipoResiduo> doInBackground() {
                return tipoDAO.listarTodos();
            }

            @Override
            protected void done() {
                try {
                    List<TipoResiduo> lista = get();
                    modeloTabla.setRowCount(0);
                    for (TipoResiduo t : lista) {
                        modeloTabla.addRow(new Object[] {
                            t.getId(),
                            t.getNombre(),
                            t.getDescripcion(),
                            t.isEstado() ? "ACTIVO" : "INACTIVO"
                        });
                    }
                } catch (Exception ex) {
                    Validaciones.mostrarError(VentanaTiposResiduo.this, "Error al cargar tipos de residuo: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void cargarSeleccion() {
        int fila = tblTipos.getSelectedRow();
        if (fila == -1) return;

        int id = (int) modeloTabla.getValueAt(fila, 0);
        TipoResiduo t = tipoDAO.buscarPorId(id);
        if (t != null) {
            tipoSeleccionadoId = t.getId();
            txtNombre.setText(t.getNombre());
            txtDescripcion.setText(t.getDescripcion());
            chkActivo.setSelected(t.isEstado());
            btnGuardar.setText("Actualizar");
        }
    }

    private void guardarTipo() {
        if (!editable) {
            Validaciones.mostrarError(this, "Permiso denegado: El rol CONSULTA no puede modificar registros.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String desc = txtDescripcion.getText().trim();
        boolean estado = chkActivo.isSelected();

        if (Validaciones.esTextoVacio(nombre)) {
            Validaciones.mostrarError(this, "El nombre del tipo de residuo es obligatorio.");
            return;
        }

        if (tipoSeleccionadoId == null) {
            if (tipoDAO.existeNombre(nombre)) {
                Validaciones.mostrarError(this, "Ya existe un tipo de residuo registrado con ese nombre.");
                return;
            }
            TipoResiduo nuevo = new TipoResiduo(0, nombre, desc, estado);
            boolean exito = tipoDAO.insertar(nuevo);
            if (exito) {
                Validaciones.mostrarExito(this, "Tipo de residuo registrado correctamente.");
                limpiarFormulario();
                cargarTipos();
            } else {
                Validaciones.mostrarError(this, "Error al guardar el tipo de residuo.");
            }
        } else {
            if (tipoDAO.existeNombre(nombre, tipoSeleccionadoId)) {
                Validaciones.mostrarError(this, "El nombre ya pertenece a otro tipo de residuo existente.");
                return;
            }
            TipoResiduo actual = new TipoResiduo(tipoSeleccionadoId, nombre, desc, estado);
            boolean exito = tipoDAO.actualizar(actual);
            if (exito) {
                Validaciones.mostrarExito(this, "Tipo de residuo actualizado correctamente.");
                limpiarFormulario();
                cargarTipos();
            } else {
                Validaciones.mostrarError(this, "Error al actualizar el tipo de residuo.");
            }
        }
    }

    private void alternarEstado() {
        if (!editable) return;
        int fila = tblTipos.getSelectedRow();
        if (fila == -1) {
            Validaciones.mostrarError(this, "Seleccione un tipo de residuo de la tabla.");
            return;
        }

        int id = (int) modeloTabla.getValueAt(fila, 0);
        String estadoStr = (String) modeloTabla.getValueAt(fila, 3);
        boolean nuevoEstado = !"ACTIVO".equalsIgnoreCase(estadoStr);

        // Si se va a desactivar, verificar si hay contenedores asociados
        if (!nuevoEstado && tipoDAO.tieneContenedoresAsociados(id)) {
            boolean continuar = Validaciones.confirmar(this,
                    "Este tipo de residuo está asignado a uno o más contenedores.\n" +
                    "¿Desea desactivarlo de todos modos? (Los contenedores existentes mantendrán su historial).");
            if (!continuar) return;
        }

        boolean exito = tipoDAO.cambiarEstado(id, nuevoEstado);
        if (exito) {
            Validaciones.mostrarExito(this, "Estado actualizado correctamente.");
            limpiarFormulario();
            cargarTipos();
        } else {
            Validaciones.mostrarError(this, "Error al actualizar el estado.");
        }
    }

    private void limpiarFormulario() {
        tipoSeleccionadoId = null;
        txtNombre.setText("");
        txtDescripcion.setText("");
        chkActivo.setSelected(true);
        btnGuardar.setText("Guardar");
        tblTipos.clearSelection();
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
