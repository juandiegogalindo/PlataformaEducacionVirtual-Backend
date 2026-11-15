# Plataforma de Educación Virtual - Bit Criollo (Backend)

API REST para una plataforma de educación virtual con gestión de cursos, inscripciones, contenidos, evaluaciones, calificaciones y foros, construida con Spring Boot, Spring Security y JWT.

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED?logo=docker&logoColor=white)
![Postman](https://img.shields.io/badge/Postman-Collection-FF6C37?logo=postman&logoColor=white)

---

## Tabla de contenidos

1. [Descripción](#descripción)
2. [Características principales](#características-principales)
3. [Roles y permisos](#roles-y-permisos)
4. [Tecnologías utilizadas](#tecnologías-utilizadas)
5. [Arquitectura y estructura del proyecto](#arquitectura-y-estructura-del-proyecto)
6. [Modelo de datos](#modelo-de-datos)
7. [Requisitos previos](#requisitos-previos)
8. [Instalación y ejecución](#instalación-y-ejecución)
9. [Configuración](#configuración)
10. [Autenticación y seguridad](#autenticación-y-seguridad)
11. [Endpoints de la API](#endpoints-de-la-api)
12. [Reglas de negocio](#reglas-de-negocio)
13. [Manejo de errores](#manejo-de-errores)
14. [Pruebas con Postman](#pruebas-con-postman)
15. [Despliegue](#despliegue)
16. [Limitaciones conocidas](#limitaciones-conocidas)
17. [Roadmap](#roadmap)
18. [Equipo](#equipo)

---

## Descripción

**Bit Criollo** es una plataforma de educación virtual que centraliza en un solo servicio las actividades académicas de una institución: publicación de cursos, inscripción de estudiantes, lecciones, recursos bibliográficos, seguimiento del progreso, evaluaciones (exámenes y tareas), calificación y un foro por curso.

Este repositorio contiene el **backend**: una API REST sin estado (*stateless*) que expone sus servicios en formato JSON y protege sus rutas mediante autenticación con JSON Web Tokens (JWT) y control de acceso por rol. Fue desarrollado como proyecto académico del curso de Ingeniería de Software de la Universidad Piloto de Colombia (ciclo 2026-2).

## Características principales

- Registro de estudiantes e inicio de sesión con tokens de acceso de corta duración y *refresh tokens* revocables.
- Cuatro roles (Estudiante, Docente, Coordinador Académico y Administrador) con permisos diferenciados a nivel de endpoint y de servicio.
- Gestión completa de cursos con estados (`ACTIVO`, `INACTIVO`, `ARCHIVADO`), cupo máximo y fechas de inicio y fin.
- Lecciones ordenadas por curso (video, texto, PDF o enlace) y recursos bibliográficos asociados.
- Seguimiento del progreso por lección y por curso para cada estudiante.
- Evaluaciones de dos tipos: **exámenes** (con tiempo límite, intentos permitidos y fecha de aplicación) y **tareas** (con fecha límite y penalización por entrega tardía).
- Ciclo de vida de evaluaciones (`BORRADOR`, `PUBLICADA`, `CERRADA`) y calificación en escala de 0.0 a 5.0 con retroalimentación.
- Foro por curso con mensajes, respuestas anidadas y edición o eliminación de mensajes propios.
- Auditoría de accesos y bloqueo temporal de cuentas tras intentos fallidos de inicio de sesión.
- Perfil de usuario con actualización de datos y cambio de contraseña.
- Administración de usuarios: creación de docentes, coordinadores y administradores, y activación o desactivación de cuentas.
- Scripts SQL de creación, población y eliminación de la base de datos, y colección de Postman lista para usar.
- Imagen Docker multi-etapa para despliegue.

## Roles y permisos

| Rol | Descripción | Capacidades principales |
|---|---|---|
| Estudiante | Se registra por sí mismo mediante la API pública. | Inscribirse a cursos y cancelar su inscripción, consultar contenidos, marcar lecciones como completadas, presentar exámenes y tareas, ver sus calificaciones y participar en el foro. |
| Docente | Creado por un administrador. | Crear y gestionar sus cursos, lecciones, recursos y evaluaciones; publicar evaluaciones; calificar resultados; consultar las calificaciones de sus cursos. |
| Coordinador Académico | Creado por un administrador. | Supervisar y gestionar el contenido de cualquier curso; crear cursos asignando un docente responsable. |
| Administrador | Creado por un administrador. | Crear usuarios de los demás roles, listar usuarios y activar o desactivar cuentas. Existen los niveles `SUPER_ADMIN` y `ADMIN`. |

## Tecnologías utilizadas

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.4 (Web, Data JPA, Security, Validation) |
| Persistencia | JPA / Hibernate sobre PostgreSQL |
| Seguridad | Spring Security, JWT (JJWT 0.12.6), BCrypt |
| Utilidades | Lombok |
| Construcción | Maven |
| Contenedores | Docker (multi-etapa con Eclipse Temurin 17) |
| Pruebas de API | Postman |
| Despliegue | Render |

## Arquitectura y estructura del proyecto

El backend sigue una arquitectura por capas dentro del paquete `com.bitcriollo.plataforma`:

```text
PlataformaEducacionVirtual-Backend/
├── Dockerfile                      # Imagen multi-etapa (build con Maven, ejecución con JRE)
├── pom.xml
├── docs/
│   └── API.md                      # Documentación de la API (en construcción)
├── postman/
│   └── PlataformaEducacionVirtual.postman_collection.json
├── scripts/
│   ├── 01_creacion.sql             # Creación del esquema
│   ├── 02_poblacion.sql            # Datos de prueba (un usuario por rol)
│   └── 03_eliminacion.sql          # Eliminación de tablas
└── src/main/
    ├── java/com/bitcriollo/plataforma/
    │   ├── PlataformaEducativaApplication.java
    │   ├── config/                 # SecurityConfig (filtros, CORS, rutas públicas)
    │   ├── controller/             # Endpoints REST
    │   ├── dto/                    # Objetos de petición y respuesta
    │   ├── exception/              # Manejador global de errores
    │   ├── model/                  # Entidades JPA y enumeraciones
    │   ├── repository/             # Interfaces Spring Data JPA
    │   ├── security/               # Filtro JWT, servicio de tokens, UserDetails
    │   └── service/                # Lógica de negocio
    └── resources/
        └── application.properties
```

Flujo típico de una petición: el filtro `JwtAuthFilter` valida el token, el controlador recibe y valida el DTO, el servicio aplica las reglas de negocio y de acceso, y el repositorio persiste en PostgreSQL. Los errores se centralizan en `GlobalExceptionHandler`.

## Modelo de datos

La base de datos se compone de las siguientes tablas:

| Grupo | Tablas |
|---|---|
| Usuarios (herencia `JOINED`) | `usuarios`, `estudiantes`, `docentes`, `coordinadores_academicos`, `administradores` |
| Cursos y contenido | `cursos`, `lecciones`, `recursos_bibliograficos`, `foros`, `mensajes_foro` |
| Seguimiento | `inscripciones`, `progreso_lecciones` |
| Evaluación (herencia `JOINED`) | `evaluaciones`, `examenes`, `tareas`, `resultados` |
| Seguridad y auditoría | `refresh_tokens`, `log_accesos` |

Relaciones relevantes:

- Cada usuario concreto comparte la llave primaria de `usuarios`; el rol se determina por la tabla de subtipo.
- Un curso pertenece a un docente, registra quién lo creó y tiene exactamente un foro.
- Una inscripción es única por pareja estudiante-curso, y el progreso es único por pareja estudiante-lección.
- Un resultado es único por evaluación, estudiante y número de intento.

## Requisitos previos

- JDK 17
- Maven 3.9 o superior (o el que incluya tu IDE)
- PostgreSQL 13 o superior
- Docker (opcional, para ejecutar mediante contenedor)
- Postman (opcional, para probar la API)

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/juandiegogalindo/PlataformaEducacionVirtual-Backend.git
cd PlataformaEducacionVirtual-Backend
```

### 2. Crear la base de datos

```sql
CREATE DATABASE plataforma_educativa;
```

No es necesario ejecutar scripts para arrancar: con `ddl-auto=update`, Hibernate crea las tablas automáticamente al conectarse a una base vacía. Si prefieres crear el esquema de forma explícita, o cargar datos de prueba:

```bash
psql -U postgres -d plataforma_educativa -f scripts/01_creacion.sql
psql -U postgres -d plataforma_educativa -f scripts/02_poblacion.sql
```

### 3. Configurar variables de entorno

Consulta la sección [Configuración](#configuración). Para desarrollo local, los valores por defecto funcionan si tu PostgreSQL usa el usuario `postgres` y el puerto 5432.

### 4. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La API quedará disponible en `http://localhost:8080`. Verifica que esté activa:

```bash
curl http://localhost:8080/api/health
```

```json
{
  "estado": "activo",
  "proyecto": "Plataforma de Educacion Virtual",
  "equipo": "Bit Criollo"
}
```

### Ejecución con Docker

```bash
docker build -t bitcriollo-plataforma .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/plataforma_educativa \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=tu_contrasena \
  -e JWT_SECRET=una_clave_aleatoria_de_al_menos_32_caracteres \
  bitcriollo-plataforma
```

### Usuarios de prueba

Si ejecutaste `02_poblacion.sql`, existe un usuario por rol, todos con la contraseña `clave12345`. Ejemplos: `admin@test.com` (administrador) y `coord@test.com` (coordinador). Estos datos son exclusivamente para desarrollo.

## Configuración

Toda la configuración sensible se inyecta mediante variables de entorno:

| Variable | Descripción | Valor por defecto (solo desarrollo) |
|---|---|---|
| `PORT` | Puerto del servidor | `8080` |
| `DB_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/plataforma_educativa` |
| `DB_USERNAME` | Usuario de la base de datos | `postgres` |
| `DB_PASSWORD` | Contraseña de la base de datos | valor de desarrollo |
| `JWT_SECRET` | Clave para firmar los tokens (mínimo 256 bits) | valor de ejemplo |
| `CORS_ALLOWED_ORIGINS` | Orígenes del frontend permitidos, separados por coma | `http://localhost:5173` |

Parámetros fijos en `application.properties`: el token de acceso expira a los **15 minutos** y el *refresh token* a los **7 días**.

## Autenticación y seguridad

1. El estudiante se registra en `POST /api/auth/registro` o cualquier usuario inicia sesión en `POST /api/auth/login`.
2. La respuesta incluye un `accessToken` (JWT), un `refreshToken`, el tipo `Bearer`, el correo y el rol.
3. Las rutas protegidas requieren la cabecera:

```text
Authorization: Bearer <accessToken>
```

4. Cuando el token de acceso expira, se solicita uno nuevo en `POST /api/auth/refresh`. Cada *refresh token* se usa una sola vez: al renovarse, el anterior queda revocado.
5. `POST /api/auth/logout` revoca el *refresh token* enviado.

Medidas implementadas:

- Contraseñas almacenadas con hash BCrypt, con longitud mínima de 8 caracteres.
- API sin sesiones en el servidor (`STATELESS`) y respuesta `401` para tokens ausentes o inválidos y `403` para falta de permisos.
- Control de acceso por rol con `@PreAuthorize` y verificación adicional de pertenencia al curso en la capa de servicio.
- CORS restringido a los orígenes configurados.
- Bloqueo temporal de la cuenta durante 15 minutos tras 3 intentos fallidos consecutivos.
- Registro de cada intento de acceso (correo, resultado, IP y fecha) en `log_accesos`.
- Cuentas desactivables por un administrador.

## Endpoints de la API

Todas las rutas parten de la URL base del servidor. Las rutas marcadas como públicas no requieren token.

### Salud y autenticación

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/health` | Público | Verifica el estado del servicio |
| POST | `/api/auth/registro` | Público | Registra un estudiante |
| POST | `/api/auth/login` | Público | Inicia sesión |
| POST | `/api/auth/refresh` | Público | Renueva el token de acceso |
| POST | `/api/auth/logout` | Público | Revoca el refresh token |

### Perfil

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/perfil` | Autenticado | Consulta el perfil propio |
| PATCH | `/api/perfil` | Autenticado | Actualiza datos del perfil |
| PATCH | `/api/perfil/contrasena` | Autenticado | Cambia la contraseña |

### Administración

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/admin/docentes` | Administrador | Crea un docente |
| POST | `/api/admin/coordinadores` | Administrador | Crea un coordinador |
| POST | `/api/admin/administradores` | Administrador | Crea un administrador |
| GET | `/api/admin/docentes` | Administrador | Lista docentes |
| GET | `/api/admin/coordinadores` | Administrador | Lista coordinadores |
| GET | `/api/admin/administradores` | Administrador | Lista administradores |
| GET | `/api/admin/estudiantes` | Administrador | Lista estudiantes |
| PATCH | `/api/admin/usuarios/{id}/desactivar` | Administrador | Desactiva una cuenta |
| PATCH | `/api/admin/usuarios/{id}/activar` | Administrador | Reactiva una cuenta |

### Cursos

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/cursos` | Público | Lista los cursos |
| GET | `/api/cursos/{id}` | Público | Consulta un curso |
| POST | `/api/cursos` | Docente, Coordinador | Crea un curso |
| PUT | `/api/cursos/{id}` | Docente, Coordinador | Actualiza un curso |
| DELETE | `/api/cursos/{id}` | Docente, Coordinador | Archiva un curso |

### Inscripciones

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/inscripciones` | Estudiante | Se inscribe en un curso |
| GET | `/api/inscripciones/mis-cursos` | Estudiante | Lista sus cursos |
| PATCH | `/api/inscripciones/{id}/cancelar` | Estudiante | Cancela su inscripción |

### Contenido del curso

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/cursos/{cursoId}/lecciones` | Con acceso al curso | Lista las lecciones |
| POST | `/api/cursos/{cursoId}/lecciones` | Docente, Coordinador | Crea una lección |
| PUT | `/api/cursos/{cursoId}/lecciones/{leccionId}` | Docente, Coordinador | Actualiza una lección |
| DELETE | `/api/cursos/{cursoId}/lecciones/{leccionId}` | Docente, Coordinador | Elimina una lección |
| GET | `/api/cursos/{cursoId}/recursos` | Con acceso al curso | Lista los recursos bibliográficos |
| POST | `/api/cursos/{cursoId}/recursos` | Docente, Coordinador | Crea un recurso |
| PUT | `/api/cursos/{cursoId}/recursos/{recursoId}` | Docente, Coordinador | Actualiza un recurso |
| DELETE | `/api/cursos/{cursoId}/recursos/{recursoId}` | Docente, Coordinador | Elimina un recurso |

### Progreso

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| PATCH | `/api/cursos/{cursoId}/lecciones/{leccionId}/progreso` | Estudiante | Marca una lección como completada |
| GET | `/api/cursos/{cursoId}/progreso` | Estudiante | Consulta su progreso en el curso |

### Foro

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/cursos/{cursoId}/foro/mensajes` | Estudiante, Docente, Coordinador | Lista los mensajes |
| POST | `/api/cursos/{cursoId}/foro/mensajes` | Estudiante, Docente, Coordinador | Publica un mensaje o una respuesta |
| PUT | `/api/cursos/{cursoId}/foro/mensajes/{mensajeId}` | Estudiante, Docente, Coordinador | Edita un mensaje propio |
| DELETE | `/api/cursos/{cursoId}/foro/mensajes/{mensajeId}` | Estudiante, Docente, Coordinador | Elimina un mensaje propio |

### Evaluaciones y calificaciones

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/cursos/{cursoId}/evaluaciones` | Con acceso al curso | Lista las evaluaciones |
| POST | `/api/cursos/{cursoId}/examenes` | Docente | Crea un examen |
| GET | `/api/examenes/{id}` | Con acceso al curso | Consulta un examen |
| POST | `/api/cursos/{cursoId}/tareas` | Docente | Crea una tarea |
| GET | `/api/tareas/{id}` | Con acceso al curso | Consulta una tarea |
| PATCH | `/api/evaluaciones/{id}/publicar` | Docente | Publica una evaluación |
| POST | `/api/evaluaciones/{evaluacionId}/resultados` | Estudiante | Registra un intento de examen o una entrega de tarea |
| PATCH | `/api/resultados/{id}/calificar` | Docente | Califica un resultado |
| GET | `/api/resultados/mis-calificaciones` | Estudiante | Consulta sus calificaciones |
| GET | `/api/cursos/{cursoId}/calificaciones` | Docente | Consulta las calificaciones del curso |

### Ejemplo: inicio de sesión

Petición:

```http
POST /api/auth/login
Content-Type: application/json

{
  "correo": "admin@test.com",
  "contrasena": "clave12345"
}
```

Respuesta:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "3f1c9a52-8d7e-4b6a-9c1f-2a7e5d0b8c34",
  "tipo": "Bearer",
  "correo": "admin@test.com",
  "rol": "Administrador"
}
```

## Reglas de negocio

- **Inscripciones:** solo se permiten en cursos activos, una sola vez por estudiante y curso, y respetando el cupo máximo. La verificación del cupo bloquea la fila del curso para evitar que inscripciones simultáneas lo superen. Una inscripción cancelada no puede cancelarse de nuevo.
- **Acceso al contenido:** el estudiante accede mientras su inscripción esté activa; con la inscripción completada conserva la consulta de su progreso; con la inscripción cancelada pierde el acceso. El coordinador puede gestionar cualquier curso y el docente solo los suyos.
- **Cursos:** un coordinador que crea un curso debe indicar el docente responsable. Cada curso nuevo se crea con su foro asociado.
- **Evaluaciones:** toda evaluación nace en `BORRADOR` y solo pasa a `PUBLICADA` una vez. Los estudiantes no ven evaluaciones sin publicar. La suma de los pesos porcentuales de las evaluaciones de un curso no puede superar el 100 %.
- **Exámenes:** no se pueden presentar antes de su fecha de aplicación ni superar el número de intentos permitidos.
- **Tareas:** admiten una sola entrega por estudiante, que debe incluir contenido. Pasada la fecha límite solo se aceptan entregas si la tarea permite entrega tardía, y en ese caso se aplica la penalización configurada.
- **Calificaciones:** escala de 0.0 a 5.0. Solo el docente asignado al curso puede calificar y consultar las calificaciones.
- **Autenticación:** tras 3 intentos fallidos la cuenta se bloquea 15 minutos; un inicio de sesión exitoso reinicia el contador.

## Manejo de errores

Los errores se devuelven con una estructura uniforme que incluye fecha y hora, código de estado y mensaje:

| Código | Situación |
|---|---|
| 400 | Datos inválidos, JSON mal formado o argumentos no válidos |
| 401 | Credenciales incorrectas o token ausente, inválido o expirado |
| 403 | El usuario autenticado no tiene permiso para la operación |
| 409 | Conflicto con el estado actual (cupo lleno, inscripción duplicada, cuenta bloqueada, violación de integridad) |

## Pruebas con Postman

La carpeta `postman/` incluye una colección con más de 50 peticiones organizadas por módulo (autenticación, administración, perfil, cursos, inscripciones, lecciones, recursos, progreso, foro, evaluaciones y resultados).

1. Importa `PlataformaEducacionVirtual.postman_collection.json` en Postman.
2. Ajusta la variable `baseUrl`. Por defecto apunta al despliegue en Render; para pruebas locales usa `http://localhost:8080`.
3. Inicia sesión con alguno de los usuarios de prueba: la colección administra las variables `token`, `refreshToken` y los identificadores de recursos.
4. Recorre las carpetas en orden numérico.

## Despliegue

El proyecto incluye un `Dockerfile` multi-etapa que compila con Maven y ejecuta el artefacto sobre un JRE ligero. El puerto se toma de la variable `PORT`, lo que lo hace compatible con plataformas como Render. En el despliegue deben definirse las variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` y `CORS_ALLOWED_ORIGINS`.

## Limitaciones conocidas

- **Valores por defecto sensibles:** `application.properties` incluye valores de reserva para la contraseña de la base de datos y la clave JWT. Son solo para desarrollo; en cualquier entorno compartido deben sobrescribirse con variables de entorno.
- **Esquema gestionado por Hibernate:** `ddl-auto=update` es cómodo en desarrollo, pero en producción se recomienda una herramienta de migraciones como Flyway o Liquibase.
- **Registro de consultas SQL activo:** `spring.jpa.show-sql=true` imprime todas las sentencias; debería desactivarse en producción.
- **Sin pruebas automatizadas:** el proyecto incluye las dependencias de pruebas, pero aún no existe el directorio `src/test`. La validación se realiza con la colección de Postman.
- **Documentación de API incompleta:** `docs/API.md` cubre por ahora la descripción general y la autenticación; el detalle de cada endpoint está en la colección de Postman y en este README.
- **Sin límite de peticiones global:** la protección contra fuerza bruta se limita al bloqueo por cuenta tras intentos fallidos de inicio de sesión.
- **Sin verificación de correo ni recuperación de contraseña:** el campo `email_verificado` existe en el modelo, pero aún no hay un flujo que lo utilice.
- **Usuarios de prueba:** `02_poblacion.sql` crea cuentas con una contraseña conocida; no debe ejecutarse en un entorno con datos reales.

## Equipo

Proyecto desarrollado por el grupo **Bit Criollo** en la Universidad Piloto de Colombia (ciclo 2026-2).

Contribuyentes según el historial de commits del repositorio:

| Usuario de GitHub | Perfil |
|---|---|
| juandiegogalindo | [@juandiegogalindo](https://github.com/juandiegogalindo) |
| CristianCortes1 | [@CristianCortes1](https://github.com/CristianCortes1) |
| Sofia-Torres11 | [@Sofia-Torres11](https://github.com/Sofia-Torres11) |
| miguelardila | [@miguelardila](https://github.com/miguelardila) |
| JPcoronado3 | [@miguelardila](https://github.com/JPcoronado3) |
