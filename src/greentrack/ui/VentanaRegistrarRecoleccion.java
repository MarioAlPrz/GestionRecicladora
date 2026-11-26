package greentrack.ui;

import greentrack.model.Contenedor;
import greentrack.service.ContenedorService;
import greentrack.service.RecoleccionService;
import greentrack.util.SesionUsuario;
import greentrack.util.Validaciones;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Ventana para registrar recolecciones mediante lector de código de barras físico o entrada manual.
 * Valida capacidad disponible y procesa la transacción de recolección en MySQL.
 */
public class VentanaRegistrarRecoleccion extends JDialog {

    private static final Color COLOR_CAFE = new Color(74, 44, 23);
    private static final Color COLOR_AMARILLO = new Color(204, 153, 51);
    private static final Color COLOR_VERDE = new Color(46, 125, 50);
    private static final Color COLOR_BLANCO = Color.WHITE;

    private JTextField txtCodigoBarras;
    private JButton btnBuscar;

    private JLabel lblNombreContenedor;
    private JLabel lblTipoResiduo;
    private JLabel lblUbicacion;
    private JLabel lblCapacidad;
    private JLabel lblPesoActual;
    private JLabel lblDisponible;
    private JLabel lblEstado;
    private JProgressBar barraLlenado;

    private JTextField txtPesoRecolectado;
    private JTextField txtObservaciones;
    private JButton btnRegistrar;
    private JButton btnCancelar;

    private final ContenedorService contenedorService = new ContenedorService();
    private final RecoleccionService recoleccionService = new RecoleccionService();

    private Contenedor contenedorActual = null;

    public VentanaRegistrarRecoleccion(Window owner) {
        this(owner, null);
    }

    public VentanaRegistrarRecoleccion(Window owner, String codigoBarrasInicial) {
        super(owner, "Registrar Recolección de Residuos", ModalityType.APPLICATION_MODAL);
        setSize(680, 620);
        setMinimumSize(new Dimension(580, 520));
        setLocationRelativeTo(owner);

        inicializarComponentes();

        if (codigoBarrasInicial != null && !codigoBarrasInicial.isEmpty()) {
            txtCodigoBarras.setText(codigoBarrasInicial);
            buscarContenedor();
        }

        setVisible(true);
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
        JLabel lblTitulo = new JLabel("Registro de Recolección de Residuos");
        lblTitulo.setForeground(COLOR_BLANCO);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // Panel Central
        JPanel panelCentro = new JPanel(new BorderLayout(0, 12));
        panelCentro.setOpaque(false);

        // 1. Sección Código de Barras (Escáner o Teclado)
        JPanel cardEscaner = crearTarjetaPanel(new BorderLayout(8, 6), new EmptyBorder(12, 14, 12, 14));
        JLabel lblHint = new JLabel("Escanee el código de barras o escríbalo y presione ENTER:");
        lblHint.setForeground(new Color(230, 230, 230));
        lblHint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cardEscaner.add(lblHint, BorderLayout.NORTH);

        JPanel inputScan = new JPanel(new BorderLayout(8, 0));
        inputScan.setOpaque(false);

        txtCodigoBarras = new JTextField();
        txtCodigoBarras.setFont(new Font("SansSerif", Font.BOLD, 15));
        txtCodigoBarras.setPreferredSize(new Dimension(0, 36));
        txtCodigoBarras.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    buscarContenedor();
                }
            }
        });
        inputScan.add(txtCodigoBarras, BorderLayout.CENTER);

        btnBuscar = crearBoton("Buscar", COLOR_VERDE, 100, 36);
        btnBuscar.addActionListener(e -> buscarContenedor());
        inputScan.add(btnBuscar, BorderLayout.EAST);

        cardEscaner.add(inputScan, BorderLayout.CENTER);
        panelCentro.add(cardEscaner, BorderLayout.NORTH);

        // 2. Información del Contenedor Identificado
        JPanel cardInfo = crearTarjetaPanel(new BorderLayout(0, 8), new EmptyBorder(14, 16, 14, 16));
        JLabel lblInfoTitle = new JLabel("Datos del Contenedor Identificado");
        lblInfoTitle.setForeground(COLOR_BLANCO);
        lblInfoTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        cardInfo.add(lblInfoTitle, BorderLayout.NORTH);

        JPanel gridInfo = new JPanel(new GridLayout(4, 2, 10, 6));
        gridInfo.setOpaque(false);

        lblNombreContenedor = crearValor("—");
        lblTipoResiduo = crearValor("—");
        lblUbicacion = crearValor("—");
        lblCapacidad = crearValor("—");
        lblPesoActual = crearValor("—");
        lblDisponible = crearValor("—");
        lblEstado = crearValor("—");

        gridInfo.add(crearFila("Nombre:", lblNombreContenedor));
        gridInfo.add(crearFila("Tipo de Residuo:", lblTipoResiduo));
        gridInfo.add(crearFila("Ubicación:", lblUbicacion));
        gridInfo.add(crearFila("Capacidad Máx:", lblCapacidad));
        gridInfo.add(crearFila("Peso Actual:", lblPesoActual));
        gridInfo.add(crearFila("Capacidad Disponible:", lblDisponible));
        gridInfo.add(crearFila("Estado:", lblEstado));

        JPanel panelProgress = new JPanel(new BorderLayout(6, 0));
        panelProgress.setOpaque(false);
        panelProgress.add(crearLabel("Nivel de Llenado:"), BorderLayout.WEST);
        barraLlenado = new JProgressBar(0, 100);
        barraLlenado.setStringPainted(true);
        barraLlenado.setString("0%");
        panelProgress.add(barraLlenado, BorderLayout.CENTER);
        gridInfo.add(panelProgress);

        cardInfo.add(gridInfo, BorderLayout.CENTER);
        panelCentro.add(cardInfo, BorderLayout.CENTER);

        // 3. Formulario de Recolección
        JPanel cardForm = crearTarjetaPanel(new GridLayout(2, 2, 12, 8), new EmptyBorder(12, 16, 12, 16));

        JPanel f1 = new JPanel(new BorderLayout(0, 4)); f1.setOpaque(false);
        f1.add(crearLabel("Peso a Recolectar (kg) *:"), BorderLayout.NORTH);
        txtPesoRecolectado = new JTextField();
        txtPesoRecolectado.setFont(new Font("SansSerif", Font.BOLD, 14));
        f1.add(txtPesoRecolectado, BorderLayout.CENTER);
        cardForm.add(f1);

        JPanel f2 = new JPanel(new BorderLayout(0, 4)); f2.setOpaque(false);
        f2.add(crearLabel("Observaciones:"), BorderLayout.NORTH);
        txtObservaciones = new JTextField();
        f2.add(txtObservaciones, BorderLayout.CENTER);
        cardForm.add(f2);

        panelCentro.add(cardForm, BorderLayout.SOUTH);

        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        // Botones Inferiores
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panelBotones.setOpaque(false);

        btnRegistrar = crearBoton("Confirmar Recolección", COLOR_VERDE, 180, 38);
        btnRegistrar.setEnabled(false);
        btnRegistrar.addActionListener(e -> ejecutarRecoleccion());

        btnCancelar = crearBoton("Cerrar", new Color(130, 130, 130), 100, 38);
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnCancelar);

        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        setContentPane(panelPrincipal);
    }

    private void buscarContenedor() {
        String codigo = txtCodigoBarras.getText().trim();
        if (Validaciones.esTextoVacio(codigo)) {
            Validaciones.mostrarError(this, "Ingrese o escanee un código de barras.");
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
                    contenedorActual = get();
                    if (contenedorActual != null) {
                        lblNombreContenedor.setText(contenedorActual.getNombre());
                        lblTipoResiduo.setText(contenedorActual.getTipoNombre());
                        lblUbicacion.setText(contenedorActual.getUbicacionNombre());
                        lblCapacidad.setText(Validaciones.formatearDecimal(contenedorActual.getCapacidadKg()) + " kg");
                        lblPesoActual.setText(Validaciones.formatearDecimal(contenedorActual.getPesoActualKg()) + " kg");

                        double disponible = contenedorActual.getCapacidadDisponible();
                        lblDisponible.setText(Validaciones.formatearDecimal(disponible) + " kg");
                        lblEstado.setText(contenedorActual.getEstado());

                        int pct = (int) Math.round(contenedorActual.calcularPorcentajeLlenado());
                        barraLlenado.setValue(Math.min(100, pct));
                        barraLlenado.setString(pct + "%");

                        if (pct >= 90) {
                            barraLlenado.setForeground(new Color(200, 40, 40));
                        } else if (pct >= 60) {
                            barraLlenado.setForeground(new Color(210, 140, 0));
                        } else {
                            barraLlenado.setForeground(COLOR_VERDE);
                        }

                        if (!"ACTIVO".equalsIgnoreCase(contenedorActual.getEstado())) {
                            Validaciones.mostrarError(VentanaRegistrarRecoleccion.this,
                                    "El contenedor seleccionado se encuentra en estado: " + contenedorActual.getEstado() + ".\n" +
                                    "Solo se pueden registrar recolecciones en contenedores ACTIVOS.");
                            btnRegistrar.setEnabled(false);
                        } else if (disponible <= 0) {
                            Validaciones.mostrarError(VentanaRegistrarRecoleccion.this,
                                    "El contenedor ya alcanzó o superó su capacidad máxima de carga.");
                            btnRegistrar.setEnabled(false);
                        } else {
                            btnRegistrar.setEnabled(true);
                            txtPesoRecolectado.requestFocus();
                        }
                    } else {
                        limpiarDatosContenedor();
                        Validaciones.mostrarError(VentanaRegistrarRecoleccion.this,
                                "No se encontró ningún contenedor con el código de barras '" + codigo + "'.\n" +
                                "Verifique que el código esté correctamente registrado en el sistema.");
                    }
                } catch (Exception ex) {
                    Validaciones.mostrarError(VentanaRegistrarRecoleccion.this, "Error al buscar: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void ejecutarRecoleccion() {
        if (contenedorActual == null) {
            Validaciones.mostrarError(this, "Primero debe buscar un contenedor válido.");
            return;
        }

        String pesoStr = txtPesoRecolectado.getText().trim();
        if (!Validaciones.esDecimalPositivo(pesoStr)) {
            Validaciones.mostrarError(this, "El peso a recolectar debe ser un número decimal mayor a 0 kg.");
            return;
        }

        double peso = Double.parseDouble(pesoStr);
        String obs = txtObservaciones.getText().trim();

        btnRegistrar.setEnabled(false);
        btnRegistrar.setText("Procesando...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return recoleccionService.registrarRecoleccion(
                        contenedorActual.getCodigoBarras(),
                        peso,
                        obs
                );
            }

            @Override
            protected void done() {
                btnRegistrar.setEnabled(true);
                btnRegistrar.setText("Confirmar Recolección");
                try {
                    String respuesta = get();
                    if (respuesta != null && respuesta.startsWith("OK")) {
                        Validaciones.mostrarExito(VentanaRegistrarRecoleccion.this,
                                "¡Recolección registrada exitosamente!\n" +
                                "Contenedor: " + contenedorActual.getNombre() + "\n" +
                                "Peso recolectado: " + Validaciones.formatearDecimal(peso) + " kg\n" +
                                "Operador: " + SesionUsuario.getNombreCompleto());

                        // Refrescar datos del contenedor
                        txtCodigoBarras.setText(contenedorActual.getCodigoBarras());
                        txtPesoRecolectado.setText("");
                        txtObservaciones.setText("");
                        buscarContenedor();
                    } else {
                        Validaciones.mostrarError(VentanaRegistrarRecoleccion.this, respuesta != null ? respuesta : "Error desconocido.");
                    }
                } catch (Exception ex) {
                    Validaciones.mostrarError(VentanaRegistrarRecoleccion.this, "Error en la transacción: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void limpiarDatosContenedor() {
        contenedorActual = null;
        lblNombreContenedor.setText("—");
        lblTipoResiduo.setText("—");
        lblUbicacion.setText("—");
        lblCapacidad.setText("—");
        lblPesoActual.setText("—");
        lblDisponible.setText("—");
        lblEstado.setText("—");
        barraLlenado.setValue(0);
        barraLlenado.setString("0%");
        btnRegistrar.setEnabled(false);
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
