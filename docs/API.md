# Documentación de API REST
## Plataforma de Educación Virtual - Backend

**Grupo:** Bit Criollo  
**Proyecto:** Plataforma de Educación Virtual  
**Rama:** desarrollo  
**Ciclo:** 2026-2  
**Universidad:** Universidad Piloto de Colombia

---

## 1. Descripción

El backend de la Plataforma de Educación Virtual proporciona una API REST para la gestión de usuarios, autenticación, cursos, lecciones, inscripciones, evaluaciones, resultados, progreso, recursos bibliográficos y foro.

La comunicación entre el frontend y el backend se realiza mediante peticiones HTTP utilizando información en formato JSON.

---

## 2. Autenticación

Las funcionalidades protegidas utilizan autenticación mediante JWT.

Para las solicitudes que requieren autenticación se utiliza:

```text
Authorization: Bearer <token>