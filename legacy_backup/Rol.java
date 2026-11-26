// ============================================================================
// Rol.java — Modelo que representa un rol de usuario en el sistema
// ============================================================================
// Corresponde a la tabla 'roles' en la base de datos green_track.
// Ejemplos de roles: Administrador, Operador, Consulta.
// ============================================================================

/**
 * Modelo de datos para los roles del sistema.
 * Define los permisos generales que tiene cada usuario.
 */
public class Rol {

    // --- Atributos ---
    private int id;
    private String nombre;
    private String descripcion;

    // =========================================================================
    // CONSTRUCTORES
    // =========================================================================

    /** Constructor vacío (necesario para instanciación desde ResultSet) */
    public Rol() {
    }

    /**
     * Constructor completo.
     *
     * @param id          ID del rol en la base de datos
     * @param nombre      Nombre del rol (ej: "Administrador")
     * @param descripcion Descripción de las funciones del rol
     */
    public Rol(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
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

    // =========================================================================
    // REPRESENTACIÓN EN TEXTO
    // =========================================================================

    @Override
    public String toString() {
        return nombre; // Útil para mostrar en JComboBox directamente
    }
}
