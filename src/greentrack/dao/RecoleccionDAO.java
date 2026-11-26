package greentrack.dao;

import greentrack.config.DatabaseConnection;
import greentrack.model.Recoleccion;
import greentrack.util.SesionUsuario;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla recolecciones.
 * Registra recolecciones de forma transaccional mediante sp_registrar_recoleccion o transacción manual.
 */
public class RecoleccionDAO {

    /**
     * Registra una recolección invocando el procedimiento almacenado sp_registrar_recoleccion.
     * Retorna el mensaje del parámetro OUT.
     */
    public String registrar(Recoleccion r) throws SQLException {
        String callSql = "{CALL sp_registrar_recoleccion(?, ?, ?, ?, ?)}";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int uid = r.getUsuarioId() > 0 ? r.getUsuarioId() : SesionUsuario.getUsuarioId();
            DatabaseConnection.actualizarVariableSesionMySQL();

            try (CallableStatement cs = conn.prepareCall(callSql)) {
                cs.setInt(1, r.getContenedorId());
                cs.setInt(2, uid);
                cs.setDouble(3, r.getPesoRecolectado());
                cs.setString(4, r.getObservaciones() != null ? r.getObservaciones() : "");
                cs.registerOutParameter(5, Types.VARCHAR);

                cs.execute();
                return cs.getString(5);
            }
        }
    }

    /**
     * Implementación con transacción JDBC explícita y SELECT FOR UPDATE.
     */
    public boolean registrarConTransaccionManual(Recoleccion r) {
        String lockSql = "SELECT estado, capacidad_kg, peso_actual_kg FROM contenedores WHERE id = ? FOR UPDATE";
        String insertSql = "INSERT INTO recolecciones (contenedor_id, usuario_id, peso_recolectado, observaciones, fecha) VALUES (?, ?, ?, ?, NOW())";
        String updateSql = "UPDATE contenedores SET peso_actual_kg = peso_actual_kg + ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción explícita

            int uid = r.getUsuarioId() > 0 ? r.getUsuarioId() : SesionUsuario.getUsuarioId();
            try (Statement st = conn.createStatement()) {
                st.execute("SET @usuario_id = " + uid);
            }

            // 1. Bloquear y verificar el contenedor
            double capacidad = 0;
            double pesoActual = 0;
            String estado = "";
            try (PreparedStatement psLock = conn.prepareStatement(lockSql)) {
                psLock.setInt(1, r.getContenedorId());
                try (ResultSet rs = psLock.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    estado = rs.getString("estado");
                    capacidad = rs.getDouble("capacidad_kg");
                    pesoActual = rs.getDouble("peso_actual_kg");
                }
            }

            // 2. Validar estado y disponibilidad
            if (!"ACTIVO".equalsIgnoreCase(estado)) {
                conn.rollback();
                return false;
            }

            double disponible = capacidad - pesoActual;
            if (r.getPesoRecolectado() <= 0 || r.getPesoRecolectado() > disponible) {
                conn.rollback();
                return false;
            }

            // 3. Insertar recolección
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                psInsert.setInt(1, r.getContenedorId());
                psInsert.setInt(2, uid);
                psInsert.setDouble(3, r.getPesoRecolectado());
                psInsert.setString(4, r.getObservaciones());
                psInsert.executeUpdate();
                try (ResultSet rs = psInsert.getGeneratedKeys()) {
                    if (rs.next()) r.setId(rs.getInt(1));
                }
            }

            // 4. Actualizar peso del contenedor
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setDouble(1, r.getPesoRecolectado());
                psUpdate.setInt(2, r.getContenedorId());
                psUpdate.executeUpdate();
            }

            // 5. Confirmar transacción
            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            System.err.println("Error en transacción manual de recolección: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public List<Recoleccion> listarTodas() {
        List<Recoleccion> lista = new ArrayList<>();
        String sql = """
            SELECT r.id, r.contenedor_id, c.nombre AS contenedor_nombre, c.codigo_barras,
                   r.usuario_id, u.nombre_completo AS usuario_nombre,
                   r.peso_recolectado, r.fecha, r.observaciones,
                   t.nombre AS tipo_residuo
            FROM recolecciones r
            INNER JOIN contenedores c ON r.contenedor_id = c.id
            INNER JOIN usuarios u     ON r.usuario_id = u.id
            INNER JOIN tipos_residuos t ON c.tipo_id = t.id
            ORDER BY r.fecha DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearRecoleccion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar recolecciones: " + e.getMessage());
        }
        return lista;
    }

    public List<Recoleccion> filtrar(LocalDate desde, LocalDate hasta, Integer tipoId) {
        List<Recoleccion> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT r.id, r.contenedor_id, c.nombre AS contenedor_nombre, c.codigo_barras,
                   r.usuario_id, u.nombre_completo AS usuario_nombre,
                   r.peso_recolectado, r.fecha, r.observaciones,
                   t.nombre AS tipo_residuo
            FROM recolecciones r
            INNER JOIN contenedores c ON r.contenedor_id = c.id
            INNER JOIN usuarios u     ON r.usuario_id = u.id
            INNER JOIN tipos_residuos t ON c.tipo_id = t.id
            WHERE 1=1
        """);

        if (desde != null) sql.append(" AND DATE(r.fecha) >= ?");
        if (hasta != null) sql.append(" AND DATE(r.fecha) <= ?");
        if (tipoId != null && tipoId > 0) sql.append(" AND c.tipo_id = ?");
        sql.append(" ORDER BY r.fecha DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (desde != null) ps.setDate(idx++, Date.valueOf(desde));
            if (hasta != null) ps.setDate(idx++, Date.valueOf(hasta));
            if (tipoId != null && tipoId > 0) ps.setInt(idx++, tipoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearRecoleccion(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al filtrar recolecciones: " + e.getMessage());
        }
        return lista;
    }

    public int contarHoy() {
        String sql = "SELECT COUNT(*) FROM recolecciones WHERE DATE(fecha) = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error al contar recolecciones de hoy: " + e.getMessage());
        }
        return 0;
    }

    public double totalRecolectadoHoy() {
        String sql = "SELECT COALESCE(SUM(peso_recolectado), 0.0) FROM recolecciones WHERE DATE(fecha) = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error al calcular total recolectado hoy: " + e.getMessage());
        }
        return 0.0;
    }

    private Recoleccion mapearRecoleccion(ResultSet rs) throws SQLException {
        Recoleccion r = new Recoleccion();
        r.setId(rs.getInt("id"));
        r.setContenedorId(rs.getInt("contenedor_id"));
        r.setContenedorNombre(rs.getString("contenedor_nombre"));
        r.setCodigoBarras(rs.getString("codigo_barras"));
        r.setUsuarioId(rs.getInt("usuario_id"));
        r.setUsuarioNombre(rs.getString("usuario_nombre"));
        r.setPesoRecolectado(rs.getDouble("peso_recolectado"));
        Timestamp ts = rs.getTimestamp("fecha");
        if (ts != null) r.setFecha(ts.toLocalDateTime());
        r.setObservaciones(rs.getString("observaciones"));
        r.setTipoResiduo(rs.getString("tipo_residuo"));
        return r;
    }
}
