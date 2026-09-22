# Plataforma de Educación Virtual - Backend

Backend de la Plataforma de Educación Virtual del grupo Bit Criollo.

Este proyecto corresponde al servidor de la plataforma y proporciona una API REST para gestionar usuarios, autenticación, cursos, lecciones, inscripciones, evaluaciones, resultados, progreso, recursos bibliográficos y foro.

## Tecnologías utilizadas

- Java 17
- Spring Boot 3.3.4
- Maven
- Spring Data JPA
- Spring Security
- JWT (JSON Web Token)
- Lombok
- PostgreSQL

## Arquitectura

El backend utiliza una arquitectura por capas:

- **Controller:** recibe y expone las peticiones HTTP mediante la API REST.
- **Service:** contiene la lógica de negocio.
- **Repository:** gestiona el acceso y persistencia de datos.
- **Model:** representa las entidades del dominio.
- **DTO:** define los objetos utilizados para la transferencia de información.
- **Security:** contiene los componentes relacionados con autenticación y autorización.

La comunicación con el frontend se realiza mediante peticiones HTTP utilizando formato JSON.

## Estructura principal

```text
src/
└── main/
    ├── java/
    │   └── com/
    │       └── bitcriollo/
    │           └── plataforma/
    │               ├── controller/
    │               ├── service/
    │               ├── repository/
    │               ├── model/
    │               ├── dto/
    │               ├── security/
    │               └── exception/
    │
    └── resources/
        └── application.properties