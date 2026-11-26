// ============================================================================
// Validaciones.java — Clase utilitaria de validación y mensajes
// ============================================================================
// Proporciona métodos estáticos para validar entradas del usuario,
// mostrar mensajes con JOptionPane y formatear datos para la interfaz.
// ============================================================================

import java.awt.Component;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;

/**
 * Utilidades de validación, mensajes y formato para toda la aplicación.
 * Todos los mensajes se muestran en español.
 */
public class Validaciones {

    // --- Formateadores reutilizables ---
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat FORMATO_DECIMAL =
            new DecimalFormat("#,##0.00");

    // Constructor privado: clase utilitaria, no se instancia
    private Validaciones() {
    }

    // =========================================================================
    // VALIDACIONES DE TEXTO
    // =========================================================================

    /**
     * Verifica si un texto es nulo o está vacío (solo espacios en blanco).
     *
     * @param texto Texto a validar
     * @return true si el texto es nulo, vacío o solo tiene espacios
     */
    public static boolean esTextoVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    // =========================================================================
    // VALIDACIONES NUMÉRICAS
    // =========================================================================

    /**
     * Verifica si un texto representa un número entero positivo.
     *
     * @param texto Texto a validar
     * @return true si es un número entero mayor que cero
     */
    public static boolean esNumeroPositivo(String texto) {
        if (esTextoVacio(texto)) return false;
        try {
            int numero = Integer.parseInt(texto.trim());
            return numero > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Verifica si un texto representa un número decimal positivo.
     *
     * @param texto Texto a validar
     * @return true si es un número decimal mayor que cero
     */
    public static boolean esDecimalPositivo(String texto) {
        if (esTextoVacio(texto)) return false;
        try {
            double numero = Double.parseDouble(texto.trim());
            return numero > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // =========================================================================
    // VALIDACIONES ESPECÍFICAS DEL NEGOCIO
    // =========================================================================

    /**
     * Verifica si un código de barras tiene un formato válido.
     * Acepta códigos alfanuméricos de entre 4 y 50 caracteres.
     *
     * @param codigo Código de barras a validar
     * @return true si el código tiene un formato válido
     */
    public static boolean esCodigoBarrasValido(String codigo) {
        if (esTextoVacio(codigo)) return false;
        String codigoLimpio = codigo.trim();
        // Acepta letras, números y guiones, entre 4 y 50 caracteres
        return codigoLimpio.matches("^[A-Za-z0-9\\-]{4,50}$");
    }

    // =========================================================================
    // MENSAJES AL USUARIO (JOptionPane)
    // =========================================================================

    /**
     * Muestra un mensaje de error al usuario.
     *
     * @param padre   Componente padre para centrar el diálogo
     * @param mensaje Mensaje de error a mostrar
     */
    public static void mostrarError(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(
                padre,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Muestra un mensaje de éxito al usuario.
     *
     * @param padre   Componente padre para centrar el diálogo
     * @param mensaje Mensaje de éxito a mostrar
     */
    public static void mostrarExito(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(
                padre,
                mensaje,
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Muestra un diálogo de confirmación (Sí/No) al usuario.
     *
     * @param padre   Componente padre para centrar el diálogo
     * @param mensaje Pregunta a confirmar
     * @return true si el usuario seleccionó "Sí"
     */
    public static boolean confirmar(Component padre, String mensaje) {
        int respuesta = JOptionPane.showConfirmDialog(
                padre,
                mensaje,
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return respuesta == JOptionPane.YES_OPTION;
    }

    // =========================================================================
    // FORMATO DE DATOS
    // =========================================================================

    /**
     * Formatea una fecha y hora al formato "dd/MM/yyyy HH:mm".
     *
     * @param fecha Fecha a formatear
     * @return Cadena formateada o "—" si la fecha es nula
     */
    public static String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) return "—";
        return fecha.format(FORMATO_FECHA);
    }

    /**
     * Formatea un valor decimal con separador de miles y dos decimales.
     * Ejemplo: 1234.5 → "1,234.50"
     *
     * @param valor Valor numérico a formatear
     * @return Cadena formateada
     */
    public static String formatearDecimal(double valor) {
        return FORMATO_DECIMAL.format(valor);
    }
}
