package greentrack.service;

import greentrack.dao.ReporteDAO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Servicio para la generación de reportes y KPIs para el dashboard.
 */
public class ReporteService {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    public List<Map<String, Object>> generarReporteRecoleccionesPorTipo(LocalDate inicio, LocalDate fin) {
        return reporteDAO.reporteRecoleccionesPorTipo(inicio, fin);
    }

    public List<Map<String, Object>> generarReporteEstadoContenedores(Integer tipoId, Integer ubicacionId) {
        return reporteDAO.reporteEstadoContenedores(tipoId, ubicacionId);
    }

    public double obtenerTotalKg() {
        return reporteDAO.totalKgRecolectados();
    }

    public Map<String, Double> obtenerTotalesParaGrafico() {
        return reporteDAO.obtenerTotalesPorTipoParaGrafico();
    }
}
