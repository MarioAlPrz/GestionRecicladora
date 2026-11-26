package greentrack.config;

import greentrack.util.SesionUsuario;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Administrador central de la conexión JDBC a MySQL.
 * Proporciona conexiones seguras para cada operación y sincroniza
 * la variable de sesión @usuario_id para los triggers de auditoría.
 */
public class DatabaseConnection {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {
            }
        }
    }

    private DatabaseConnection() {
    }

    /**
     * Obtiene una conexión activa a MySQL.
     * Cada llamada devuelve una conexión independiente para evitar condiciones de carrera entre hilos.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword()
        );

        // Sincronizar el usuario de sesión para los triggers de auditoría
        int uid = SesionUsuario.getUsuarioId();
        try (Statement st = conn.createStatement()) {
            if (uid > 0) {
                st.execute("SET @usuario_id = " + uid);
            } else {
                st.execute("SET @usuario_id = NULL");
            }
        } catch (SQLException ignored) {
        }

        return conn;
    }

    /**
     * Mantenido por compatibilidad.
     */
    public static void actualizarVariableSesionMySQL() {
    }

    /**
     * Mantenido por compatibilidad.
     */
    public static void closeConnection() {
    }
}
