package greentrack.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Clase que centraliza la configuración de la conexión JDBC a MySQL.
 * Busca las credenciales en 'resources/db.properties', 'config/db.properties' o en el directorio actual.
 * Si no encuentra el archivo, utiliza valores por defecto seguros para entorno local.
 */
public class DatabaseConfig {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/green_track?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static String url;
    private static String user;
    private static String password;

    static {
        cargarConfiguracion();
    }

    private static void cargarConfiguracion() {
        Properties props = new Properties();
        boolean cargado = false;

        String[] posiblesRutas = {
            "resources/db.properties",
            "config/db.properties",
            "db.properties",
            "GestionRecicladora/resources/db.properties",
            "GestionRecicladora/db.properties"
        };

        for (String ruta : posiblesRutas) {
            File f = new File(ruta);
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    props.load(fis);
                    cargado = true;
                    break;
                } catch (Exception ignored) {
                }
            }
        }

        if (!cargado) {
            try (InputStream is = DatabaseConfig.class.getResourceAsStream("/db.properties")) {
                if (is != null) {
                    props.load(is);
                    cargado = true;
                }
            } catch (Exception ignored) {
            }
        }

        url = props.getProperty("db.url", DEFAULT_URL);
        user = props.getProperty("db.user", DEFAULT_USER);
        password = props.getProperty("db.password", DEFAULT_PASSWORD);
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static String getPassword() {
        return password;
    }
}
