-- ============================================================================
-- Migracion de refresh_tokens para FR-01/FR-02/FR-03
-- ============================================================================
-- Ejecutar una sola vez sobre bases existentes antes de desplegar el backend
-- que guarda refresh tokens como SHA-256.
--
-- Los valores antiguos almacenados en claro no se pueden convertir a su hash
-- sin conocer el token entregado al cliente. Por seguridad, se invalidan.
-- Nunca registrar ni exportar esos valores.
--
-- No ejecutar junto con 01_creacion.sql sobre una base vacia.
-- ============================================================================

BEGIN;

ALTER TABLE refresh_tokens
    ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP NULL;

ALTER TABLE refresh_tokens
    ADD COLUMN IF NOT EXISTS session_id VARCHAR(36) NULL;

-- Fuerza un nuevo inicio de sesión y elimina cualquier valor antiguo en claro.
-- md5 solo se usa como marcador irreversible de un registro ya revocado; los
-- tokens nuevos siempre se guardan como SHA-256 por la aplicación.
UPDATE refresh_tokens
SET token = md5(token || ':' || id),
    revocado = TRUE,
    revoked_at = COALESCE(revoked_at, CURRENT_TIMESTAMP);

UPDATE refresh_tokens
SET token = lpad(token, 64, '0')
WHERE length(token) < 64;

ALTER TABLE refresh_tokens
    ALTER COLUMN token TYPE VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_session_id
    ON refresh_tokens (session_id);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_usuario_activo
    ON refresh_tokens (usuario_id, revocado, fecha_expiracion);

COMMIT;
