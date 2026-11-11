# API del backend — Plataforma de Educación Virtual (Bit Criollo)

Referencia de los endpoints REST del backend (Spring Boot). Generada a partir del código de la rama `main`.

## Índice

1. [Convenciones](#convenciones)
2. [Autenticación](#autenticación)
3. [Perfil y foto de perfil](#perfil-y-foto-de-perfil)
4. [Administración de usuarios](#administración-de-usuarios)
5. [Cursos](#cursos)
6. [Inscripciones](#inscripciones)
7. [Lecciones](#lecciones)
8. [Recursos bibliográficos](#recursos-bibliográficos)
9. [Progreso de lecciones](#progreso-de-lecciones)
10. [Seguimiento de estudiantes](#seguimiento-de-estudiantes)
11. [Foro](#foro)
12. [Evaluaciones y resultados](#evaluaciones-y-resultados)
13. [Códigos de error](#códigos-de-error)
14. [Enumeraciones](#enumeraciones)
15. [Modelos (DTO)](#modelos-dto)

## Convenciones

- **Base URL local:** `http://localhost:8080`. En el despliegue, la URL de Render configurada en la colección de Postman.
- **Formato:** JSON (`Content-Type: application/json`), salvo la subida de foto (`multipart/form-data`).
- **Autenticación:** `Authorization: Bearer <accessToken>`. El access token dura **15 minutos** y el refresh token **7 días**: al vencer el access token el backend responde `401` y hay que pedir uno nuevo en `POST /api/auth/refresh`.
- **Roles:** `ESTUDIANTE`, `DOCENTE`, `COORDINADORACADEMICO`, `ADMINISTRADOR`. Un usuario tiene exactamente un rol, que queda fijado al crearlo.
- **Fechas y horas:** fechas `YYYY-MM-DD`; fecha y hora `YYYY-MM-DDTHH:mm:ss`; horas de horario `HH:mm` (se devuelven como `HH:mm:ss`).
- **Endpoints públicos (sin token):** `GET /api/health`, `POST /api/auth/**`, `GET /api/cursos`, `GET /api/cursos/{id}` y `GET /api/usuarios/{id}/foto`. Todo lo demás exige sesión.
- **Filtros opcionales** (`?estado=`): un valor que no pertenezca al enum responde `400`.
- **Salud del servicio:** `GET /api/health` (público) devuelve el estado del proyecto.
- **Migración de refresh:** con `REFRESH_COOKIE_ENABLED=true` el backend emite `refresh_token` como cookie `HttpOnly`, `SameSite=Lax`, `Path=/api/auth` y duración de 7 días. En producción use `REFRESH_COOKIE_SECURE=true`; el frontend debe usar `withCredentials: true`, solicitar antes `GET /api/auth/csrf`, enviar `X-XSRF-TOKEN` y eliminar el refresh token de `localStorage`.
- **Desarrollo local:** puede iniciar con `--spring.profiles.active=local` para habilitar cookies sin `Secure` y permitir `http://localhost:5173`. Este perfil no debe usarse en producción.

## Autenticación

### Despliegue y rollback de seguridad

1. Configure `JWT_SECRET` con una clave aleatoria de al menos 256 bits,
   `JWT_ISSUER`, `JWT_AUDIENCE`, `CORS_ALLOWED_ORIGINS` y, si se migra a
   cookies, `REFRESH_COOKIE_ENABLED=true` y `REFRESH_COOKIE_SECURE=true`.
2. Ejecute `scripts/04_migracion_refresh_tokens.sql` antes de desplegar una
   versión que use hashes. Los refresh tokens antiguos se invalidan y los
   usuarios deberán iniciar sesión de nuevo.
3. Verifique login, refresh, logout, `GET /api/auth/csrf`, CORS y una solicitud
   protegida antes de habilitar tráfico general.
4. Para rollback, desactive `REFRESH_COOKIE_ENABLED` y vuelva a la versión
   anterior sólo si mantiene el esquema compatible. No restaure refresh tokens
   antiguos ni exponga la columna `token`; la recuperación segura requiere que
   los usuarios vuelvan a autenticarse.

### `POST /api/auth/registro`

- **Quién:** Público
- **Cuerpo:** [`RegistroRequest`](#registrorequest)
- **Respuesta:** `201` [`AuthResponse`](#authresponse)

Registra un estudiante y devuelve los tokens. El resto de roles los crea un administrador.

### `POST /api/auth/login`

- **Quién:** Público
- **Cuerpo:** [`LoginRequest`](#loginrequest)
- **Respuesta:** `200` [`AuthResponse`](#authresponse)

Inicia sesión. Devuelve `accessToken` (15 min) y `refreshToken` (7 días). Tras 3 intentos fallidos la cuenta se bloquea 15 minutos (409). Una cuenta desactivada también recibe 409.

### `POST /api/auth/refresh`

- **Quién:** Público
- **Cuerpo:** [`RefreshTokenRequest`](#refreshtokenrequest)
- **Respuesta:** `200` [`AuthResponse`](#authresponse)

Cambia un `refreshToken` vigente por un par de tokens nuevo. El anterior queda revocado (cada refresh token se usa una sola vez).
Con cookies habilitadas puede omitirse el cuerpo: el token se lee de `refresh_token` y se rota la cookie. La reutilización de un token revocado revoca la sesión comprometida y responde `401`.

### Migración de base de datos

En producción ejecute antes del despliegue: `ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS revoked_at timestamp NULL;` y `ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS session_id varchar(36) NULL;`. Los tokens nuevos se guardan como SHA-256 en la columna `token`; los registros antiguos en claro deben invalidarse o migrarse mediante un proceso controlado, nunca registrando su valor.

### `POST /api/auth/logout`

- **Quién:** Público
- **Cuerpo:** [`RefreshTokenRequest`](#refreshtokenrequest)
- **Respuesta:** `204`

Revoca el `refreshToken` indicado: cierra esa sesión.

## Perfil y foto de perfil

La foto se guarda en la base de datos. Cada usuario tiene como máximo una; al reemplazarla se sobrescribe.

### `PUT /api/perfil/foto`

- **Quién:** Cualquier usuario con sesión
- **Cuerpo:** `multipart/form-data` con el campo `foto`
- **Parámetros de consulta (opcionales):** `foto` (MultipartFile)
- **Respuesta:** `200` [`UsuarioResponse`](#usuarioresponse)

Sube o reemplaza la foto de perfil. `multipart/form-data` con el campo `foto`: JPEG, PNG o WebP, máximo 5 MB. El tipo se valida por el contenido real del archivo. Deja `fotoPerfilUrl` en `/api/usuarios/{id}/foto`. 400 si el formato no es válido, 413 si pesa más de 5 MB.

### `DELETE /api/perfil/foto`

- **Quién:** Cualquier usuario con sesión
- **Respuesta:** `200` [`UsuarioResponse`](#usuarioresponse)

Elimina la foto de perfil y devuelve el perfil con `fotoPerfilUrl` en null.

### `GET /api/usuarios/{usuarioId}/foto`

- **Quién:** Público
- **Respuesta:** `200` `byte[]`

Imagen de perfil del usuario (se usa directo en `<img>`). 404 si no tiene foto. Incluye `ETag`: responde 304 si se envía `If-None-Match` con la versión vigente.

### `GET /api/perfil`

- **Quién:** Cualquier usuario con sesión
- **Respuesta:** `200` [`UsuarioResponse`](#usuarioresponse)

Datos del propio perfil.

### `PATCH /api/perfil`

- **Quién:** Cualquier usuario con sesión
- **Cuerpo:** [`ActualizarPerfilRequest`](#actualizarperfilrequest)
- **Respuesta:** `200` [`UsuarioResponse`](#usuarioresponse)

Actualiza nombre, apellido, teléfono y `fotoPerfilUrl`. Solo se modifican los campos enviados.

### `PATCH /api/perfil/contrasena`

- **Quién:** Cualquier usuario con sesión
- **Cuerpo:** [`CambiarContrasenaRequest`](#cambiarcontrasenarequest)
- **Respuesta:** `200` `Void`

Cambia la contraseña (exige la actual; la nueva debe ser distinta) y revoca todos los refresh tokens del usuario.

## Administración de usuarios

Todo el módulo es exclusivo del rol Administrador. Solo el auto-registro de estudiantes es público.

### `POST /api/admin/docentes`

- **Quién:** Administrador
- **Cuerpo:** [`DocenteRequest`](#docenterequest)
- **Respuesta:** `201` [`UsuarioResponse`](#usuarioresponse)

Crea un docente.

### `POST /api/admin/administradores`

- **Quién:** Administrador
- **Cuerpo:** [`AdministradorRequest`](#administradorrequest)
- **Respuesta:** `201` [`UsuarioResponse`](#usuarioresponse)

Crea un administrador (`nivelAcceso` opcional, por defecto ADMIN).

### `POST /api/admin/coordinadores`

- **Quién:** Administrador
- **Cuerpo:** [`CoordinadorRequest`](#coordinadorrequest)
- **Respuesta:** `201` [`UsuarioResponse`](#usuarioresponse)

Crea un coordinador académico.

### `GET /api/admin/docentes`

- **Quién:** Administrador
- **Respuesta:** `200` [`List<UsuarioResponse>`](#usuarioresponse)

Lista todos los docentes.

### `GET /api/admin/administradores`

- **Quién:** Administrador
- **Respuesta:** `200` [`List<UsuarioResponse>`](#usuarioresponse)

Lista todos los administradores.

### `GET /api/admin/coordinadores`

- **Quién:** Administrador
- **Respuesta:** `200` [`List<UsuarioResponse>`](#usuarioresponse)

Lista todos los coordinadores.

### `GET /api/admin/estudiantes`

- **Quién:** Administrador
- **Respuesta:** `200` [`List<UsuarioResponse>`](#usuarioresponse)

Lista todos los estudiantes.

### `PATCH /api/admin/usuarios/{id}/desactivar`

- **Quién:** Administrador
- **Respuesta:** `200` [`UsuarioResponse`](#usuarioresponse)

Desactiva un usuario: no puede iniciar sesión y sus tokens vigentes dejan de servir.

### `PATCH /api/admin/usuarios/{id}/activar`

- **Quién:** Administrador
- **Respuesta:** `200` [`UsuarioResponse`](#usuarioresponse)

Reactiva un usuario desactivado.

## Cursos

**Reglas de negocio**

- **Estados:** `ACTIVO`, `ARCHIVADO`, `INACTIVO` (este último hoy no se asigna).
- **Horario obligatorio al crear:** al menos una sesión semanal (`diaSemana`, `horaInicio`, `horaFin`).
- **Cruce de horarios:** un docente no puede dictar dos cursos ACTIVOS cuyas fechas se solapen y que tengan una sesión el mismo día con horas que se cruzan (409). Que una sesión termine justo cuando empieza otra no cuenta como cruce. Los cursos anteriores sin horario no generan conflictos. La regla se aplica al crear, editar, reasignar docente y reactivar.
- **Reasignar docente:** solo coordinador o administrador, y el docente destino debe estar activo.
- **Quién modifica:** el docente asignado, un coordinador o un administrador. El creador original pierde el permiso si el curso se reasigna.

### `POST /api/cursos`

- **Quién:** Docente, Coordinador, Administrador
- **Cuerpo:** [`CursoRequest`](#cursorequest)
- **Respuesta:** `201` [`CursoResponse`](#cursoresponse)

Crea un curso. Un docente lo crea a su nombre (ignora `docenteId`); coordinador y administrador deben indicar `docenteId` (docente activo). `horarios` es obligatorio (al menos una sesión). Crea también el foro del curso.

### `GET /api/cursos`

- **Quién:** Público
- **Respuesta:** `200` [`List<CursoResponse>`](#cursoresponse)

Catálogo público: solo cursos en estado ACTIVO.

### `GET /api/cursos/{id}`

- **Quién:** Público
- **Respuesta:** `200` [`CursoResponse`](#cursoresponse)

Detalle de un curso, en cualquier estado (permite abrir cursos archivados desde "mis cursos").

### `PUT /api/cursos/{id}`

- **Quién:** Docente asignado, Coordinador, Administrador
- **Cuerpo:** [`CursoRequest`](#cursorequest)
- **Respuesta:** `200` [`CursoResponse`](#cursoresponse)

Edita el curso. `horarios`: si se omite se conservan; si se envía reemplaza a los anteriores (lista vacía = 400). Solo coordinador o administrador pueden enviar otro `docenteId` para reasignar (un docente recibe 403).

### `DELETE /api/cursos/{id}`

- **Quién:** Docente asignado, Coordinador, Administrador
- **Respuesta:** `204`

Archiva el curso (pasa a ARCHIVADO).

### `PATCH /api/cursos/{id}/activar`

- **Quién:** Docente asignado, Coordinador, Administrador
- **Respuesta:** `200` [`CursoResponse`](#cursoresponse)

Reactiva un curso archivado. 409 si ya está activo o si su horario se cruza con otro curso activo del docente.

### `GET /api/docentes`

- **Quién:** Coordinador, Administrador
- **Respuesta:** `200` [`List<DocenteResumenResponse>`](#docenteresumenresponse)

Docentes activos (id, nombre, apellido, correo, especialidad) ordenados por apellido, para el selector de docente.

### `GET /api/docentes/mis-cursos`

- **Quién:** Docente
- **Parámetros de consulta (opcionales):** `estado` (EstadoCurso)
- **Respuesta:** `200` [`List<CursoResponse>`](#cursoresponse)

Cursos que dicta el docente autenticado, de cualquier estado. Filtro opcional `estado`.

### `GET /api/gestion/cursos`

- **Quién:** Coordinador, Administrador
- **Parámetros de consulta (opcionales):** `estado` (EstadoCurso)
- **Respuesta:** `200` [`List<CursoResponse>`](#cursoresponse)

Todos los cursos de la plataforma, de cualquier estado. Filtro opcional `estado`.

## Inscripciones

**Reglas de negocio**

- Solo estudiantes; el curso debe estar ACTIVO y tener cupo.
- Hay una sola inscripción por estudiante y curso: si estaba CANCELADA se reactiva (mismo `id`, fecha original); si está ACTIVA o COMPLETADA responde 409.
- Hoy ningún endpoint marca una inscripción como COMPLETADA.

### `POST /api/inscripciones`

- **Quién:** Estudiante
- **Cuerpo:** [`InscripcionRequest`](#inscripcionrequest)
- **Respuesta:** `201` [`InscripcionResponse`](#inscripcionresponse)

Inscribe al estudiante en un curso ACTIVO con cupo. Si tenía una inscripción CANCELADA, se reactiva la misma (mismo `id`).

### `PATCH /api/inscripciones/{id}/cancelar`

- **Quién:** Estudiante (dueño de la inscripción)
- **Respuesta:** `204`

Cancela la inscripción (queda CANCELADA, no se borra).

### `GET /api/inscripciones/mis-cursos`

- **Quién:** Estudiante
- **Respuesta:** `200` [`List<InscripcionResponse>`](#inscripcionresponse)

Inscripciones del estudiante autenticado, de cualquier estado.

## Lecciones

Pueden **leer** el docente, los coordinadores y los estudiantes con inscripción no cancelada (ACTIVA o COMPLETADA). Pueden **gestionar** el docente asignado y los coordinadores.

### `POST /api/cursos/{cursoId}/lecciones`

- **Quién:** Docente asignado, Coordinador
- **Cuerpo:** [`LeccionRequest`](#leccionrequest)
- **Respuesta:** `201` [`LeccionResponse`](#leccionresponse)

Crea una lección del curso.

### `GET /api/cursos/{cursoId}/lecciones`

- **Quién:** Docente asignado, Coordinador, Estudiante inscrito
- **Respuesta:** `200` [`List<LeccionResponse>`](#leccionresponse)

Lista las lecciones del curso ordenadas por `orden`.

### `PUT /api/cursos/{cursoId}/lecciones/{leccionId}`

- **Quién:** Docente asignado, Coordinador
- **Cuerpo:** [`LeccionRequest`](#leccionrequest)
- **Respuesta:** `200` [`LeccionResponse`](#leccionresponse)

Edita una lección.

### `DELETE /api/cursos/{cursoId}/lecciones/{leccionId}`

- **Quién:** Docente asignado, Coordinador
- **Respuesta:** `204`

Elimina la lección (borrado definitivo).

## Recursos bibliográficos

Mismos permisos que las lecciones.

### `POST /api/cursos/{cursoId}/recursos`

- **Quién:** Docente asignado, Coordinador
- **Cuerpo:** [`RecursoBibliograficoRequest`](#recursobibliograficorequest)
- **Respuesta:** `201` [`RecursoBibliograficoResponse`](#recursobibliograficoresponse)

Crea un recurso bibliográfico del curso.

### `GET /api/cursos/{cursoId}/recursos`

- **Quién:** Docente asignado, Coordinador, Estudiante inscrito
- **Respuesta:** `200` [`List<RecursoBibliograficoResponse>`](#recursobibliograficoresponse)

Lista los recursos del curso.

### `PUT /api/cursos/{cursoId}/recursos/{recursoId}`

- **Quién:** Docente asignado, Coordinador
- **Cuerpo:** [`RecursoBibliograficoRequest`](#recursobibliograficorequest)
- **Respuesta:** `200` [`RecursoBibliograficoResponse`](#recursobibliograficoresponse)

Edita un recurso.

### `DELETE /api/cursos/{cursoId}/recursos/{recursoId}`

- **Quién:** Docente asignado, Coordinador
- **Respuesta:** `204`

Elimina el recurso (borrado definitivo).

## Progreso de lecciones

Marcar progreso exige inscripción ACTIVA. Consultar el propio avance también se permite con inscripción COMPLETADA.

### `PATCH /api/cursos/{cursoId}/lecciones/{leccionId}/progreso`

- **Quién:** Estudiante con inscripción ACTIVA
- **Cuerpo:** [`ProgresoLeccionRequest`](#progresoleccionrequest)
- **Respuesta:** `200` [`ProgresoLeccionResponse`](#progresoleccionresponse)

Marca o desmarca una lección como completada.

### `GET /api/cursos/{cursoId}/progreso`

- **Quién:** Estudiante inscrito
- **Respuesta:** `200` [`CursoProgresoResponse`](#cursoprogresoresponse)

Avance propio en el curso: porcentaje y detalle por lección.

## Seguimiento de estudiantes

Los datos mostrados son solo los necesarios para el seguimiento (no incluyen documento de identidad).

### `GET /api/cursos/{cursoId}/estudiantes`

- **Quién:** Docente del curso, Coordinador, Administrador
- **Parámetros de consulta (opcionales):** `estado` (EstadoInscripcion)
- **Respuesta:** `200` [`List<EstudianteInscritoResponse>`](#estudianteinscritoresponse)

Inscritos del curso con datos básicos, estado de la inscripción y porcentaje de avance. Filtro opcional `estado` (estado de la inscripción).

### `GET /api/cursos/{cursoId}/estudiantes/{estudianteId}/progreso`

- **Quién:** Docente del curso, Coordinador, Administrador
- **Respuesta:** `200` [`CursoProgresoResponse`](#cursoprogresoresponse)

Avance lección por lección de un estudiante inscrito. 400 si no está inscrito en el curso.

## Foro

Cada curso tiene un foro (se crea con el curso). **Leer:** docente, coordinadores y estudiantes con inscripción ACTIVA o COMPLETADA. **Publicar:** docente, coordinadores y estudiantes con inscripción ACTIVA. **Editar o eliminar:** el autor, el docente del curso o un coordinador.

### `GET /api/cursos/{cursoId}/foro/mensajes`

- **Quién:** Docente asignado, Coordinador, Estudiante inscrito
- **Respuesta:** `200` [`List<MensajeForoResponse>`](#mensajefororesponse)

Mensajes raíz del foro con sus respuestas anidadas.

### `POST /api/cursos/{cursoId}/foro/mensajes`

- **Quién:** Docente asignado, Coordinador, Estudiante con inscripción ACTIVA
- **Cuerpo:** [`MensajeForoRequest`](#mensajefororequest)
- **Respuesta:** `201` [`MensajeForoResponse`](#mensajefororesponse)

Publica un mensaje; con `mensajePadreId` es una respuesta.

### `PUT /api/cursos/{cursoId}/foro/mensajes/{mensajeId}`

- **Quién:** Autor, Docente del curso, Coordinador
- **Cuerpo:** [`MensajeForoRequest`](#mensajefororequest)
- **Respuesta:** `200` [`MensajeForoResponse`](#mensajefororesponse)

Edita un mensaje (queda marcado como editado).

### `DELETE /api/cursos/{cursoId}/foro/mensajes/{mensajeId}`

- **Quién:** Autor, Docente del curso, Coordinador
- **Respuesta:** `204`

Elimina un mensaje.

## Evaluaciones y resultados

Las evaluaciones nacen en BORRADOR y deben publicarse. Los resultados los registra el estudiante con inscripción ACTIVA y los califica el docente del curso.

### `GET /api/cursos/{cursoId}/evaluaciones`

- **Quién:** Docente del curso, Estudiante inscrito
- **Respuesta:** `200` [`List<EvaluacionResponse>`](#evaluacionresponse)

Lista las evaluaciones del curso. Los estudiantes no ven las que están en BORRADOR.

### `POST /api/cursos/{cursoId}/examenes`

- **Quién:** Docente del curso
- **Cuerpo:** [`ExamenRequest`](#examenrequest)
- **Respuesta:** `201` [`ExamenResponse`](#examenresponse)

Crea un examen en estado BORRADOR. La suma de los pesos de las evaluaciones del curso no puede superar el máximo (409).

### `GET /api/examenes/{id}`

- **Quién:** Docente del curso, Estudiante inscrito
- **Respuesta:** `200` [`ExamenResponse`](#examenresponse)

Detalle de un examen.

### `POST /api/cursos/{cursoId}/tareas`

- **Quién:** Docente del curso
- **Cuerpo:** [`TareaRequest`](#tarearequest)
- **Respuesta:** `201` [`TareaResponse`](#tarearesponse)

Crea una tarea en estado BORRADOR. Mismo límite de suma de pesos.

### `GET /api/tareas/{id}`

- **Quién:** Docente del curso, Estudiante inscrito
- **Respuesta:** `200` [`TareaResponse`](#tarearesponse)

Detalle de una tarea.

### `PATCH /api/evaluaciones/{id}/publicar`

- **Quién:** Docente del curso
- **Respuesta:** `200` [`EvaluacionResponse`](#evaluacionresponse)

Publica una evaluación (solo desde BORRADOR).

### `POST /api/evaluaciones/{evaluacionId}/resultados`

- **Quién:** Estudiante con inscripción ACTIVA
- **Cuerpo:** [`RegistrarResultadoRequest`](#registrarresultadorequest)
- **Respuesta:** `201` [`ResultadoResponse`](#resultadoresponse)

Registra un intento de examen o la entrega de una tarea (`contenido` obligatorio en tareas). 409 si la evaluación está cerrada, el examen aún no está disponible, se agotaron los intentos, la tarea ya fue entregada o pasó la fecha límite.

### `PATCH /api/resultados/{id}/calificar`

- **Quién:** Docente del curso
- **Cuerpo:** [`CalificarResultadoRequest`](#calificarresultadorequest)
- **Respuesta:** `200` [`ResultadoResponse`](#resultadoresponse)

Califica un resultado (no puede superar la nota máxima).

### `GET /api/resultados/mis-calificaciones`

- **Quién:** Estudiante
- **Respuesta:** `200` [`List<ResultadoResponse>`](#resultadoresponse)

Resultados y calificaciones del estudiante autenticado.

### `GET /api/cursos/{cursoId}/calificaciones`

- **Quién:** Docente del curso
- **Respuesta:** `200` [`List<ResultadoResponse>`](#resultadoresponse)

Todas las calificaciones de los estudiantes del curso.

## Códigos de error

Todos los errores devuelven `{ "timestamp": "...", "status": 400, "mensaje": "..." }`.

| Código | Cuándo |
|---|---|
| 400 | Datos inválidos, validación de campos, valor de parámetro no válido, recurso no encontrado, formato de archivo no permitido |
| 401 | Sin token o token vencido o inválido |
| 403 | El rol o la relación con el recurso no permiten la acción |
| 409 | Conflicto con las reglas de negocio (cruce de horario, cupo lleno, ya inscrito, cuenta bloqueada o desactivada, restricción de la base de datos) |
| 413 | Archivo de más de 5 MB |

## Enumeraciones

| Enum | Valores |
|---|---|
| `DiaSemana` | `LUNES`, `MARTES`, `MIERCOLES`, `JUEVES`, `VIERNES`, `SABADO`, `DOMINGO` |
| `EstadoCurso` | `ACTIVO`, `INACTIVO`, `ARCHIVADO` |
| `EstadoEvaluacion` | `BORRADOR`, `PUBLICADA`, `CERRADA` |
| `EstadoInscripcion` | `ACTIVA`, `COMPLETADA`, `CANCELADA` |
| `EstadoResultado` | `PENDIENTE`, `CALIFICADO` |
| `NivelAccesoAdmin` | `SUPER_ADMIN`, `ADMIN` |
| `TipoContenidoLeccion` | `VIDEO`, `TEXTO`, `PDF`, `ENLACE` |
| `TipoDocumento` | `CEDULA`, `PASAPORTE`, `TARJETA_DE_IDENTIDAD` |
| `TipoRecurso` | `LIBRO`, `ARTICULO`, `VIDEO`, `ENLACE`, `DOCUMENTO` |

## Modelos (DTO)

Campos de las peticiones y respuestas. Las reglas indicadas son las validaciones del backend.

### ActualizarPerfilRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `nombre` | `String` | — |
| `apellido` | `String` | — |
| `telefono` | `String` | — |
| `fotoPerfilUrl` | `String` | — |

### AdministradorRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `nombre` | `String` | obligatorio |
| `apellido` | `String` | obligatorio |
| `correo` | `String` | obligatorio, correo válido |
| `contrasena` | `String` | obligatorio, mínimo 8 caracteres |
| `codigo` | `String` | obligatorio |
| `cargo` | `String` | obligatorio |
| `nivelAcceso` | `NivelAccesoAdmin` | — |

### AuthResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `accessToken` | `String` | — |
| `refreshToken` | `String` | — |
| `tipo` | `String` | — |
| `correo` | `String` | — |
| `rol` | `String` | — |

### CalificarResultadoRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `calificacion` | `Double` | obligatorio, mínimo 0.0 |
| `retroalimentacion` | `String` | — |

### CambiarContrasenaRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `contrasenaActual` | `String` | obligatorio |
| `contrasenaNueva` | `String` | obligatorio, mínimo 8 caracteres |
| `confirmarContrasenaNueva` | `String` | obligatorio |

### CoordinadorRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `nombre` | `String` | obligatorio |
| `apellido` | `String` | obligatorio |
| `correo` | `String` | obligatorio, correo válido |
| `contrasena` | `String` | obligatorio, mínimo 8 caracteres |
| `codigo` | `String` | obligatorio |
| `facultad` | `String` | obligatorio |
| `telefonoOficina` | `String` | — |

### CursoProgresoResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `cursoId` | `Long` | — |
| `totalLecciones` | `int` | — |
| `leccionesCompletadas` | `int` | — |
| `porcentajeAvance` | `double` | — |
| `detalle` | `List<ProgresoLeccionResponse>` | — |

### CursoRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `nombre` | `String` | obligatorio |
| `descripcion` | `String` | — |
| `fechaInicio` | `LocalDate` | obligatorio |
| `fechaFin` | `LocalDate` | obligatorio |
| `cupoMaximo` | `Integer` | mínimo 1 |
| `imagenPortadaUrl` | `String` | — |
| `docenteId` | `Long` | — |
| `horarios` | `List<HorarioRequest>` | se valida cada elemento |

### CursoResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | — |
| `nombre` | `String` | — |
| `descripcion` | `String` | — |
| `estado` | `EstadoCurso` | — |
| `docenteId` | `Long` | — |
| `docenteNombre` | `String` | — |
| `docenteCorreo` | `String` | — |
| `creadoPorNombre` | `String` | — |
| `fechaInicio` | `LocalDate` | — |
| `fechaFin` | `LocalDate` | — |
| `cupoMaximo` | `Integer` | — |
| `imagenPortadaUrl` | `String` | — |
| `createdAt` | `LocalDateTime` | — |
| `horarios` | `List<HorarioResponse>` | — |

### DocenteRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `nombre` | `String` | obligatorio |
| `apellido` | `String` | obligatorio |
| `correo` | `String` | obligatorio, correo válido |
| `contrasena` | `String` | obligatorio, mínimo 8 caracteres |
| `codigoDocente` | `String` | obligatorio |
| `especialidad` | `String` | obligatorio |
| `tituloProfesional` | `String` | obligatorio |
| `aniosExperiencia` | `Integer` | — |
| `biografia` | `String` | — |

### DocenteResumenResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | — |
| `nombre` | `String` | — |
| `apellido` | `String` | — |
| `correo` | `String` | — |
| `especialidad` | `String` | — |

### EstudianteInscritoResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `inscripcionId` | `Long` | — |
| `estudianteId` | `Long` | — |
| `nombre` | `String` | — |
| `correo` | `String` | — |
| `codigoEstudiantil` | `String` | — |
| `programaAcademico` | `String` | — |
| `semestre` | `Integer` | — |
| `estadoInscripcion` | `EstadoInscripcion` | — |
| `fechaInscripcion` | `LocalDateTime` | — |
| `calificacionFinal` | `Double` | — |
| `totalLecciones` | `int` | — |
| `leccionesCompletadas` | `int` | — |
| `porcentajeAvance` | `double` | — |

### EvaluacionResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | — |
| `cursoId` | `Long` | — |
| `tipo` | `String` | — |
| `titulo` | `String` | — |
| `descripcion` | `String` | — |
| `pesoPorcentual` | `Double` | — |
| `estado` | `EstadoEvaluacion` | — |
| `fechaPublicacion` | `LocalDateTime` | — |
| `createdAt` | `LocalDateTime` | — |

### ExamenRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `titulo` | `String` | obligatorio |
| `descripcion` | `String` | — |
| `pesoPorcentual` | `Double` | obligatorio, mínimo 0.0 (exclusivo), máximo 100.0 |
| `fechaAplicacion` | `LocalDateTime` | obligatorio |
| `tiempoLimite` | `Integer` | obligatorio, mínimo 1 |
| `numeroIntentosPermitidos` | `Integer` | mínimo 1 |
| `aleatorio` | `boolean` | — |

### ExamenResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | — |
| `cursoId` | `Long` | — |
| `titulo` | `String` | — |
| `descripcion` | `String` | — |
| `pesoPorcentual` | `Double` | — |
| `estado` | `EstadoEvaluacion` | — |
| `fechaPublicacion` | `LocalDateTime` | — |
| `fechaAplicacion` | `LocalDateTime` | — |
| `tiempoLimite` | `Integer` | — |
| `numeroIntentosPermitidos` | `Integer` | — |
| `aleatorio` | `boolean` | — |
| `createdAt` | `LocalDateTime` | — |

### HorarioRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `diaSemana` | `DiaSemana` | obligatorio |
| `horaInicio` | `LocalTime` | obligatorio |
| `horaFin` | `LocalTime` | obligatorio |

### HorarioResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `diaSemana` | `DiaSemana` | — |
| `horaInicio` | `LocalTime` | — |
| `horaFin` | `LocalTime` | — |

### InscripcionRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `cursoId` | `Long` | obligatorio |

### InscripcionResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | — |
| `cursoId` | `Long` | — |
| `cursoNombre` | `String` | — |
| `docenteNombre` | `String` | — |
| `estado` | `EstadoInscripcion` | — |
| `fechaInscripcion` | `LocalDateTime` | — |
| `calificacionFinal` | `Double` | — |

### LeccionRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `titulo` | `String` | obligatorio |
| `contenido` | `String` | — |
| `tipoContenido` | `TipoContenidoLeccion` | obligatorio |
| `duracionMinutos` | `Integer` | — |
| `orden` | `Integer` | obligatorio, mínimo 1 |

### LeccionResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | — |
| `cursoId` | `Long` | — |
| `titulo` | `String` | — |
| `contenido` | `String` | — |
| `tipoContenido` | `TipoContenidoLeccion` | — |
| `duracionMinutos` | `Integer` | — |
| `orden` | `Integer` | — |
| `createdAt` | `LocalDateTime` | — |

### LoginRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `correo` | `String` | obligatorio, correo válido |
| `contrasena` | `String` | obligatorio |

### MensajeForoRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `contenido` | `String` | obligatorio |
| `mensajePadreId` | `Long` | — |

### MensajeForoResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | — |
| `autorNombre` | `String` | — |
| `autorRol` | `String` | — |
| `contenido` | `String` | — |
| `editado` | `boolean` | — |
| `fechaPublicacion` | `LocalDateTime` | — |
| `respuestas` | `List<MensajeForoResponse>` | — |

### ProgresoLeccionRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `completado` | `Boolean` | obligatorio |

### ProgresoLeccionResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `leccionId` | `Long` | — |
| `leccionTitulo` | `String` | — |
| `orden` | `Integer` | — |
| `completado` | `boolean` | — |
| `fechaCompletado` | `LocalDateTime` | — |

### RecursoBibliograficoRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `titulo` | `String` | obligatorio |
| `autor` | `String` | — |
| `anioPublicacion` | `Integer` | — |
| `tipoRecurso` | `TipoRecurso` | obligatorio |
| `enlace` | `String` | obligatorio |

### RecursoBibliograficoResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | — |
| `cursoId` | `Long` | — |
| `titulo` | `String` | — |
| `autor` | `String` | — |
| `anioPublicacion` | `Integer` | — |
| `tipoRecurso` | `TipoRecurso` | — |
| `enlace` | `String` | — |
| `createdAt` | `LocalDateTime` | — |

### RefreshTokenRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `refreshToken` | `String` | obligatorio |

### RegistrarResultadoRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `contenido` | `String` | — |

### RegistroRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `nombre` | `String` | obligatorio |
| `apellido` | `String` | obligatorio |
| `correo` | `String` | obligatorio, correo válido |
| `contrasena` | `String` | obligatorio, mínimo 8 caracteres |
| `codigoEstudiantil` | `String` | obligatorio |
| `tipoDocumento` | `TipoDocumento` | obligatorio |
| `numeroDocumento` | `String` | obligatorio |
| `programaAcademico` | `String` | obligatorio |
| `semestre` | `Integer` | obligatorio, mínimo 1 |

### ResultadoResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | — |
| `evaluacionId` | `Long` | — |
| `evaluacionTitulo` | `String` | — |
| `cursoId` | `Long` | — |
| `estudianteId` | `Long` | — |
| `estudianteNombre` | `String` | — |
| `numeroIntento` | `Integer` | — |
| `calificacion` | `Double` | — |
| `retroalimentacion` | `String` | — |
| `estado` | `EstadoResultado` | — |
| `contenidoEntrega` | `String` | — |
| `entregaTardia` | `boolean` | — |
| `fechaRegistro` | `LocalDateTime` | — |

### TareaRequest

| Campo | Tipo | Reglas |
|---|---|---|
| `titulo` | `String` | obligatorio |
| `descripcion` | `String` | — |
| `pesoPorcentual` | `Double` | obligatorio, mínimo 0.0 (exclusivo), máximo 100.0 |
| `instrucciones` | `String` | — |
| `fechaLimite` | `LocalDateTime` | obligatorio |
| `permiteEntregaTardia` | `boolean` | — |
| `penalizacionTardanzaPorcentaje` | `Double` | mínimo 0.0, máximo 100.0 |

### TareaResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | — |
| `cursoId` | `Long` | — |
| `titulo` | `String` | — |
| `descripcion` | `String` | — |
| `pesoPorcentual` | `Double` | — |
| `estado` | `EstadoEvaluacion` | — |
| `fechaPublicacion` | `LocalDateTime` | — |
| `instrucciones` | `String` | — |
| `fechaLimite` | `LocalDateTime` | — |
| `permiteEntregaTardia` | `boolean` | — |
| `penalizacionTardanzaPorcentaje` | `Double` | — |
| `createdAt` | `LocalDateTime` | — |

### UsuarioResponse

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | `Long` | — |
| `nombre` | `String` | — |
| `apellido` | `String` | — |
| `correo` | `String` | — |
| `rol` | `String` | — |
| `activo` | `boolean` | — |
| `createdAt` | `LocalDateTime` | — |
| `telefono` | `String` | — |
| `fotoPerfilUrl` | `String` | — |