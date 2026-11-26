// ============================================================================
// Auditoria.java — Modelo que representa un registro de auditoría
// ============================================================================
// Corresponde a la tabla 'auditoria' en la base de datos green_track.
// Los triggers de MySQL generan estos registros automáticamente cuando
// se realizan operaciones INSERT, UPDATE o DELETE en las tablas principales.
// ============================================================================

import java.time.LocalDateTime;

/**
 * Modelo de datos para los registros de auditoría del sistema.
 * Permite rastrear quién hizo qué cambio y cuándo.
 */
public class Auditoria {

    // --- Atributos ---
    private int id;
    private int usuarioId;              // FK → usuarios (quien realizó la acción)
    private String usuarioNombre;       // Nombre del usuario (JOIN)
    private String tablaAfectada;       // Nombre de la tabla modificada
    private String tipoOperacion;       // "INSERT", "UPDATE" o "DELETE"
    private LocalDateTime fecha;        // Fecha y hora de la operación
    private String datosAnteriores;     // JSON con datos antes del cambio (UPDATE/DELETE)
    private String datosNuevos;         // JSON con datos después del cambio (INSERT/UPDATE)

    // =========================================================================
    // CONSTRUCTORES
    // =========================================================================

    /** Constructor vacío */
    public Auditoria() {
    }

    /**
     * Constructor completo.
     *
     * @param id               ID del registro de auditoría
     * @param usuarioId        ID del usuario que realizó la operación
     * @param usuarioNombre    Nombre del usuario
     * @param tablaAfectada    Tabla que fue modificada
     * @param tipoOperacion    Tipo de operación (INSERT, UPDATE, DELETE)
     * @param fecha            Fecha y hora de la operación
     * @param datosAnteriores  Datos antes del cambio (formato JSON)
     * @param datosNuevos      Datos después del cambio (formato JSON)
     */
    public Auditoria(int id, int usuarioId, String usuarioNombre,
                     String tablaAfectada, String tipoOperacion,
                     LocalDateTime fecha, String datosAnteriores,
                     String datosNuevos) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.tablaAfectada = tablaAfectada;
        this.tipoOperacion = tipoOperacion;
        this.fecha = fecha;
        this.datosAnteriores = datosAnteriores;
        this.datosNuevos = datosNuevos;
    }

    // =========================================================================
    // GETTERS Y SETTERS
    // =========================================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(String tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getDatosAnteriores() {
        return datosAnteriores;
    }

    public void setDatosAnteriores(String datosAnteriores) {
        this.datosAnteriores = datosAnteriores;
    }

    public String getDatosNuevos() {
        return datosNuevos;
    }

    public void setDatosNuevos(String datosNuevos) {
        this.datosNuevos = datosNuevos;
    }

    // =========================================================================
    // REPRESENTACIÓN EN TEXTO
    // =========================================================================

    @Override
    public String toString() {
        return String.format("Auditoría #%d: %s en '%s' por %s (%s)",
                id, tipoOperacion, tablaAfectada, usuarioNombre,
                fecha != null ? fecha.toString() : "sin fecha");
    }
}
