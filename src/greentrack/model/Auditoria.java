package greentrack.model;

import java.time.LocalDateTime;

/**
 * Entidad Auditoria: Rastreabilidad de operaciones INSERT, UPDATE, DELETE generadas por triggers.
 */
public class Auditoria {

    private int id;
    private Integer usuarioId;
    private String usuarioNombre;
    private String tablaAfectada;
    private String tipoOperacion; // INSERT, UPDATE, DELETE
    private LocalDateTime fecha;
    private String datosAnteriores; // JSON
    private String datosNuevos;     // JSON

    public Auditoria() {
    }

    public Auditoria(int id, Integer usuarioId, String usuarioNombre,
                     String tablaAfectada, String tipoOperacion,
                     LocalDateTime fecha, String datosAnteriores, String datosNuevos) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.tablaAfectada = tablaAfectada;
        this.tipoOperacion = tipoOperacion;
        this.fecha = fecha;
        this.datosAnteriores = datosAnteriores;
        this.datosNuevos = datosNuevos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(String tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getDatosAnteriores() {
        return datosAnteriores;
    }

    public void setDatosAnteriores(String datosAnteriores) {
        this.datosAnteriores = datosAnteriores;
    }

    public String getDatosNuevos() {
        return datosNuevos;
    }

    public void setDatosNuevos(String datosNuevos) {
        this.datosNuevos = datosNuevos;
    }

    @Override
    public String toString() {
        return "[" + tipoOperacion + "] en " + tablaAfectada + " por " + usuarioNombre;
    }
}
