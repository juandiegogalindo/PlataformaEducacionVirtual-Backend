# Scripts PostgreSQL

Ejecutar desde la raiz del repositorio contra una base de datos ya creada.
Reemplazar `plataforma_educativa` y `postgres` por el nombre y usuario propios.
Para reiniciar el entorno de desarrollo, ejecutar eliminacion, creacion y
poblacion en ese orden. La eliminacion borra todos los datos existentes.

```powershell
psql -U postgres -d plataforma_educativa -v ON_ERROR_STOP=1 -f scripts/03_eliminacion.sql
psql -U postgres -d plataforma_educativa -v ON_ERROR_STOP=1 -f scripts/01_creacion.sql
psql -U postgres -d plataforma_educativa -v ON_ERROR_STOP=1 -f scripts/02_poblacion.sql
```

`01_creacion.sql` crea el esquema de las entidades JPA en una base vacia.
La tabla `refresh_tokens` guarda únicamente el SHA-256 del token, además de
estado de revocación y sesión. Omitirlo si Hibernate ya creó las tablas.

Para validar el esquema y el ciclo de reinicio sin una instancia externa de
PostgreSQL, ejecute `mvn -Dtest=ScriptCreacionSchemaValidationTest test`. La
prueba carga `01_creacion.sql`, valida las entidades con Hibernate, ejecuta
`03_eliminacion.sql` y vuelve a crear el esquema.

Para una base existente ejecutar primero:

```powershell
psql -U postgres -d plataforma_educativa -v ON_ERROR_STOP=1 -f scripts/04_migracion_refresh_tokens.sql
```

La migración invalida los refresh tokens antiguos almacenados en claro; los
usuarios deberán iniciar sesión de nuevo. No es posible recuperar esos tokens
de forma segura para convertirlos sin exponerlos.
`02_poblacion.sql` carga datos de desarrollo una sola vez: cinco usuarios,
un curso, dos horarios, dos inscripciones, contenido, foro y resultados de tarea
y examen. Las fechas del curso son relativas al dia de ejecucion.

Todos los usuarios de prueba usan la contrasena `clave12345` (BCrypt):

| Rol | Correo |
| --- | --- |
| Administrador | admin@test.com |
| Coordinador | coord@test.com |
| Docente | docente@test.com |
| Estudiante | ana@test.com |
| Estudiante | luis@test.com |

`03_eliminacion.sql` borra todas las tablas y sus datos. Usarlo unicamente
para reiniciar un entorno de pruebas; despues ejecutar de nuevo la creacion
y la poblacion. El administrador se crea como parte de `02_poblacion.sql`.

`04_migracion_refresh_tokens.sql` es solo para bases existentes. No ejecutarlo
después de `03_eliminacion.sql` y `01_creacion.sql`, porque el esquema nuevo ya
incluye sus columnas e índices.

## Reportes académicos

No se requiere una migración para los reportes: el período se determina a partir
de las fechas de inicio y fin del curso. `02_poblacion.sql` crea el curso de
prueba desde la fecha actual hasta 30 días después, con dos matrículas activas
del programa `Ingenieria de Sistemas`. Así el curso coincide con el semestre
calendario actual (`YYYY-1` para enero-junio o `YYYY-2` para julio-diciembre).

Después de iniciar sesión como Coordinador (`coord@test.com`), consulta el
reporte o descarga un archivo usando el token devuelto por login:

```http
GET /api/reportes/academicos?tipoReporte=MATRICULAS&periodo=2026-2&programa=Ingenieria%20de%20Sistemas
Authorization: Bearer <accessToken>

GET /api/reportes/academicos/exportar/pdf?tipoReporte=MATRICULAS&periodo=2026-2&programa=Ingenieria%20de%20Sistemas
Authorization: Bearer <accessToken>

GET /api/reportes/academicos/exportar/excel?tipoReporte=MATRICULAS&periodo=2026-2&programa=Ingenieria%20de%20Sistemas
Authorization: Bearer <accessToken>
```

Reemplaza `2026-2` por el semestre del curso sembrado. El filtro `programa` se
puede omitir; `cursoId` también es opcional. `05_consulta_reporte_matriculas.sql`
contiene la consulta SQL equivalente para revisar las filas directamente en
PostgreSQL.
