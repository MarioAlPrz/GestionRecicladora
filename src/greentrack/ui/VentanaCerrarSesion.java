package greentrack.ui;

import greentrack.service.AuthService;
import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Cuadro de diálogo para confirmar el cierre de sesión.
 */
public class VentanaCerrarSesion {

    private final AuthService authService = new AuthService();

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
            authService.cerrarSesion();
            if (owner != null) {
                owner.dispose();
            }
            SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
        }
    }
}
