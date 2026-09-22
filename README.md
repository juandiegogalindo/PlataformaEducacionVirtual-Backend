PlataformaEducacionVirtual-Backend

Diseñar, desarrollar e implementar el prototipo de una plataforma de educación virtual que permita aprender en espacios en línea con acceso a diversas fuentes de información. El proyecto implementará el diseño y desarrollo de un prototipo de plataforma de educación virtual.

Estás en la rama desarrollo: aquí se sube el código día a día mientras se programa. Puede tener errores o estar a medias — aquí se trabaja libremente.

Ramas del proyecto
Rama	Para qué sirve
desarrollo	Donde suben el código día a día, mientras programan. Puede tener errores, estar a medias, no importa — aquí se trabaja libremente.
pruebas	Cuando algo en desarrollo ya está "terminado" y quieren validarlo (correr las pruebas unitarias, revisar que funcione), lo pasan aquí antes de darlo por bueno.
main	La versión estable y probada, lista para mostrarle al profesor/cliente. Solo debería recibir código que ya pasó por pruebas sin problemas.
Tecnologías
Java 17
Spring Boot 3.3.4
Spring Web (controladores REST)
Spring Data JPA
Spring Security
Bean Validation
PostgreSQL (base de datos)
JWT (jjwt) para autenticación y refresh tokens
Lombok (reduce código repetitivo)
Maven (gestión del proyecto)
Estructura del proyecto
src/main/java/com/bitcriollo/plataforma
├── config/          # Configuración general (SecurityConfig, etc.)
├── controller/       # Endpoints REST
├── dto/               # Objetos de transferencia (Request/Response)
├── model/             # Entidades del dominio
└── repository/        # Acceso a datos (Spring Data JPA)

postman/
└── PlataformaEducacionVirtual.postman_collection.json   # Colección Postman con los endpoints
Módulos del dominio

Administrador, CoordinadorAcademico, Curso, Docente, Estudiante, Evaluacion, Examen, Foro, Inscripcion, Leccion, LogAcceso, MensajeForo, ProgresoLeccion, RecursoBibliografico, RefreshToken, Resultado, Tarea, Usuario.

Controladores disponibles

Admin, Auth, Curso, Evaluacion, Health, Inscripcion, Leccion, MensajeForo, Perfil, ProgresoLeccion, RecursoBibliografico, Resultado.

Requisitos previos
JDK 17
Maven 3.9+
PostgreSQL corriendo localmente (o accesible por red)
Configuración

El proyecto se configura con variables de entorno (con valores por defecto para desarrollo local):

Variable	Descripción	Valor por defecto
DB_URL	URL de conexión a PostgreSQL	jdbc:postgresql://localhost:5432/plataforma_educativa
DB_USERNAME	Usuario de la base de datos	postgres
DB_PASSWORD	Contraseña de la base de datos	admin
JWT_SECRET	Clave secreta para firmar los JWT (mínimo 256 bits)	(placeholder de desarrollo, cambiar en producción)
CORS_ALLOWED_ORIGINS	Orígenes permitidos para CORS (separados por coma)	http://localhost:5173

spring.jpa.hibernate.ddl-auto=update: Hibernate crea/ajusta las tablas automáticamente a partir de las entidades — pensado solo para desarrollo, no para producción.

Cómo ejecutar el proyecto
Clonar el repositorio y ubicarse en la rama desarrollo.
Crear la base de datos en PostgreSQL (por defecto plataforma_educativa).
(Opcional) Definir las variables de entorno si no se usan los valores por defecto.
Ejecutar:
bash
   mvn spring-boot:run
La API queda disponible en http://localhost:8080.
Probar la API

La carpeta postman/ incluye la colección PlataformaEducacionVirtual.postman_collection.json con los endpoints listos para importar en Postman.

Autenticación

La API usa JWT: al autenticarse (AuthController) se recibe un access token y un refresh token. El access token expira según app.jwt.expiration-ms y debe enviarse en el header Authorization: Bearer <token> en las peticiones protegidas. El refresh token permite renovar el access token sin volver a iniciar sesión.
