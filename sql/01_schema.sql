-- ============================================================================
-- SCRIPT 01: ESTRUCTURA DE LA BASE DE DATOS (green_track)
-- ============================================================================
-- Crea la base de datos 'green_track' y todas sus tablas con relaciones,
-- llaves primarias, llaves foráneas, índices y restricciones CHECK.
-- ============================================================================

DROP DATABASE IF EXISTS green_track;
CREATE DATABASE green_track CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE green_track;

-- 1. Tabla: roles
CREATE TABLE roles (
    id          INT          PRIMARY KEY AUTO_INCREMENT,
    nombre      VARCHAR(30)  UNIQUE NOT NULL,
    descripcion VARCHAR(150)
) ENGINE=InnoDB;

-- 2. Tabla: usuarios
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

-- 3. Tabla: tipos_residuos
CREATE TABLE tipos_residuos (
    id          INT         PRIMARY KEY AUTO_INCREMENT,
    nombre      VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(150),
    estado      BOOLEAN     NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- 4. Tabla: ubicaciones
CREATE TABLE ubicaciones (
    id          INT          PRIMARY KEY AUTO_INCREMENT,
    nombre      VARCHAR(80)  NOT NULL,
    descripcion VARCHAR(150),
    referencia  VARCHAR(100),
    estado      BOOLEAN      NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- 5. Tabla: contenedores
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

-- 6. Tabla: recolecciones
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

-- 7. Tabla: auditoria
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

-- Índices de optimización para búsquedas frecuentes
CREATE INDEX idx_usuarios_username ON usuarios(username);
CREATE INDEX idx_contenedores_codigo ON contenedores(codigo_barras);
CREATE INDEX idx_contenedores_tipo ON contenedores(tipo_id);
CREATE INDEX idx_contenedores_ubicacion ON contenedores(ubicacion_id);
CREATE INDEX idx_recolecciones_fecha ON recolecciones(fecha);
CREATE INDEX idx_recolecciones_contenedor ON recolecciones(contenedor_id);
CREATE INDEX idx_auditoria_fecha ON auditoria(fecha);
