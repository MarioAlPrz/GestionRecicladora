package greentrack.model;

import java.time.LocalDateTime;

/**
 * Entidad Contenedor: Cada contenedor almacena un solo tipo de residuo,
 * tiene un código de barras único, capacidad máxima en kg y peso actual.
 */
public class Contenedor {

    private int id;
    private String codigoBarras;
    private String nombre;
    private int tipoId;
    private String tipoNombre;
    private int ubicacionId;
    private String ubicacionNombre;
    private double capacidadKg;
    private double pesoActualKg;
    private String estado; // ACTIVO / MANTENIMIENTO / INACTIVO
    private LocalDateTime fechaRegistro;
    private double porcentajeLlenado;

    public Contenedor() {
    }

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

    public double calcularPorcentajeLlenado() {
        if (capacidadKg <= 0) return 0.0;
        return (pesoActualKg / capacidadKg) * 100.0;
    }

    public double getCapacidadDisponible() {
        return Math.max(0.0, capacidadKg - pesoActualKg);
    }

    public boolean estaActivo() {
        return "ACTIVO".equalsIgnoreCase(estado);
    }

    @Override
    public String toString() {
        return nombre + " [" + codigoBarras + "] (" + tipoNombre + ")";
    }
}
