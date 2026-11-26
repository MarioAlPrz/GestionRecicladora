package greentrack.dao;

import greentrack.config.DatabaseConnection;
import greentrack.model.Auditoria;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla auditoria.
 */
public class AuditoriaDAO {

    public List<Auditoria> listarTodas() {
        return filtrar(null, null, null);
    }

    public List<Auditoria> filtrar(LocalDate desde, LocalDate hasta, String tabla) {
        List<Auditoria> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT a.id, a.usuario_id,
                   COALESCE(u.nombre_completo, 'Sistema / Sin sesión') AS usuario_nombre,
                   a.tabla_afectada, a.tipo_operacion, a.fecha,
                   a.datos_anteriores, a.datos_nuevos
            FROM auditoria a
            LEFT JOIN usuarios u ON a.usuario_id = u.id
            WHERE 1=1
        """);

        if (desde != null) sql.append(" AND DATE(a.fecha) >= ?");
        if (hasta != null) sql.append(" AND DATE(a.fecha) <= ?");
        if (tabla != null && !tabla.trim().isEmpty() && !"TODAS".equalsIgnoreCase(tabla)) {
            sql.append(" AND a.tabla_afectada = ?");
        }
        sql.append(" ORDER BY a.fecha DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (desde != null) ps.setDate(idx++, Date.valueOf(desde));
            if (hasta != null) ps.setDate(idx++, Date.valueOf(hasta));
            if (tabla != null && !tabla.trim().isEmpty() && !"TODAS".equalsIgnoreCase(tabla)) {
                ps.setString(idx++, tabla.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Auditoria a = new Auditoria();
                    a.setId(rs.getInt("id"));
                    a.setUsuarioId(rs.getInt("usuario_id"));
                    a.setUsuarioNombre(rs.getString("usuario_nombre"));
                    a.setTablaAfectada(rs.getString("tabla_afectada"));
                    a.setTipoOperacion(rs.getString("tipo_operacion"));
                    Timestamp ts = rs.getTimestamp("fecha");
                    if (ts != null) a.setFecha(ts.toLocalDateTime());
                    a.setDatosAnteriores(rs.getString("datos_anteriores"));
                    a.setDatosNuevos(rs.getString("datos_nuevos"));
                    lista.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar auditoría: " + e.getMessage());
        }
        return lista;
    }
}
