package greentrack.ui;

import greentrack.model.Contenedor;
import greentrack.service.ContenedorService;
import greentrack.util.Validaciones;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Ventana para la búsqueda rápida de contenedores mediante código de barras (lector o teclado).
 * Permite a los operadores verificar la información de un contenedor y registrar recolecciones.
 */
public class VentanaBuscarContenedor extends JDialog {

    private static final Color COLOR_CAFE = new Color(74, 44, 23);
    private static final Color COLOR_AMARILLO = new Color(204, 153, 51);
    private static final Color COLOR_VERDE = new Color(46, 125, 50);
    private static final Color COLOR_BLANCO = Color.WHITE;

    private JTextField txtCodigoBarras;
    private JButton btnBuscar;

    private JPanel cardResultado;
    private JLabel lblNombre;
    private JLabel lblCodigo;
    private JLabel lblTipo;
    private JLabel lblUbicacion;
    private JLabel lblCapacidad;
    private JLabel lblPesoActual;
    private JLabel lblDisponible;
    private JLabel lblEstado;
    private JProgressBar barraLlenado;

    private JButton btnIrARecoleccion;

    private final ContenedorService contenedorService = new ContenedorService();
    private Contenedor contenedorEncontrado = null;

    public VentanaBuscarContenedor(Window owner) {
        super(owner, "Buscar Contenedor - GreenTrack", ModalityType.APPLICATION_MODAL);
        setSize(650, 560);
        setMinimumSize(new Dimension(560, 480));
        setLocationRelativeTo(owner);

        inicializarComponentes();
        setVisible(true);
    }

    public VentanaBuscarContenedor() {
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
        panelPrincipal.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel lblTitulo = new JLabel("Búsqueda de Contenedores por Código de Barras");
        lblTitulo.setForeground(COLOR_BLANCO);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new BorderLayout(0, 14));
        panelCentro.setOpaque(false);

        // Barra de búsqueda
        JPanel cardBuscador = crearTarjetaPanel(new BorderLayout(10, 8), new EmptyBorder(14, 16, 14, 16));
        JLabel lblHint = new JLabel("Pase el lector óptico sobre el código o escríbalo manualmente:");
        lblHint.setForeground(new Color(230, 230, 230));
        lblHint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cardBuscador.add(lblHint, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setOpaque(false);

        txtCodigoBarras = new JTextField();
        txtCodigoBarras.setFont(new Font("SansSerif", Font.BOLD, 16));
        txtCodigoBarras.setPreferredSize(new Dimension(0, 38));
        txtCodigoBarras.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ejecutarBusqueda();
                }
            }
        });
        inputPanel.add(txtCodigoBarras, BorderLayout.CENTER);

        btnBuscar = crearBoton("Buscar", COLOR_VERDE, 110, 38);
        btnBuscar.addActionListener(e -> ejecutarBusqueda());
        inputPanel.add(btnBuscar, BorderLayout.EAST);

        cardBuscador.add(inputPanel, BorderLayout.CENTER);
        panelCentro.add(cardBuscador, BorderLayout.NORTH);

        // Tarjeta de Resultados
        cardResultado = crearTarjetaPanel(new BorderLayout(0, 12), new EmptyBorder(16, 18, 16, 18));
        cardResultado.setVisible(false); // Se muestra al encontrar

        lblNombre = new JLabel("—");
        lblNombre.setForeground(COLOR_BLANCO);
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 18));
        cardResultado.add(lblNombre, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(4, 2, 12, 8));
        grid.setOpaque(false);

        lblCodigo = crearValor("—");
        lblTipo = crearValor("—");
        lblUbicacion = crearValor("—");
        lblCapacidad = crearValor("—");
        lblPesoActual = crearValor("—");
        lblDisponible = crearValor("—");
        lblEstado = crearValor("—");

        grid.add(crearFila("Código:", lblCodigo));
        grid.add(crearFila("Tipo de Residuo:", lblTipo));
        grid.add(crearFila("Ubicación:", lblUbicacion));
        grid.add(crearFila("Capacidad Máx:", lblCapacidad));
        grid.add(crearFila("Peso Actual:", lblPesoActual));
        grid.add(crearFila("Espacio Disponible:", lblDisponible));
        grid.add(crearFila("Estado:", lblEstado));

        JPanel progPanel = new JPanel(new BorderLayout(6, 0));
        progPanel.setOpaque(false);
        progPanel.add(crearLabel("Llenado:"), BorderLayout.WEST);
        barraLlenado = new JProgressBar(0, 100);
        barraLlenado.setStringPainted(true);
        progPanel.add(barraLlenado, BorderLayout.CENTER);
        grid.add(progPanel);

        cardResultado.add(grid, BorderLayout.CENTER);

        JPanel panelAccion = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        panelAccion.setOpaque(false);

        btnIrARecoleccion = crearBoton("Registrar Recolección en este Contenedor", COLOR_VERDE, 310, 38);
        btnIrARecoleccion.addActionListener(e -> {
            if (contenedorEncontrado != null) {
                new VentanaRegistrarRecoleccion(this, contenedorEncontrado.getCodigoBarras());
                ejecutarBusqueda(); // Refrescar datos
            }
        });
        panelAccion.add(btnIrARecoleccion);
        cardResultado.add(panelAccion, BorderLayout.SOUTH);

        panelCentro.add(cardResultado, BorderLayout.CENTER);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        setContentPane(panelPrincipal);
    }

    private void ejecutarBusqueda() {
        String codigo = txtCodigoBarras.getText().trim();
        if (Validaciones.esTextoVacio(codigo)) {
            Validaciones.mostrarError(this, "Por favor ingrese o escanee un código de barras.");
            return;
        }

        btnBuscar.setEnabled(false);
        btnBuscar.setText("Buscando...");

        SwingWorker<Contenedor, Void> worker = new SwingWorker<>() {
            @Override
            protected Contenedor doInBackground() {
                return contenedorService.buscarPorCodigoBarras(codigo);
            }

            @Override
            protected void done() {
                btnBuscar.setEnabled(true);
                btnBuscar.setText("Buscar");
                try {
                    contenedorEncontrado = get();
                    if (contenedorEncontrado != null) {
                        lblNombre.setText(contenedorEncontrado.getNombre());
                        lblCodigo.setText(contenedorEncontrado.getCodigoBarras());
                        lblTipo.setText(contenedorEncontrado.getTipoNombre());
                        lblUbicacion.setText(contenedorEncontrado.getUbicacionNombre());
                        lblCapacidad.setText(Validaciones.formatearDecimal(contenedorEncontrado.getCapacidadKg()) + " kg");
                        lblPesoActual.setText(Validaciones.formatearDecimal(contenedorEncontrado.getPesoActualKg()) + " kg");
                        lblDisponible.setText(Validaciones.formatearDecimal(contenedorEncontrado.getCapacidadDisponible()) + " kg");
                        lblEstado.setText(contenedorEncontrado.getEstado());

                        int pct = (int) Math.round(contenedorEncontrado.calcularPorcentajeLlenado());
                        barraLlenado.setValue(Math.min(100, pct));
                        barraLlenado.setString(pct + "%");

                        if (pct >= 90) barraLlenado.setForeground(new Color(200, 40, 40));
                        else if (pct >= 60) barraLlenado.setForeground(new Color(210, 140, 0));
                        else barraLlenado.setForeground(COLOR_VERDE);

                        cardResultado.setVisible(true);
                        cardResultado.revalidate();
                        cardResultado.repaint();
                    } else {
                        cardResultado.setVisible(false);
                        Validaciones.mostrarError(VentanaBuscarContenedor.this,
                                "Contenedor no encontrado con el código: " + codigo);
                    }
                } catch (Exception ex) {
                    Validaciones.mostrarError(VentanaBuscarContenedor.this, "Error al buscar contenedor: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JPanel crearFila(String etiqueta, JLabel valor) {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        f.setOpaque(false);
        f.add(crearLabel(etiqueta));
        f.add(valor);
        return f;
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(new Color(220, 220, 220));
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return lbl;
    }

    private JLabel crearValor(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(COLOR_BLANCO);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
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
