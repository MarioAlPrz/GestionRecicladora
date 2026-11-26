// ============================================================================
// Contenedor.java — Modelo que representa un contenedor de reciclaje
// ============================================================================
// Corresponde a la tabla 'contenedores' en la base de datos green_track.
// Cada contenedor tiene un código de barras, tipo de residuo asignado,
// ubicación, capacidad y peso actual para calcular el llenado.
// ============================================================================

import java.time.LocalDateTime;

/**
 * Modelo de datos para los contenedores de la recicladora.
 * Incluye datos de capacidad y cálculo de porcentaje de llenado.
 */
public class Contenedor {

    // --- Atributos ---
    private int id;
    private String codigoBarras;        // Código de barras único
    private String nombre;              // Nombre descriptivo del contenedor
    private int tipoId;                 // FK → tipos_residuo
    private String tipoNombre;          // Nombre del tipo de residuo (JOIN)
    private int ubicacionId;            // FK → ubicaciones
    private String ubicacionNombre;     // Nombre de la ubicación (JOIN)
    private double capacidadKg;         // Capacidad máxima en kilogramos
    private double pesoActualKg;        // Peso actual del contenido en kg
    private String estado;              // "activo", "lleno", "mantenimiento", etc.
    private LocalDateTime fechaRegistro;
    private double porcentajeLlenado;   // Calculado: (pesoActual/capacidad)*100

    // =========================================================================
    // CONSTRUCTORES
    // =========================================================================

    /** Constructor vacío */
    public Contenedor() {
    }

    /**
     * Constructor completo con todos los campos.
     *
     * @param id                 ID del contenedor
     * @param codigoBarras       Código de barras único
     * @param nombre             Nombre descriptivo
     * @param tipoId             ID del tipo de residuo
     * @param tipoNombre         Nombre del tipo de residuo
     * @param ubicacionId        ID de la ubicación
     * @param ubicacionNombre    Nombre de la ubicación
     * @param capacidadKg        Capacidad máxima en kg
     * @param pesoActualKg       Peso actual en kg
     * @param estado             Estado del contenedor
     * @param fechaRegistro      Fecha de registro
     * @param porcentajeLlenado  Porcentaje de llenado calculado
     */
    public Contenedor(int id, String codigoBarras, String nombre,
                      int tipoId, String tipoNombre,
                      int ubicacionId, String ubicacionNombre,
                      double capacidadKg, double pesoActualKg,
                      String estado, LocalDateTime fechaRegistro,
                      double porcentajeLlenado) {
        this.id = id;
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.tipoId = tipoId;
        this.tipoNombre = tipoNombre;
        this.ubicacionId = ubicacionId;
        this.ubicacionNombre = ubicacionNombre;
        this.capacidadKg = capacidadKg;
        this.pesoActualKg = pesoActualKg;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
        this.porcentajeLlenado = porcentajeLlenado;
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

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTipoId() {
        return tipoId;
    }

    public void setTipoId(int tipoId) {
        this.tipoId = tipoId;
    }

    public String getTipoNombre() {
        return tipoNombre;
    }

    public void setTipoNombre(String tipoNombre) {
        this.tipoNombre = tipoNombre;
    }

    public int getUbicacionId() {
        return ubicacionId;
    }

    public void setUbicacionId(int ubicacionId) {
        this.ubicacionId = ubicacionId;
    }

    public String getUbicacionNombre() {
        return ubicacionNombre;
    }

    public void setUbicacionNombre(String ubicacionNombre) {
        this.ubicacionNombre = ubicacionNombre;
    }

    public double getCapacidadKg() {
        return capacidadKg;
    }

    public void setCapacidadKg(double capacidadKg) {
        this.capacidadKg = capacidadKg;
    }

    public double getPesoActualKg() {
        return pesoActualKg;
    }

    public void setPesoActualKg(double pesoActualKg) {
        this.pesoActualKg = pesoActualKg;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public double getPorcentajeLlenado() {
        return porcentajeLlenado;
    }

    public void setPorcentajeLlenado(double porcentajeLlenado) {
        this.porcentajeLlenado = porcentajeLlenado;
    }

    // =========================================================================
    // MÉTODOS DE UTILIDAD
    // =========================================================================

    /**
     * Calcula el porcentaje de llenado basado en peso actual y capacidad.
     *
     * @return Porcentaje de llenado (0.0 a 100.0+)
     */
    public double calcularPorcentajeLlenado() {
        if (capacidadKg <= 0) return 0.0;
        return (pesoActualKg / capacidadKg) * 100.0;
    }

    /** Verifica si el contenedor está activo */
    public boolean estaActivo() {
        return "activo".equalsIgnoreCase(estado);
    }

    /** Verifica si el contenedor está lleno (≥ 90%) */
    public boolean estaLleno() {
        return calcularPorcentajeLlenado() >= 90.0;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s [%.1f%%]",
                nombre, codigoBarras, tipoNombre, calcularPorcentajeLlenado());
    }
}
