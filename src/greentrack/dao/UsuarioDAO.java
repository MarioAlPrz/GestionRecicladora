package greentrack.dao;

import greentrack.config.DatabaseConnection;
import greentrack.model.Usuario;
import greentrack.util.BCryptUtil;
import greentrack.util.SesionUsuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla usuarios.
 * Gestiona autenticación con BCrypt, CRUD y cambios de estado.
 */
public class UsuarioDAO {

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
                    String estado = rs.getString("estado");
                    if (!"ACTIVO".equalsIgnoreCase(estado)) {
                        System.err.println("Usuario inactivo: " + username);
                        return null;
                    }

                    String hash = rs.getString("contrasena");
                    if (!BCryptUtil.checkPassword(contrasena, hash)) {
                        System.err.println("Contraseña inválida para: " + username);
                        return null;
                    }

                    Usuario u = mapearUsuario(rs);
                    u.setRolNombre(rs.getString("rol_nombre"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en autenticación: " + e.getMessage());
            throw new RuntimeException("Error al conectar con la base de datos MySQL: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
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
                lista.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }

    public List<Usuario> buscar(String filtro) {
        List<Usuario> lista = new ArrayList<>();
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

            String p = "%" + filtro + "%";
            ps.setString(1, p);
            ps.setString(2, p);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = mapearUsuario(rs);
                    u.setRolNombre(rs.getString("rol_nombre"));
                    lista.add(u);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuarios: " + e.getMessage());
        }
        return lista;
    }

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
            System.err.println("Error al buscar usuario por ID: " + e.getMessage());
        }
        return null;
    }

    public boolean existeUsername(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar username: " + e.getMessage());
        }
        return false;
    }

    public boolean existeUsername(String username, int excluirId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ? AND id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar username: " + e.getMessage());
        }
        return false;
    }

    public boolean insertar(Usuario u) {
        String sql = "INSERT INTO usuarios (nombre_completo, username, contrasena, rol_id, estado, fecha_creacion) VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseConnection.actualizarVariableSesionMySQL();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                String hash = BCryptUtil.hashPassword(u.getContrasena());
                ps.setString(1, u.getNombreCompleto());
                ps.setString(2, u.getUsername());
                ps.setString(3, hash);
                ps.setInt(4, u.getRolId());
                ps.setString(5, u.getEstado() != null ? u.getEstado() : "ACTIVO");

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            u.setId(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
        }
        return false;
    }

    public boolean actualizar(Usuario u) {
        boolean cambiarPass = u.getContrasena() != null && !u.getContrasena().trim().isEmpty();
        String sql = cambiarPass
                ? "UPDATE usuarios SET nombre_completo = ?, username = ?, contrasena = ?, rol_id = ?, estado = ? WHERE id = ?"
                : "UPDATE usuarios SET nombre_completo = ?, username = ?, rol_id = ?, estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseConnection.actualizarVariableSesionMySQL();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                ps.setString(idx++, u.getNombreCompleto());
                ps.setString(idx++, u.getUsername());
                if (cambiarPass) {
                    ps.setString(idx++, BCryptUtil.hashPassword(u.getContrasena()));
                }
                ps.setInt(idx++, u.getRolId());
                ps.setString(idx++, u.getEstado());
                ps.setInt(idx++, u.getId());

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
        }
        return false;
    }

    public boolean cambiarEstado(int id, String estado) {
        String sql = "UPDATE usuarios SET estado = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseConnection.actualizarVariableSesionMySQL();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, estado);
                ps.setInt(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado de usuario: " + e.getMessage());
        }
        return false;
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setContrasena(rs.getString("contrasena"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setRolId(rs.getInt("rol_id"));
        u.setEstado(rs.getString("estado"));
        Timestamp ts = rs.getTimestamp("fecha_creacion");
        if (ts != null) {
            u.setFechaCreacion(ts.toLocalDateTime());
        }
        return u;
    }
}
