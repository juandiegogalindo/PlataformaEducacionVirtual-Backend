-- ============================================================================
-- Script de eliminacion de la base de datos - Plataforma de Educacion Virtual
-- ============================================================================
-- Elimina todas las tablas del esquema, en el orden inverso de dependencia
-- de llave foranea respecto a 01_creacion.sql. Uso tipico: reiniciar el
-- entorno de pruebas desde cero antes de volver a correr 01_creacion.sql y
-- 02_poblacion.sql.
--
-- ADVERTENCIA: esta operacion es irreversible y borra toda la informacion
-- almacenada. No ejecutar contra una base de datos con datos reales.
-- ============================================================================

BEGIN;

DROP TABLE IF EXISTS log_accesos;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS resultados;
DROP TABLE IF EXISTS tareas;
DROP TABLE IF EXISTS examenes;
DROP TABLE IF EXISTS evaluaciones;
DROP TABLE IF EXISTS mensajes_foro;
DROP TABLE IF EXISTS foros;
DROP TABLE IF EXISTS progreso_lecciones;
DROP TABLE IF EXISTS inscripciones;
DROP TABLE IF EXISTS recursos_bibliograficos;
DROP TABLE IF EXISTS lecciones;
DROP TABLE IF EXISTS cursos;
DROP TABLE IF EXISTS coordinadores_academicos;
DROP TABLE IF EXISTS estudiantes;
DROP TABLE IF EXISTS docentes;
DROP TABLE IF EXISTS administradores;
DROP TABLE IF EXISTS usuarios;

COMMIT;
