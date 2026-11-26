import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla tipos_residuos.
 * Gestiona CRUD, validaciones de existencia y verificación de dependencias
 * con contenedores para los tipos de residuo del sistema.
 */
public class TipoResiduoDAO {

    // ─── Consultas ──────────────────────────────────────────────────────

    /**
     * Lista todos los tipos de residuo registrados.
     * @return lista de tipos de residuo ordenados por nombre
     */
    public List<TipoResiduo> listarTodos() {
        List<TipoResiduo> tipos = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, estado FROM tipos_residuos ORDER BY nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tipos.add(mapearTipoResiduo(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar tipos de residuo: " + e.getMessage());
        }
        return tipos;
    }

    /**
     * Lista solamente los tipos de residuo con estado activo.
     * @return lista de tipos de residuo activos
     */
    public List<TipoResiduo> listarActivos() {
        List<TipoResiduo> tipos = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, estado FROM tipos_residuos WHERE estado = TRUE ORDER BY nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tipos.add(mapearTipoResiduo(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar tipos de residuo activos: " + e.getMessage());
        }
        return tipos;
    }

    /**
     * Busca un tipo de residuo por su identificador único.
     * @param id identificador del tipo de residuo
     * @return el TipoResiduo encontrado, o null si no existe
     */
    public TipoResiduo buscarPorId(int id) {
        String sql = "SELECT id, nombre, descripcion, estado FROM tipos_residuos WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearTipoResiduo(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar tipo de residuo por ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    // ─── Validaciones de existencia ─────────────────────────────────────

    /**
     * Verifica si ya existe un tipo de residuo con el nombre dado.
     * @param nombre nombre a verificar
     * @return true si el nombre ya está en uso
     */
    public boolean existeNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM tipos_residuos WHERE UPPER(nombre) = UPPER(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de tipo '" + nombre + "': " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si ya existe un tipo de residuo con el nombre dado,
     * excluyendo un registro específico (útil para actualizaciones).
     * @param nombre nombre a verificar
     * @param exceptoId ID del registro a excluir de la búsqueda
     * @return true si otro registro ya usa ese nombre
     */
    public boolean existeNombre(String nombre, int exceptoId) {
        String sql = "SELECT COUNT(*) FROM tipos_residuos WHERE UPPER(nombre) = UPPER(?) AND id != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, exceptoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de tipo '" + nombre + "' excluyendo ID " + exceptoId + ": " + e.getMessage());
        }
        return false;
    }

    // ─── Operaciones de Modificación (CUD) ──────────────────────────────

    /**
     * Inserta un nuevo tipo de residuo en la base de datos.
     * @param tipo objeto TipoResiduo con los datos a insertar
     * @return true si la inserción fue exitosa
     */
    public boolean insertar(TipoResiduo tipo) {
        String sql = "INSERT INTO tipos_residuos (nombre, descripcion, estado) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute("SET @usuario_id = " + SesionUsuario.getUsuarioId());

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, tipo.getNombre());
                ps.setString(2, tipo.getDescripcion());
                ps.setBoolean(3, tipo.isEstado());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            tipo.setId(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar tipo de residuo '" + tipo.getNombre() + "': " + e.getMessage());
        }
        return false;
    }

    /**
     * Actualiza los datos de un tipo de residuo existente.
     * @param tipo objeto TipoResiduo con los datos actualizados
     * @return true si la actualización fue exitosa
     */
    public boolean actualizar(TipoResiduo tipo) {
        String sql = "UPDATE tipos_residuos SET nombre = ?, descripcion = ?, estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute("SET @usuario_id = " + SesionUsuario.getUsuarioId());

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tipo.getNombre());
                ps.setString(2, tipo.getDescripcion());
                ps.setBoolean(3, tipo.isEstado());
                ps.setInt(4, tipo.getId());

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar tipo de residuo ID " + tipo.getId() + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Cambia el estado activo/inactivo de un tipo de residuo.
     * @param id identificador del tipo de residuo
     * @param estado true para activar, false para desactivar
     * @return true si el cambio fue exitoso
     */
    public boolean cambiarEstado(int id, boolean estado) {
        String sql = "UPDATE tipos_residuos SET estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute("SET @usuario_id = " + SesionUsuario.getUsuarioId());

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, estado);
                ps.setInt(2, id);

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado de tipo de residuo ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    // ─── Verificación de dependencias ───────────────────────────────────

    /**
     * Verifica si un tipo de residuo tiene contenedores asociados.
     * Útil antes de intentar desactivar o eliminar un tipo.
     * @param id identificador del tipo de residuo
     * @return true si existen contenedores asociados a este tipo
     */
    public boolean tieneContenedoresAsociados(int id) {
        String sql = "SELECT COUNT(*) FROM contenedores WHERE tipo_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar contenedores asociados a tipo ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    // ─── Métodos auxiliares ─────────────────────────────────────────────

    /**
     * Mapea la fila actual del ResultSet a un objeto TipoResiduo.
     */
    private TipoResiduo mapearTipoResiduo(ResultSet rs) throws SQLException {
        TipoResiduo tipo = new TipoResiduo();
        tipo.setId(rs.getInt("id"));
        tipo.setNombre(rs.getString("nombre"));
        tipo.setDescripcion(rs.getString("descripcion"));
        tipo.setEstado(rs.getBoolean("estado"));
        return tipo;
    }
}
