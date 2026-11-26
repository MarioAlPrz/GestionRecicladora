import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla ubicaciones.
 * Gestiona el CRUD de ubicaciones físicas y zonas donde se colocan los contenedores.
 */
public class UbicacionDAO {

    // ─── Consultas ──────────────────────────────────────────────────────

    /**
     * Lista todas las ubicaciones registradas.
     * @return lista de ubicaciones ordenadas por nombre
     */
    public List<Ubicacion> listarTodas() {
        List<Ubicacion> ubicaciones = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, referencia, estado FROM ubicaciones ORDER BY nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ubicaciones.add(mapearUbicacion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar ubicaciones: " + e.getMessage());
        }
        return ubicaciones;
    }

    /**
     * Lista solamente las ubicaciones con estado activo.
     * @return lista de ubicaciones activas
     */
    public List<Ubicacion> listarActivas() {
        List<Ubicacion> ubicaciones = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, referencia, estado FROM ubicaciones WHERE estado = TRUE ORDER BY nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ubicaciones.add(mapearUbicacion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar ubicaciones activas: " + e.getMessage());
        }
        return ubicaciones;
    }

    /**
     * Busca una ubicación por su identificador único.
     * @param id identificador de la ubicación
     * @return el objeto Ubicacion encontrado, o null si no existe
     */
    public Ubicacion buscarPorId(int id) {
        String sql = "SELECT id, nombre, descripcion, referencia, estado FROM ubicaciones WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUbicacion(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar ubicación por ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    // ─── Validaciones de existencia ─────────────────────────────────────

    /**
     * Verifica si ya existe una ubicación con el nombre dado.
     * @param nombre nombre a verificar
     * @return true si el nombre ya está en uso
     */
    public boolean existeNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM ubicaciones WHERE UPPER(nombre) = UPPER(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de ubicación '" + nombre + "': " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si ya existe una ubicación con el nombre dado,
     * excluyendo un registro específico (útil para actualizaciones).
     * @param nombre nombre a verificar
     * @param exceptoId ID del registro a excluir de la búsqueda
     * @return true si otro registro ya usa ese nombre
     */
    public boolean existeNombre(String nombre, int exceptoId) {
        String sql = "SELECT COUNT(*) FROM ubicaciones WHERE UPPER(nombre) = UPPER(?) AND id != ?";

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
            System.err.println("Error al verificar existencia de ubicación '" + nombre + "' excluyendo ID " + exceptoId + ": " + e.getMessage());
        }
        return false;
    }

    // ─── Operaciones de Modificación (CUD) ──────────────────────────────

    /**
     * Inserta una nueva ubicación en la base de datos.
     * @param ubicacion objeto Ubicacion con los datos a insertar
     * @return true si la inserción fue exitosa
     */
    public boolean insertar(Ubicacion ubicacion) {
        String sql = "INSERT INTO ubicaciones (nombre, descripcion, referencia, estado) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute("SET @usuario_id = " + SesionUsuario.getUsuarioId());

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, ubicacion.getNombre());
                ps.setString(2, ubicacion.getDescripcion());
                ps.setString(3, ubicacion.getReferencia());
                ps.setBoolean(4, ubicacion.isEstado());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            ubicacion.setId(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar ubicación '" + ubicacion.getNombre() + "': " + e.getMessage());
        }
        return false;
    }

    /**
     * Actualiza una ubicación existente.
     * @param ubicacion objeto Ubicacion con los datos actualizados
     * @return true si la actualización fue exitosa
     */
    public boolean actualizar(Ubicacion ubicacion) {
        String sql = "UPDATE ubicaciones SET nombre = ?, descripcion = ?, referencia = ?, estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute("SET @usuario_id = " + SesionUsuario.getUsuarioId());

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ubicacion.getNombre());
                ps.setString(2, ubicacion.getDescripcion());
                ps.setString(3, ubicacion.getReferencia());
                ps.setBoolean(4, ubicacion.isEstado());
                ps.setInt(5, ubicacion.getId());

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar ubicación ID " + ubicacion.getId() + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Cambia el estado activo/inactivo de una ubicación.
     * @param id identificador de la ubicación
     * @param estado true para activar, false para desactivar
     * @return true si el cambio fue exitoso
     */
    public boolean cambiarEstado(int id, boolean estado) {
        String sql = "UPDATE ubicaciones SET estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute("SET @usuario_id = " + SesionUsuario.getUsuarioId());

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, estado);
                ps.setInt(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado de ubicación ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    // ─── Verificación de dependencias ───────────────────────────────────

    /**
     * Verifica si una ubicación tiene contenedores asociados.
     * Útil antes de intentar desactivar o eliminar una ubicación.
     * @param id identificador de la ubicación
     * @return true si existen contenedores en esta ubicación
     */
    public boolean tieneContenedoresAsociados(int id) {
        String sql = "SELECT COUNT(*) FROM contenedores WHERE ubicacion_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar contenedores asociados a ubicación ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    // ─── Métodos auxiliares ─────────────────────────────────────────────

    /**
     * Mapea la fila actual del ResultSet a un objeto Ubicacion.
     */
    private Ubicacion mapearUbicacion(ResultSet rs) throws SQLException {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setId(rs.getInt("id"));
        ubicacion.setNombre(rs.getString("nombre"));
        ubicacion.setDescripcion(rs.getString("descripcion"));
        ubicacion.setReferencia(rs.getString("referencia"));
        ubicacion.setEstado(rs.getBoolean("estado"));
        return ubicacion;
    }
}
