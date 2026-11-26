package greentrack.service;

import greentrack.dao.ContenedorDAO;
import greentrack.model.Contenedor;
import greentrack.util.Validaciones;
import java.util.List;

/**
 * Servicio para la administración y búsqueda de contenedores.
 */
public class ContenedorService {

    private final ContenedorDAO contenedorDAO = new ContenedorDAO();

    public List<Contenedor> obtenerTodos() {
        return contenedorDAO.listarTodos();
    }

    public List<Contenedor> buscar(String filtro) {
        if (Validaciones.esTextoVacio(filtro)) {
            return contenedorDAO.listarTodos();
        }
        return contenedorDAO.buscar(filtro.trim());
    }

    public List<Contenedor> filtrar(Integer tipoId, Integer ubicacionId) {
        return contenedorDAO.filtrar(tipoId, ubicacionId);
    }

    public Contenedor buscarPorCodigoBarras(String codigoBarras) {
        if (Validaciones.esTextoVacio(codigoBarras)) {
            return null;
        }
        return contenedorDAO.buscarPorCodigoBarras(codigoBarras.trim());
    }

    public String guardarContenedor(Contenedor c, boolean esNuevo) {
        if (Validaciones.esTextoVacio(c.getNombre())) {
            return "El nombre del contenedor es obligatorio.";
        }
        if (!Validaciones.esCodigoBarrasValido(c.getCodigoBarras())) {
            return "El código de barras no tiene un formato válido (3 a 50 caracteres alfanuméricos).";
        }
        if (c.getCapacidadKg() <= 0) {
            return "La capacidad del contenedor debe ser mayor a 0 kg.";
        }
        if (c.getTipoId() <= 0) {
            return "Debe seleccionar un tipo de residuo válido.";
        }
        if (c.getUbicacionId() <= 0) {
            return "Debe seleccionar una ubicación válida.";
        }

        if (esNuevo) {
            if (contenedorDAO.existeCodigoBarras(c.getCodigoBarras())) {
                return "El código de barras ya se encuentra registrado en otro contenedor.";
            }
            boolean exito = contenedorDAO.insertar(c);
            return exito ? "OK" : "Error al registrar el contenedor en la base de datos.";
        } else {
            if (contenedorDAO.existeCodigoBarras(c.getCodigoBarras(), c.getId())) {
                return "El código de barras ya pertenece a otro contenedor registrado.";
            }
            boolean exito = contenedorDAO.actualizar(c);
            return exito ? "OK" : "Error al actualizar el contenedor. Verifique que la capacidad no sea menor al peso actual.";
        }
    }

    public boolean cambiarEstado(int id, String nuevoEstado) {
        return contenedorDAO.cambiarEstado(id, nuevoEstado);
    }

    public int contarActivos() {
        return contenedorDAO.contarActivos();
    }

    public int contarTotal() {
        return contenedorDAO.contarTotal();
    }

    public int contarProximosALlenarse(double porcentajeMinimo) {
        return contenedorDAO.contarProximosALlenarse(porcentajeMinimo);
    }
}
