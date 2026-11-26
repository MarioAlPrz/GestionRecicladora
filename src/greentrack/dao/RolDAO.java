package greentrack.dao;

import greentrack.config.DatabaseConnection;
import greentrack.model.Rol;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla roles.
 */
public class RolDAO {

    public List<Rol> listarTodos() {
        List<Rol> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion FROM roles ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Rol(rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar roles: " + e.getMessage());
        }
        return lista;
    }

    public Rol buscarPorId(int id) {
        String sql = "SELECT id, nombre, descripcion FROM roles WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Rol(rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar rol por ID: " + e.getMessage());
        }
        return null;
    }

    public Rol buscarPorNombre(String nombre) {
        String sql = "SELECT id, nombre, descripcion FROM roles WHERE UPPER(nombre) = UPPER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Rol(rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar rol por nombre: " + e.getMessage());
        }
        return null;
    }
}
