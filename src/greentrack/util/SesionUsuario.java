package greentrack.util;

/**
 * Gestión de la sesión del usuario autenticado en GreenTrack.
 * Almacena de forma estática los datos del usuario en sesión,
 * permitiendo validar roles y permisos en cualquier punto del sistema.
 */
public class SesionUsuario {

    private static int id = -1;
    private static String nombreCompleto = "";
    private static String username = "";
    private static String rol = "";

    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    public static final String ROL_OPERADOR = "OPERADOR";
    public static final String ROL_CONSULTA = "CONSULTA";

    private SesionUsuario() {
    }

    public static void iniciarSesion(int idUsuario, String nombre, String nombreUsuario, String rolUsuario) {
        id = idUsuario;
        nombreCompleto = nombre != null ? nombre : "";
        username = nombreUsuario != null ? nombreUsuario : "";
        rol = rolUsuario != null ? rolUsuario.trim().toUpperCase() : "";
    }

    public static void cerrarSesion() {
        id = -1;
        nombreCompleto = "";
        username = "";
        rol = "";
    }

    public static int getUsuarioId() {
        return id;
    }

    public static String getNombreCompleto() {
        return nombreCompleto;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRol() {
        return rol;
    }

    public static boolean estaAutenticado() {
        return id > 0 && !username.isEmpty();
    }

    public static boolean esAdministrador() {
        return ROL_ADMINISTRADOR.equalsIgnoreCase(rol);
    }

    public static boolean esOperador() {
        return ROL_OPERADOR.equalsIgnoreCase(rol);
    }

    public static boolean esConsulta() {
        return ROL_CONSULTA.equalsIgnoreCase(rol);
    }

    public static boolean puedeModificar() {
        return esAdministrador() || esOperador();
    }
}
