// ============================================================================
// SesionUsuario.java — Gestión de la sesión del usuario autenticado
// ============================================================================
// Singleton estático que almacena los datos del usuario que inició sesión.
// Permite verificar autenticación y permisos desde cualquier parte del sistema.
// ============================================================================

/**
 * Clase estática que mantiene la información de la sesión activa.
 * Solo puede haber un usuario autenticado a la vez.
 */
public class SesionUsuario {

    // --- Datos de la sesión ---
    private static int id = -1;
    private static String nombreCompleto = "";
    private static String username = "";
    private static String rol = "";

    // --- Constantes de roles ---
    private static final String ROL_ADMINISTRADOR = "Administrador";
    private static final String ROL_OPERADOR = "Operador";
    private static final String ROL_CONSULTA = "Consulta";

    // Constructor privado: no se debe instanciar
    private SesionUsuario() {
    }

    // =========================================================================
    // GESTIÓN DE SESIÓN
    // =========================================================================

    /**
     * Inicia la sesión almacenando los datos del usuario autenticado.
     *
     * @param idUsuario       ID del usuario en la base de datos
     * @param nombre          Nombre completo del usuario
     * @param nombreUsuario   Nombre de usuario (login)
     * @param rolUsuario      Rol asignado al usuario
     */
    public static void iniciarSesion(int idUsuario, String nombre,
                                     String nombreUsuario, String rolUsuario) {
        id = idUsuario;
        nombreCompleto = nombre != null ? nombre : "";
        username = nombreUsuario != null ? nombreUsuario : "";
        rol = rolUsuario != null ? rolUsuario : "";
        System.out.println("[SesionUsuario] Sesión iniciada para: " + username + " (" + rol + ")");
    }

    /**
     * Cierra la sesión actual, limpiando todos los datos almacenados.
     */
    public static void cerrarSesion() {
        System.out.println("[SesionUsuario] Sesión cerrada para: " + username);
        id = -1;
        nombreCompleto = "";
        username = "";
        rol = "";
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    /** Retorna el ID del usuario autenticado, o -1 si no hay sesión */
    public static int getUsuarioId() {
        return id;
    }

    /** Retorna el nombre completo del usuario */
    public static String getNombreCompleto() {
        return nombreCompleto;
    }

    /** Retorna el nombre de usuario (login) */
    public static String getUsername() {
        return username;
    }

    /** Retorna el rol del usuario */
    public static String getRol() {
        return rol;
    }

    // =========================================================================
    // VERIFICACIÓN DE AUTENTICACIÓN
    // =========================================================================

    /**
     * Verifica si hay un usuario autenticado actualmente.
     *
     * @return true si hay una sesión activa
     */
    public static boolean estaAutenticado() {
        return id > 0 && !username.isEmpty();
    }

    // =========================================================================
    // VERIFICACIÓN DE ROLES
    // =========================================================================

    /**
     * Verifica si el usuario actual es Administrador.
     *
     * @return true si el rol es "Administrador"
     */
    public static boolean esAdministrador() {
        return ROL_ADMINISTRADOR.equalsIgnoreCase(rol);
    }

    /**
     * Verifica si el usuario actual es Operador.
     *
     * @return true si el rol es "Operador"
     */
    public static boolean esOperador() {
        return ROL_OPERADOR.equalsIgnoreCase(rol);
    }

    /**
     * Verifica si el usuario actual tiene rol de Consulta.
     *
     * @return true si el rol es "Consulta"
     */
    public static boolean esConsulta() {
        return ROL_CONSULTA.equalsIgnoreCase(rol);
    }

    /**
     * Verifica si el usuario tiene un permiso específico basado en su rol.
     * Permisos soportados:
     *   - "crear", "editar", "eliminar" → solo Administrador
     *   - "registrar_recoleccion"       → Administrador u Operador
     *   - "consultar", "ver_reportes"   → todos los roles
     *   - "gestionar_usuarios"          → solo Administrador
     *
     * @param permiso Nombre del permiso a verificar
     * @return true si el usuario tiene el permiso solicitado
     */
    public static boolean tienePermiso(String permiso) {
        if (!estaAutenticado() || permiso == null) {
            return false;
        }

        String permisoLower = permiso.toLowerCase().trim();

        return switch (permisoLower) {
            // --- Permisos exclusivos de Administrador ---
            case "crear", "editar", "eliminar", "gestionar_usuarios" ->
                    esAdministrador();

            // --- Permisos de operación (Admin + Operador) ---
            case "registrar_recoleccion", "escanear" ->
                    esAdministrador() || esOperador();

            // --- Permisos de lectura (todos los roles) ---
            case "consultar", "ver_reportes", "ver_dashboard" ->
                    true;

            // --- Permiso desconocido: denegar por defecto ---
            default -> {
                System.out.println("[SesionUsuario] Permiso desconocido: " + permiso);
                yield false;
            }
        };
    }

    /**
     * Retorna una representación legible de la sesión actual.
     * Útil para depuración.
     */
    public static String infoSesion() {
        if (!estaAutenticado()) {
            return "Sin sesión activa";
        }
        return String.format("Usuario: %s | Nombre: %s | Rol: %s | ID: %d",
                username, nombreCompleto, rol, id);
    }
}
