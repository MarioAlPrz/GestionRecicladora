import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * DAO para consultas avanzadas de reportes y estadísticas del sistema GreenTrack.
 * Implementa consultas con JOIN de al menos 3 tablas, GROUP BY, funciones de agregación
 * y las funciones almacenadas fn_porcentaje_llenado y fn_total_recolectado.
 */
public class ReporteDAO {

    /**
     * Reporte 1: Recolecciones agrupadas por tipo de residuo.
     * Retorna:
     * - Tipo de residuo
     * - Número de recolecciones
     * - Peso total recolectado (kg)
     * - Peso promedio por recolección (kg)
     */
    public List<Map<String, Object>> reporteRecoleccionesPorTipo(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Map<String, Object>> resultados = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT t.nombre AS tipo_residuo,
                   COUNT(r.id) AS num_recolecciones,
                   COALESCE(SUM(r.peso_recolectado), 0.0) AS peso_total,
                   COALESCE(AVG(r.peso_recolectado), 0.0) AS peso_promedio
            FROM tipos_residuos t
            LEFT JOIN contenedores c ON c.tipo_id = t.id
            LEFT JOIN recolecciones r ON r.contenedor_id = c.id
        """);

        boolean hayFiltroFecha = (fechaInicio != null || fechaFin != null);
        if (hayFiltroFecha) {
            sql.append(" AND (r.id IS NULL OR (");
            List<String> condiciones = new ArrayList<>();
            if (fechaInicio != null) condiciones.add("DATE(r.fecha) >= ?");
            if (fechaFin != null) condiciones.add("DATE(r.fecha) <= ?");
            sql.append(String.join(" AND ", condiciones));
            sql.append("))");
        }

        sql.append(" GROUP BY t.id, t.nombre ORDER BY peso_total DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (fechaInicio != null) {
                ps.setDate(idx++, Date.valueOf(fechaInicio));
            }
            if (fechaFin != null) {
                ps.setDate(idx++, Date.valueOf(fechaFin));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("tipo_residuo", rs.getString("tipo_residuo"));
                    fila.put("num_recolecciones", rs.getInt("num_recolecciones"));
                    fila.put("peso_total", rs.getDouble("peso_total"));
                    fila.put("peso_promedio", rs.getDouble("peso_promedio"));
                    resultados.add(fila);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en reporteRecoleccionesPorTipo: " + e.getMessage());
        }
        return resultados;
    }

    /**
     * Reporte 2: Estado general de los contenedores.
     * Retorna:
     * - Nombre del contenedor
     * - Código de barras
     * - Tipo de residuo
     * - Ubicación
     * - Capacidad (kg)
     * - Peso actual (kg)
     * - Porcentaje de llenado (calculado con fn_porcentaje_llenado)
     * - Estado
     */
    public List<Map<String, Object>> reporteEstadoContenedores(Integer tipoId, Integer ubicacionId) {
        List<Map<String, Object>> resultados = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT c.nombre AS contenedor_nombre,
                   c.codigo_barras,
                   t.nombre AS tipo_residuo,
                   u.nombre AS ubicacion,
                   c.capacidad_kg,
                   c.peso_actual_kg,
                   fn_porcentaje_llenado(c.id) AS porcentaje_llenado,
                   c.estado
            FROM contenedores c
            INNER JOIN tipos_residuos t ON c.tipo_id = t.id
            INNER JOIN ubicaciones u    ON c.ubicacion_id = u.id
            WHERE 1=1
        """);

        if (tipoId != null && tipoId > 0) {
            sql.append(" AND c.tipo_id = ?");
        }
        if (ubicacionId != null && ubicacionId > 0) {
            sql.append(" AND c.ubicacion_id = ?");
        }
        sql.append(" ORDER BY porcentaje_llenado DESC, c.nombre ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (tipoId != null && tipoId > 0) {
                ps.setInt(idx++, tipoId);
            }
            if (ubicacionId != null && ubicacionId > 0) {
                ps.setInt(idx++, ubicacionId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("contenedor_nombre", rs.getString("contenedor_nombre"));
                    fila.put("codigo_barras", rs.getString("codigo_barras"));
                    fila.put("tipo_residuo", rs.getString("tipo_residuo"));
                    fila.put("ubicacion", rs.getString("ubicacion"));
                    fila.put("capacidad_kg", rs.getDouble("capacidad_kg"));
                    fila.put("peso_actual_kg", rs.getDouble("peso_actual_kg"));
                    fila.put("porcentaje_llenado", rs.getDouble("porcentaje_llenado"));
                    fila.put("estado", rs.getString("estado"));
                    resultados.add(fila);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en reporteEstadoContenedores: " + e.getMessage());
        }
        return resultados;
    }

    /**
     * Calcula el peso total histórico recolectado en toda la planta.
     */
    public double totalKgRecolectados() {
        String sql = "SELECT COALESCE(SUM(peso_recolectado), 0.0) FROM recolecciones";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error al obtener total kg recolectados: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Obtiene el total recolectado por tipo de residuo para alimentar el gráfico de barras del Dashboard.
     * Retorna un mapa con clave = nombre del tipo y valor = total kg recolectados.
     * Utiliza la función almacenada fn_total_recolectado(t.id).
     */
    public Map<String, Double> obtenerTotalesPorTipoParaGrafico() {
        Map<String, Double> datos = new LinkedHashMap<>();
        String sql = """
            SELECT t.nombre, fn_total_recolectado(t.id) AS total_kg
            FROM tipos_residuos t
            WHERE t.estado = TRUE
            ORDER BY t.id
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                datos.put(rs.getString("nombre"), rs.getDouble("total_kg"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener datos para gráfico: " + e.getMessage());
        }
        return datos;
    }
}
