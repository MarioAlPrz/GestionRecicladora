// ============================================================================
// Ubicacion.java — Modelo que representa una ubicación de contenedores
// ============================================================================
// Corresponde a la tabla 'ubicaciones' en la base de datos green_track.
// Cada ubicación es un punto físico donde se colocan contenedores.
// ============================================================================

/**
 * Modelo de datos para las ubicaciones donde se instalan contenedores.
 * Puede representar una sucursal, zona, o punto de recolección.
 */
public class Ubicacion {

    // --- Atributos ---
    private int id;
    private String nombre;
    private String descripcion;
    private String referencia;  // Referencia geográfica o dirección
    private boolean estado;     // true = activa, false = inactiva

    // =========================================================================
    // CONSTRUCTORES
    // =========================================================================

    /** Constructor vacío */
    public Ubicacion() {
    }

    /**
     * Constructor completo.
     *
     * @param id          ID de la ubicación
     * @param nombre      Nombre de la ubicación (ej: "Planta Norte")
     * @param descripcion Descripción del lugar
     * @param referencia  Referencia o dirección física
     * @param estado      true si está activa
     */
    public Ubicacion(int id, String nombre, String descripcion,
                     String referencia, boolean estado) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.referencia = referencia;
        this.estado = estado;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    // =========================================================================
    // REPRESENTACIÓN EN TEXTO
    // =========================================================================

    /** Retorna el nombre: útil para JComboBox */
    @Override
    public String toString() {
        return nombre;
    }
}
