// ============================================================================
// Usuario.java — Modelo que representa un usuario del sistema
// ============================================================================
// Corresponde a la tabla 'usuarios' en la base de datos green_track.
// Almacena datos de autenticación y el rol asignado.
// ============================================================================

import java.time.LocalDateTime;

/**
 * Modelo de datos para los usuarios del sistema GreenTrack.
 * Incluye credenciales, rol y estado de la cuenta.
 */
public class Usuario {

    // --- Atributos ---
    private int id;
    private String nombreCompleto;
    private String username;
    private String contrasena;      // Hash BCrypt, nunca texto plano
    private int rolId;
    private String rolNombre;       // Nombre del rol (JOIN con tabla roles)
    private String estado;          // "activo" o "inactivo"
    private LocalDateTime fechaCreacion;

    // =========================================================================
    // CONSTRUCTORES
    // =========================================================================

    /** Constructor vacío */
    public Usuario() {
    }

    /**
     * Constructor completo con todos los campos.
     *
     * @param id              ID del usuario
     * @param nombreCompleto  Nombre completo del usuario
     * @param username        Nombre de usuario para login
     * @param contrasena      Contraseña hasheada con BCrypt
     * @param rolId           ID del rol asignado
     * @param rolNombre       Nombre del rol (obtenido por JOIN)
     * @param estado          Estado de la cuenta ("activo"/"inactivo")
     * @param fechaCreacion   Fecha y hora de creación de la cuenta
     */
    public Usuario(int id, String nombreCompleto, String username, String contrasena,
                   int rolId, String rolNombre, String estado, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.username = username;
        this.contrasena = contrasena;
        this.rolId = rolId;
        this.rolNombre = rolNombre;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
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

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public int getRolId() {
        return rolId;
    }

    public void setRolId(int rolId) {
        this.rolId = rolId;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    // =========================================================================
    // MÉTODOS DE UTILIDAD
    // =========================================================================

    /** Verifica si la cuenta del usuario está activa */
    public boolean estaActivo() {
        return "activo".equalsIgnoreCase(estado);
    }

    @Override
    public String toString() {
        return String.format("Usuario{id=%d, nombre='%s', username='%s', rol='%s', estado='%s'}",
                id, nombreCompleto, username, rolNombre, estado);
    }
}
