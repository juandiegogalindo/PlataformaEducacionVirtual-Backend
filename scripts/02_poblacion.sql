-- ============================================================================
-- Script de poblacion de la base de datos - Plataforma de Educacion Virtual
-- ============================================================================
-- Inserta un usuario de cada rol, un curso con contenido, una inscripcion,
-- un mensaje de foro y una evaluacion de cada tipo (examen y tarea) con su
-- respectivo resultado calificado.
--
-- Contrasena para todos los usuarios de prueba: clave12345
-- (hash BCrypt precalculado, valido para autenticarse contra el backend)
--
-- Requiere haber ejecutado antes 01_creacion.sql (o que Hibernate haya
-- creado el esquema con ddl-auto=update).
-- ============================================================================

BEGIN;

WITH u_admin AS (
    INSERT INTO usuarios (nombre, apellido, correo, contrasena_hash, activo, email_verificado, intentos_fallidos, created_at, updated_at)
    VALUES ('Root', 'Admin', 'admin@test.com', '$2b$10$M.Bk2y8moYgCCNUbtN8nsOPoKjnAWgzCAjPjD3gRhJ6hV4RVTCPme', TRUE, TRUE, 0, now(), now())
    RETURNING id
), admin_ins AS (
    INSERT INTO administradores (id, codigo, cargo, nivel_acceso)
    SELECT id, 'A001', 'Administrador', 'ADMIN' FROM u_admin
    RETURNING id
),
u_coord AS (
    INSERT INTO usuarios (nombre, apellido, correo, contrasena_hash, activo, email_verificado, intentos_fallidos, created_at, updated_at)
    VALUES ('Laura', 'Ruiz', 'coord@test.com', '$2b$10$M.Bk2y8moYgCCNUbtN8nsOPoKjnAWgzCAjPjD3gRhJ6hV4RVTCPme', TRUE, TRUE, 0, now(), now())
    RETURNING id
), coord_ins AS (
    INSERT INTO coordinadores_academicos (id, codigo, facultad, telefono_oficina)
    SELECT id, 'C001', 'Ingenieria', '3000000' FROM u_coord
    RETURNING id
),
u_docente AS (
    INSERT INTO usuarios (nombre, apellido, correo, contrasena_hash, activo, email_verificado, intentos_fallidos, created_at, updated_at)
    VALUES ('Carlos', 'Gomez', 'docente@test.com', '$2b$10$M.Bk2y8moYgCCNUbtN8nsOPoKjnAWgzCAjPjD3gRhJ6hV4RVTCPme', TRUE, TRUE, 0, now(), now())
    RETURNING id
), docente_ins AS (
    INSERT INTO docentes (id, codigo_docente, especialidad, titulo_profesional, anios_experiencia, biografia)
    SELECT id, 'D001', 'Matematicas', 'Licenciado', 5, 'Docente de prueba' FROM u_docente
    RETURNING id
),
u_est1 AS (
    INSERT INTO usuarios (nombre, apellido, correo, contrasena_hash, activo, email_verificado, intentos_fallidos, created_at, updated_at)
    VALUES ('Ana', 'Perez', 'ana@test.com', '$2b$10$M.Bk2y8moYgCCNUbtN8nsOPoKjnAWgzCAjPjD3gRhJ6hV4RVTCPme', TRUE, TRUE, 0, now(), now())
    RETURNING id
), est1_ins AS (
    INSERT INTO estudiantes (id, codigo_estudiantil, tipo_documento, numero_documento, programa_academico, semestre)
    SELECT id, 'E001', 'CEDULA', '1001', 'Ingenieria de Sistemas', 3 FROM u_est1
    RETURNING id
),
u_est2 AS (
    INSERT INTO usuarios (nombre, apellido, correo, contrasena_hash, activo, email_verificado, intentos_fallidos, created_at, updated_at)
    VALUES ('Luis', 'Torres', 'luis@test.com', '$2b$10$M.Bk2y8moYgCCNUbtN8nsOPoKjnAWgzCAjPjD3gRhJ6hV4RVTCPme', TRUE, TRUE, 0, now(), now())
    RETURNING id
), est2_ins AS (
    INSERT INTO estudiantes (id, codigo_estudiantil, tipo_documento, numero_documento, programa_academico, semestre)
    SELECT id, 'E002', 'CEDULA', '1002', 'Ingenieria de Sistemas', 3 FROM u_est2
    RETURNING id
),
curso_ins AS (
    INSERT INTO cursos (nombre, descripcion, estado, docente_id, creado_por_usuario_id, fecha_inicio, fecha_fin, cupo_maximo, created_at, updated_at)
    SELECT 'Algebra I', 'Curso de prueba', 'ACTIVO', docente_ins.id, docente_ins.id, DATE '2026-10-01', DATE '2026-12-01', 30, now(), now()
    FROM docente_ins
    RETURNING id
),
leccion1 AS (
    INSERT INTO lecciones (curso_id, titulo, contenido, tipo_contenido, duracion_minutos, orden, created_at)
    SELECT id, 'Introduccion', 'Contenido de la leccion 1', 'TEXTO', 30, 1, now() FROM curso_ins
    RETURNING id
),
leccion2 AS (
    INSERT INTO lecciones (curso_id, titulo, contenido, tipo_contenido, duracion_minutos, orden, created_at)
    SELECT id, 'Ecuaciones lineales', 'Contenido de la leccion 2', 'VIDEO', 45, 2, now() FROM curso_ins
    RETURNING id
),
recurso_ins AS (
    INSERT INTO recursos_bibliograficos (curso_id, titulo, autor, anio_publicacion, tipo_recurso, enlace, created_at)
    SELECT id, 'Algebra Lineal', 'Gilbert Strang', 2016, 'LIBRO', 'https://example.com/algebra-lineal', now() FROM curso_ins
    RETURNING id
),
insc1 AS (
    INSERT INTO inscripciones (estudiante_id, curso_id, estado, fecha_inscripcion)
    SELECT est1_ins.id, curso_ins.id, 'ACTIVA', now() FROM est1_ins, curso_ins
    RETURNING id
),
insc2 AS (
    INSERT INTO inscripciones (estudiante_id, curso_id, estado, fecha_inscripcion)
    SELECT est2_ins.id, curso_ins.id, 'ACTIVA', now() FROM est2_ins, curso_ins
    RETURNING id
),
progreso1 AS (
    INSERT INTO progreso_lecciones (estudiante_id, leccion_id, completado, fecha_completado, created_at)
    SELECT est1_ins.id, leccion1.id, TRUE, now(), now() FROM est1_ins, leccion1
    RETURNING id
),
foro_ins AS (
    INSERT INTO foros (curso_id, titulo, descripcion, created_at)
    SELECT id, 'Foro Algebra I', 'Foro de discusion del curso', now() FROM curso_ins
    RETURNING id
),
mensaje1 AS (
    INSERT INTO mensajes_foro (foro_id, usuario_id, contenido, editado, fecha_publicacion)
    SELECT foro_ins.id, u_docente.id, 'Bienvenidos al curso, cualquier duda la resolvemos aqui', FALSE, now()
    FROM foro_ins, u_docente
    RETURNING id
),
mensaje2 AS (
    INSERT INTO mensajes_foro (foro_id, usuario_id, mensaje_padre_id, contenido, editado, fecha_publicacion)
    SELECT foro_ins.id, u_est1.id, mensaje1.id, 'Gracias profe!', FALSE, now()
    FROM foro_ins, u_est1, mensaje1
    RETURNING id
),
eval_examen AS (
    INSERT INTO evaluaciones (curso_id, titulo, descripcion, peso_porcentual, estado, fecha_publicacion, created_at)
    SELECT id, 'Parcial 1', 'Primer parcial', 30, 'PUBLICADA', now(), now() FROM curso_ins
    RETURNING id
),
examen_ins AS (
    INSERT INTO examenes (id, fecha_aplicacion, tiempo_limite, numero_intentos_permitidos, aleatorio)
    SELECT id, now() + INTERVAL '7 days', 60, 2, TRUE FROM eval_examen
    RETURNING id
),
eval_tarea AS (
    INSERT INTO evaluaciones (curso_id, titulo, descripcion, peso_porcentual, estado, fecha_publicacion, created_at)
    SELECT id, 'Taller 1', 'Primer taller', 20, 'PUBLICADA', now(), now() FROM curso_ins
    RETURNING id
),
tarea_ins AS (
    INSERT INTO tareas (id, instrucciones, fecha_limite, permite_entrega_tardia, penalizacion_tardanza_porcentaje)
    SELECT id, 'Resolver los ejercicios 1 a 10', now() + INTERVAL '30 days', TRUE, 10 FROM eval_tarea
    RETURNING id
),
resultado1 AS (
    INSERT INTO resultados (evaluacion_id, estudiante_id, numero_intento, calificacion, retroalimentacion, contenido_entrega, estado, fecha_registro)
    SELECT tarea_ins.id, est1_ins.id, 1, 4.5, 'Buen trabajo', 'https://drive.google.com/mi-entrega', 'CALIFICADO', now()
    FROM tarea_ins, est1_ins
    RETURNING id
)
SELECT 'poblacion completada' AS resultado;

COMMIT;
