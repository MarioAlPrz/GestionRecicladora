package GestionRecicladora;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class front_principal extends JFrame {
    private static final Color COLOR_CAFE = new Color(74, 44, 23);
    private static final Color COLOR_AMARILLO = new Color(204, 153, 51);
    private static final Color COLOR_VERDE = new Color(46, 125, 50);
    private static final Color COLOR_BLANCO = Color.WHITE;

    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    public static final String ROL_CONSULTA = "CONSULTA";
    public static final String ROL_OPERADOR = "OPERADOR";

    private static final String CARD_ADMIN_CONSULTA = "ADMIN_CONSULTA";
    private static final String CARD_OPERADOR = "OPERADOR";

    private static ClassLoader cargadorVentanas;

    private String rolActual;
    private String usuarioActual;

    private JPanel panelGeneral;
    private JPanel panelContenido;
    private CardLayout cardLayout;

    private JLabel lblTituloModulos;
    private JPanel panelBotonesModulos;

    public front_principal() {
        this(ROL_ADMINISTRADOR, "Usuario");
    }

    public front_principal(String rol) {
        this(rol, "Usuario");
    }

    public front_principal(String rol, String usuario) {
        this.rolActual = normalizarRol(rol);
        this.usuarioActual = (usuario == null || usuario.trim().isEmpty()) ? "Usuario" : usuario.trim();

        setTitle("Sistema de Reciclaje");
        setSize(1000, 680);
        setMinimumSize(new Dimension(880, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
        actualizarVistaPorRol();
        setVisible(true);
    }

    private String normalizarRol(String rol) {
        if (rol == null) {
            return ROL_ADMINISTRADOR;
        }
        String r = rol.trim().toUpperCase();
        if (ROL_OPERADOR.equals(r) || ROL_CONSULTA.equals(r) || ROL_ADMINISTRADOR.equals(r)) {
            return r;
        }
        return ROL_ADMINISTRADOR;
    }

    private void inicializarComponentes() {
        panelGeneral = crearPanelDegradado(new BorderLayout(16, 16), new EmptyBorder(16, 20, 20, 20));
        panelGeneral.add(crearEncabezado(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setOpaque(false);

        panelContenido.add(crearVistaAdminConsulta(), CARD_ADMIN_CONSULTA);
        panelContenido.add(crearVistaOperador(), CARD_OPERADOR);

        panelGeneral.add(panelContenido, BorderLayout.CENTER);
        setContentPane(panelGeneral);
    }

    private JPanel crearEncabezado() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(4, 6, 8, 6));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        izquierda.setOpaque(false);

        JLabel lblLogo = crearLogoEscalado(42, 42);
        if (lblLogo != null) {
            izquierda.add(lblLogo);
        }

        JLabel lblTitulo = crearEtiqueta("Sistema de Reciclaje", COLOR_BLANCO, 20, Font.BOLD);
        izquierda.add(lblTitulo);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 4));
        derecha.setOpaque(false);

        JLabel lblUsuario = crearEtiqueta("Hola, " + usuarioActual + " (" + rolActual + ")", COLOR_BLANCO, 13,
                Font.PLAIN);
        derecha.add(lblUsuario);

        JButton btnCerrarSesion = crearBotonRedondeado("Cerrar Sesión", COLOR_VERDE, 128, 32);
        btnCerrarSesion
                .addActionListener(e -> abrirVentana("VentanaCerrarSesion", new Class<?>[] { JFrame.class }, this));
        derecha.add(btnCerrarSesion);

        header.add(izquierda, BorderLayout.WEST);
        header.add(derecha, BorderLayout.EAST);
        return header;
    }

    private JPanel crearVistaAdminConsulta() {
        JPanel vista = new JPanel(new BorderLayout(0, 16));
        vista.setOpaque(false);

        JPanel panelKpis = new JPanel(new GridLayout(1, 4, 14, 0));
        panelKpis.setOpaque(false);
        panelKpis.setPreferredSize(new Dimension(0, 100));

        panelKpis.add(crearTarjetaKpi("Total kg recolectados", "1,480.5 kg"));
        panelKpis.add(crearTarjetaKpi("Contenedores activos", "24 / 26"));
        panelKpis.add(crearTarjetaKpi("Próximos a llenarse", "5"));
        panelKpis.add(crearTarjetaKpi("Recolecciones hoy", "12"));

        vista.add(panelKpis, BorderLayout.NORTH);

        JPanel panelInferior = new JPanel(new GridBagLayout());
        panelInferior.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1.0;

        JPanel cardModulos = crearTarjetaPanel(new BorderLayout(0, 12), new EmptyBorder(16, 18, 16, 18));
        lblTituloModulos = crearEtiqueta("Accesos rápidos", COLOR_BLANCO, 16, Font.BOLD);
        cardModulos.add(lblTituloModulos, BorderLayout.NORTH);

        panelBotonesModulos = new JPanel(new GridLayout(0, 2, 12, 12));
        panelBotonesModulos.setOpaque(false);
        cardModulos.add(panelBotonesModulos, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0.43;
        gbc.insets = new Insets(0, 0, 0, 14);
        panelInferior.add(cardModulos, gbc);

        JPanel cardGrafico = crearTarjetaPanel(new BorderLayout(0, 10), new EmptyBorder(16, 18, 16, 18));
        JLabel lblTituloGrafico = crearEtiqueta("Recolección por tipo de residuo", COLOR_BLANCO, 16, Font.BOLD);

        cardGrafico.add(lblTituloGrafico, BorderLayout.NORTH);
        cardGrafico.add(new GraficoResiduosPanel(), BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 0.57;
        gbc.insets = new Insets(0, 0, 0, 0);
        panelInferior.add(cardGrafico, gbc);

        vista.add(panelInferior, BorderLayout.CENTER);
        return vista;
    }

    private JPanel crearVistaOperador() {
        JPanel vista = new JPanel(new GridLayout(1, 2, 24, 0));
        vista.setOpaque(false);

        vista.add(crearTarjetaAccionOperador("Registrar recolección", true));
        vista.add(crearTarjetaAccionOperador("Buscar contenedor", false));

        return vista;
    }

    private JPanel crearTarjetaAccionOperador(String titulo, boolean esRegistro) {
        JPanel tarjeta = crearTarjetaPanel(new BorderLayout(0, 16), new EmptyBorder(40, 28, 40, 28));

        JPanel icono = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 72;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                g2.setColor(COLOR_VERDE);
                g2.fillOval(x, y, size, size);

                g2.setColor(COLOR_BLANCO);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                if (esRegistro) {
                    g2.drawLine(cx - 14, cy, cx + 14, cy);
                    g2.drawLine(cx, cy - 14, cx, cy + 14);
                } else {
                    g2.drawOval(cx - 14, cy - 14, 22, 22);
                    g2.drawLine(cx + 5, cy + 5, cx + 15, cy + 15);
                }
                g2.dispose();
            }
        };
        icono.setOpaque(false);
        icono.setPreferredSize(new Dimension(0, 110));

        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelBtn.setOpaque(false);
        JButton btnAccion = crearBotonRedondeado(titulo, COLOR_VERDE, 230, 44);
        btnAccion.addActionListener(e -> abrirModulo(titulo));
        panelBtn.add(btnAccion);

        tarjeta.add(icono, BorderLayout.CENTER);
        tarjeta.add(panelBtn, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearTarjetaKpi(String titulo, String valor) {
        JPanel tarjeta = crearTarjetaPanel(new BorderLayout(0, 8), new EmptyBorder(14, 16, 14, 16));

        JLabel lblTitulo = crearEtiqueta(titulo, new Color(235, 235, 235), 13, Font.PLAIN);
        JLabel lblValor = crearEtiqueta(valor, COLOR_BLANCO, 22, Font.BOLD);

        tarjeta.add(lblTitulo, BorderLayout.NORTH);
        tarjeta.add(lblValor, BorderLayout.CENTER);
        return tarjeta;
    }

    private void actualizarVistaPorRol() {
        if (ROL_OPERADOR.equals(rolActual)) {
            cardLayout.show(panelContenido, CARD_OPERADOR);
        } else {
            reconstruirAccesosRapidos();
            cardLayout.show(panelContenido, CARD_ADMIN_CONSULTA);
        }
    }

    private void reconstruirAccesosRapidos() {
        panelBotonesModulos.removeAll();

        String[] modulos;
        if (ROL_CONSULTA.equals(rolActual)) {
            lblTituloModulos.setText("Accesos rápidos (Consulta)");
            modulos = new String[] {
                    "Consultar Contenedores",
                    "Historial Recolecciones",
                    "Reportes y Estadísticas",
                    "Estado de Rutas"
            };
        } else {
            lblTituloModulos.setText("Accesos rápidos");
            modulos = new String[] {
                    "Contenedores",
                    "Recolecciones",
                    "Tipos de Residuo",
                    "Rutas y Operadores",
                    "Reportes",
                    "Usuarios y Roles"
            };
        }

        for (String nombreModulo : modulos) {
            JButton btn = crearBotonRedondeado(nombreModulo, COLOR_VERDE, 160, 44);
            btn.addActionListener(e -> abrirModulo(nombreModulo));
            panelBotonesModulos.add(btn);
        }

        panelBotonesModulos.revalidate();
        panelBotonesModulos.repaint();
    }

    private void abrirModulo(String nombreModulo) {
        switch (nombreModulo) {
            case "Contenedores":
                abrirVentana("VentanaContenedores", new Class<?>[] { Window.class, boolean.class }, this, true);
                break;
            case "Consultar Contenedores":
                abrirVentana("VentanaContenedores", new Class<?>[] { Window.class, boolean.class }, this, false);
                break;
            case "Recolecciones":
                abrirVentana("VentanaRecolecciones", new Class<?>[] { Window.class, boolean.class }, this, true);
                break;
            case "Historial Recolecciones":
                abrirVentana("VentanaRecolecciones", new Class<?>[] { Window.class, boolean.class }, this, false);
                break;
            case "Tipos de Residuo":
                abrirVentana("VentanaTiposResiduo", new Class<?>[] { Window.class }, this);
                break;
            case "Rutas y Operadores":
                abrirVentana("VentanaRutas", new Class<?>[] { Window.class, boolean.class }, this, true);
                break;
            case "Estado de Rutas":
                abrirVentana("VentanaRutas", new Class<?>[] { Window.class, boolean.class }, this, false);
                break;
            case "Reportes":
            case "Reportes y Estadísticas":
                abrirVentana("VentanaReportes", new Class<?>[] { Window.class }, this);
                break;
            case "Usuarios y Roles":
                abrirVentana("VentanaUsuarios", new Class<?>[] { Window.class }, this);
                break;
            case "Registrar recolección":
                abrirVentana("VentanaRegistrarRecoleccion", new Class<?>[] { Window.class }, this);
                break;
            case "Buscar contenedor":
                abrirVentana("VentanaBuscarContenedor", new Class<?>[] { Window.class }, this);
                break;
        }
    }

    private void abrirVentana(String nombreClase, Class<?>[] tipos, Object... args) {
        try {
            Class<?> clazz = cargarClaseVentana(nombreClase);
            clazz.getDeclaredConstructor(tipos).newInstance(args);
        } catch (Exception ex) {
            ex.printStackTrace(); // Imprime el rastro completo en consola
            Throwable causa = (ex.getCause() != null) ? ex.getCause() : ex;
            JOptionPane.showMessageDialog(
                    this,
                    "Error al abrir " + nombreClase + ":\n" + causa.getClass().getSimpleName() + ": "
                            + causa.getMessage(),
                    "Error de ejecución",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Class<?> cargarClaseVentana(String nombreClase) throws Exception {
        try {
            return Class.forName(nombreClase);
        } catch (ClassNotFoundException ignored) {
        }

        if (cargadorVentanas == null) {
            File carpeta = new File("GestionRecicladora").isDirectory()
                    ? new File("GestionRecicladora")
                    : new File(".");
            File salida = Files.createTempDirectory("recicladora_ui").toFile();
            salida.deleteOnExit();

            JavaCompiler compilador = ToolProvider.getSystemJavaCompiler();
            if (compilador == null) {
                throw new IllegalStateException(
                        "No se encontró el compilador (JDK). Ejecuta el proyecto usando un JDK, no solo JRE.");
            }

            File[] archivos = carpeta
                    .listFiles((dir, name) -> name.endsWith(".java") && !"front_principal.java".equals(name));
            if (archivos == null || archivos.length == 0) {
                throw new IllegalStateException("No se encontraron archivos .java en: " + carpeta.getAbsolutePath());
            }

            List<String> argumentos = new ArrayList<>();
            argumentos.add("-encoding");
            argumentos.add("UTF-8");
            argumentos.add("-cp");
            argumentos.add(System.getProperty("java.class.path"));
            argumentos.add("-d");
            argumentos.add(salida.getAbsolutePath());
            for (File archivo : archivos) {
                argumentos.add(archivo.getAbsolutePath());
            }

            ByteArrayOutputStream errStream = new ByteArrayOutputStream();
            int resultado = compilador.run(null, null, errStream, argumentos.toArray(new String[0]));
            if (resultado != 0) {
                String erroresCompilacion = errStream.toString(StandardCharsets.UTF_8);
                throw new RuntimeException("Error de compilación en las ventanas:\n" + erroresCompilacion);
            }

            cargadorVentanas = new URLClassLoader(new URL[] { salida.toURI().toURL() },
                    front_principal.class.getClassLoader());
        }
        return Class.forName(nombreClase, true, cargadorVentanas);
    }

    private JPanel crearPanelDegradado(LayoutManager layout, EmptyBorder padding) {
        JPanel panel = new JPanel(layout) {
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
        panel.setBorder(padding);
        return panel;
    }

    private JPanel crearTarjetaPanel(LayoutManager layout, EmptyBorder padding) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CAFE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(padding);
        return panel;
    }

    private JButton crearBotonRedondeado(String texto, Color colorFondo, int ancho, int alto) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(colorFondo.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(colorFondo.brighter());
                } else {
                    g2.setColor(colorFondo);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setForeground(COLOR_BLANCO);
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setPreferredSize(new Dimension(ancho, alto));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private JLabel crearEtiqueta(String texto, Color color, int tamano, int estilo) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(color);
        lbl.setFont(new Font("SansSerif", estilo, tamano));
        return lbl;
    }

    private JLabel crearLogoEscalado(int ancho, int alto) {
        String[] rutasPosibles = { "loginimage.png", "GestionRecicladora/loginimage.png" };
        for (String ruta : rutasPosibles) {
            File archivo = new File(ruta);
            if (archivo.exists()) {
                try {
                    BufferedImage original = ImageIO.read(archivo);
                    if (original != null) {
                        Image escalada = original.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                        return new JLabel(new ImageIcon(escalada));
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    private static class GraficoResiduosPanel extends JPanel {
        private final String[] tipos = { "Plástico", "Papel/Cartón", "Vidrio", "Metal", "Orgánico" };
        private final double[] valoresKg = { 485.0, 410.0, 275.0, 190.0, 120.5 };
        private final Color[] coloresBarras = {
                COLOR_VERDE,
                COLOR_AMARILLO,
                COLOR_VERDE,
                COLOR_AMARILLO,
                COLOR_VERDE
        };

        GraficoResiduosPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padIzq = 48;
            int padDer = 18;
            int padSup = 26;
            int padInf = 44;

            int areaAncho = getWidth() - padIzq - padDer;
            int areaAlto = getHeight() - padSup - padInf;
            if (areaAncho <= 0 || areaAlto <= 0) {
                g2.dispose();
                return;
            }

            double maxValor = 500.0;
            int pasos = 5;

            for (int i = 0; i <= pasos; i++) {
                int y = padSup + areaAlto - (int) ((i / (double) pasos) * areaAlto);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawLine(padIzq, y, padIzq + areaAncho, y);

                int etiqueta = (int) ((maxValor / pasos) * i);
                g2.setColor(COLOR_BLANCO);
                String textoY = etiqueta + " kg";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(textoY, padIzq - fm.stringWidth(textoY) - 6, y + 4);
            }

            g2.setColor(COLOR_AMARILLO);
            g2.drawLine(padIzq, padSup + areaAlto, padIzq + areaAncho, padSup + areaAlto);

            int n = tipos.length;
            int anchoSlot = areaAncho / n;
            int anchoBarra = Math.min(54, (int) (anchoSlot * 0.55));

            for (int i = 0; i < n; i++) {
                int alturaBarra = (int) ((valoresKg[i] / maxValor) * areaAlto);
                int x = padIzq + i * anchoSlot + (anchoSlot - anchoBarra) / 2;
                int y = padSup + areaAlto - alturaBarra;

                g2.setColor(coloresBarras[i]);
                g2.fillRoundRect(x, y, anchoBarra, alturaBarra, 12, 12);

                String strValor = (valoresKg[i] % 1 == 0)
                        ? String.format("%.0f kg", valoresKg[i])
                        : String.format("%.1f kg", valoresKg[i]);
                g2.setColor(COLOR_BLANCO);
                FontMetrics fmVal = g2.getFontMetrics();
                g2.drawString(strValor, x + (anchoBarra - fmVal.stringWidth(strValor)) / 2, y - 6);

                g2.setColor(COLOR_BLANCO);
                FontMetrics fmTipo = g2.getFontMetrics();
                g2.drawString(tipos[i], x + (anchoBarra - fmTipo.stringWidth(tipos[i])) / 2, padSup + areaAlto + 18);
            }

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new front_principal(ROL_ADMINISTRADOR, "Admin"));
    }
}