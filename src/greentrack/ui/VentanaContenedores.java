package greentrack.ui;

import greentrack.dao.TipoResiduoDAO;
import greentrack.dao.UbicacionDAO;
import greentrack.model.Contenedor;
import greentrack.model.TipoResiduo;
import greentrack.model.Ubicacion;
import greentrack.service.ContenedorService;
import greentrack.util.Validaciones;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana de administración de contenedores de reciclaje.
 * Permite listar, filtrar, registrar, editar y consultar estado de llenado mediante fn_porcentaje_llenado.
 */
public class VentanaContenedores extends JDialog {

    private static final Color COLOR_CAFE = new Color(74, 44, 23);
    private static final Color COLOR_AMARILLO = new Color(204, 153, 51);
    private static final Color COLOR_VERDE = new Color(46, 125, 50);
    private static final Color COLOR_BLANCO = Color.WHITE;

    private JTextField txtCodigoBarras;
    private JTextField txtNombre;
    private JComboBox<TipoResiduo> cmbTipoResiduo;
    private JComboBox<Ubicacion> cmbUbicacion;
    private JTextField txtCapacidadKg;
    private JComboBox<String> cmbEstado;

    private JComboBox<TipoResiduo> cmbFiltroTipo;
    private JComboBox<Ubicacion> cmbFiltroUbicacion;
    private JButton btnFiltrar;
    private JButton btnLimpiarFiltro;

    private JButton btnGuardar;
    private JButton btnLimpiar;
    private JButton btnAlternarEstado;

    private JTable tblContenedores;
    private DefaultTableModel modeloTabla;

    private final ContenedorService contenedorService = new ContenedorService();
    private final TipoResiduoDAO tipoDAO = new TipoResiduoDAO();
    private final UbicacionDAO ubicacionDAO = new UbicacionDAO();

    private Integer contenedorSeleccionadoId = null;
    private final boolean editable;

    public VentanaContenedores(Window owner, boolean editable) {
        super(owner, editable ? "Gestión de Contenedores" : "Consulta de Contenedores", ModalityType.APPLICATION_MODAL);
        this.editable = editable;

        setSize(1020, 680);
        setMinimumSize(new Dimension(880, 580));
        setLocationRelativeTo(owner);

        inicializarComponentes();
        cargarCombos();
        cargarContenedores();
        setVisible(true);
    }

    public VentanaContenedores() {
        this(null, true);
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(14, 14)) {
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

        // Título
        JLabel lblTitulo = new JLabel(editable ? "Administración de Contenedores de Reciclaje" : "Consulta de Contenedores (Modo Lectura)");
        lblTitulo.setForeground(COLOR_BLANCO);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // Formulario (solo visible o editable si editable=true)
        if (editable) {
            JPanel panelForm = crearTarjetaPanel(new BorderLayout(0, 8), new EmptyBorder(12, 16, 12, 16));
            JLabel lblSub = new JLabel("Registrar / Modificar Contenedor");
            lblSub.setForeground(COLOR_BLANCO);
            lblSub.setFont(new Font("SansSerif", Font.BOLD, 15));
            panelForm.add(lblSub, BorderLayout.NORTH);

            JPanel campos = new JPanel(new GridLayout(2, 3, 12, 6));
            campos.setOpaque(false);

            JPanel c1 = new JPanel(new GridLayout(2, 1)); c1.setOpaque(false);
            c1.add(crearLabel("Código de Barras:"));
            txtCodigoBarras = new JTextField();
            c1.add(txtCodigoBarras);
            campos.add(c1);

            JPanel c2 = new JPanel(new GridLayout(2, 1)); c2.setOpaque(false);
            c2.add(crearLabel("Nombre del Contenedor:"));
            txtNombre = new JTextField();
            c2.add(txtNombre);
            campos.add(c2);

            JPanel c3 = new JPanel(new GridLayout(2, 1)); c3.setOpaque(false);
            c3.add(crearLabel("Tipo de Residuo (Único por contenedor):"));
            cmbTipoResiduo = new JComboBox<>();
            c3.add(cmbTipoResiduo);
            campos.add(c3);

            JPanel c4 = new JPanel(new GridLayout(2, 1)); c4.setOpaque(false);
            c4.add(crearLabel("Ubicación / Planta:"));
            cmbUbicacion = new JComboBox<>();
            c4.add(cmbUbicacion);
            campos.add(c4);

            JPanel c5 = new JPanel(new GridLayout(2, 1)); c5.setOpaque(false);
            c5.add(crearLabel("Capacidad Máxima (kg):"));
            txtCapacidadKg = new JTextField();
            c5.add(txtCapacidadKg);
            campos.add(c5);

            JPanel c6 = new JPanel(new GridLayout(2, 1)); c6.setOpaque(false);
            c6.add(crearLabel("Estado:"));
            cmbEstado = new JComboBox<>(new String[] { "ACTIVO", "MANTENIMIENTO", "INACTIVO" });
            c6.add(cmbEstado);
            campos.add(c6);

            panelForm.add(campos, BorderLayout.CENTER);

            JPanel panelBtnForm = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
            panelBtnForm.setOpaque(false);

            btnGuardar = crearBoton("Guardar", COLOR_VERDE, 120, 32);
            btnGuardar.addActionListener(e -> guardarContenedor());

            btnLimpiar = crearBoton("Limpiar", new Color(120, 120, 120), 100, 32);
            btnLimpiar.addActionListener(e -> limpiarFormulario());

            panelBtnForm.add(btnGuardar);
            panelBtnForm.add(btnLimpiar);
            panelForm.add(panelBtnForm, BorderLayout.SOUTH);

            panelPrincipal.add(panelForm, BorderLayout.CENTER);
        }

        // Panel Inferior: Filtros y Tabla
        JPanel panelInferior = crearTarjetaPanel(new BorderLayout(0, 10), new EmptyBorder(12, 16, 12, 16));

        // Filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelFiltros.setOpaque(false);

        panelFiltros.add(crearLabel("Filtrar por Tipo:"));
        cmbFiltroTipo = new JComboBox<>();
        cmbFiltroTipo.setPreferredSize(new Dimension(160, 28));
        panelFiltros.add(cmbFiltroTipo);

        panelFiltros.add(crearLabel("Ubicación:"));
        cmbFiltroUbicacion = new JComboBox<>();
        cmbFiltroUbicacion.setPreferredSize(new Dimension(160, 28));
        panelFiltros.add(cmbFiltroUbicacion);

        btnFiltrar = crearBoton("Filtrar", COLOR_VERDE, 85, 28);
        btnFiltrar.addActionListener(e -> aplicarFiltros());
        panelFiltros.add(btnFiltrar);

        btnLimpiarFiltro = crearBoton("Ver Todos", COLOR_AMARILLO.darker(), 95, 28);
        btnLimpiarFiltro.addActionListener(e -> {
            cmbFiltroTipo.setSelectedIndex(0);
            cmbFiltroUbicacion.setSelectedIndex(0);
            cargarContenedores();
        });
        panelFiltros.add(btnLimpiarFiltro);

        panelInferior.add(panelFiltros, BorderLayout.NORTH);

        // Tabla
        String[] columnas = { "ID", "Código Barras", "Nombre", "Tipo Residuo", "Ubicación", "Capacidad (kg)", "Peso Actual (kg)", "% Llenado", "Estado" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblContenedores = new JTable(modeloTabla);
        tblContenedores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblContenedores.setRowHeight(24);

        // Renderizador con color para la columna de porcentaje
        tblContenedores.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    try {
                        String s = value.toString().replace("%", "").trim();
                        double pct = Double.parseDouble(s);
                        if (pct >= 85.0) {
                            c.setForeground(new Color(220, 40, 40));
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else if (pct >= 60.0) {
                            c.setForeground(new Color(210, 140, 0));
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else {
                            c.setForeground(new Color(30, 130, 40));
                        }
                    } catch (Exception ignored) {
                    }
                }
                return c;
            }
        });

        if (editable) {
            tblContenedores.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && tblContenedores.getSelectedRow() != -1) {
                    cargarSeleccionEnFormulario();
                }
            });
        }

        panelInferior.add(new JScrollPane(tblContenedores), BorderLayout.CENTER);

        if (editable) {
            JPanel panelBtnTabla = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            panelBtnTabla.setOpaque(false);

            btnAlternarEstado = crearBoton("Cambiar Estado (Mantenimiento)", COLOR_AMARILLO.darker(), 210, 32);
            btnAlternarEstado.addActionListener(e -> alternarMantenimiento());
            panelBtnTabla.add(btnAlternarEstado);

            panelInferior.add(panelBtnTabla, BorderLayout.SOUTH);
        }

        if (editable) {
            panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
            panelInferior.setPreferredSize(new Dimension(0, 320));
        } else {
            panelPrincipal.add(panelInferior, BorderLayout.CENTER);
        }

        setContentPane(panelPrincipal);
    }

    private void cargarCombos() {
        List<TipoResiduo> tipos = tipoDAO.listarActivos();
        List<Ubicacion> ubicaciones = ubicacionDAO.listarActivas();

        if (editable) {
            cmbTipoResiduo.removeAllItems();
            for (TipoResiduo t : tipos) cmbTipoResiduo.addItem(t);

            cmbUbicacion.removeAllItems();
            for (Ubicacion u : ubicaciones) cmbUbicacion.addItem(u);
        }

        cmbFiltroTipo.removeAllItems();
        cmbFiltroTipo.addItem(new TipoResiduo(0, "-- Todos los Tipos --", "", true));
        for (TipoResiduo t : tipos) cmbFiltroTipo.addItem(t);

        cmbFiltroUbicacion.removeAllItems();
        cmbFiltroUbicacion.addItem(new Ubicacion(0, "-- Todas las Ubicaciones --", "", "", true));
        for (Ubicacion u : ubicaciones) cmbFiltroUbicacion.addItem(u);
    }

    private void cargarContenedores() {
        SwingWorker<List<Contenedor>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Contenedor> doInBackground() {
                return contenedorService.obtenerTodos();
            }

            @Override
            protected void done() {
                try {
                    mostrarEnTabla(get());
                } catch (Exception ex) {
                    Validaciones.mostrarError(VentanaContenedores.this, "Error al cargar contenedores: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void aplicarFiltros() {
        TipoResiduo tipoSel = (TipoResiduo) cmbFiltroTipo.getSelectedItem();
        Ubicacion ubiSel = (Ubicacion) cmbFiltroUbicacion.getSelectedItem();

        Integer tid = (tipoSel != null && tipoSel.getId() > 0) ? tipoSel.getId() : null;
        Integer uid = (ubiSel != null && ubiSel.getId() > 0) ? ubiSel.getId() : null;

        SwingWorker<List<Contenedor>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Contenedor> doInBackground() {
                return contenedorService.filtrar(tid, uid);
            }

            @Override
            protected void done() {
                try {
                    mostrarEnTabla(get());
                } catch (Exception ex) {
                    Validaciones.mostrarError(VentanaContenedores.this, "Error al filtrar: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void mostrarEnTabla(List<Contenedor> lista) {
        modeloTabla.setRowCount(0);
        for (Contenedor c : lista) {
            modeloTabla.addRow(new Object[] {
                c.getId(),
                c.getCodigoBarras(),
                c.getNombre(),
                c.getTipoNombre(),
                c.getUbicacionNombre(),
                Validaciones.formatearDecimal(c.getCapacidadKg()),
                Validaciones.formatearDecimal(c.getPesoActualKg()),
                String.format("%.1f%%", c.getPorcentajeLlenado()),
                c.getEstado()
            });
        }
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tblContenedores.getSelectedRow();
        if (fila == -1) return;

        int id = (int) modeloTabla.getValueAt(fila, 0);
        Contenedor c = contenedorService.buscarPorCodigoBarras((String) modeloTabla.getValueAt(fila, 1));
        if (c != null) {
            contenedorSeleccionadoId = c.getId();
            txtCodigoBarras.setText(c.getCodigoBarras());
            txtNombre.setText(c.getNombre());
            txtCapacidadKg.setText(String.valueOf(c.getCapacidadKg()));
            cmbEstado.setSelectedItem(c.getEstado());

            for (int i = 0; i < cmbTipoResiduo.getItemCount(); i++) {
                if (cmbTipoResiduo.getItemAt(i).getId() == c.getTipoId()) {
                    cmbTipoResiduo.setSelectedIndex(i);
                    break;
                }
            }

            for (int i = 0; i < cmbUbicacion.getItemCount(); i++) {
                if (cmbUbicacion.getItemAt(i).getId() == c.getUbicacionId()) {
                    cmbUbicacion.setSelectedIndex(i);
                    break;
                }
            }

            btnGuardar.setText("Actualizar");
        }
    }

    private void guardarContenedor() {
        if (!editable) return;

        String codigo = txtCodigoBarras.getText().trim();
        String nombre = txtNombre.getText().trim();
        String capStr = txtCapacidadKg.getText().trim();
        TipoResiduo tipo = (TipoResiduo) cmbTipoResiduo.getSelectedItem();
        Ubicacion ubi = (Ubicacion) cmbUbicacion.getSelectedItem();
        String estado = (String) cmbEstado.getSelectedItem();

        if (tipo == null || ubi == null) {
            Validaciones.mostrarError(this, "Debe seleccionar un tipo de residuo y una ubicación.");
            return;
        }

        if (!Validaciones.esDecimalPositivo(capStr)) {
            Validaciones.mostrarError(this, "La capacidad debe ser un número decimal mayor a 0 kg.");
            return;
        }
        double capacidad = Double.parseDouble(capStr);

        Contenedor c = new Contenedor();
        c.setId(contenedorSeleccionadoId != null ? contenedorSeleccionadoId : 0);
        c.setCodigoBarras(codigo);
        c.setNombre(nombre);
        c.setTipoId(tipo.getId());
        c.setUbicacionId(ubi.getId());
        c.setCapacidadKg(capacidad);
        c.setEstado(estado);

        boolean esNuevo = (contenedorSeleccionadoId == null);
        String resultado = contenedorService.guardarContenedor(c, esNuevo);

        if ("OK".equals(resultado)) {
            Validaciones.mostrarExito(this, esNuevo ? "Contenedor registrado exitosamente." : "Contenedor actualizado correctamente.");
            limpiarFormulario();
            cargarContenedores();
        } else {
            Validaciones.mostrarError(this, resultado);
        }
    }

    private void alternarMantenimiento() {
        int fila = tblContenedores.getSelectedRow();
        if (fila == -1) {
            Validaciones.mostrarError(this, "Seleccione un contenedor de la tabla.");
            return;
        }

        int id = (int) modeloTabla.getValueAt(fila, 0);
        String estadoActual = (String) modeloTabla.getValueAt(fila, 8);
        String nuevoEstado = "ACTIVO".equalsIgnoreCase(estadoActual) ? "MANTENIMIENTO" : "ACTIVO";

        if (Validaciones.confirmar(this, "¿Desea cambiar el estado del contenedor a " + nuevoEstado + "?")) {
            boolean exito = contenedorService.cambiarEstado(id, nuevoEstado);
            if (exito) {
                Validaciones.mostrarExito(this, "Estado actualizado a " + nuevoEstado + ".");
                cargarContenedores();
                limpiarFormulario();
            } else {
                Validaciones.mostrarError(this, "No fue posible cambiar el estado.");
            }
        }
    }

    private void limpiarFormulario() {
        contenedorSeleccionadoId = null;
        if (txtCodigoBarras != null) txtCodigoBarras.setText("");
        if (txtNombre != null) txtNombre.setText("");
        if (txtCapacidadKg != null) txtCapacidadKg.setText("");
        if (cmbEstado != null) cmbEstado.setSelectedItem("ACTIVO");
        if (btnGuardar != null) btnGuardar.setText("Guardar");
        tblContenedores.clearSelection();
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
