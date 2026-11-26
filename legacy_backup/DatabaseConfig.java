// ============================================================================
// DatabaseConfig.java — Cargador de configuración de base de datos
// ============================================================================
// Lee las credenciales de conexión desde el archivo db.properties.
// Si el archivo no existe, usa valores predeterminados para desarrollo local.
// ============================================================================

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Clase utilitaria que centraliza la configuración de la base de datos.
 * Carga las credenciales desde db.properties o usa valores por defecto.
 */
public class DatabaseConfig {

    // --- Valores por defecto para desarrollo local ---
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/green_track?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    // --- Propiedades cargadas ---
    private static String url;
    private static String user;
    private static String password;

    // Bloque estático: se ejecuta una sola vez al cargar la clase
    static {
        cargarConfiguracion();
    }

    /**
     * Carga la configuración desde db.properties.
     * Busca el archivo en el mismo directorio que la clase compilada,
     * luego en el classpath, y finalmente usa valores por defecto.
     */
    private static void cargarConfiguracion() {
        Properties props = new Properties();
        boolean cargado = false;

        // Intento 1: buscar db.properties en el directorio de la clase
        try {
            // Obtener el directorio donde se encuentra esta clase
            Path directorioClase = Paths.get(
                    DatabaseConfig.class.getProtectionDomain()
                            .getCodeSource().getLocation().toURI()
            );
            // Si es un archivo .jar o directorio, resolver la ruta
            if (Files.isDirectory(directorioClase)) {
                Path archivoProp = directorioClase.resolve("db.properties");
                if (Files.exists(archivoProp)) {
                    try (InputStream is = new FileInputStream(archivoProp.toFile())) {
                        props.load(is);
                        cargado = true;
                        System.out.println("[DatabaseConfig] Configuración cargada desde: " + archivoProp);
                    }
                }
            }
        } catch (Exception e) {
            // Si falla la resolución de ruta, continuar con otros métodos
        }

        // Intento 2: buscar en el directorio de trabajo actual
        if (!cargado) {
            Path archivoPropLocal = Paths.get("db.properties");
            if (Files.exists(archivoPropLocal)) {
                try (InputStream is = new FileInputStream(archivoPropLocal.toFile())) {
                    props.load(is);
                    cargado = true;
                    System.out.println("[DatabaseConfig] Configuración cargada desde directorio actual.");
                } catch (IOException e) {
                    System.err.println("[DatabaseConfig] Error al leer db.properties: " + e.getMessage());
                }
            }
        }

        // Intento 3: buscar en el classpath
        if (!cargado) {
            try (InputStream is = DatabaseConfig.class.getResourceAsStream("/db.properties")) {
                if (is != null) {
                    props.load(is);
                    cargado = true;
                    System.out.println("[DatabaseConfig] Configuración cargada desde el classpath.");
                }
            } catch (IOException e) {
                System.err.println("[DatabaseConfig] Error al leer desde classpath: " + e.getMessage());
            }
        }

        // Asignar valores: propiedades cargadas o valores por defecto
        if (cargado) {
            url = props.getProperty("db.url", DEFAULT_URL);
            user = props.getProperty("db.user", DEFAULT_USER);
            password = props.getProperty("db.password", DEFAULT_PASSWORD);
        } else {
            System.out.println("[DatabaseConfig] No se encontró db.properties. Usando valores por defecto.");
            url = DEFAULT_URL;
            user = DEFAULT_USER;
            password = DEFAULT_PASSWORD;
        }
    }

    // --- Getters estáticos ---

    /** Retorna la URL de conexión JDBC */
    public static String getUrl() {
        return url;
    }

    /** Retorna el nombre de usuario de la base de datos */
    public static String getUser() {
        return user;
    }

    /** Retorna la contraseña de la base de datos */
    public static String getPassword() {
        return password;
    }

    /**
     * Fuerza la recarga de la configuración.
     * Útil si se modifica db.properties en tiempo de ejecución.
     */
    public static void recargar() {
        cargarConfiguracion();
    }

    // Constructor privado para evitar instanciación
    private DatabaseConfig() {
    }
}
