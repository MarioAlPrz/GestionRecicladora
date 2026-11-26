package greentrack.model;

import java.time.LocalDateTime;

/**
 * Entidad Recoleccion: Registra un vaciado o recolección de peso de un contenedor por un operador.
 */
public class Recoleccion {

    private int id;
    private int contenedorId;
    private String contenedorNombre;
    private String codigoBarras;
    private int usuarioId;
    private String usuarioNombre;
    private double pesoRecolectado;
    private LocalDateTime fecha;
    private String observaciones;
    private String tipoResiduo;

    public Recoleccion() {
    }

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

    @Override
    public String toString() {
        return "Recolección #" + id + ": " + pesoRecolectado + " kg de " + contenedorNombre;
    }
}
