-- ============================================================================
-- SCRIPT UNIFICADO COMPLETO: GREEN TRACK RECYCLING MANAGEMENT SYSTEM
-- ============================================================================
-- Contiene:
--   1. Creación de la base de datos 'green_track'
--   2. Tablas (roles, usuarios, tipos_residuos, ubicaciones, contenedores, recolecciones, auditoria)
--   3. Restricciones CHECK, Foreign Keys e Índices
--   4. Procedimientos Almacenados (sp_buscar_contenedor, sp_registrar_recoleccion, sp_actualizar_contenedor)
--   5. Funciones Almacenadas (fn_porcentaje_llenado, fn_total_recolectado)
--   6. Triggers de Validación y Auditoría (trg_recoleccion_validar_peso, trg_recoleccion_auditoria_insert, etc.)
--   7. Datos Iniciales de Prueba y Usuarios con hash seguro BCrypt
--
-- Ejecución:
--   mysql -u root -p < green_track_complete.sql
-- ============================================================================

DROP DATABASE IF EXISTS green_track;
CREATE DATABASE green_track CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE green_track;

-- ============================================================================
-- 1. TABLAS
-- ============================================================================

CREATE TABLE roles (
    id          INT          PRIMARY KEY AUTO_INCREMENT,
    nombre      VARCHAR(30)  UNIQUE NOT NULL,
    descripcion VARCHAR(150)
) ENGINE=InnoDB;

CREATE TABLE usuarios (
    id              INT          PRIMARY KEY AUTO_INCREMENT,
    nombre_completo VARCHAR(100) NOT NULL,
    username        VARCHAR(50)  UNIQUE NOT NULL,
    contrasena      VARCHAR(255) NOT NULL COMMENT 'Hash BCrypt ($2a$)',
    rol_id          INT          NOT NULL,
    estado          ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_roles FOREIGN KEY (rol_id) REFERENCES roles(id)
) ENGINE=InnoDB;

CREATE TABLE tipos_residuos (
    id          INT         PRIMARY KEY AUTO_INCREMENT,
    nombre      VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(150),
    estado      BOOLEAN     NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE ubicaciones (
    id          INT          PRIMARY KEY AUTO_INCREMENT,
    nombre      VARCHAR(80)  NOT NULL,
    descripcion VARCHAR(150),
    referencia  VARCHAR(100),
    estado      BOOLEAN      NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE contenedores (
    id             INT          PRIMARY KEY AUTO_INCREMENT,
    codigo_barras  VARCHAR(50)  UNIQUE NOT NULL,
    nombre         VARCHAR(80)  NOT NULL,
    tipo_id        INT          NOT NULL,
    ubicacion_id   INT          NOT NULL,
    capacidad_kg   DECIMAL(10,2) NOT NULL,
    peso_actual_kg DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estado         ENUM('ACTIVO', 'MANTENIMIENTO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    fecha_registro DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_contenedor_capacidad CHECK (capacidad_kg > 0),
    CONSTRAINT chk_contenedor_peso CHECK (peso_actual_kg >= 0),
    CONSTRAINT fk_contenedores_tipos FOREIGN KEY (tipo_id) REFERENCES tipos_residuos(id),
    CONSTRAINT fk_contenedores_ubicaciones FOREIGN KEY (ubicacion_id) REFERENCES ubicaciones(id)
) ENGINE=InnoDB;

CREATE TABLE recolecciones (
    id               INT           PRIMARY KEY AUTO_INCREMENT,
    contenedor_id    INT           NOT NULL,
    usuario_id       INT           NOT NULL,
    peso_recolectado DECIMAL(10,2) NOT NULL,
    fecha            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observaciones    VARCHAR(200),
    CONSTRAINT chk_recoleccion_peso CHECK (peso_recolectado > 0),
    CONSTRAINT fk_recolecciones_contenedores FOREIGN KEY (contenedor_id) REFERENCES contenedores(id),
    CONSTRAINT fk_recolecciones_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

CREATE TABLE auditoria (
    id               INT          PRIMARY KEY AUTO_INCREMENT,
    usuario_id       INT          NULL,
    tabla_afectada   VARCHAR(50)  NOT NULL,
    tipo_operacion   ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    fecha            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    datos_anteriores JSON         NULL,
    datos_nuevos     JSON         NULL,
    CONSTRAINT fk_auditoria_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Índices
CREATE INDEX idx_usuarios_username ON usuarios(username);
CREATE INDEX idx_contenedores_codigo ON contenedores(codigo_barras);
CREATE INDEX idx_contenedores_tipo ON contenedores(tipo_id);
CREATE INDEX idx_contenedores_ubicacion ON contenedores(ubicacion_id);
CREATE INDEX idx_recolecciones_fecha ON recolecciones(fecha);
CREATE INDEX idx_recolecciones_contenedor ON recolecciones(contenedor_id);
CREATE INDEX idx_auditoria_fecha ON auditoria(fecha);

-- ============================================================================
-- 2. FUNCIONES ALMACENADAS
-- ============================================================================

DELIMITER //

DROP FUNCTION IF EXISTS fn_porcentaje_llenado //
CREATE FUNCTION fn_porcentaje_llenado(p_contenedor_id INT)
RETURNS DECIMAL(5,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_capacidad   DECIMAL(10,2) DEFAULT 0;
    DECLARE v_peso_actual DECIMAL(10,2) DEFAULT 0;
    DECLARE v_porcentaje  DECIMAL(5,2)  DEFAULT 0.00;

    SELECT COALESCE(capacidad_kg, 0), COALESCE(peso_actual_kg, 0)
    INTO v_capacidad, v_peso_actual
    FROM contenedores
    WHERE id = p_contenedor_id;

    IF v_capacidad > 0 THEN
        SET v_porcentaje = ROUND((v_peso_actual / v_capacidad) * 100.0, 2);
    ELSE
        SET v_porcentaje = 0.00;
    END IF;

    RETURN v_porcentaje;
END //

DROP FUNCTION IF EXISTS fn_total_recolectado //
CREATE FUNCTION fn_total_recolectado(p_tipo_id INT)
RETURNS DECIMAL(10,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_total DECIMAL(10,2) DEFAULT 0.00;

    SELECT COALESCE(SUM(r.peso_recolectado), 0.00)
    INTO v_total
    FROM recolecciones r
    INNER JOIN contenedores c ON r.contenedor_id = c.id
    WHERE c.tipo_id = p_tipo_id;

    RETURN v_total;
END //

-- ============================================================================
-- 3. PROCEDIMIENTOS ALMACENADOS
-- ============================================================================

DROP PROCEDURE IF EXISTS sp_buscar_contenedor //
CREATE PROCEDURE sp_buscar_contenedor(
    IN  p_codigo     VARCHAR(50),
    OUT p_encontrado BOOLEAN
)
BEGIN
    DECLARE v_count INT DEFAULT 0;

    SELECT COUNT(*) INTO v_count
    FROM contenedores
    WHERE codigo_barras = p_codigo;

    IF v_count > 0 THEN
        SET p_encontrado = TRUE;
        SELECT
            c.id,
            c.codigo_barras,
            c.nombre,
            c.tipo_id,
            t.nombre AS tipo_nombre,
            c.ubicacion_id,
            u.nombre AS ubicacion_nombre,
            c.capacidad_kg,
            c.peso_actual_kg,
            c.estado,
            c.fecha_registro,
            fn_porcentaje_llenado(c.id) AS porcentaje_llenado
        FROM contenedores c
        INNER JOIN tipos_residuos t ON c.tipo_id = t.id
        INNER JOIN ubicaciones u    ON c.ubicacion_id = u.id
        WHERE c.codigo_barras = p_codigo;
    ELSE
        SET p_encontrado = FALSE;
    END IF;
END //

DROP PROCEDURE IF EXISTS sp_registrar_recoleccion //
CREATE PROCEDURE sp_registrar_recoleccion(
    IN  p_contenedor_id  INT,
    IN  p_usuario_id     INT,
    IN  p_peso           DECIMAL(10,2),
    IN  p_observaciones  VARCHAR(200),
    OUT p_resultado      VARCHAR(100)
)
BEGIN
    DECLARE v_estado      VARCHAR(20);
    DECLARE v_capacidad   DECIMAL(10,2);
    DECLARE v_peso_actual DECIMAL(10,2);
    DECLARE v_disponible  DECIMAL(10,2);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_resultado = 'Error: Ocurrió un error SQL inesperado en la transacción.';
    END;

    START TRANSACTION;

    SELECT estado, capacidad_kg, peso_actual_kg
    INTO v_estado, v_capacidad, v_peso_actual
    FROM contenedores
    WHERE id = p_contenedor_id
    FOR UPDATE;

    IF v_estado IS NULL THEN
        SET p_resultado = 'Error: El contenedor especificado no existe.';
        ROLLBACK;
    ELSEIF v_estado != 'ACTIVO' THEN
        SET p_resultado = CONCAT('Error: El contenedor no está ACTIVO (Estado: ', v_estado, ').');
        ROLLBACK;
    ELSE
        SET v_disponible = v_capacidad - v_peso_actual;

        IF p_peso <= 0 THEN
            SET p_resultado = 'Error: El peso recolectado debe ser mayor a 0 kg.';
            ROLLBACK;
        ELSEIF p_peso > v_disponible THEN
            SET p_resultado = CONCAT('Error: Capacidad insuficiente. Disponible: ', v_disponible, ' kg.');
            ROLLBACK;
        ELSE
            SET @usuario_id = p_usuario_id;

            INSERT INTO recolecciones (contenedor_id, usuario_id, peso_recolectado, observaciones, fecha)
            VALUES (p_contenedor_id, p_usuario_id, p_peso, p_observaciones, NOW());

            UPDATE contenedores
            SET peso_actual_kg = peso_actual_kg + p_peso
            WHERE id = p_contenedor_id;

            COMMIT;
            SET p_resultado = CONCAT('Recolección registrada exitosamente. Peso: ', p_peso, ' kg.');
        END IF;
    END IF;
END //

DROP PROCEDURE IF EXISTS sp_actualizar_contenedor //
CREATE PROCEDURE sp_actualizar_contenedor(
    IN  p_id           INT,
    IN  p_codigo       VARCHAR(50),
    IN  p_nombre       VARCHAR(80),
    IN  p_tipo_id      INT,
    IN  p_ubicacion_id INT,
    IN  p_capacidad    DECIMAL(10,2),
    IN  p_estado       ENUM('ACTIVO', 'MANTENIMIENTO', 'INACTIVO'),
    OUT p_resultado    VARCHAR(100)
)
BEGIN
    DECLARE v_peso_actual DECIMAL(10,2);
    DECLARE v_existe      INT DEFAULT 0;

    SELECT COUNT(*), COALESCE(MAX(peso_actual_kg), 0)
    INTO v_existe, v_peso_actual
    FROM contenedores
    WHERE id = p_id;

    IF v_existe = 0 THEN
        SET p_resultado = 'Error: El contenedor especificado no existe.';
    ELSEIF p_capacidad < v_peso_actual THEN
        SET p_resultado = CONCAT('Error: La nueva capacidad (', p_capacidad, ' kg) no puede ser menor al peso actual (', v_peso_actual, ' kg).');
    ELSE
        UPDATE contenedores
        SET codigo_barras = p_codigo,
            nombre        = p_nombre,
            tipo_id       = p_tipo_id,
            ubicacion_id  = p_ubicacion_id,
            capacidad_kg  = p_capacidad,
            estado        = p_estado
        WHERE id = p_id;

        SET p_resultado = 'Contenedor actualizado exitosamente.';
    END IF;
END //

-- ============================================================================
-- 4. TRIGGERS
-- ============================================================================

DROP TRIGGER IF EXISTS trg_recoleccion_validar_peso //
CREATE TRIGGER trg_recoleccion_validar_peso
BEFORE INSERT ON recolecciones
FOR EACH ROW
BEGIN
    DECLARE v_capacidad   DECIMAL(10,2);
    DECLARE v_peso_actual DECIMAL(10,2);
    DECLARE v_disponible  DECIMAL(10,2);

    IF NEW.peso_recolectado <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error [Trigger]: El peso recolectado debe ser mayor a 0 kg.';
    END IF;

    SELECT capacidad_kg, peso_actual_kg
    INTO v_capacidad, v_peso_actual
    FROM contenedores
    WHERE id = NEW.contenedor_id;

    SET v_disponible = v_capacidad - v_peso_actual;

    IF NEW.peso_recolectado > v_disponible THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error [Trigger]: El peso recolectado excede la capacidad disponible del contenedor.';
    END IF;
END //

DROP TRIGGER IF EXISTS trg_recoleccion_validar_peso_update //
CREATE TRIGGER trg_recoleccion_validar_peso_update
BEFORE UPDATE ON recolecciones
FOR EACH ROW
BEGIN
    DECLARE v_capacidad   DECIMAL(10,2);
    DECLARE v_peso_actual DECIMAL(10,2);
    DECLARE v_disponible  DECIMAL(10,2);

    IF NEW.peso_recolectado <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error [Trigger]: El peso recolectado debe ser mayor a 0 kg.';
    END IF;

    SELECT capacidad_kg, peso_actual_kg
    INTO v_capacidad, v_peso_actual
    FROM contenedores
    WHERE id = NEW.contenedor_id;

    SET v_disponible = v_capacidad - v_peso_actual + OLD.peso_recolectado;

    IF NEW.peso_recolectado > v_disponible THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error [Trigger]: La recolección excede la capacidad disponible del contenedor.';
    END IF;
END //

DROP TRIGGER IF EXISTS trg_recoleccion_auditoria_insert //
CREATE TRIGGER trg_recoleccion_auditoria_insert
AFTER INSERT ON recolecciones
FOR EACH ROW
BEGIN
    INSERT INTO auditoria (usuario_id, tabla_afectada, tipo_operacion, fecha, datos_anteriores, datos_nuevos)
    VALUES (
        @usuario_id,
        'recolecciones',
        'INSERT',
        NOW(),
        NULL,
        JSON_OBJECT(
            'id',               NEW.id,
            'contenedor_id',    NEW.contenedor_id,
            'usuario_id',       NEW.usuario_id,
            'peso_recolectado', NEW.peso_recolectado,
            'fecha',            NEW.fecha,
            'observaciones',    IFNULL(NEW.observaciones, '')
        )
    );
END //

DROP TRIGGER IF EXISTS trg_contenedor_auditoria_update //
CREATE TRIGGER trg_contenedor_auditoria_update
AFTER UPDATE ON contenedores
FOR EACH ROW
BEGIN
    INSERT INTO auditoria (usuario_id, tabla_afectada, tipo_operacion, fecha, datos_anteriores, datos_nuevos)
    VALUES (
        @usuario_id,
        'contenedores',
        'UPDATE',
        NOW(),
        JSON_OBJECT(
            'id',             OLD.id,
            'codigo_barras',  OLD.codigo_barras,
            'nombre',         OLD.nombre,
            'tipo_id',        OLD.tipo_id,
            'ubicacion_id',   OLD.ubicacion_id,
            'capacidad_kg',   OLD.capacidad_kg,
            'peso_actual_kg', OLD.peso_actual_kg,
            'estado',         OLD.estado
        ),
        JSON_OBJECT(
            'id',             NEW.id,
            'codigo_barras',  NEW.codigo_barras,
            'nombre',         NEW.nombre,
            'tipo_id',        NEW.tipo_id,
            'ubicacion_id',   NEW.ubicacion_id,
            'capacidad_kg',   NEW.capacidad_kg,
            'peso_actual_kg', NEW.peso_actual_kg,
            'estado',         NEW.estado
        )
    );
END //

DELIMITER ;

-- ============================================================================
-- 5. DATOS INICIALES DE PRUEBA
-- ============================================================================

INSERT INTO roles (nombre, descripcion) VALUES
    ('ADMINISTRADOR', 'Acceso total y configuración del sistema'),
    ('OPERADOR',      'Registro de contenedores, pesaje y recolecciones'),
    ('CONSULTA',      'Consulta de información, métricas y reportes');

-- Contraseña para todos los usuarios iniciales: 'admin123'
-- Hash BCrypt: $2a$10$sgvFEewTBpYyI3pQzlOx5.Lm2fHIXWbSJXYuNIkjAZ8DcL.zqZtAy
INSERT INTO usuarios (nombre_completo, username, contrasena, rol_id, estado, fecha_creacion) VALUES
    ('Administrador del Sistema', 'admin',    '$2a$10$sgvFEewTBpYyI3pQzlOx5.Lm2fHIXWbSJXYuNIkjAZ8DcL.zqZtAy', 1, 'ACTIVO', NOW()),
    ('Carlos López Martínez',    'operador', '$2a$10$sgvFEewTBpYyI3pQzlOx5.Lm2fHIXWbSJXYuNIkjAZ8DcL.zqZtAy', 2, 'ACTIVO', NOW()),
    ('María García Hernández',   'consulta', '$2a$10$sgvFEewTBpYyI3pQzlOx5.Lm2fHIXWbSJXYuNIkjAZ8DcL.zqZtAy', 3, 'ACTIVO', NOW());

INSERT INTO tipos_residuos (nombre, descripcion, estado) VALUES
    ('Plástico',      'Botellas PET, polietileno de alta densidad, bolsas reciclables', TRUE),
    ('Papel/Cartón',  'Cajas de cartón corrugado, periódicos, revistas y hojas blancas', TRUE),
    ('Vidrio',        'Botellas, tarros y frascos de vidrio transparente y de color',   TRUE),
    ('Metal',         'Latas de aluminio, envases de hojalata y chatarra ferrosa',      TRUE),
    ('Orgánico',      'Restos de poda, materia compostable y residuos de comedor',      TRUE);

INSERT INTO ubicaciones (nombre, descripcion, referencia, estado) VALUES
    ('Zona Norte - Nave Principal',     'Patio de recepción exterior',     'Frente al portón vehicular 1', TRUE),
    ('Zona Sur - Almacén de Acopio',    'Área de clasificación y prensa',  'Junto a la compactadora #2',   TRUE),
    ('Zona Centro - Patio de Maniobras', 'Área de báscula de camiones',    'Costado de caseta de pesaje',  TRUE),
    ('Zona Industrial - Estación 4',    'Línea de descarga rápida',        'Andén de carga 3B',            TRUE);

INSERT INTO contenedores (codigo_barras, nombre, tipo_id, ubicacion_id, capacidad_kg, peso_actual_kg, estado, fecha_registro) VALUES
    ('CONT-PLAS-001', 'Contenedor PET Norte 1',     1, 1, 500.00, 385.00, 'ACTIVO',        NOW()),
    ('CONT-CART-002', 'Contenedor Cartón Almacén',  2, 2, 600.00, 420.00, 'ACTIVO',        NOW()),
    ('CONT-VIDR-003', 'Contenedor Vidrio Patio',    3, 3, 400.00, 275.00, 'ACTIVO',        NOW()),
    ('CONT-METL-004', 'Contenedor Metal Estación 4', 4, 4, 450.00, 190.00, 'ACTIVO',        NOW()),
    ('CONT-ORGA-005', 'Contenedor Orgánico Comedor', 5, 1, 300.00, 120.50, 'ACTIVO',        NOW()),
    ('CONT-PLAS-006', 'Contenedor Plástico Duro Sur',1, 2, 500.00,  90.00, 'ACTIVO',        NOW()),
    ('CONT-CART-007', 'Contenedor Papel Oficinas',   2, 3, 350.00,   0.00, 'MANTENIMIENTO', NOW()),
    ('CONT-VIDR-008', 'Contenedor Vidrio Ámbar',     3, 4, 400.00,   0.00, 'INACTIVO',      NOW());

INSERT INTO recolecciones (contenedor_id, usuario_id, peso_recolectado, fecha, observaciones) VALUES
    (1, 2, 120.00, DATE_SUB(NOW(), INTERVAL 5 DAY), 'Recolección matutina botellas PET'),
    (1, 2, 140.00, DATE_SUB(NOW(), INTERVAL 3 DAY), 'Vaciado intermedio de botellas'),
    (1, 2, 125.00, DATE_SUB(NOW(), INTERVAL 1 DAY), 'Recolección turno vespertino'),
    (2, 2, 210.00, DATE_SUB(NOW(), INTERVAL 4 DAY), 'Cajas embalaje aplastadas'),
    (2, 2, 210.00, DATE_SUB(NOW(), INTERVAL 2 DAY), 'Papel archivo y cartón corrugado'),
    (3, 2, 150.00, DATE_SUB(NOW(), INTERVAL 3 DAY), 'Envases de vidrio seleccionados'),
    (3, 2, 125.00, DATE_SUB(NOW(), INTERVAL 1 DAY), 'Botellas de refresco no retornables'),
    (4, 2, 110.00, DATE_SUB(NOW(), INTERVAL 2 DAY), 'Latas de refresco prensadas'),
    (4, 2,  80.00, NOW(),                           'Hojalata y perfiles metálicos'),
    (5, 2, 120.50, NOW(),                           'Residuos orgánicos frescos compostables'),
    (6, 2,  90.00, NOW(),                           'Contenedor plástico secundario');
