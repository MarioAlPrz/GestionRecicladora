package greentrack.util;

import java.awt.Component;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;

/**
 * Utilidades de validación, formateo de números y fechas, y diálogos en español.
 */
public class Validaciones {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat FORMATO_DECIMAL =
            new DecimalFormat("#,##0.00");

    private Validaciones() {
    }

    public static boolean esTextoVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    public static boolean esNumeroPositivo(String texto) {
        if (esTextoVacio(texto)) return false;
        try {
            int n = Integer.parseInt(texto.trim());
            return n > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean esDecimalPositivo(String texto) {
        if (esTextoVacio(texto)) return false;
        try {
            double d = Double.parseDouble(texto.trim());
            return d > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean esCodigoBarrasValido(String codigo) {
        if (esTextoVacio(codigo)) return false;
        return codigo.trim().matches("^[A-Za-z0-9\\-_]{3,50}$");
    }

    public static void mostrarError(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error del Sistema", JOptionPane.ERROR_MESSAGE);
    }

    public static void mostrarExito(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        int r = JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar Operación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }

    public static String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) return "—";
        return fecha.format(FORMATO_FECHA);
    }

    public static String formatearDecimal(double valor) {
        return FORMATO_DECIMAL.format(valor);
    }
}
