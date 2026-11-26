-- ============================================================================
-- SCRIPT 05: DATOS INICIALES DE PRUEBA
-- ============================================================================
-- Inserta:
-- 1. Roles del sistema (ADMINISTRADOR, OPERADOR, CONSULTA)
-- 2. Usuario administrador inicial y usuarios de prueba con hash BCrypt
--    Contraseña para todos: 'admin123'
-- 3. Tipos de residuos reciclables
-- 4. Ubicaciones físicas de la planta
-- 5. Contenedores con códigos de barras realistas
-- 6. Recolecciones de prueba para alimentar reportes y KPIs iniciales
-- ============================================================================

USE green_track;

-- 1. Roles
INSERT INTO roles (nombre, descripcion) VALUES
    ('ADMINISTRADOR', 'Acceso total y configuración del sistema'),
    ('OPERADOR',      'Registro de contenedores, pesaje y recolecciones'),
    ('CONSULTA',      'Consulta de información, métricas y reportes');

-- 2. Usuarios Iniciales
-- Hash BCrypt de 'admin123': $2a$10$sgvFEewTBpYyI3pQzlOx5.Lm2fHIXWbSJXYuNIkjAZ8DcL.zqZtAy
INSERT INTO usuarios (nombre_completo, username, contrasena, rol_id, estado, fecha_creacion) VALUES
    ('Administrador del Sistema', 'admin',    '$2a$10$sgvFEewTBpYyI3pQzlOx5.Lm2fHIXWbSJXYuNIkjAZ8DcL.zqZtAy', 1, 'ACTIVO', NOW()),
    ('Carlos López Martínez',    'operador', '$2a$10$sgvFEewTBpYyI3pQzlOx5.Lm2fHIXWbSJXYuNIkjAZ8DcL.zqZtAy', 2, 'ACTIVO', NOW()),
    ('María García Hernández',   'consulta', '$2a$10$sgvFEewTBpYyI3pQzlOx5.Lm2fHIXWbSJXYuNIkjAZ8DcL.zqZtAy', 3, 'ACTIVO', NOW());

-- 3. Tipos de Residuos
INSERT INTO tipos_residuos (nombre, descripcion, estado) VALUES
    ('Plástico',      'Botellas PET, polietileno de alta densidad, bolsas reciclables', TRUE),
    ('Papel/Cartón',  'Cajas de cartón corrugado, periódicos, revistas y hojas blancas', TRUE),
    ('Vidrio',        'Botellas, tarros y frascos de vidrio transparente y de color',   TRUE),
    ('Metal',         'Latas de aluminio, envases de hojalata y chatarra ferrosa',      TRUE),
    ('Orgánico',      'Restos de poda, materia compostable y residuos de comedor',      TRUE);

-- 4. Ubicaciones
INSERT INTO ubicaciones (nombre, descripcion, referencia, estado) VALUES
    ('Zona Norte - Nave Principal',     'Patio de recepción exterior',     'Frente al portón vehicular 1', TRUE),
    ('Zona Sur - Almacén de Acopio',    'Área de clasificación y prensa',  'Junto a la compactadora #2',   TRUE),
    ('Zona Centro - Patio de Maniobras', 'Área de báscula de camiones',    'Costado de caseta de pesaje',  TRUE),
    ('Zona Industrial - Estación 4',    'Línea de descarga rápida',        'Andén de carga 3B',            TRUE);

-- 5. Contenedores
-- Cada contenedor tiene un único tipo de residuo asignado y código de barras
INSERT INTO contenedores (codigo_barras, nombre, tipo_id, ubicacion_id, capacidad_kg, peso_actual_kg, estado, fecha_registro) VALUES
    ('CONT-PLAS-001', 'Contenedor PET Norte 1',     1, 1, 500.00, 385.00, 'ACTIVO',        NOW()),
    ('CONT-CART-002', 'Contenedor Cartón Almacén',  2, 2, 600.00, 420.00, 'ACTIVO',        NOW()),
    ('CONT-VIDR-003', 'Contenedor Vidrio Patio',    3, 3, 400.00, 275.00, 'ACTIVO',        NOW()),
    ('CONT-METL-004', 'Contenedor Metal Estación 4', 4, 4, 450.00, 190.00, 'ACTIVO',        NOW()),
    ('CONT-ORGA-005', 'Contenedor Orgánico Comedor', 5, 1, 300.00, 120.50, 'ACTIVO',        NOW()),
    ('CONT-PLAS-006', 'Contenedor Plástico Duro Sur',1, 2, 500.00,  90.00, 'ACTIVO',        NOW()),
    ('CONT-CART-007', 'Contenedor Papel Oficinas',   2, 3, 350.00,   0.00, 'MANTENIMIENTO', NOW()),
    ('CONT-VIDR-008', 'Contenedor Vidrio Ámbar',     3, 4, 400.00,   0.00, 'INACTIVO',      NOW());

-- 6. Recolecciones de Prueba (Registros históricos)
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
