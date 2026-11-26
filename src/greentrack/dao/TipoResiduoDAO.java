package greentrack.dao;

import greentrack.config.DatabaseConnection;
import greentrack.model.TipoResiduo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla tipos_residuos.
 */
public class TipoResiduoDAO {

    public List<TipoResiduo> listarTodos() {
        List<TipoResiduo> tipos = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, estado FROM tipos_residuos ORDER BY nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tipos.add(mapearTipo(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar tipos de residuo: " + e.getMessage());
        }
        return tipos;
    }

    public List<TipoResiduo> listarActivos() {
        List<TipoResiduo> tipos = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, estado FROM tipos_residuos WHERE estado = TRUE ORDER BY nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tipos.add(mapearTipo(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar tipos activos: " + e.getMessage());
        }
        return tipos;
    }

    public TipoResiduo buscarPorId(int id) {
        String sql = "SELECT id, nombre, descripcion, estado FROM tipos_residuos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearTipo(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar tipo de residuo por ID: " + e.getMessage());
        }
        return null;
    }

    public boolean existeNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM tipos_residuos WHERE UPPER(nombre) = UPPER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar nombre de tipo: " + e.getMessage());
        }
        return false;
    }

    public boolean existeNombre(String nombre, int excluirId) {
        String sql = "SELECT COUNT(*) FROM tipos_residuos WHERE UPPER(nombre) = UPPER(?) AND id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar nombre de tipo: " + e.getMessage());
        }
        return false;
    }

    public boolean insertar(TipoResiduo t) {
        String sql = "INSERT INTO tipos_residuos (nombre, descripcion, estado) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseConnection.actualizarVariableSesionMySQL();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, t.getNombre());
                ps.setString(2, t.getDescripcion());
                ps.setBoolean(3, t.isEstado());
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) t.setId(rs.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar tipo de residuo: " + e.getMessage());
        }
        return false;
    }

    public boolean actualizar(TipoResiduo t) {
        String sql = "UPDATE tipos_residuos SET nombre = ?, descripcion = ?, estado = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseConnection.actualizarVariableSesionMySQL();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, t.getNombre());
                ps.setString(2, t.getDescripcion());
                ps.setBoolean(3, t.isEstado());
                ps.setInt(4, t.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar tipo de residuo: " + e.getMessage());
        }
        return false;
    }

    public boolean cambiarEstado(int id, boolean estado) {
        String sql = "UPDATE tipos_residuos SET estado = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseConnection.actualizarVariableSesionMySQL();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, estado);
                ps.setInt(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado de tipo de residuo: " + e.getMessage());
        }
        return false;
    }

    public boolean tieneContenedoresAsociados(int id) {
        String sql = "SELECT COUNT(*) FROM contenedores WHERE tipo_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar relaciones de tipo: " + e.getMessage());
        }
        return false;
    }

    private TipoResiduo mapearTipo(ResultSet rs) throws SQLException {
        return new TipoResiduo(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBoolean("estado")
        );
    }
}
