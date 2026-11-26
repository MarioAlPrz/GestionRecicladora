import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla contenedores.
 * Gestiona el CRUD de contenedores, búsquedas por código de barras (usando sp_buscar_contenedor),
 * actualizaciones (usando sp_actualizar_contenedor) y consultas de llenado (usando fn_porcentaje_llenado).
 */
public class ContenedorDAO {

    /**
     * Lista todos los contenedores con información de su tipo de residuo y ubicación.
     * Utiliza la función fn_porcentaje_llenado(c.id).
     */
    public List<Contenedor> listarTodos() {
        List<Contenedor> lista = new ArrayList<>();
        String sql = """
            SELECT c.id, c.codigo_barras, c.nombre, c.tipo_id, t.nombre AS tipo_nombre,
                   c.ubicacion_id, u.nombre AS ubicacion_nombre, c.capacidad_kg,
                   c.peso_actual_kg, c.estado, c.fecha_registro,
                   fn_porcentaje_llenado(c.id) AS porcentaje_llenado
            FROM contenedores c
            INNER JOIN tipos_residuos t ON c.tipo_id = t.id
            INNER JOIN ubicaciones u ON c.ubicacion_id = u.id
            ORDER BY c.nombre
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearContenedor(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar contenedores: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca contenedores por nombre o código de barras.
     */
    public List<Contenedor> buscar(String filtro) {
        List<Contenedor> lista = new ArrayList<>();
        String sql = """
            SELECT c.id, c.codigo_barras, c.nombre, c.tipo_id, t.nombre AS tipo_nombre,
                   c.ubicacion_id, u.nombre AS ubicacion_nombre, c.capacidad_kg,
                   c.peso_actual_kg, c.estado, c.fecha_registro,
                   fn_porcentaje_llenado(c.id) AS porcentaje_llenado
            FROM contenedores c
            INNER JOIN tipos_residuos t ON c.tipo_id = t.id
            INNER JOIN ubicaciones u ON c.ubicacion_id = u.id
            WHERE c.nombre LIKE ? OR c.codigo_barras LIKE ?
            ORDER BY c.nombre
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String patron = "%" + filtro + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearContenedor(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar contenedores con filtro '" + filtro + "': " + e.getMessage());
        }
        return lista;
    }

    /**
     * Filtra contenedores por tipo de residuo y/o ubicación.
     */
    public List<Contenedor> filtrar(Integer tipoId, Integer ubicacionId) {
        List<Contenedor> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT c.id, c.codigo_barras, c.nombre, c.tipo_id, t.nombre AS tipo_nombre,
                   c.ubicacion_id, u.nombre AS ubicacion_nombre, c.capacidad_kg,
                   c.peso_actual_kg, c.estado, c.fecha_registro,
                   fn_porcentaje_llenado(c.id) AS porcentaje_llenado
            FROM contenedores c
            INNER JOIN tipos_residuos t ON c.tipo_id = t.id
            INNER JOIN ubicaciones u ON c.ubicacion_id = u.id
            WHERE 1=1
        """);

        if (tipoId != null && tipoId > 0) {
            sql.append(" AND c.tipo_id = ?");
        }
        if (ubicacionId != null && ubicacionId > 0) {
            sql.append(" AND c.ubicacion_id = ?");
        }
        sql.append(" ORDER BY c.nombre");

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
                    lista.add(mapearContenedor(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al filtrar contenedores: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca un contenedor por su identificador único.
     */
    public Contenedor buscarPorId(int id) {
        String sql = """
            SELECT c.id, c.codigo_barras, c.nombre, c.tipo_id, t.nombre AS tipo_nombre,
                   c.ubicacion_id, u.nombre AS ubicacion_nombre, c.capacidad_kg,
                   c.peso_actual_kg, c.estado, c.fecha_registro,
                   fn_porcentaje_llenado(c.id) AS porcentaje_llenado
            FROM contenedores c
            INNER JOIN tipos_residuos t ON c.tipo_id = t.id
            INNER JOIN ubicaciones u ON c.ubicacion_id = u.id
            WHERE c.id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearContenedor(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar contenedor por ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca un contenedor por su código de barras invocando el procedimiento almacenado
     * sp_buscar_contenedor(IN p_codigo, OUT p_encontrado).
     */
    public Contenedor buscarPorCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.trim().isEmpty()) {
            return null;
        }

        String callSql = "{CALL sp_buscar_contenedor(?, ?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {

            cs.setString(1, codigoBarras.trim());
            cs.registerOutParameter(2, Types.BOOLEAN);

            boolean hasResultSet = cs.execute();
            boolean encontrado = cs.getBoolean(2);

            if (encontrado) {
                // Si el procedimiento devolvió un ResultSet
                if (hasResultSet) {
                    try (ResultSet rs = cs.getResultSet()) {
                        if (rs != null && rs.next()) {
                            Contenedor c = new Contenedor();
                            c.setId(rs.getInt("id"));
                            c.setCodigoBarras(rs.getString("codigo_barras"));
                            c.setNombre(rs.getString("nombre"));
                            c.setTipoId(rs.getInt("tipo_id"));
                            c.setTipoNombre(rs.getString("tipo_nombre"));
                            c.setUbicacionId(rs.getInt("ubicacion_id"));
                            c.setUbicacionNombre(rs.getString("ubicacion_nombre"));
                            c.setCapacidadKg(rs.getDouble("capacidad_kg"));
                            c.setPesoActualKg(rs.getDouble("peso_actual_kg"));
                            c.setEstado(rs.getString("estado"));
                            Timestamp ts = rs.getTimestamp("fecha_registro");
                            if (ts != null) {
                                c.setFechaRegistro(ts.toLocalDateTime());
                            }
                            c.setPorcentajeLlenado(c.calcularPorcentajeLlenado());
                            return c;
                        }
                    }
                } else {
                    // Fallback en caso de que el driver consuma el result set de otra manera
                    return buscarPorCodigoBarrasDirecto(codigoBarras.trim());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en sp_buscar_contenedor ('" + codigoBarras + "'): " + e.getMessage());
            // Intento con consulta directa como respaldo seguro
            return buscarPorCodigoBarrasDirecto(codigoBarras.trim());
        }
        return null;
    }

    private Contenedor buscarPorCodigoBarrasDirecto(String codigoBarras) {
        String sql = """
            SELECT c.id, c.codigo_barras, c.nombre, c.tipo_id, t.nombre AS tipo_nombre,
                   c.ubicacion_id, u.nombre AS ubicacion_nombre, c.capacidad_kg,
                   c.peso_actual_kg, c.estado, c.fecha_registro,
                   fn_porcentaje_llenado(c.id) AS porcentaje_llenado
            FROM contenedores c
            INNER JOIN tipos_residuos t ON c.tipo_id = t.id
            INNER JOIN ubicaciones u ON c.ubicacion_id = u.id
            WHERE c.codigo_barras = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigoBarras);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearContenedor(rs);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error en búsqueda directa por código de barras: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Verifica si ya existe un código de barras.
     */
    public boolean existeCodigoBarras(String codigo) {
        String sql = "SELECT COUNT(*) FROM contenedores WHERE codigo_barras = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar código de barras: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si existe un código de barras excluyendo el ID actual.
     */
    public boolean existeCodigoBarras(String codigo, int excluirId) {
        String sql = "SELECT COUNT(*) FROM contenedores WHERE codigo_barras = ? AND id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar código de barras: " + e.getMessage());
        }
        return false;
    }

    /**
     * Inserta un nuevo contenedor.
     */
    public boolean insertar(Contenedor c) {
        String sql = """
            INSERT INTO contenedores (codigo_barras, nombre, tipo_id, ubicacion_id, capacidad_kg, peso_actual_kg, estado, fecha_registro)
            VALUES (?, ?, ?, ?, ?, 0.00, ?, NOW())
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute("SET @usuario_id = " + SesionUsuario.getUsuarioId());

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getCodigoBarras());
                ps.setString(2, c.getNombre());
                ps.setInt(3, c.getTipoId());
                ps.setInt(4, c.getUbicacionId());
                ps.setDouble(5, c.getCapacidadKg());
                ps.setString(6, c.getEstado() != null ? c.getEstado() : "ACTIVO");

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            c.setId(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar contenedor: " + e.getMessage());
        }
        return false;
    }

    /**
     * Actualiza un contenedor existente utilizando el procedimiento almacenado
     * sp_actualizar_contenedor(IN p_id, IN p_codigo, IN p_nombre, IN p_tipo_id, IN p_ubicacion_id, IN p_capacidad, IN p_estado, OUT p_resultado).
     */
    public boolean actualizar(Contenedor c) {
        String callSql = "{CALL sp_actualizar_contenedor(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute("SET @usuario_id = " + SesionUsuario.getUsuarioId());

            try (CallableStatement cs = conn.prepareCall(callSql)) {
                cs.setInt(1, c.getId());
                cs.setString(2, c.getCodigoBarras());
                cs.setString(3, c.getNombre());
                cs.setInt(4, c.getTipoId());
                cs.setInt(5, c.getUbicacionId());
                cs.setDouble(6, c.getCapacidadKg());
                cs.setString(7, c.getEstado());
                cs.registerOutParameter(8, Types.VARCHAR);

                cs.execute();
                String resultado = cs.getString(8);

                if (resultado != null && !resultado.startsWith("Error")) {
                    return true;
                } else {
                    System.err.println("Mensaje de sp_actualizar_contenedor: " + resultado);
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar contenedor con stored procedure: " + e.getMessage());
            // Fallback con UPDATE directo si el procedimiento diera problema
            return actualizarDirecto(c);
        }
    }

    private boolean actualizarDirecto(Contenedor c) {
        String sql = """
            UPDATE contenedores
            SET codigo_barras = ?, nombre = ?, tipo_id = ?, ubicacion_id = ?, capacidad_kg = ?, estado = ?
            WHERE id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute("SET @usuario_id = " + SesionUsuario.getUsuarioId());
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, c.getCodigoBarras());
                ps.setString(2, c.getNombre());
                ps.setInt(3, c.getTipoId());
                ps.setInt(4, c.getUbicacionId());
                ps.setDouble(5, c.getCapacidadKg());
                ps.setString(6, c.getEstado());
                ps.setInt(7, c.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error en actualización directa de contenedor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cambia el estado de un contenedor (ACTIVO, MANTENIMIENTO, INACTIVO).
     */
    public boolean cambiarEstado(int id, String estado) {
        String sql = "UPDATE contenedores SET estado = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute("SET @usuario_id = " + SesionUsuario.getUsuarioId());
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, estado);
                ps.setInt(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del contenedor " + id + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Cuenta cuántos contenedores están en estado ACTIVO.
     */
    public int contarActivos() {
        String sql = "SELECT COUNT(*) FROM contenedores WHERE estado = 'ACTIVO'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error al contar contenedores activos: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Cuenta el total de contenedores registrados.
     */
    public int contarTotal() {
        String sql = "SELECT COUNT(*) FROM contenedores";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error al contar total de contenedores: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Cuenta los contenedores cuyo porcentaje de llenado es mayor o igual al porcentaje dado (ej. 80%).
     */
    public int contarProximosALlenarse(double porcentajeMinimo) {
        String sql = """
            SELECT COUNT(*)
            FROM contenedores
            WHERE estado = 'ACTIVO' AND fn_porcentaje_llenado(id) >= ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, porcentajeMinimo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar contenedores próximos a llenarse: " + e.getMessage());
        }
        return 0;
    }

    private Contenedor mapearContenedor(ResultSet rs) throws SQLException {
        Contenedor c = new Contenedor();
        c.setId(rs.getInt("id"));
        c.setCodigoBarras(rs.getString("codigo_barras"));
        c.setNombre(rs.getString("nombre"));
        c.setTipoId(rs.getInt("tipo_id"));
        c.setTipoNombre(rs.getString("tipo_nombre"));
        c.setUbicacionId(rs.getInt("ubicacion_id"));
        c.setUbicacionNombre(rs.getString("ubicacion_nombre"));
        c.setCapacidadKg(rs.getDouble("capacidad_kg"));
        c.setPesoActualKg(rs.getDouble("peso_actual_kg"));
        c.setEstado(rs.getString("estado"));
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) {
            c.setFechaRegistro(ts.toLocalDateTime());
        }
        c.setPorcentajeLlenado(rs.getDouble("porcentaje_llenado"));
        return c;
    }
}
