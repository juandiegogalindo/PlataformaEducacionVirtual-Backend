# Objetivo del Entorno
Este repositorio y su suite de pruebas permiten:Validar la lógica de negocio mediante pruebas de unidad e integración con JUnit 5 y Spring Security Test.
Verificar la disponibilidad y comportamiento del contrato REST API mediante la Colección de Postman en entornos locales/QA.
Garantizar el correcto aislamiento de roles (ESTUDIANTE, DOCENTE, COORDINADOR, ADMINISTRADOR) mediante tokens JWT.
# Requisitos Previos
Asegúrate de contar con los siguientes elementos instalados en tu máquina:
Java JDK 17 o superior.
Apache Maven 3.8+.
PostgreSQL 14+ ejecutándose localmente o en contenedor Docker.
Postman (o Newman para ejecución mediante CLI).
# Configuración del Entorno de Pruebas
La aplicación utiliza variables de entorno con valores por defecto orientados al desarrollo local (application.properties):
#Servidor
server.port=8080
spring.application.name=plataforma-educativa-virtual

#Base de Datos
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/plataforma_educativa}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:admin}

#Seguridad & JWT
app.jwt.secret=${JWT_SECRET:cambia_esta_clave_por_una_generada_de_al_menos_256_bits}
app.jwt.expiration-ms=86400000        # 24 horas
app.jwt.refresh-expiration-ms=604800000 # 7 días

#CORS
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:5173}


# Base de Datos de Pruebas
Para aislar los datos durante la ejecución de pruebas locales:Bash# Crear la base de datos PostgreSQL
psql -U postgres -c "CREATE DATABASE plataforma_educativa;"
Nota: La propiedad spring.jpa.hibernate.ddl-auto=update generará y actualizará automáticamente el esquema de tablas en desarrollo y pruebas.

# Ejecución de Pruebas Automáticas (JUnit 5 & Mockito)
Ejecuta las pruebas integradas y unitarias definidas en el proyecto con Maven:Bash# Ejecutar todas las pruebas unitarias y de integración
mvn clean test

# Ejecutar un test específico
mvn test -Dtest=NombreDeLaClaseTest


# Pruebas de Endpoints API (Postman Collection)
El proyecto incluye una colección completa de Postman (v2.1.0) que cubre todo el ciclo de vida de la aplicación.
Variables de Colección
La colección maneja automáticamente las siguientes variables de entorno/colección para encadenar las peticiones de prueba:
Variable            Valor por Defecto                     Descripción
baseUrl         http://localhost:8080                  URL base del backend
token                 (Dinámico)                          Token JWT (Bearer) activo
refreshToken          (Dinámico)                       Token de refresco de sesión 
cursoId               (Dinámico)                       ID del curso creado durante la prueba
docenteId             (Dinámico)                       ID del docente creado
inscripcionId         (Dinámico)                       ID de la inscripción activa
leccionId             (Dinámico)                       ID de lección de un curso
recursoId             (Dinámico)                       ID de recurso bibliográfico

# Módulos Evaluados
La colección está organizada en las siguientes carpetas:

├── 0. Health                # Endpoint de chequeo de estado
├── 1. Auth                  # Registro, Login (Admin/Docente/Estudiante), Refresh & Logout
├── 2. Admin                 # Gestión de Docentes, Coordinadores, Admins y estado de usuarios
├── 3. Perfil                # Consulta, actualización y cambio de contraseña
├── 4. Cursos                # CRUD de Cursos (Público, Docente, Admin)
├── 5. Inscripciones         # Inscripción de alumnos, mis cursos y cancelación
├── 6. Lecciones             # Gestión de contenidos por curso
└── 7. Recursos bibliograficos # Gestión de material bibliográfico

# Flujo de Autenticación y Test Scripts
Las peticiones de Login y Registro incluyen scripts de prueba automáticos en Javascript que capturan y asignan el token JWT globalmente:
JavaScript// Postman Test Script
const response = pm.response.json();
if (response.accessToken) {
    pm.collectionVariables.set('token', response.accessToken);
    pm.collectionVariables.set('refreshToken', response.refreshToken);
}
# Roles y Control de Acceso
Durante la suite de pruebas se deben validar los permisos según el rol:
Rol                             Permisos principales evaluados
PUBLIC                          Ver catálogo de cursos, Health check
ESTUDIANTE                      Inscribirse a cursos, ver contenidos, actualizar su perfil
DOCENTE                         Crear y editar cursos, agregar lecciones y recursos bibliográficos
ADMINISTRADOR                   Crear docentes, coordinadores, administradores y activar/desactivar
