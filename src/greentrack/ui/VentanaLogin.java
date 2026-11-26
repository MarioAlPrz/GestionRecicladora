package greentrack.ui;

import greentrack.model.Usuario;
import greentrack.service.AuthService;
import greentrack.util.Validaciones;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Ventana de inicio de sesión para el sistema GreenTrack.
 * Punto de entrada principal de la aplicación.
 */
public class VentanaLogin extends JFrame {

    private static final Color COLOR_CAFE = new Color(74, 44, 23);
    private static final Color COLOR_AMARILLO = new Color(204, 153, 51);
    private static final Color COLOR_VERDE = new Color(46, 125, 50);
    private static final Color COLOR_BLANCO = Color.WHITE;

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;
    private JButton btnSalir;
    private JComboBox<String> cmbAccesoRapido;

    private final AuthService authService = new AuthService();

    public VentanaLogin() {
        setTitle("GreenTrack - Iniciar Sesión");
        setSize(460, 520);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout()) {
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
        panelPrincipal.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Encabezado
        JPanel panelHeader = new JPanel(new GridLayout(2, 1, 0, 4));
        panelHeader.setOpaque(false);

        JLabel lblTitulo = new JLabel("GreenTrack", SwingConstants.CENTER);
        lblTitulo.setForeground(COLOR_BLANCO);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));

        JLabel lblSubtitulo = new JLabel("Sistema de Gestión de Reciclaje", SwingConstants.CENTER);
        lblSubtitulo.setForeground(new Color(230, 230, 230));
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));

        panelHeader.add(lblTitulo);
        panelHeader.add(lblSubtitulo);
        panelPrincipal.add(panelHeader, BorderLayout.NORTH);

        // Tarjeta central de formulario
        JPanel tarjeta = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(50, 30, 15, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Campo Usuario
        gbc.gridy = 0;
        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setForeground(COLOR_BLANCO);
        lblUser.setFont(new Font("SansSerif", Font.BOLD, 13));
        tarjeta.add(lblUser, gbc);

        gbc.gridy = 1;
        txtUsuario = new JTextField();
        txtUsuario.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtUsuario.setPreferredSize(new Dimension(0, 34));
        tarjeta.add(txtUsuario, gbc);

        // Campo Contraseña
        gbc.gridy = 2;
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setForeground(COLOR_BLANCO);
        lblPass.setFont(new Font("SansSerif", Font.BOLD, 13));
        tarjeta.add(lblPass, gbc);

        gbc.gridy = 3;
        txtContrasena = new JPasswordField();
        txtContrasena.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtContrasena.setPreferredSize(new Dimension(0, 34));
        txtContrasena.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ejecutarLogin();
                }
            }
        });
        tarjeta.add(txtContrasena, gbc);

        // Selector rápido para pruebas
        gbc.gridy = 4;
        JLabel lblDemo = new JLabel("Usuarios de prueba:");
        lblDemo.setForeground(new Color(220, 220, 220));
        lblDemo.setFont(new Font("SansSerif", Font.ITALIC, 11));
        tarjeta.add(lblDemo, gbc);

        gbc.gridy = 5;
        cmbAccesoRapido = new JComboBox<>(new String[] {
            "-- Seleccionar para autocompletar --",
            "admin (Administrador)",
            "operador (Operador)",
            "consulta (Consulta)"
        });
        cmbAccesoRapido.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cmbAccesoRapido.addActionListener(e -> {
            int sel = cmbAccesoRapido.getSelectedIndex();
            if (sel == 1) {
                txtUsuario.setText("admin");
                txtContrasena.setText("admin123");
            } else if (sel == 2) {
                txtUsuario.setText("operador");
                txtContrasena.setText("admin123");
            } else if (sel == 3) {
                txtUsuario.setText("consulta");
                txtContrasena.setText("admin123");
            }
        });
        tarjeta.add(cmbAccesoRapido, gbc);

        panelPrincipal.add(tarjeta, BorderLayout.CENTER);

        // Panel de Botones Inferior
        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 14, 0));
        panelBotones.setOpaque(false);
        panelBotones.setBorder(new EmptyBorder(16, 0, 0, 0));

        btnIngresar = crearBotonRedondeado("Ingresar", COLOR_VERDE);
        btnIngresar.addActionListener(e -> ejecutarLogin());

        btnSalir = crearBotonRedondeado("Salir", new Color(160, 40, 40));
        btnSalir.addActionListener(e -> System.exit(0));

        panelBotones.add(btnIngresar);
        panelBotones.add(btnSalir);

        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        setContentPane(panelPrincipal);
    }

    private void ejecutarLogin() {
        String username = txtUsuario.getText().trim();
        String password = new String(txtContrasena.getPassword());

        if (Validaciones.esTextoVacio(username) || Validaciones.esTextoVacio(password)) {
            Validaciones.mostrarError(this, "Por favor ingrese su usuario y contraseña.");
            return;
        }

        btnIngresar.setEnabled(false);
        btnIngresar.setText("Verificando...");

        SwingWorker<Usuario, Void> worker = new SwingWorker<>() {
            @Override
            protected Usuario doInBackground() {
                return authService.iniciarSesion(username, password);
            }

            @Override
            protected void done() {
                btnIngresar.setEnabled(true);
                btnIngresar.setText("Ingresar");
                try {
                    Usuario usuario = get();
                    if (usuario != null) {
                        new front_principal(usuario.getRolNombre(), usuario.getNombreCompleto());
                        dispose();
                    } else {
                        Validaciones.mostrarError(VentanaLogin.this,
                                "Credenciales incorrectas o usuario inactivo.\n" +
                                "Verifique su usuario y contraseña.");
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    Validaciones.mostrarError(VentanaLogin.this,
                            "Error al conectar con la base de datos MySQL:\n" + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JButton crearBotonRedondeado(String texto, Color fondo) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? fondo.darker() : (getModel().isRollover() ? fondo.brighter() : fondo));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(COLOR_BLANCO);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new VentanaLogin().setVisible(true);
        });
    }
}
