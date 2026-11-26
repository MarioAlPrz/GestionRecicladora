package greentrack.service;

import greentrack.dao.ContenedorDAO;
import greentrack.dao.RecoleccionDAO;
import greentrack.model.Contenedor;
import greentrack.model.Recoleccion;
import greentrack.util.SesionUsuario;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de Recolección con lógica transaccional y validaciones estrictas.
 */
public class RecoleccionService {

    private final RecoleccionDAO recoleccionDAO = new RecoleccionDAO();
    private final ContenedorDAO contenedorDAO = new ContenedorDAO();

    /**
     * Registra una recolección asegurando consistencia transaccional y validaciones de negocio.
     */
    public String registrarRecoleccion(String codigoBarras, double pesoRecolectado, String observaciones) {
        if (!SesionUsuario.puedeModificar()) {
            return "Error: Su rol actual (" + SesionUsuario.getRol() + ") no tiene permisos para registrar recolecciones.";
        }

        if (pesoRecolectado <= 0) {
            return "Error: El peso recolectado debe ser mayor a 0 kg.";
        }

        Contenedor c = contenedorDAO.buscarPorCodigoBarras(codigoBarras);
        if (c == null) {
            return "Error: No se encontró ningún contenedor con el código de barras proporcionado.";
        }

        if (!"ACTIVO".equalsIgnoreCase(c.getEstado())) {
            return "Error: El contenedor no está disponible para recolección (Estado: " + c.getEstado() + ").";
        }

        double disponible = c.getCapacidadDisponible();
        if (pesoRecolectado > disponible) {
            return String.format("Error: El peso ingresado (%.2f kg) excede la capacidad disponible del contenedor (%.2f kg).",
                    pesoRecolectado, disponible);
        }

        Recoleccion r = new Recoleccion();
        r.setContenedorId(c.getId());
        r.setUsuarioId(SesionUsuario.getUsuarioId());
        r.setPesoRecolectado(pesoRecolectado);
        r.setObservaciones(observaciones);

        try {
            // Intenta primero con el procedimiento almacenado sp_registrar_recoleccion
            String resSP = recoleccionDAO.registrar(r);
            if (resSP != null && !resSP.startsWith("Error")) {
                return "OK: " + resSP;
            }
            // Si el procedimiento devolvió mensaje de error de negocio
            if (resSP != null && resSP.startsWith("Error")) {
                return resSP;
            }
        } catch (Exception e) {
            System.err.println("Advertencia: sp_registrar_recoleccion falló, intentando con transacción manual JDBC: " + e.getMessage());
        }

        // Respaldo con transacción explícita JDBC y SELECT FOR UPDATE
        boolean exitoManual = recoleccionDAO.registrarConTransaccionManual(r);
        if (exitoManual) {
            return "OK: Recolección registrada exitosamente mediante transacción segura.";
        } else {
            return "Error: No se pudo completar la recolección. Se aplicó ROLLBACK para proteger la base de datos.";
        }
    }

    public List<Recoleccion> obtenerTodas() {
        return recoleccionDAO.listarTodas();
    }

    public List<Recoleccion> filtrar(LocalDate desde, LocalDate hasta, Integer tipoId) {
        return recoleccionDAO.filtrar(desde, hasta, tipoId);
    }

    public int contarHoy() {
        return recoleccionDAO.contarHoy();
    }

    public double totalRecolectadoHoy() {
        return recoleccionDAO.totalRecolectadoHoy();
    }
}
