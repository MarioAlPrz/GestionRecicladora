// ============================================================================
// TipoResiduo.java — Modelo que representa un tipo de residuo reciclable
// ============================================================================
// Corresponde a la tabla 'tipos_residuo' en la base de datos green_track.
// Ejemplos: Plástico, Vidrio, Cartón, Metal, Orgánico.
// ============================================================================

/**
 * Modelo de datos para los tipos de residuos que maneja la recicladora.
 */
public class TipoResiduo {

    // --- Atributos ---
    private int id;
    private String nombre;
    private String descripcion;
    private boolean estado;     // true = activo, false = inactivo

    // =========================================================================
    // CONSTRUCTORES
    // =========================================================================

    /** Constructor vacío */
    public TipoResiduo() {
    }

    /**
     * Constructor completo.
     *
     * @param id          ID del tipo de residuo
     * @param nombre      Nombre del tipo (ej: "Plástico PET")
     * @param descripcion Descripción detallada
     * @param estado      true si está activo
     */
    public TipoResiduo(int id, String nombre, String descripcion, boolean estado) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
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
