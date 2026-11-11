-- Método y estado del pago de los presupuestos: tarjeta (Stripe) o efectivo en el taller.
-- Requiere haber ejecutado antes migracion_pagos_stripe.sql.
-- Ejecutar una sola vez en cada base de datos ya creada (motorlabdb y motorlab_testdb).
-- Es seguro repetirlo: no hace nada si las columnas y las restricciones ya existen.

BEGIN;

ALTER TABLE presupuestos
  ADD COLUMN IF NOT EXISTS metodo_pago VARCHAR(10),   -- NULL hasta que el cliente elige
  ADD COLUMN IF NOT EXISTS estado_pago VARCHAR(20);

ALTER TABLE presupuestos DROP CONSTRAINT IF EXISTS ck_presupuestos_metodo_pago;
ALTER TABLE presupuestos ADD CONSTRAINT ck_presupuestos_metodo_pago
  CHECK (metodo_pago IN ('tarjeta', 'efectivo'));

ALTER TABLE presupuestos DROP CONSTRAINT IF EXISTS ck_presupuestos_estado_pago;
ALTER TABLE presupuestos ADD CONSTRAINT ck_presupuestos_estado_pago
  CHECK (estado_pago IN ('pendiente_efectivo', 'pagado_tarjeta', 'pagado_efectivo'));

-- Los presupuestos que ya se pagaron con Stripe antes de esta migración
UPDATE presupuestos
   SET metodo_pago = 'tarjeta', estado_pago = 'pagado_tarjeta'
 WHERE pagado_en IS NOT NULL AND estado_pago IS NULL;

-- La vista añade la columna al final: CREATE OR REPLACE lo permite sin borrarla
CREATE OR REPLACE VIEW v_ordenes_resumen AS
SELECT
  o.id                AS orden_id,
  o.estado,
  CASE o.estado
    WHEN 'recibido'        THEN 20
    WHEN 'en_preparacion'  THEN 40
    WHEN 'esperando_pieza' THEN 60
    WHEN 'arreglando'      THEN 80
    WHEN 'listo'           THEN 100
    WHEN 'entregado'       THEN 100
    ELSE 0
  END                 AS progreso,
  o.urgencia,
  o.tipo_servicio,
  o.sintoma,
  o.elevador,
  o.fecha_entrada,
  o.fecha_estimada,
  v.id                AS vehiculo_id,
  v.matricula,
  v.marca,
  v.modelo,
  u.id                AS cliente_id,
  u.nombre            AS cliente_nombre,
  m.id                AS mecanico_id,
  m.nombre            AS mecanico_nombre,
  p.estado            AS presupuesto_estado,
  p.total             AS presupuesto_total,
  p.estado_pago       AS presupuesto_estado_pago
FROM ordenes_trabajo o
JOIN vehiculos v         ON v.id = o.vehiculo_id
JOIN usuarios  u         ON u.id = v.usuario_id
LEFT JOIN usuarios m     ON m.id = o.mecanico_id
LEFT JOIN presupuestos p ON p.orden_id = o.id;

COMMIT;
