package greentrack.model;

/**
 * Entidad Ubicacion: Ubicación física o zona donde se colocan los contenedores.
 */
public class Ubicacion {

    private int id;
    private String nombre;
    private String descripcion;
    private String referencia;
    private boolean estado; // true = ACTIVO, false = INACTIVO

    public Ubicacion() {
    }

    public Ubicacion(int id, String nombre, String descripcion, String referencia, boolean estado) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.referencia = referencia;
        this.estado = estado;
    }

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

    @Override
    public String toString() {
        return nombre;
    }
}
