-- ============================================================================
-- SCRIPT 02: PROCEDIMIENTOS ALMACENADOS
-- ============================================================================
-- Implementa los procedimientos requeridos:
-- 1. sp_buscar_contenedor (con parámetro OUT p_encontrado)
-- 2. sp_registrar_recoleccion (con transacción y parámetro OUT p_resultado)
-- 3. sp_actualizar_contenedor (actualización y validaciones)
-- ============================================================================

USE green_track;

DELIMITER //

-- ----------------------------------------------------------------------------
-- 1. sp_buscar_contenedor
-- Busca un contenedor por código de barras y devuelve sus datos vía ResultSet,
-- estableciendo p_encontrado en TRUE o FALSE.
-- ----------------------------------------------------------------------------
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

-- ----------------------------------------------------------------------------
-- 2. sp_registrar_recoleccion
-- Registra una recolección asegurando atomicidad y concurrencia segura
-- con SELECT FOR UPDATE y COMMIT/ROLLBACK.
-- ----------------------------------------------------------------------------
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

    -- Bloqueo pesimista para evitar inconsistencias por accesos simultáneos
    SELECT estado, capacidad_kg, peso_actual_kg
    INTO v_estado, v_capacidad, v_peso_actual
    FROM contenedores
    WHERE id = p_contenedor_id
    FOR UPDATE;

    IF v_estado IS NULL THEN
        SET p_resultado = 'Error: El contenedor especificado no existe.';
        ROLLBACK;
    ELSEIF v_estado != 'ACTIVO' THEN
        SET p_resultado = CONCAT('Error: El contenedor no está ACTIVO (Estado actual: ', v_estado, ').');
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
            -- Sincronizar variable de sesión para auditoría
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

-- ----------------------------------------------------------------------------
-- 3. sp_actualizar_contenedor
-- Actualiza la información del contenedor validando coherencia de capacidad.
-- ----------------------------------------------------------------------------
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

DELIMITER ;
