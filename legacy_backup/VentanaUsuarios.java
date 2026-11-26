import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana de gestión de usuarios y asignación de roles.
 * Permite listar, buscar, crear, editar y activar/desactivar usuarios.
 * Requiere permisos de ADMINISTRADOR.
 */
public class VentanaUsuarios extends JDialog {

    private static final Color COLOR_CAFE = new Color(74, 44, 23);
    private static final Color COLOR_AMARILLO = new Color(204, 153, 51);
    private static final Color COLOR_VERDE = new Color(46, 125, 50);
    private static final Color COLOR_BLANCO = Color.WHITE;

    private JTextField txtNombreCompleto;
    private JTextField txtUsername;
    private JPasswordField txtContrasena;
    private JComboBox<Rol> cmbRol;
    private JComboBox<String> cmbEstado;
    private JTextField txtBuscar;

    private JButton btnGuardar;
    private JButton btnLimpiar;
    private JButton btnBuscar;
    private JButton btnAlternarEstado;

    private JTable tblUsuarios;
    private DefaultTableModel modeloTabla;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();

    private Integer usuarioSeleccionadoId = null;

    public VentanaUsuarios(Window owner) {
        super(owner, "Gestión de Usuarios y Roles - GreenTrack", ModalityType.APPLICATION_MODAL);
        setSize(980, 640);
        setMinimumSize(new Dimension(860, 540));
        setLocationRelativeTo(owner);

        // Control de permisos: solo administradores
        if (!SesionUsuario.esAdministrador()) {
            Validaciones.mostrarError(owner, "Acceso denegado: Solo los usuarios con rol ADMINISTRADOR pueden gestionar usuarios.");
            dispose();
            return;
        }

        inicializarComponentes();
        cargarRoles();
        cargarUsuarios(null);
        setVisible(true);
    }

    public VentanaUsuarios() {
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

        // Encabezado
        JLabel lblTitulo = new JLabel("Administración de Usuarios y Roles");
        lblTitulo.setForeground(COLOR_BLANCO);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // Contenedor Central dividido
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setOpaque(false);
        split.setDividerLocation(360);
        split.setContinuousLayout(true);

        // Panel Izquierdo: Formulario
        JPanel panelForm = crearTarjetaPanel(new BorderLayout(0, 12), new EmptyBorder(16, 16, 16, 16));
        JLabel lblSub = new JLabel("Datos del Usuario");
        lblSub.setForeground(COLOR_BLANCO);
        lblSub.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelForm.add(lblSub, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(0, 1, 6, 8));
        campos.setOpaque(false);

        campos.add(crearLabel("Nombre Completo:"));
        txtNombreCompleto = new JTextField();
        campos.add(txtNombreCompleto);

        campos.add(crearLabel("Nombre de Usuario (Username):"));
        txtUsername = new JTextField();
        campos.add(txtUsername);

        campos.add(crearLabel("Contraseña (dejar en blanco para conservar actual):"));
        txtContrasena = new JPasswordField();
        campos.add(txtContrasena);

        campos.add(crearLabel("Rol del Sistema:"));
        cmbRol = new JComboBox<>();
        campos.add(cmbRol);

        campos.add(crearLabel("Estado:"));
        cmbEstado = new JComboBox<>(new String[] { "ACTIVO", "INACTIVO" });
        campos.add(cmbEstado);

        panelForm.add(campos, BorderLayout.CENTER);

        // Botones del formulario
        JPanel panelBtnForm = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelBtnForm.setOpaque(false);

        btnGuardar = crearBoton("Guardar", COLOR_VERDE, 120, 36);
        btnGuardar.addActionListener(e -> guardarUsuario());

        btnLimpiar = crearBoton("Limpiar", new Color(120, 120, 120), 100, 36);
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBtnForm.add(btnGuardar);
        panelBtnForm.add(btnLimpiar);
        panelForm.add(panelBtnForm, BorderLayout.SOUTH);

        split.setLeftComponent(panelForm);

        // Panel Derecho: Tabla y Búsqueda
        JPanel panelTablaContenedor = crearTarjetaPanel(new BorderLayout(0, 10), new EmptyBorder(16, 16, 16, 16));

        // Barra de búsqueda
        JPanel panelBusqueda = new JPanel(new BorderLayout(8, 0));
        panelBusqueda.setOpaque(false);
        txtBuscar = new JTextField();
        txtBuscar.addActionListener(e -> cargarUsuarios(txtBuscar.getText().trim()));

        btnBuscar = crearBoton("Buscar", COLOR_VERDE, 90, 30);
        btnBuscar.addActionListener(e -> cargarUsuarios(txtBuscar.getText().trim()));

        JButton btnRefrescar = crearBoton("Todos", COLOR_AMARILLO.darker(), 80, 30);
        btnRefrescar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarUsuarios(null);
        });

        JPanel panelBtnsSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelBtnsSearch.setOpaque(false);
        panelBtnsSearch.add(btnBuscar);
        panelBtnsSearch.add(btnRefrescar);

        panelBusqueda.add(new JLabel("Filtrar: ") {{ setForeground(COLOR_BLANCO); }}, BorderLayout.WEST);
        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);
        panelBusqueda.add(panelBtnsSearch, BorderLayout.EAST);

        panelTablaContenedor.add(panelBusqueda, BorderLayout.NORTH);

        // Tabla
        String[] columnas = { "ID", "Nombre Completo", "Username", "Rol", "Estado", "Fecha Creación" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblUsuarios = new JTable(modeloTabla);
        tblUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUsuarios.setRowHeight(24);
        tblUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblUsuarios.getSelectedRow() != -1) {
                cargarSeleccionEnFormulario();
            }
        });

        panelTablaContenedor.add(new JScrollPane(tblUsuarios), BorderLayout.CENTER);

        // Barra inferior de tabla
        JPanel panelInferiorTabla = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelInferiorTabla.setOpaque(false);

        btnAlternarEstado = crearBoton("Cambiar Estado", COLOR_AMARILLO.darker(), 140, 34);
        btnAlternarEstado.addActionListener(e -> alternarEstadoUsuario());

        panelInferiorTabla.add(btnAlternarEstado);
        panelTablaContenedor.add(panelInferiorTabla, BorderLayout.SOUTH);

        split.setRightComponent(panelTablaContenedor);

        panelPrincipal.add(split, BorderLayout.CENTER);
        setContentPane(panelPrincipal);
    }

    private void cargarRoles() {
        cmbRol.removeAllItems();
        List<Rol> roles = rolDAO.listarTodos();
        for (Rol r : roles) {
            cmbRol.addItem(r);
        }
    }

    private void cargarUsuarios(String filtro) {
        SwingWorker<List<Usuario>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Usuario> doInBackground() {
                if (filtro == null || filtro.isEmpty()) {
                    return usuarioDAO.listarTodos();
                } else {
                    return usuarioDAO.buscar(filtro);
                }
            }

            @Override
            protected void done() {
                try {
                    List<Usuario> lista = get();
                    modeloTabla.setRowCount(0);
                    for (Usuario u : lista) {
                        modeloTabla.addRow(new Object[] {
                            u.getId(),
                            u.getNombreCompleto(),
                            u.getUsername(),
                            u.getRolNombre(),
                            u.getEstado(),
                            Validaciones.formatearFecha(u.getFechaCreacion())
                        });
                    }
                } catch (Exception ex) {
                    Validaciones.mostrarError(VentanaUsuarios.this, "Error al cargar usuarios: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tblUsuarios.getSelectedRow();
        if (fila == -1) return;

        int id = (int) modeloTabla.getValueAt(fila, 0);
        Usuario u = usuarioDAO.buscarPorId(id);
        if (u != null) {
            usuarioSeleccionadoId = u.getId();
            txtNombreCompleto.setText(u.getNombreCompleto());
            txtUsername.setText(u.getUsername());
            txtContrasena.setText(""); // Por seguridad no se muestra la contraseña

            for (int i = 0; i < cmbRol.getItemCount(); i++) {
                Rol r = cmbRol.getItemAt(i);
                if (r.getId() == u.getRolId()) {
                    cmbRol.setSelectedIndex(i);
                    break;
                }
            }
            cmbEstado.setSelectedItem(u.getEstado());
            btnGuardar.setText("Actualizar");
        }
    }

    private void guardarUsuario() {
        String nombre = txtNombreCompleto.getText().trim();
        String username = txtUsername.getText().trim();
        String pass = new String(txtContrasena.getPassword());
        Rol rol = (Rol) cmbRol.getSelectedItem();
        String estado = (String) cmbEstado.getSelectedItem();

        if (Validaciones.esTextoVacio(nombre) || Validaciones.esTextoVacio(username)) {
            Validaciones.mostrarError(this, "El nombre completo y el nombre de usuario son obligatorios.");
            return;
        }

        if (rol == null) {
            Validaciones.mostrarError(this, "Debe seleccionar un rol para el usuario.");
            return;
        }

        if (usuarioSeleccionadoId == null) {
            // Nuevo usuario: contraseña obligatoria
            if (Validaciones.esTextoVacio(pass)) {
                Validaciones.mostrarError(this, "Debe especificar una contraseña para el nuevo usuario.");
                return;
            }
            if (usuarioDAO.existeUsername(username)) {
                Validaciones.mostrarError(this, "El nombre de usuario '" + username + "' ya se encuentra registrado.");
                return;
            }

            Usuario nuevo = new Usuario();
            nuevo.setNombreCompleto(nombre);
            nuevo.setUsername(username);
            nuevo.setContrasena(pass);
            nuevo.setRolId(rol.getId());
            nuevo.setEstado(estado);

            boolean exito = usuarioDAO.insertar(nuevo);
            if (exito) {
                Validaciones.mostrarExito(this, "Usuario creado exitosamente.");
                limpiarFormulario();
                cargarUsuarios(null);
            } else {
                Validaciones.mostrarError(this, "No se pudo crear el usuario. Verifique la conexión a MySQL.");
            }
        } else {
            // Actualización
            if (usuarioDAO.existeUsername(username, usuarioSeleccionadoId)) {
                Validaciones.mostrarError(this, "El nombre de usuario '" + username + "' ya pertenece a otro usuario.");
                return;
            }

            Usuario actual = new Usuario();
            actual.setId(usuarioSeleccionadoId);
            actual.setNombreCompleto(nombre);
            actual.setUsername(username);
            actual.setContrasena(pass); // Si viene vacía, el DAO conserva la actual
            actual.setRolId(rol.getId());
            actual.setEstado(estado);

            boolean exito = usuarioDAO.actualizar(actual);
            if (exito) {
                Validaciones.mostrarExito(this, "Usuario actualizado exitosamente.");
                limpiarFormulario();
                cargarUsuarios(null);
            } else {
                Validaciones.mostrarError(this, "No se pudo actualizar el usuario.");
            }
        }
    }

    private void alternarEstadoUsuario() {
        int fila = tblUsuarios.getSelectedRow();
        if (fila == -1) {
            Validaciones.mostrarError(this, "Seleccione un usuario de la tabla para cambiar su estado.");
            return;
        }

        int id = (int) modeloTabla.getValueAt(fila, 0);
        String estadoActual = (String) modeloTabla.getValueAt(fila, 4);
        String nuevoEstado = "ACTIVO".equalsIgnoreCase(estadoActual) ? "INACTIVO" : "ACTIVO";

        // Impedir que el administrador se desactive a sí mismo
        if (id == SesionUsuario.getUsuarioId() && "INACTIVO".equals(nuevoEstado)) {
            Validaciones.mostrarError(this, "No puede desactivar su propio usuario activo.");
            return;
        }

        if (Validaciones.confirmar(this, "¿Desea cambiar el estado del usuario a " + nuevoEstado + "?")) {
            boolean exito = usuarioDAO.cambiarEstado(id, nuevoEstado);
            if (exito) {
                Validaciones.mostrarExito(this, "Estado del usuario actualizado a " + nuevoEstado + ".");
                cargarUsuarios(txtBuscar.getText().trim());
                limpiarFormulario();
            } else {
                Validaciones.mostrarError(this, "No fue posible cambiar el estado del usuario.");
            }
        }
    }

    private void limpiarFormulario() {
        usuarioSeleccionadoId = null;
        txtNombreCompleto.setText("");
        txtUsername.setText("");
        txtContrasena.setText("");
        if (cmbRol.getItemCount() > 0) cmbRol.setSelectedIndex(0);
        cmbEstado.setSelectedItem("ACTIVO");
        btnGuardar.setText("Guardar");
        tblUsuarios.clearSelection();
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
