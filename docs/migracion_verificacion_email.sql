-- Verificación del correo al crear una cuenta.
-- Ejecutar una sola vez en cada base de datos ya creada (motorlabdb y motorlab_testdb).
-- Los usuarios existentes quedan verificados para que puedan seguir entrando.

BEGIN;

ALTER TABLE usuarios
  ADD COLUMN email_verificado BOOLEAN NOT NULL DEFAULT TRUE,
  ADD COLUMN token_verificacion_hash VARCHAR(64),
  ADD COLUMN token_verificacion_expira TIMESTAMP,
  ADD CONSTRAINT uq_usuarios_token_verificacion UNIQUE (token_verificacion_hash);

COMMIT;
