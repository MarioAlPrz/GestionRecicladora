package greentrack;

import greentrack.model.*;
import greentrack.util.BCryptUtil;
import greentrack.util.SesionUsuario;
import greentrack.util.Validaciones;

/**
 * Suite de verificación y pruebas unitarias de los componentes del backend de GreenTrack.
 */
public class TestSuite {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  INICIANDO PRUEBAS UNITARIAS DEL SISTEMA GREENTRACK");
        System.out.println("=================================================================");

        int pruebasPasadas = 0;
        int totalPruebas = 0;

        // 1. Prueba de Hashing BCrypt
        totalPruebas++;
        System.out.print("[TEST 1] Generación y verificación de hash con BCryptUtil... ");
        String passPlano = "admin123";
        String nuevoHash = BCryptUtil.hashPassword(passPlano, 10);
        boolean hashValido = BCryptUtil.checkPassword(passPlano, nuevoHash);
        boolean hashInvalido = BCryptUtil.checkPassword("password_incorrecta", nuevoHash);

        if (hashValido && !hashInvalido && nuevoHash.startsWith("$2a$10$")) {
            System.out.println("CORRECTO (Hash: " + nuevoHash + ")");
            pruebasPasadas++;
        } else {
            System.out.println("FALLIDO");
        }

        // 2. Prueba del Hash de Base de Datos ($2a$10$sgvFEewTBpYyI3pQzlOx5.Lm2fHIXWbSJXYuNIkjAZ8DcL.zqZtAy)
        totalPruebas++;
        System.out.print("[TEST 2] Verificación de compatibilidad con hash SQL existente... ");
        String hashSQL = "$2a$10$sgvFEewTBpYyI3pQzlOx5.Lm2fHIXWbSJXYuNIkjAZ8DcL.zqZtAy";
        boolean matchAdmin = BCryptUtil.checkPassword("admin123", hashSQL);
        boolean noMatchWrong = BCryptUtil.checkPassword("adminErroneo", hashSQL);

        if (matchAdmin && !noMatchWrong) {
            System.out.println("CORRECTO (admin123 verificado con éxito contra hash SQL)");
            pruebasPasadas++;
        } else {
            System.out.println("FALLIDO (no coincidió con hash de BD)");
        }

        // 3. Prueba de Sesión y Control de Permisos por Rol
        totalPruebas++;
        System.out.print("[TEST 3] Validación de SesionUsuario y control de roles... ");
        SesionUsuario.iniciarSesion(1, "Administrador Prueba", "admin_test", "ADMINISTRADOR");
        boolean esAdmin = SesionUsuario.esAdministrador();
        boolean noEsOp = !SesionUsuario.esOperador();
        boolean noEsCons = !SesionUsuario.esConsulta();
        boolean puedeMod = SesionUsuario.puedeModificar();

        SesionUsuario.iniciarSesion(2, "Operador Prueba", "operador_test", "OPERADOR");
        boolean opEsOp = SesionUsuario.esOperador();
        boolean opNoAdmin = !SesionUsuario.esAdministrador();
        boolean opPuedeMod = SesionUsuario.puedeModificar();

        SesionUsuario.iniciarSesion(3, "Consulta Prueba", "consulta_test", "CONSULTA");
        boolean consEsCons = SesionUsuario.esConsulta();
        boolean consNoMod = !SesionUsuario.puedeModificar();

        SesionUsuario.cerrarSesion();
        boolean sesionCerrada = !SesionUsuario.estaAutenticado();

        if (esAdmin && noEsOp && noEsCons && puedeMod && opEsOp && opNoAdmin && opPuedeMod && consEsCons && consNoMod && sesionCerrada) {
            System.out.println("CORRECTO (Los 3 roles y sus permisos validados)");
            pruebasPasadas++;
        } else {
            System.out.println("FALLIDO");
        }

        // 4. Prueba de Validaciones de Negocio
        totalPruebas++;
        System.out.print("[TEST 4] Validaciones de texto, números y códigos de barras... ");
        boolean v1 = Validaciones.esTextoVacio("");
        boolean v2 = !Validaciones.esTextoVacio("Hola");
        boolean v3 = Validaciones.esNumeroPositivo("42");
        boolean v4 = !Validaciones.esNumeroPositivo("-5");
        boolean v5 = Validaciones.esDecimalPositivo("125.75");
        boolean v6 = !Validaciones.esDecimalPositivo("abc");
        boolean v7 = Validaciones.esCodigoBarrasValido("CONT-PLAS-001");
        boolean v8 = !Validaciones.esCodigoBarrasValido("c"); // muy corto

        if (v1 && v2 && v3 && v4 && v5 && v6 && v7 && v8) {
            System.out.println("CORRECTO");
            pruebasPasadas++;
        } else {
            System.out.println("FALLIDO");
        }

        // 5. Prueba de Cálculos de Contenedor
        totalPruebas++;
        System.out.print("[TEST 5] Modelo Contenedor: capacidad y % llenado... ");
        Contenedor cont = new Contenedor();
        cont.setCapacidadKg(500.0);
        cont.setPesoActualKg(250.0);
        double pct = cont.calcularPorcentajeLlenado();
        double disp = cont.getCapacidadDisponible();

        if (Math.abs(pct - 50.0) < 0.001 && Math.abs(disp - 250.0) < 0.001) {
            System.out.println("CORRECTO (50% llenado, 250 kg disponible)");
            pruebasPasadas++;
        } else {
            System.out.println("FALLIDO");
        }

        System.out.println("=================================================================");
        System.out.println("  RESULTADO: " + pruebasPasadas + " / " + totalPruebas + " PRUEBAS SUPERADAS");
        System.out.println("=================================================================");

        if (pruebasPasadas == totalPruebas) {
            System.out.println("  >>> TODOS LOS COMPONENTES FUNCIONAN PERFECTAMENTE <<<");
        } else {
            System.exit(1);
        }
    }
}
