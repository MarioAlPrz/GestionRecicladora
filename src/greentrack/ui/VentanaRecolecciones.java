package greentrack.ui;

import greentrack.dao.TipoResiduoDAO;
import greentrack.model.Recoleccion;
import greentrack.model.TipoResiduo;
import greentrack.service.RecoleccionService;
import greentrack.util.Validaciones;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana de historial y consulta de recolecciones realizadas en los contenedores.
 */
public class VentanaRecolecciones extends JDialog {

    private static final Color COLOR_CAFE = new Color(74, 44, 23);
    private static final Color COLOR_AMARILLO = new Color(204, 153, 51);
    private static final Color COLOR_VERDE = new Color(46, 125, 50);
    private static final Color COLOR_BLANCO = Color.WHITE;

    private JTextField txtFechaDesde;
    private JTextField txtFechaHasta;
    private JComboBox<TipoResiduo> cmbFiltroTipo;
    private JButton btnFiltrar;
    private JButton btnTodos;
    private JButton btnNuevaRecoleccion;

    private JTable tblRecolecciones;
    private DefaultTableModel modeloTabla;

    private final RecoleccionService recoleccionService = new RecoleccionService();
    private final TipoResiduoDAO tipoDAO = new TipoResiduoDAO();
    private final boolean editable;

    public VentanaRecolecciones(Window owner, boolean editable) {
        super(owner, editable ? "Historial y Registro de Recolecciones" : "Historial de Recolecciones (Consulta)", ModalityType.APPLICATION_MODAL);
        this.editable = editable;

        setSize(1000, 620);
        setMinimumSize(new Dimension(860, 500));
        setLocationRelativeTo(owner);

        inicializarComponentes();
        cargarComboTipos();
        cargarRecolecciones(null, null, null);
        setVisible(true);
    }

    public VentanaRecolecciones() {
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

        // Encabezado
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setOpaque(false);

        JLabel lblTitulo = new JLabel("Historial de Recolecciones de Residuos");
        lblTitulo.setForeground(COLOR_BLANCO);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        panelHeader.add(lblTitulo, BorderLayout.WEST);

        if (editable) {
            btnNuevaRecoleccion = crearBoton("+ Registrar Recolección", COLOR_VERDE, 185, 36);
            btnNuevaRecoleccion.addActionListener(e -> {
                new VentanaRegistrarRecoleccion(this);
                cargarRecolecciones(null, null, null);
            });
            panelHeader.add(btnNuevaRecoleccion, BorderLayout.EAST);
        }

        panelPrincipal.add(panelHeader, BorderLayout.NORTH);

        // Panel Central: Filtros y Tabla
        JPanel panelCard = crearTarjetaPanel(new BorderLayout(0, 12), new EmptyBorder(14, 16, 14, 16));

        // Filtros superiores
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelFiltros.setOpaque(false);

        panelFiltros.add(crearLabel("Desde (AAAA-MM-DD):"));
        txtFechaDesde = new JTextField(8);
        panelFiltros.add(txtFechaDesde);

        panelFiltros.add(crearLabel("Hasta:"));
        txtFechaHasta = new JTextField(8);
        panelFiltros.add(txtFechaHasta);

        panelFiltros.add(crearLabel("Tipo de Residuo:"));
        cmbFiltroTipo = new JComboBox<>();
        cmbFiltroTipo.setPreferredSize(new Dimension(160, 28));
        panelFiltros.add(cmbFiltroTipo);

        btnFiltrar = crearBoton("Filtrar", COLOR_VERDE, 85, 28);
        btnFiltrar.addActionListener(e -> aplicarFiltros());
        panelFiltros.add(btnFiltrar);

        btnTodos = crearBoton("Ver Todas", COLOR_AMARILLO.darker(), 95, 28);
        btnTodos.addActionListener(e -> {
            txtFechaDesde.setText("");
            txtFechaHasta.setText("");
            cmbFiltroTipo.setSelectedIndex(0);
            cargarRecolecciones(null, null, null);
        });
        panelFiltros.add(btnTodos);

        panelCard.add(panelFiltros, BorderLayout.NORTH);

        // Tabla
        String[] columnas = { "ID", "Contenedor", "Cód. Barras", "Tipo Residuo", "Operador", "Peso Recolectado (kg)", "Fecha y Hora", "Observaciones" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblRecolecciones = new JTable(modeloTabla);
        tblRecolecciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRecolecciones.setRowHeight(24);

        panelCard.add(new JScrollPane(tblRecolecciones), BorderLayout.CENTER);
        panelPrincipal.add(panelCard, BorderLayout.CENTER);

        setContentPane(panelPrincipal);
    }

    private void cargarComboTipos() {
        cmbFiltroTipo.removeAllItems();
        cmbFiltroTipo.addItem(new TipoResiduo(0, "-- Todos --", "", true));
        List<TipoResiduo> tipos = tipoDAO.listarTodos();
        for (TipoResiduo t : tipos) {
            cmbFiltroTipo.addItem(t);
        }
    }

    private void aplicarFiltros() {
        LocalDate desde = parsearFecha(txtFechaDesde.getText().trim());
        LocalDate hasta = parsearFecha(txtFechaHasta.getText().trim());
        TipoResiduo sel = (TipoResiduo) cmbFiltroTipo.getSelectedItem();
        Integer tipoId = (sel != null && sel.getId() > 0) ? sel.getId() : null;

        cargarRecolecciones(desde, hasta, tipoId);
    }

    private LocalDate parsearFecha(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception e) {
            Validaciones.mostrarError(this, "Formato de fecha inválido ('" + s + "'). Use AAAA-MM-DD (ej: 2026-05-15).");
            return null;
        }
    }

    private void cargarRecolecciones(LocalDate desde, LocalDate hasta, Integer tipoId) {
        SwingWorker<List<Recoleccion>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Recoleccion> doInBackground() {
                if (desde == null && hasta == null && tipoId == null) {
                    return recoleccionService.obtenerTodas();
                } else {
                    return recoleccionService.filtrar(desde, hasta, tipoId);
                }
            }

            @Override
            protected void done() {
                try {
                    List<Recoleccion> lista = get();
                    modeloTabla.setRowCount(0);
                    for (Recoleccion r : lista) {
                        modeloTabla.addRow(new Object[] {
                            r.getId(),
                            r.getContenedorNombre(),
                            r.getCodigoBarras(),
                            r.getTipoResiduo(),
                            r.getUsuarioNombre(),
                            Validaciones.formatearDecimal(r.getPesoRecolectado()),
                            Validaciones.formatearFecha(r.getFecha()),
                            r.getObservaciones() != null ? r.getObservaciones() : ""
                        });
                    }
                } catch (Exception ex) {
                    Validaciones.mostrarError(VentanaRecolecciones.this, "Error al cargar recolecciones: " + ex.getMessage());
                }
            }
        };
        worker.execute();
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
