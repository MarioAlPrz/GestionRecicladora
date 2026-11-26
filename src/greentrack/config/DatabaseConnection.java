package greentrack.config;

import greentrack.util.SesionUsuario;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Administrador central de la conexión JDBC mediante el patrón Singleton.
 * Garantiza reutilización de la conexión y configuración de la variable de sesión
 * @usuario_id requerida por los triggers de auditoría de MySQL.
 */
public class DatabaseConnection {

    private static Connection conexion = null;

    private DatabaseConnection() {
    }

    /**
     * Obtiene una conexión activa a MySQL.
     * Si no existe o se cerró, establece una nueva.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                // Registrar driver MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                // Si no está el driver nuevo, intentar con el anterior
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                } catch (ClassNotFoundException ignored) {
                }
            }

            conexion = DriverManager.getConnection(
                    DatabaseConfig.getUrl(),
                    DatabaseConfig.getUser(),
                    DatabaseConfig.getPassword()
            );

            // Sincronizar usuario de sesión para triggers de auditoría
            actualizarVariableSesionMySQL();
        }
        return conexion;
    }

    /**
     * Actualiza la variable de sesión @usuario_id en MySQL para que los triggers
     * de auditoría registren correctamente qué usuario ejecutó la acción.
     */
    public static void actualizarVariableSesionMySQL() {
        if (conexion != null) {
            try (Statement st = conexion.createStatement()) {
                int uid = SesionUsuario.getUsuarioId();
                if (uid > 0) {
                    st.execute("SET @usuario_id = " + uid);
                } else {
                    st.execute("SET @usuario_id = NULL");
                }
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Cierra la conexión activa si existe.
     */
    public static synchronized void closeConnection() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión JDBC: " + e.getMessage());
            } finally {
                conexion = null;
            }
        }
    }
}
