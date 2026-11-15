-- Consulta de referencia equivalente a GET /api/reportes/academicos.
-- Uso:
-- psql -U postgres -d plataforma_educativa -v ON_ERROR_STOP=1 \
--   -v periodo=2026-2 -v programa="Ingenieria de Sistemas" \
--   -f scripts/05_consulta_reporte_matriculas.sql
-- Opcionalmente agregue -v curso_id=12 para limitar el resultado a un curso.
-- periodo debe ser YYYY-1 (enero-junio) o YYYY-2 (julio-diciembre).

\if :{?periodo}
\else
\set periodo '2026-2'
\endif
\if :{?programa}
\else
\set programa ''
\endif
\if :{?curso_id}
\else
\set curso_id ''
\endif

WITH parametros AS (
    SELECT :'periodo'::text AS periodo,
           NULLIF(:'programa', '') AS programa,
           NULLIF(:'curso_id', '')::BIGINT AS curso_id
), rango AS (
    SELECT periodo,
           programa,
           curso_id,
           CASE substring(periodo FROM 6 FOR 1)
               WHEN '1' THEN make_date(substring(periodo FROM 1 FOR 4)::INTEGER, 1, 1)
               ELSE make_date(substring(periodo FROM 1 FOR 4)::INTEGER, 7, 1)
           END AS desde,
           CASE substring(periodo FROM 6 FOR 1)
               WHEN '1' THEN make_date(substring(periodo FROM 1 FOR 4)::INTEGER, 7, 1) - 1
               ELSE make_date(substring(periodo FROM 1 FOR 4)::INTEGER + 1, 1, 1) - 1
           END AS hasta
    FROM parametros
    WHERE periodo ~ '^[0-9]{4}-[12]$'
)
SELECT i.id AS inscripcion_id,
       e.id AS estudiante_id,
       e.codigo_estudiantil,
       u.nombre || ' ' || u.apellido AS estudiante,
       u.correo,
       e.programa_academico,
       e.semestre,
       c.id AS curso_id,
       c.nombre AS curso,
       i.estado,
       i.fecha_inscripcion,
       i.calificacion_final
FROM inscripciones i
JOIN estudiantes e ON e.id = i.estudiante_id
JOIN usuarios u ON u.id = e.id
JOIN cursos c ON c.id = i.curso_id
CROSS JOIN rango r
WHERE c.fecha_inicio IS NOT NULL
  AND c.fecha_fin IS NOT NULL
  AND c.fecha_inicio <= r.hasta
  AND c.fecha_fin >= r.desde
  AND (r.programa IS NULL OR lower(trim(e.programa_academico)) = lower(trim(r.programa)))
  AND (r.curso_id IS NULL OR c.id = r.curso_id)
ORDER BY u.apellido, u.nombre, c.nombre;
