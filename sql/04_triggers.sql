-- ============================================================================
-- SCRIPT 04: TRIGGERS DE VALIDACIÓN Y AUDITORÍA
-- ============================================================================
-- 1. Triggers de validación:
--    - trg_recoleccion_validar_peso (BEFORE INSERT)
--    - trg_recoleccion_validar_peso_update (BEFORE UPDATE)
-- 2. Triggers de auditoría:
--    - trg_recoleccion_auditoria_insert (AFTER INSERT en recolecciones)
--    - trg_contenedor_auditoria_update (AFTER UPDATE en contenedores)
-- Utilizan la variable de sesión @usuario_id configurada por Java JDBC.
-- ============================================================================

USE green_track;

DELIMITER //

-- ----------------------------------------------------------------------------
-- 1. trg_recoleccion_validar_peso (BEFORE INSERT)
-- Impide recolecciones con peso <= 0 o que superen el peso disponible.
-- ----------------------------------------------------------------------------
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
            SET MESSAGE_TEXT = 'Error [Trigger]: El peso recolectado debe ser estrictamente mayor a 0 kg.';
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

-- ----------------------------------------------------------------------------
-- 2. trg_recoleccion_validar_peso_update (BEFORE UPDATE)
-- ----------------------------------------------------------------------------
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
            SET MESSAGE_TEXT = 'Error [Trigger]: El peso recolectado debe ser estrictamente mayor a 0 kg.';
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

-- ----------------------------------------------------------------------------
-- 3. trg_recoleccion_auditoria_insert (AFTER INSERT)
-- Registra en auditoria cada nueva recolección con snapshot en JSON.
-- ----------------------------------------------------------------------------
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

-- ----------------------------------------------------------------------------
-- 4. trg_contenedor_auditoria_update (AFTER UPDATE)
-- Registra en auditoria cada modificación a un contenedor.
-- ----------------------------------------------------------------------------
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
