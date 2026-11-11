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
