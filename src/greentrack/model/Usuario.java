package greentrack.model;

import java.time.LocalDateTime;

/**
 * Entidad Usuario: Representa las cuentas de acceso al sistema con contraseñas hasheadas en BCrypt.
 */
public class Usuario {

    private int id;
    private String nombreCompleto;
    private String username;
    private String contrasena;
    private int rolId;
    private String rolNombre;
    private String estado; // ACTIVO / INACTIVO
    private LocalDateTime fechaCreacion;

    public Usuario() {
    }

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

    public boolean estaActivo() {
        return "ACTIVO".equalsIgnoreCase(estado);
    }

    @Override
    public String toString() {
        return nombreCompleto + " (" + username + ") - " + rolNombre;
    }
}
