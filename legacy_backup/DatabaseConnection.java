// ============================================================================
// DatabaseConnection.java — Administrador de conexiones JDBC (Singleton)
// ============================================================================
// Gestiona una única conexión a la base de datos MySQL usando el patrón
// Singleton. Después de cada conexión exitosa, establece la variable de
// sesión @usuario_id para que los triggers de auditoría funcionen.
// ============================================================================

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Administrador de conexiones a la base de datos.
 * Implementa el patrón Singleton para reutilizar la conexión activa.
 */
public class DatabaseConnection {

    // --- Instancia única (Singleton) ---
    private static Connection conexion = null;

    // Constructor privado para evitar instanciación directa
    private DatabaseConnection() {
    }

    /**
     * Obtiene la conexión activa a la base de datos.
     * Si no existe o está cerrada, crea una nueva conexión y establece
     * la variable de sesión @usuario_id para los triggers de auditoría.
     *
     * @return Conexión activa a la base de datos
     * @throws SQLException Si no se puede establecer la conexión
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Verificar si la conexión existente sigue activa
            if (conexion == null || conexion.isClosed()) {
                // Cargar el driver de MySQL explícitamente (compatibilidad)
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    System.err.println("[DatabaseConnection] ERROR: No se encontró el driver de MySQL.");
                    System.err.println("  Asegúrese de tener mysql-connector-j en el classpath.");
                    throw new SQLException("Driver de MySQL no encontrado. " +
                            "Verifique que mysql-connector-j esté en el classpath.", e);
                }

                // Obtener credenciales desde la configuración
                String url = DatabaseConfig.getUrl();
                String usuario = DatabaseConfig.getUser();
                String contrasena = DatabaseConfig.getPassword();

                System.out.println("[DatabaseConnection] Conectando a: " + url);

                // Establecer la conexión
                conexion = DriverManager.getConnection(url, usuario, contrasena);

                // Configurar la variable de sesión @usuario_id
                // Esto es necesario para que los triggers de auditoría
                // registren qué usuario realizó la operación
                establecerUsuarioSesion();

                System.out.println("[DatabaseConnection] Conexión establecida exitosamente.");
            }
        } catch (SQLException e) {
            String mensajeError = construirMensajeError(e);
            System.err.println("[DatabaseConnection] " + mensajeError);
            throw new SQLException(mensajeError, e);
        }

        return conexion;
    }

    /**
     * Establece la variable de sesión @usuario_id en MySQL.
     * Los triggers de auditoría usan esta variable para saber qué
     * usuario realizó cada operación (INSERT, UPDATE, DELETE).
     */
    private static void establecerUsuarioSesion() {
        try {
            int usuarioId = SesionUsuario.getUsuarioId();
            if (usuarioId > 0) {
                Statement stmt = conexion.createStatement();
                stmt.execute("SET @usuario_id = " + usuarioId);
                stmt.close();
                System.out.println("[DatabaseConnection] Variable @usuario_id establecida: " + usuarioId);
            }
        } catch (SQLException e) {
            // No lanzar excepción aquí; la conexión sigue siendo válida
            System.err.println("[DatabaseConnection] Advertencia: No se pudo establecer @usuario_id: "
                    + e.getMessage());
        }
    }

    /**
     * Actualiza la variable @usuario_id en la conexión activa.
     * Llamar después de un login exitoso para que los triggers
     * registren al usuario correcto.
     */
    public static void actualizarUsuarioSesion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    establecerUsuarioSesion();
                }
            } catch (SQLException e) {
                System.err.println("[DatabaseConnection] Error al actualizar sesión: " + e.getMessage());
            }
        }
    }

    /**
     * Cierra la conexión activa a la base de datos.
     * Es importante llamar este método al cerrar la aplicación
     * para liberar recursos del servidor.
     */
    public static void closeConnection() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                conexion = null;
                System.out.println("[DatabaseConnection] Conexión cerrada correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Error al cerrar la conexión: " + e.getMessage());
        }
    }

    /**
     * Verifica si hay una conexión activa y válida.
     *
     * @return true si la conexión está activa
     */
    public static boolean estaConectado() {
        try {
            return conexion != null && !conexion.isClosed() && conexion.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Construye un mensaje de error descriptivo en español basado
     * en el código de error SQL.
     *
     * @param e Excepción SQL original
     * @return Mensaje de error legible en español
     */
    private static String construirMensajeError(SQLException e) {
        int codigo = e.getErrorCode();
        String sqlState = e.getSQLState();

        return switch (codigo) {
            case 0 -> "No se pudo conectar al servidor de base de datos. "
                    + "Verifique que MySQL esté ejecutándose en "
                    + DatabaseConfig.getUrl();
            case 1045 -> "Acceso denegado. El usuario o la contraseña de la base de datos son incorrectos. "
                    + "Revise el archivo db.properties.";
            case 1049 -> "La base de datos 'green_track' no existe. "
                    + "Ejecute el script SQL para crearla.";
            case 1044 -> "El usuario no tiene permisos para acceder a la base de datos 'green_track'.";
            default -> String.format(
                    "Error de base de datos (Código: %d, Estado SQL: %s): %s",
                    codigo, sqlState, e.getMessage()
            );
        };
    }
}
