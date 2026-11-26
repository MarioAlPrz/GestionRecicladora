# 🌱 GreenTrack - Sistema Integral de Gestión para Planta de Reciclaje

**GreenTrack** es una solución de escritorio desarrollada en **Java Swing** y **MySQL** mediante **JDBC**, diseñada para administrar y controlar de manera eficiente las operaciones de una planta de reciclaje: pesaje de residuos, control de inventario de contenedores mediante lectores de códigos de barras, gestión de ubicaciones, control de acceso por roles, transacciones seguras de pesaje y auditoría de operaciones en tiempo real.

---

## 📁 Arquitectura y Organización en Paquetes

El proyecto sigue una arquitectura en capas desacoplada y orientada a objetos, separando nítidamente la interfaz gráfica (**Frontend**), la lógica de negocio (**Service**), el acceso a datos (**DAO**), las entidades del dominio (**Model**), la infraestructura de conexión (**Config**) y las utilidades criptográficas y de validación (**Util**):

```
GestionRecicladora/
├── pom.xml                               # Configuración de compilación Maven
├── compilar.sh                           # Script para compilar con javac
├── ejecutar.sh                           # Script para ejecutar la interfaz de usuario
├── probar.sh                             # Script para ejecutar la suite de pruebas unitarias
├── README.md                             # Documentación completa del proyecto
├── lib/                                  # Carpeta para colocar el conector MySQL JDBC (.jar)
│   └── LEEME.txt
├── resources/                            # Archivos de configuración y propiedades
│   ├── db.properties                     # Configuración activa de MySQL (no versionada con contraseñas sensibles)
│   └── db.properties.ejemplo             # Plantilla de ejemplo para nuevos entornos
├── sql/                                  # Scripts SQL modulares y unificados
│   ├── 01_schema.sql                     # Tablas, llaves foráneas, checks e índices
│   ├── 02_procedures.sql                 # Procedimientos almacenados (IN y OUT)
│   ├── 03_functions.sql                  # Funciones almacenadas de cálculo y métricas
│   ├── 04_triggers.sql                   # Triggers de validación y auditoría con @usuario_id
│   ├── 05_test_data.sql                  # Datos semilla (roles, usuarios BCrypt, residuos, contenedores)
│   └── green_track_complete.sql          # Script consolidado todo-en-uno
├── src/                                  # Código fuente Java
│   └── greentrack/
│       ├── TestSuite.java                # Pruebas unitarias completas
│       ├── config/                       # Infraestructura y conexión
│       │   ├── DatabaseConfig.java       # Lector de propiedades desacoplado
│       │   └── DatabaseConnection.java   # Manejador centralizado de conexiones JDBC y auditoría
│       ├── model/                        # Entidades del Dominio (POJOs)
│       │   ├── Auditoria.java            # Registro de auditoría
│       │   ├── Contenedor.java           # Modelo de contenedor y cálculos de capacidad
│       │   ├── Recoleccion.java          # Registro de recolección y pesaje
│       │   ├── Rol.java                  # Rol y permisos
│       │   ├── TipoResiduo.java          # Catálogo de materiales reciclables
│       │   ├── Ubicacion.java            # Áreas y naves de la planta
│       │   └── Usuario.java              # Usuarios y estado de cuentas
│       ├── dao/                          # Capa de Acceso a Datos (Data Access Object)
│       │   ├── AuditoriaDAO.java         # Consulta de registros de auditoría
│       │   ├── ContenedorDAO.java        # CRUD de contenedores y llamadas a sp_buscar_contenedor
│       │   ├── RecoleccionDAO.java       # Registro transaccional con sp_registrar_recoleccion y rollback
│       │   ├── ReporteDAO.java           # Consultas analíticas avanzadas (JOINs, GROUP BY, KPIs)
│       │   ├── RolDAO.java               # Consulta y mapeo de roles
│       │   ├── TipoResiduoDAO.java       # CRUD y validación de dependencias de tipos de residuo
│       │   ├── UbicacionDAO.java         # CRUD de ubicaciones y zonas
│       │   └── UsuarioDAO.java           # Autenticación BCrypt y administración de usuarios
│       ├── service/                      # Capa de Lógica de Negocio
│       │   ├── AuthService.java          # Control de inicio de sesión y sesión global
│       │   ├── ContenedorService.java    # Reglas de negocio para contenedores
│       │   ├── RecoleccionService.java   # Validación de pesajes y transacciones seguras
│       │   └── ReporteService.java       # Agrupaciones y reportes para presentación
│       ├── util/                         # Utilerías del Sistema
│       │   ├── BCryptUtil.java           # Algoritmo EksBlowfish/BCrypt ($2a$) nativo
│       │   ├── BCryptConstants.java      # Constantes criptográficas estándar (P-box y S-boxes)
│       │   ├── SesionUsuario.java        # Control de sesión activa y verificación de roles
│       │   └── Validaciones.java         # Validadores de códigos de barras, pesos y campos
│       └── ui/                           # Capa de Presentación (Frontend Java Swing)
│           ├── front_principal.java      # Dashboard principal con métricas dinámicas y gráficos
│           ├── VentanaLogin.java         # Pantalla de acceso con selector rápido de usuarios demo
│           ├── VentanaCerrarSesion.java  # Diálogo de confirmación de salida
│           ├── VentanaUsuarios.java      # Módulo CRUD de usuarios (Solo ADMINISTRADOR)
│           ├── VentanaTiposResiduo.java  # Catálogo de materiales y control de desactivación
│           ├── VentanaContenedores.java  # Inventario con barras de llenado y alertas de capacidad
│           ├── VentanaRecolecciones.java # Historial de recolecciones con filtros
│           ├── VentanaRegistrarRecoleccion.java # Módulo para lector de código de barras (Enter) y pesaje
│           ├── VentanaBuscarContenedor.java     # Tarjeta de consulta rápida por código de barras
│           ├── VentanaRutas.java         # Administración de ubicaciones y zonas
│           └── VentanaReportes.java      # Módulo analítico (Residuos, Contenedores y Auditoría)
└── bin/                                  # Binarios compilados (.class)
```

---

## 🗄️ Base de Datos MySQL (`green_track`)

### 1. Creación e Importación

Puedes importar la base de datos completa de dos maneras:

#### Opción A: Script Consolidado (Recomendado)
Ejecuta el archivo consolidado en MySQL Workbench o desde la terminal:
```bash
mysql -u root -p < sql/green_track_complete.sql
```

#### Opción B: Scripts Modulares por Fases
Si deseas ejecutarlo paso a paso:
1. `mysql -u root -p < sql/01_schema.sql` (Tablas, llaves foráneas, checks e índices)
2. `mysql -u root -p < sql/02_procedures.sql` (Procedimientos almacenados)
3. `mysql -u root -p < sql/03_functions.sql` (Funciones de cálculo)
4. `mysql -u root -p < sql/04_triggers.sql` (Triggers de validación y auditoría)
5. `mysql -u root -p < sql/05_test_data.sql` (Datos iniciales de prueba)

---

### 2. Estructura de Tablas y Restricciones
- **`roles`**: Catálogo de roles con restricción UNIQUE en el nombre (`ADMINISTRADOR`, `OPERADOR`, `CONSULTA`).
- **`usuarios`**: Cuentas de usuario con contraseñas encriptadas mediante **BCrypt ($2a$)**, estado (`ACTIVO` / `INACTIVO`) y relación foránea a `roles`.
- **`tipos_residuos`**: Tipos de materiales reciclables (Plástico, Papel/Cartón, Vidrio, Metal, Orgánico) con bandera de estado booleana para borrado lógico.
- **`ubicaciones`**: Áreas físicas de la planta con nombre, descripción y referencias.
- **`contenedores`**: Contenedores asignados a un tipo de residuo y ubicación, con código de barras único (`UNIQUE`), validaciones de capacidad positiva (`capacidad_kg > 0`) y peso no negativo (`peso_actual_kg >= 0`).
- **`recolecciones`**: Registro histórico de recolecciones con peso recolectado (`peso_recolectado > 0`), usuario responsable y observaciones.
- **`auditoria`**: Bitácora histórica con usuario responsable, tabla afectada, tipo de operación (`INSERT`, `UPDATE`, `DELETE`), fecha y datos en formato JSON (`datos_anteriores`, `datos_nuevos`).

---

### 3. Procedimientos Almacenados
- **`sp_buscar_contenedor(IN p_codigo_barras, OUT p_encontrado)`**:
  Busca un contenedor activo por su código de barras, devolviendo todos sus atributos (tipo de residuo, ubicación, capacidad, peso y % de llenado) e indicando en el parámetro `OUT` si fue localizado.
- **`sp_registrar_recoleccion(IN p_codigo_barras, IN p_usuario_id, IN p_peso, IN p_observaciones, OUT p_resultado)`**:
  Ejecuta una transacción segura con bloqueo de fila (`FOR UPDATE`), valida que el contenedor esté activo, que el peso sea positivo y que no sobrepase la capacidad máxima, registra la recolección, actualiza el peso del contenedor y confirma con `COMMIT` (o realiza `ROLLBACK` ante errores).
- **`sp_actualizar_contenedor(...)`**:
  Actualiza los datos de un contenedor con validación de existencia y capacidad.

---

### 4. Funciones Almacenadas
- **`fn_porcentaje_llenado(p_contenedor_id)`**:
  Calcula el porcentaje exacto de llenado con fórmula `(peso_actual_kg / capacidad_kg) * 100`, con protección total contra división entre cero.
- **`fn_total_recolectado(p_tipo_id)`**:
  Devuelve la sumatoria acumulada de kilogramos recolectados para un tipo de material.

---

### 5. Triggers de Validación y Auditoría
- **`trg_recoleccion_validar_peso`**: Bloquea con `SIGNAL SQLSTATE '45000'` cualquier recolección con peso menor o igual a cero.
- **`trg_recoleccion_validar_capacidad`**: Bloquea recolecciones que excedan la capacidad disponible del contenedor.
- **`trg_recoleccion_auditoria_insert`**: Registra automáticamente en la tabla `auditoria` cada nueva recolección usando la variable de sesión `@usuario_id`.
- **`trg_contenedor_auditoria_update`**: Registra en `auditoria` los datos anteriores y nuevos en formato JSON cuando se modifica el estado o peso de un contenedor.

---

## 🔐 Autenticación y Control de Acceso por Roles

El sistema implementa seguridad mediante **BCrypt** sin dependencias externas obligatorias. Las contraseñas se almacenan mediante hash con sal única por usuario.

### Usuarios Preconfigurados para Pruebas:
Todos los usuarios iniciales comparten la contraseña: **`admin123`**

| Usuario | Contraseña | Rol | Permisos |
| :--- | :--- | :--- | :--- |
| **`admin`** | `admin123` | **ADMINISTRADOR** | **Acceso total**: gestión de usuarios, catálogos, contenedores, recolecciones, reportes avanzados y auditoría. |
| **`operador`** | `admin123` | **OPERADOR** | **Operaciones de planta**: escaneo de códigos de barras, registro de recolecciones, consulta y actualización de contenedores. |
| **`consulta`** | `admin123` | **CONSULTA** | **Solo lectura**: consulta de historiales, monitoreo de métricas en dashboard y visualización de reportes (botones de modificación deshabilitados). |

> En la ventana de inicio de sesión (`VentanaLogin`), se incluye un selector rápido de usuarios de prueba para facilitar la demostración de los diferentes niveles de permisos.

---

## ⚙️ Configuración de Conexión (`resources/db.properties`)

Las credenciales no están cableadas en el código fuente. Se configuran en el archivo `resources/db.properties`:

```properties
# Configuración de Conexión a MySQL para GreenTrack
db.url=jdbc:mysql://localhost:3306/green_track?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
db.user=root
db.password=tu_contraseña_aqui
```

Si el archivo no existe, el sistema utiliza valores por defecto seguros para servidores MySQL locales estándar (`localhost:3306`, usuario `root`, contraseña vacía).

---

## 🚀 Compilación y Ejecución

### Opción 1: Mediante los Scripts Automatizados (Terminal macOS / Linux)

1. **Dar permisos de ejecución (ya configurados):**
   ```bash
   chmod +x compilar.sh ejecutar.sh probar.sh
   ```

2. **Compilar el proyecto:**
   ```bash
   ./compilar.sh
   ```

3. **Ejecutar la suite de pruebas unitarias:**
   ```bash
   ./probar.sh
   ```
   *(Valida el algoritmo BCrypt, verificación de contraseñas de BD, control de sesión por roles, validadores de formatos y lógica de contenedores).*

4. **Iniciar la aplicación:**
   ```bash
   ./ejecutar.sh
   ```

---

### Opción 2: Compilación y Ejecución Manual con `javac` y `java`

1. **Compilar:**
   ```bash
   mkdir -p bin
   javac -encoding UTF-8 -d bin $(find src -name "*.java")
   ```

2. **Ejecutar con el Driver JDBC:**
   Asegúrate de incluir el archivo `.jar` del conector de MySQL (por ejemplo en `lib/mysql-connector-j-8.3.0.jar`):
   ```bash
   java -cp "bin:resources:lib/*" greentrack.ui.VentanaLogin
   ```

---

### Opción 3: Mediante Maven

Si dispones de Maven instalado en tu equipo:
```bash
# Compilar
mvn compile

# Ejecutar la aplicación
mvn exec:java
```

---

### Opción 4: Importar en un IDE (NetBeans, IntelliJ IDEA, Eclipse o VS Code)

1. Abre el IDE y selecciona **Open Project** o **Import Existing Project**.
2. Selecciona la carpeta `GestionRecicladora`.
3. El IDE reconocerá la estructura estándar Maven (`pom.xml`) o la carpeta `src/`.
4. Si tu IDE no gestiona dependencias Maven automáticamente, agrega `mysql-connector-j-8.3.0.jar` a las librerías del proyecto.
5. Ejecuta la clase principal: `greentrack.ui.VentanaLogin`.

---

## 📟 Uso del Lector de Códigos de Barras

1. En el menú principal, haz clic en **"Registrar Recolección"** o **"Buscar Contenedor"**.
2. El cursor se posicionará automáticamente en el campo de texto del código de barras.
3. Al escanear con una pistola lectora física conectada por USB, esta enviará el código seguido del caracter `ENTER`.
4. El sistema captura el evento `ENTER`, ejecuta la consulta contra MySQL y muestra:
   - Nombre y ubicación del contenedor.
   - Tipo de residuo que almacena.
   - Capacidad máxima, peso actual acumulado y porcentaje de llenado en una barra gráfica.
   - Estado del contenedor (`ACTIVO`, `MANTENIMIENTO`, `INACTIVO`).
5. Permite ingresar el peso recolectado y registrar la transacción con confirmación inmediata.
6. Si no se cuenta con un lector físico, el operador puede escribir el código manualmente y presionar la tecla `ENTER` o el botón **"Buscar"**.

---

## 📊 Reportes Disponibles en la Interfaz

En la sección **"Reportes"** del menú principal se encuentran disponibles tres paneles analíticos:

1. **Recolecciones por Tipo de Residuo:**
   - Consulta SQL agregada (`JOIN`, `GROUP BY`, `SUM`, `AVG`).
   - Muestra el nombre del residuo, número total de recolecciones realizadas, peso total acumulado y peso promedio por recolección.
2. **Estado de Contenedores:**
   - Consulta avanzada con `JOIN` entre `contenedores`, `tipos_residuos` y `ubicaciones`.
   - Muestra el código de barras, nombre, residuo, ubicación, capacidad, peso actual y el porcentaje de llenado calculado con la función SQL `fn_porcentaje_llenado`.
3. **Bitácora de Auditoría:**
   - Muestra los registros generados por los triggers en la tabla `auditoria`, con fecha/hora, usuario responsable, tabla afectada, tipo de operación y datos JSON.

---

## 🛡️ Transacciones Seguras y Concurrencia

El registro de recolecciones implementa transacciones atómicas con nivel de aislamiento y bloqueo pesimista:
```sql
SELECT id, capacidad_kg, peso_actual_kg, estado FROM contenedores WHERE id = ? FOR UPDATE;
```
Esto previene condiciones de carrera (*race conditions*) cuando dos operadores intentan registrar recolecciones simultáneamente sobre el mismo contenedor físico, garantizando la consistencia absoluta de los datos. En caso de fallo o sobrepeso, se ejecuta un `ROLLBACK` inmediato y se notifica al usuario con un mensaje claro en pantalla.
