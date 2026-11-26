-- ============================================================================
-- SCRIPT 03: FUNCIONES ALMACENADAS
-- ============================================================================
-- 1. fn_porcentaje_llenado: Calcula (peso_actual_kg / capacidad_kg) * 100
--    evitando divisiones entre cero.
-- 2. fn_total_recolectado: Suma el peso total recolectado de un tipo de residuo.
-- ============================================================================

USE green_track;

DELIMITER //

-- ----------------------------------------------------------------------------
-- 1. fn_porcentaje_llenado
-- Retorna el porcentaje de llenado (0 a 100%) de un contenedor dado.
-- ----------------------------------------------------------------------------
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

    -- Prevenir división entre cero
    IF v_capacidad > 0 THEN
        SET v_porcentaje = ROUND((v_peso_actual / v_capacidad) * 100.0, 2);
    ELSE
        SET v_porcentaje = 0.00;
    END IF;

    RETURN v_porcentaje;
END //

-- ----------------------------------------------------------------------------
-- 2. fn_total_recolectado
-- Retorna el acumulado total en kg recolectados de un tipo de residuo específico.
-- ----------------------------------------------------------------------------
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

DELIMITER ;
