package greentrack.ui;

import greentrack.dao.AuditoriaDAO;
import greentrack.dao.TipoResiduoDAO;
import greentrack.dao.UbicacionDAO;
import greentrack.model.Auditoria;
import greentrack.model.TipoResiduo;
import greentrack.model.Ubicacion;
import greentrack.service.ReporteService;
import greentrack.util.Validaciones;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana de Reportes y Estadísticas del Sistema GreenTrack.
 * Presenta 3 pestañas principales:
 * 1. Recolecciones por Tipo de Residuo (JOIN 3 tablas, GROUP BY, SUM/AVG)
 * 2. Estado de Contenedores (% llenado con fn_porcentaje_llenado)
 * 3. Bitácora de Auditoría del Sistema (triggers MySQL)
 */
public class VentanaReportes extends JDialog {

    private static final Color COLOR_CAFE = new Color(74, 44, 23);
    private static final Color COLOR_AMARILLO = new Color(204, 153, 51);
    private static final Color COLOR_VERDE = new Color(46, 125, 50);
    private static final Color COLOR_BLANCO = Color.WHITE;

    private JTabbedPane tabbedPane;

    // Pestaña 1
    private JTextField txtFechaDesdeR1;
    private JTextField txtFechaHastaR1;
    private JTable tblReporte1;
    private DefaultTableModel modeloR1;

    // Pestaña 2
    private JComboBox<TipoResiduo> cmbR2Tipo;
    private JComboBox<Ubicacion> cmbR2Ubicacion;
    private JTable tblReporte2;
    private DefaultTableModel modeloR2;

    // Pestaña 3: Auditoría
    private JTextField txtFechaDesdeAud;
    private JTextField txtFechaHastaAud;
    private JComboBox<String> cmbTablaAud;
    private JTable tblAuditoria;
    private DefaultTableModel modeloAuditoria;

    private final ReporteService reporteService = new ReporteService();
    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();
    private final TipoResiduoDAO tipoDAO = new TipoResiduoDAO();
    private final UbicacionDAO ubicacionDAO = new UbicacionDAO();

    public VentanaReportes(Window owner) {
        super(owner, "Reportes, Estadísticas y Auditoría - GreenTrack", ModalityType.APPLICATION_MODAL);
        setSize(1060, 680);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(owner);

        inicializarComponentes();
        cargarReporte1();
        cargarReporte2();
        cargarAuditoria();
        setVisible(true);
    }

    public VentanaReportes() {
        this(null);
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

        JLabel lblTitulo = new JLabel("Módulo de Reportes, Estadísticas y Auditoría");
        lblTitulo.setForeground(COLOR_BLANCO);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabbedPane.addTab("Recolecciones por Tipo", crearPanelReporte1());
        tabbedPane.addTab("Estado de Contenedores", crearPanelReporte2());
        tabbedPane.addTab("Bitácora de Auditoría", crearPanelAuditoria());

        panelPrincipal.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(panelPrincipal);
    }

    // ─── Pestaña 1: Recolecciones por Tipo ──────────────────────────────
    private JPanel crearPanelReporte1() {
        JPanel p = crearTarjetaPanel(new BorderLayout(0, 12), new EmptyBorder(14, 16, 14, 16));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtros.setOpaque(false);

        filtros.add(crearLabel("Fecha Desde (AAAA-MM-DD):"));
        txtFechaDesdeR1 = new JTextField(8);
        filtros.add(txtFechaDesdeR1);

        filtros.add(crearLabel("Hasta:"));
        txtFechaHastaR1 = new JTextField(8);
        filtros.add(txtFechaHastaR1);

        JButton btnGen1 = crearBoton("Generar Reporte", COLOR_VERDE, 130, 28);
        btnGen1.addActionListener(e -> cargarReporte1());
        filtros.add(btnGen1);

        JButton btnLimpiar1 = crearBoton("Histórico Completo", COLOR_AMARILLO.darker(), 145, 28);
        btnLimpiar1.addActionListener(e -> {
            txtFechaDesdeR1.setText("");
            txtFechaHastaR1.setText("");
            cargarReporte1();
        });
        filtros.add(btnLimpiar1);

        p.add(filtros, BorderLayout.NORTH);

        String[] cols = { "Tipo de Residuo", "Núm. Recolecciones", "Peso Total (kg)", "Peso Promedio (kg)" };
        modeloR1 = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblReporte1 = new JTable(modeloR1);
        tblReporte1.setRowHeight(25);
        p.add(new JScrollPane(tblReporte1), BorderLayout.CENTER);

        return p;
    }

    private void cargarReporte1() {
        LocalDate desde = parsearFecha(txtFechaDesdeR1.getText().trim());
        LocalDate hasta = parsearFecha(txtFechaHastaR1.getText().trim());

        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                return reporteService.generarReporteRecoleccionesPorTipo(desde, hasta);
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> lista = get();
                    modeloR1.setRowCount(0);
                    double granTotalKg = 0;
                    int granTotalRec = 0;

                    for (Map<String, Object> fila : lista) {
                        int numRec = (int) fila.get("num_recolecciones");
                        double pesoTot = (double) fila.get("peso_total");
                        double pesoProm = (double) fila.get("peso_promedio");

                        granTotalRec += numRec;
                        granTotalKg += pesoTot;

                        modeloR1.addRow(new Object[] {
                            fila.get("tipo_residuo"),
                            numRec,
                            Validaciones.formatearDecimal(pesoTot),
                            Validaciones.formatearDecimal(pesoProm)
                        });
                    }

                    // Fila de resumen total
                    if (!lista.isEmpty()) {
                        double promGlobal = granTotalRec > 0 ? (granTotalKg / granTotalRec) : 0;
                        modeloR1.addRow(new Object[] {
                            "TOTAL GENERAL",
                            granTotalRec,
                            Validaciones.formatearDecimal(granTotalKg),
                            Validaciones.formatearDecimal(promGlobal)
                        });
                    }
                } catch (Exception ex) {
                    Validaciones.mostrarError(VentanaReportes.this, "Error en reporte 1: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ─── Pestaña 2: Estado de Contenedores ──────────────────────────────
    private JPanel crearPanelReporte2() {
        JPanel p = crearTarjetaPanel(new BorderLayout(0, 12), new EmptyBorder(14, 16, 14, 16));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtros.setOpaque(false);

        filtros.add(crearLabel("Tipo de Residuo:"));
        cmbR2Tipo = new JComboBox<>();
        cmbR2Tipo.addItem(new TipoResiduo(0, "-- Todos --", "", true));
        for (TipoResiduo t : tipoDAO.listarTodos()) cmbR2Tipo.addItem(t);
        filtros.add(cmbR2Tipo);

        filtros.add(crearLabel("Ubicación:"));
        cmbR2Ubicacion = new JComboBox<>();
        cmbR2Ubicacion.addItem(new Ubicacion(0, "-- Todas --", "", "", true));
        for (Ubicacion u : ubicacionDAO.listarTodas()) cmbR2Ubicacion.addItem(u);
        filtros.add(cmbR2Ubicacion);

        JButton btnGen2 = crearBoton("Consultar Estado", COLOR_VERDE, 130, 28);
        btnGen2.addActionListener(e -> cargarReporte2());
        filtros.add(btnGen2);

        p.add(filtros, BorderLayout.NORTH);

        String[] cols = { "Contenedor", "Cód. Barras", "Tipo Residuo", "Ubicación", "Capacidad (kg)", "Peso Actual (kg)", "% Llenado", "Estado" };
        modeloR2 = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblReporte2 = new JTable(modeloR2);
        tblReporte2.setRowHeight(25);

        tblReporte2.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (value != null) {
                    try {
                        double pct = Double.parseDouble(value.toString().replace("%", "").trim());
                        if (pct >= 85) c.setForeground(new Color(220, 40, 40));
                        else if (pct >= 60) c.setForeground(new Color(210, 140, 0));
                        else c.setForeground(new Color(30, 130, 40));
                    } catch (Exception ignored) {}
                }
                return c;
            }
        });

        p.add(new JScrollPane(tblReporte2), BorderLayout.CENTER);
        return p;
    }

    private void cargarReporte2() {
        TipoResiduo t = (TipoResiduo) cmbR2Tipo.getSelectedItem();
        Ubicacion u = (Ubicacion) cmbR2Ubicacion.getSelectedItem();

        Integer tid = (t != null && t.getId() > 0) ? t.getId() : null;
        Integer uid = (u != null && u.getId() > 0) ? u.getId() : null;

        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                return reporteService.generarReporteEstadoContenedores(tid, uid);
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> lista = get();
                    modeloR2.setRowCount(0);
                    for (Map<String, Object> fila : lista) {
                        modeloR2.addRow(new Object[] {
                            fila.get("contenedor_nombre"),
                            fila.get("codigo_barras"),
                            fila.get("tipo_residuo"),
                            fila.get("ubicacion"),
                            Validaciones.formatearDecimal((double) fila.get("capacidad_kg")),
                            Validaciones.formatearDecimal((double) fila.get("peso_actual_kg")),
                            String.format("%.1f%%", (double) fila.get("porcentaje_llenado")),
                            fila.get("estado")
                        });
                    }
                } catch (Exception ex) {
                    Validaciones.mostrarError(VentanaReportes.this, "Error en reporte 2: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ─── Pestaña 3: Auditoría ──────────────────────────────────────────
    private JPanel crearPanelAuditoria() {
        JPanel p = crearTarjetaPanel(new BorderLayout(0, 12), new EmptyBorder(14, 16, 14, 16));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtros.setOpaque(false);

        filtros.add(crearLabel("Desde:"));
        txtFechaDesdeAud = new JTextField(8);
        filtros.add(txtFechaDesdeAud);

        filtros.add(crearLabel("Hasta:"));
        txtFechaHastaAud = new JTextField(8);
        filtros.add(txtFechaHastaAud);

        filtros.add(crearLabel("Tabla Afectada:"));
        cmbTablaAud = new JComboBox<>(new String[] { "TODAS", "recolecciones", "contenedores" });
        filtros.add(cmbTablaAud);

        JButton btnAud = crearBoton("Consultar Auditoría", COLOR_VERDE, 145, 28);
        btnAud.addActionListener(e -> cargarAuditoria());
        filtros.add(btnAud);

        p.add(filtros, BorderLayout.NORTH);

        String[] cols = { "ID", "Usuario Responsable", "Tabla", "Operación", "Fecha y Hora", "Datos Anteriores", "Datos Nuevos" };
        modeloAuditoria = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblAuditoria = new JTable(modeloAuditoria);
        tblAuditoria.setRowHeight(24);
        p.add(new JScrollPane(tblAuditoria), BorderLayout.CENTER);

        return p;
    }

    private void cargarAuditoria() {
        LocalDate desde = parsearFecha(txtFechaDesdeAud.getText().trim());
        LocalDate hasta = parsearFecha(txtFechaHastaAud.getText().trim());
        String tabla = (String) cmbTablaAud.getSelectedItem();

        SwingWorker<List<Auditoria>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Auditoria> doInBackground() {
                return auditoriaDAO.filtrar(desde, hasta, tabla);
            }

            @Override
            protected void done() {
                try {
                    List<Auditoria> lista = get();
                    modeloAuditoria.setRowCount(0);
                    for (Auditoria a : lista) {
                        modeloAuditoria.addRow(new Object[] {
                            a.getId(),
                            a.getUsuarioNombre(),
                            a.getTablaAfectada(),
                            a.getTipoOperacion(),
                            Validaciones.formatearFecha(a.getFecha()),
                            a.getDatosAnteriores() != null ? a.getDatosAnteriores() : "—",
                            a.getDatosNuevos() != null ? a.getDatosNuevos() : "—"
                        });
                    }
                } catch (Exception ex) {
                    Validaciones.mostrarError(VentanaReportes.this, "Error en auditoría: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private LocalDate parsearFecha(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception e) {
            return null;
        }
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
