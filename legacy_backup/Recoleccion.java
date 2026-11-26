// ============================================================================
// Recoleccion.java — Modelo que representa un registro de recolección
// ============================================================================
// Corresponde a la tabla 'recolecciones' en la base de datos green_track.
// Cada registro indica cuánto peso se recolectó de un contenedor específico.
// ============================================================================

import java.time.LocalDateTime;

/**
 * Modelo de datos para los registros de recolección de residuos.
 * Cada recolección vincula un contenedor, un operador y el peso recogido.
 */
public class Recoleccion {

    // --- Atributos ---
    private int id;
    private int contenedorId;           // FK → contenedores
    private String contenedorNombre;    // Nombre del contenedor (JOIN)
    private String codigoBarras;        // Código de barras del contenedor
    private int usuarioId;              // FK → usuarios (operador que recolectó)
    private String usuarioNombre;       // Nombre del operador (JOIN)
    private double pesoRecolectado;     // Peso recolectado en kilogramos
    private LocalDateTime fecha;        // Fecha y hora de la recolección
    private String observaciones;       // Notas adicionales
    private String tipoResiduo;         // Tipo de residuo del contenedor (JOIN)

    // =========================================================================
    // CONSTRUCTORES
    // =========================================================================

    /** Constructor vacío */
    public Recoleccion() {
    }

    /**
     * Constructor completo.
     *
     * @param id                 ID del registro de recolección
     * @param contenedorId       ID del contenedor
     * @param contenedorNombre   Nombre del contenedor
     * @param codigoBarras       Código de barras del contenedor
     * @param usuarioId          ID del usuario operador
     * @param usuarioNombre      Nombre del operador
     * @param pesoRecolectado    Peso recolectado en kg
     * @param fecha              Fecha y hora de la recolección
     * @param observaciones      Observaciones adicionales
     * @param tipoResiduo        Tipo de residuo del contenedor
     */
    public Recoleccion(int id, int contenedorId, String contenedorNombre,
                       String codigoBarras, int usuarioId, String usuarioNombre,
                       double pesoRecolectado, LocalDateTime fecha,
                       String observaciones, String tipoResiduo) {
        this.id = id;
        this.contenedorId = contenedorId;
        this.contenedorNombre = contenedorNombre;
        this.codigoBarras = codigoBarras;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.pesoRecolectado = pesoRecolectado;
        this.fecha = fecha;
        this.observaciones = observaciones;
        this.tipoResiduo = tipoResiduo;
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

    public int getContenedorId() {
        return contenedorId;
    }

    public void setContenedorId(int contenedorId) {
        this.contenedorId = contenedorId;
    }

    public String getContenedorNombre() {
        return contenedorNombre;
    }

    public void setContenedorNombre(String contenedorNombre) {
        this.contenedorNombre = contenedorNombre;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
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

    public double getPesoRecolectado() {
        return pesoRecolectado;
    }

    public void setPesoRecolectado(double pesoRecolectado) {
        this.pesoRecolectado = pesoRecolectado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getTipoResiduo() {
        return tipoResiduo;
    }

    public void setTipoResiduo(String tipoResiduo) {
        this.tipoResiduo = tipoResiduo;
    }

    // =========================================================================
    // REPRESENTACIÓN EN TEXTO
    // =========================================================================

    @Override
    public String toString() {
        return String.format("Recolección #%d: %.2f kg de '%s' por %s (%s)",
                id, pesoRecolectado, contenedorNombre, usuarioNombre,
                fecha != null ? fecha.toString() : "sin fecha");
    }
}
