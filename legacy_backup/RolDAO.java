import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla roles.
 * Proporciona métodos de consulta para los roles del sistema.
 */
public class RolDAO {

    /**
     * Lista todos los roles registrados en la base de datos.
     * @return lista de objetos Rol, o lista vacía si no hay resultados
     */
    public List<Rol> listarTodos() {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion FROM roles ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                roles.add(mapearRol(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar roles: " + e.getMessage());
        }
        return roles;
    }

    /**
     * Busca un rol por su identificador único.
     * @param id identificador del rol
     * @return el Rol encontrado, o null si no existe
     */
    public Rol buscarPorId(int id) {
        String sql = "SELECT id, nombre, descripcion FROM roles WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearRol(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar rol por ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca un rol por su nombre exacto (sin distinción de mayúsculas/minúsculas).
     * @param nombre nombre del rol a buscar
     * @return el Rol encontrado, o null si no existe
     */
    public Rol buscarPorNombre(String nombre) {
        String sql = "SELECT id, nombre, descripcion FROM roles WHERE UPPER(nombre) = UPPER(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearRol(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar rol por nombre '" + nombre + "': " + e.getMessage());
        }
        return null;
    }

    // ─── Métodos auxiliares ──────────────────────────────────────────────

    /**
     * Mapea la fila actual del ResultSet a un objeto Rol.
     */
    private Rol mapearRol(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        rol.setId(rs.getInt("id"));
        rol.setNombre(rs.getString("nombre"));
        rol.setDescripcion(rs.getString("descripcion"));
        return rol;
    }
}
