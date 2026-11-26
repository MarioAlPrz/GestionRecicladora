package greentrack.service;

import greentrack.dao.UsuarioDAO;
import greentrack.model.Usuario;
import greentrack.util.SesionUsuario;
import greentrack.util.Validaciones;

/**
 * Servicio de Autenticación y Control de Acceso.
 */
public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario iniciarSesion(String username, String contrasena) {
        if (Validaciones.esTextoVacio(username) || Validaciones.esTextoVacio(contrasena)) {
            return null;
        }

        Usuario u = usuarioDAO.autenticar(username.trim(), contrasena);
        if (u != null) {
            SesionUsuario.iniciarSesion(
                    u.getId(),
                    u.getNombreCompleto(),
                    u.getUsername(),
                    u.getRolNombre()
            );
        }
        return u;
    }

    public void cerrarSesion() {
        SesionUsuario.cerrarSesion();
    }
}
