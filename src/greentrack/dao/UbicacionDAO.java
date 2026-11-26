package greentrack.dao;

import greentrack.config.DatabaseConnection;
import greentrack.model.Ubicacion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla ubicaciones.
 */
public class UbicacionDAO {

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
            System.err.println("Error al buscar ubicación: " + e.getMessage());
        }
        return null;
    }

    public boolean existeNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM ubicaciones WHERE UPPER(nombre) = UPPER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar nombre de ubicación: " + e.getMessage());
        }
        return false;
    }

    public boolean existeNombre(String nombre, int excluirId) {
        String sql = "SELECT COUNT(*) FROM ubicaciones WHERE UPPER(nombre) = UPPER(?) AND id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar nombre de ubicación: " + e.getMessage());
        }
        return false;
    }

    public boolean insertar(Ubicacion u) {
        String sql = "INSERT INTO ubicaciones (nombre, descripcion, referencia, estado) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseConnection.actualizarVariableSesionMySQL();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, u.getNombre());
                ps.setString(2, u.getDescripcion());
                ps.setString(3, u.getReferencia());
                ps.setBoolean(4, u.isEstado());
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) u.setId(rs.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar ubicación: " + e.getMessage());
        }
        return false;
    }

    public boolean actualizar(Ubicacion u) {
        String sql = "UPDATE ubicaciones SET nombre = ?, descripcion = ?, referencia = ?, estado = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseConnection.actualizarVariableSesionMySQL();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, u.getNombre());
                ps.setString(2, u.getDescripcion());
                ps.setString(3, u.getReferencia());
                ps.setBoolean(4, u.isEstado());
                ps.setInt(5, u.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar ubicación: " + e.getMessage());
        }
        return false;
    }

    public boolean cambiarEstado(int id, boolean estado) {
        String sql = "UPDATE ubicaciones SET estado = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseConnection.actualizarVariableSesionMySQL();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, estado);
                ps.setInt(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado de ubicación: " + e.getMessage());
        }
        return false;
    }

    public boolean tieneContenedoresAsociados(int id) {
        String sql = "SELECT COUNT(*) FROM contenedores WHERE ubicacion_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar contenedores de la ubicación: " + e.getMessage());
        }
        return false;
    }

    private Ubicacion mapearUbicacion(ResultSet rs) throws SQLException {
        return new Ubicacion(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getString("referencia"),
                rs.getBoolean("estado")
        );
    }
}
