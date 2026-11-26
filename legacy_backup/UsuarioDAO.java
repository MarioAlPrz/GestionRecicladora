import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla usuarios.
 * Gestiona autenticación, CRUD y cambios de estado de los usuarios del sistema.
 * Utiliza BCryptUtil para el hash seguro de contraseñas.
 */
public class UsuarioDAO {

    // ─── Autenticación ──────────────────────────────────────────────────

    /**
     * Autentica un usuario verificando credenciales y estado activo.
     * @param username nombre de usuario
     * @param contrasena contraseña en texto plano
     * @return el Usuario autenticado con información de rol, o null si falla
     */
    public Usuario autenticar(String username, String contrasena) {
        String sql = """
            SELECT u.id, u.username, u.contrasena, u.nombre_completo,
                   u.rol_id, u.estado, u.fecha_creacion,
                   r.nombre AS rol_nombre
            FROM usuarios u
            INNER JOIN roles r ON u.rol_id = r.id
            WHERE u.username = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Verificar que la cuenta esté activa
                    String estado = rs.getString("estado");
                    if (!"ACTIVO".equalsIgnoreCase(estado)) {
                        System.err.println("Inicio de sesión fallido: usuario '" + username + "' está inactivo.");
                        return null;
                    }

                    // Verificar contraseña con BCrypt
                    String hashAlmacenado = rs.getString("contrasena");
                    if (!BCryptUtil.checkPassword(contrasena, hashAlmacenado)) {
                        System.err.println("Inicio de sesión fallido: contraseña incorrecta para '" + username + "'.");
                        return null;
                    }

                    // Credenciales válidas: construir el objeto Usuario
                    Usuario usuario = mapearUsuario(rs);
                    usuario.setRolNombre(rs.getString("rol_nombre"));
                    return usuario;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al autenticar usuario '" + username + "': " + e.getMessage());
        }
        return null;
    }

    // ─── Consultas ──────────────────────────────────────────────────────

    /**
     * Lista todos los usuarios con su rol asociado.
     * @return lista de usuarios ordenados por nombre completo
     */
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = """
            SELECT u.id, u.username, u.contrasena, u.nombre_completo,
                   u.rol_id, u.estado, u.fecha_creacion,
                   r.nombre AS rol_nombre
            FROM usuarios u
            INNER JOIN roles r ON u.rol_id = r.id
            ORDER BY u.nombre_completo
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = mapearUsuario(rs);
                u.setRolNombre(rs.getString("rol_nombre"));
                usuarios.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    /**
     * Busca usuarios cuyo nombre completo o username coincida parcialmente con el filtro.
     * @param filtro texto de búsqueda
     * @return lista de usuarios que coinciden
     */
    public List<Usuario> buscar(String filtro) {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = """
            SELECT u.id, u.username, u.contrasena, u.nombre_completo,
                   u.rol_id, u.estado, u.fecha_creacion,
                   r.nombre AS rol_nombre
            FROM usuarios u
            INNER JOIN roles r ON u.rol_id = r.id
            WHERE u.nombre_completo LIKE ? OR u.username LIKE ?
            ORDER BY u.nombre_completo
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String patron = "%" + filtro + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = mapearUsuario(rs);
                    u.setRolNombre(rs.getString("rol_nombre"));
                    usuarios.add(u);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuarios con filtro '" + filtro + "': " + e.getMessage());
        }
        return usuarios;
    }

    /**
     * Busca un usuario por su identificador único.
     * @param id identificador del usuario
     * @return objeto Usuario o null si no existe
     */
    public Usuario buscarPorId(int id) {
        String sql = """
            SELECT u.id, u.username, u.contrasena, u.nombre_completo,
                   u.rol_id, u.estado, u.fecha_creacion,
                   r.nombre AS rol_nombre
            FROM usuarios u
            INNER JOIN roles r ON u.rol_id = r.id
            WHERE u.id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = mapearUsuario(rs);
                    u.setRolNombre(rs.getString("rol_nombre"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Verifica si ya existe un usuario con el username proporcionado.
     * @param username nombre de usuario a verificar
     * @return true si el username ya está registrado
     */
    public boolean existeUsername(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de username '" + username + "': " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si ya existe un usuario con el username dado, excluyendo un ID específico (útil para edición).
     */
    public boolean existeUsername(String username, int excluirId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ? AND id != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setInt(2, excluirId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de username '" + username + "': " + e.getMessage());
        }
        return false;
    }

    // ─── Operaciones de Modificación (CUD) ──────────────────────────────

    /**
     * Inserta un nuevo usuario. La contraseña se hashea automáticamente con BCrypt.
     * @param usuario objeto Usuario con los datos a registrar
     * @return true si la inserción fue exitosa
     */
    public boolean insertar(Usuario usuario) {
        String sql = """
            INSERT INTO usuarios (username, contrasena, nombre_completo, rol_id, estado, fecha_creacion)
            VALUES (?, ?, ?, ?, ?, NOW())
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Establecer la variable de sesión para auditoría
            conn.createStatement().execute("SET @usuario_id = " + SesionUsuario.getUsuarioId());

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                String hashContrasena = BCryptUtil.hashPassword(usuario.getContrasena());

                ps.setString(1, usuario.getUsername());
                ps.setString(2, hashContrasena);
                ps.setString(3, usuario.getNombreCompleto());
                ps.setInt(4, usuario.getRolId());
                ps.setString(5, usuario.getEstado() != null ? usuario.getEstado() : "ACTIVO");

                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            usuario.setId(generatedKeys.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario '" + usuario.getUsername() + "': " + e.getMessage());
        }
        return false;
    }

    /**
     * Actualiza los datos de un usuario existente.
     * Si la contraseña no está vacía, se re-hashea; de lo contrario, se mantiene la anterior.
     * @param usuario objeto Usuario con los datos actualizados
     * @return true si la actualización fue exitosa
     */
    public boolean actualizar(Usuario usuario) {
        boolean cambiarContrasena = usuario.getContrasena() != null
                && !usuario.getContrasena().trim().isEmpty();

        String sql;
        if (cambiarContrasena) {
            sql = """
                UPDATE usuarios
                SET username = ?, contrasena = ?, nombre_completo = ?, rol_id = ?, estado = ?
                WHERE id = ?
            """;
        } else {
            sql = """
                UPDATE usuarios
                SET username = ?, nombre_completo = ?, rol_id = ?, estado = ?
                WHERE id = ?
            """;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute("SET @usuario_id = " + SesionUsuario.getUsuarioId());

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                ps.setString(idx++, usuario.getUsername());

                if (cambiarContrasena) {
                    String hashContrasena = BCryptUtil.hashPassword(usuario.getContrasena());
                    ps.setString(idx++, hashContrasena);
                }

                ps.setString(idx++, usuario.getNombreCompleto());
                ps.setInt(idx++, usuario.getRolId());
                ps.setString(idx++, usuario.getEstado());
                ps.setInt(idx++, usuario.getId());

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario ID " + usuario.getId() + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Cambia el estado de un usuario (ACTIVO/INACTIVO).
     * @param id identificador del usuario
     * @param estado nuevo estado ("ACTIVO" o "INACTIVO")
     * @return true si el cambio fue exitoso
     */
    public boolean cambiarEstado(int id, String estado) {
        String sql = "UPDATE usuarios SET estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute("SET @usuario_id = " + SesionUsuario.getUsuarioId());

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, estado);
                ps.setInt(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del usuario ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Elimina lógicamente un usuario estableciendo su estado como INACTIVO.
     * No se eliminan registros físicamente para mantener integridad referencial.
     * @param id identificador del usuario a desactivar
     * @return true si la desactivación fue exitosa
     */
    public boolean eliminar(int id) {
        return cambiarEstado(id, "INACTIVO");
    }

    // ─── Métodos auxiliares ─────────────────────────────────────────────

    /**
     * Mapea la fila actual del ResultSet a un objeto Usuario.
     */
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setContrasena(rs.getString("contrasena"));
        usuario.setNombreCompleto(rs.getString("nombre_completo"));
        usuario.setRolId(rs.getInt("rol_id"));
        usuario.setEstado(rs.getString("estado"));
        Timestamp ts = rs.getTimestamp("fecha_creacion");
        if (ts != null) {
            usuario.setFechaCreacion(ts.toLocalDateTime());
        }
        return usuario;
    }
}
