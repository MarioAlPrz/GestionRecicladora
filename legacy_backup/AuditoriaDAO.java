import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla auditoria.
 * Permite consultar el registro histórico de operaciones ejecutadas por los triggers
 * de base de datos (INSERTs en recolecciones y UPDATEs en contenedores).
 */
public class AuditoriaDAO {

    /**
     * Lista todos los registros de auditoría ordenados por fecha descendente.
     */
    public List<Auditoria> listarTodas() {
        return filtrar(null, null, null);
    }

    /**
     * Filtra los registros de auditoría por rango de fechas y tabla afectada.
     */
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

        if (desde != null) {
            sql.append(" AND DATE(a.fecha) >= ?");
        }
        if (hasta != null) {
            sql.append(" AND DATE(a.fecha) <= ?");
        }
        if (tabla != null && !tabla.trim().isEmpty() && !"TODAS".equalsIgnoreCase(tabla)) {
            sql.append(" AND a.tabla_afectada = ?");
        }
        sql.append(" ORDER BY a.fecha DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (desde != null) {
                ps.setDate(idx++, Date.valueOf(desde));
            }
            if (hasta != null) {
                ps.setDate(idx++, Date.valueOf(hasta));
            }
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
                    if (ts != null) {
                        a.setFecha(ts.toLocalDateTime());
                    }
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
