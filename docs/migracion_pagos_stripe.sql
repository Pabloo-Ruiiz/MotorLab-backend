-- Pago en línea de los presupuestos con Stripe.
-- Ejecutar una sola vez en cada base de datos ya creada (motorlabdb y motorlab_testdb).
-- Es seguro repetirlo: no hace nada si las columnas ya existen.

BEGIN;

ALTER TABLE presupuestos
  ADD COLUMN IF NOT EXISTS pagado_en        TIMESTAMP,      -- lo fija el webhook de Stripe al confirmar el cobro
  ADD COLUMN IF NOT EXISTS stripe_sesion_id VARCHAR(255);   -- última sesión de pago creada para el presupuesto

COMMIT;
