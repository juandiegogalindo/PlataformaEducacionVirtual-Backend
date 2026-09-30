#  Sección de Pruebas

En esta sección se documentará el proceso de **pruebas de la Plataforma de Educación Virtual**, con el objetivo de verificar que las funcionalidades desarrolladas en el backend funcionen correctamente.

Para realizar las pruebas se utilizará principalmente **Postman**, herramienta que permitirá comprobar el funcionamiento de los diferentes servicios y endpoints de la aplicación.

---

##  ¿Qué se va a probar?

Las pruebas estarán enfocadas en las principales funcionalidades de la plataforma:

-  Registro e inicio de sesión de usuarios
-  Gestión de usuarios y roles
-  Gestión del perfil
-  Creación y administración de cursos
-  Inscripciones a cursos
-  Gestión de lecciones
-  Recursos bibliográficos
-  Exámenes y tareas
-  Manejo de sesiones mediante JWT
-  Validación de permisos según el tipo de usuario

---

##  Herramienta de pruebas

### Postman

Se utilizará **Postman** para realizar las pruebas de los servicios REST del backend.

Con esta herramienta podremos enviar solicitudes de tipo:

**GET · POST · PUT · PATCH · DELETE**

y verificar las respuestas generadas por la aplicación.

Además, se utilizarán variables dentro de Postman para facilitar las pruebas y reutilizar información como:

-  Tokens de autenticación
-  Identificadores de cursos
-  Identificadores de usuarios
-  Identificadores de inscripciones
-  Identificadores de lecciones
-  Otros datos generados durante las pruebas

---

##  Autenticación

La plataforma utiliza **JWT (JSON Web Token)** para manejar la autenticación.

Durante las pruebas se verificará que los usuarios puedan iniciar sesión correctamente y que los endpoints protegidos solamente puedan ser utilizados por usuarios autenticados y con los permisos correspondientes.

También se probarán funcionalidades relacionadas con:

-  Access Token
-  Refresh Token
-  Cierre de sesión
-  Autorización por roles

---

##  Base de datos

Las pruebas del backend se realizarán utilizando **PostgreSQL**, donde se almacenará la información generada durante la ejecución de las diferentes funcionalidades.

La aplicación utiliza **Spring Data JPA y Hibernate** para la comunicación con la base de datos.

---

##  Organización de las pruebas

Las solicitudes de Postman estarán organizadas por módulos para facilitar su ejecución y seguimiento.

```text
 Plataforma Educación Virtual
│
├──  Health
├──  Auth
├──  Admin
├──  Perfil
├──  Cursos
├──  Inscripciones
├──  Lecciones
├──  Recursos bibliográficos
└──  Exámenes y tareas
