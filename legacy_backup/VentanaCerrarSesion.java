import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Cuadro de diálogo para confirmar el cierre de sesión.
 * Limpia la sesión actual del usuario, cierra la ventana principal
 * y vuelve a desplegar la ventana de login.
 */
public class VentanaCerrarSesion {

    public VentanaCerrarSesion(JFrame owner) {
        confirmarYCerrar(owner);
    }

    public VentanaCerrarSesion(Window owner) {
        confirmarYCerrar(owner);
    }

    public VentanaCerrarSesion() {
        confirmarYCerrar(null);
    }

    private void confirmarYCerrar(Window owner) {
        int opcion = JOptionPane.showConfirmDialog(
                owner,
                "¿Está seguro de que desea cerrar la sesión actual de GreenTrack?",
                "Confirmar Cierre de Sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (opcion == JOptionPane.YES_OPTION) {
            // Limpiar datos de la sesión
            SesionUsuario.cerrarSesion();

            // Cerrar la ventana que lo invocó
            if (owner != null) {
                owner.dispose();
            }

            // Regresar al login
            SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
        }
    }
}
