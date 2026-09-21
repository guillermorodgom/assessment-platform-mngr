# Servicio de gestión

API Spring Boot 3.3.0 y Java 17 para cuestionarios, preguntas, asignaciones, intentos y resultados. Valida JWT de auth y llama a compiler para respuestas de código.

## Requisitos y configuración

- Java 17, Maven 3.9 o el wrapper y PostgreSQL para `dev`.
- Esquema compartido con auth: consulte [database](../assessment-platform-database/README.md).
- `JWT_SECRET`: misma clave que auth, de al menos 32 bytes; el valor predeterminado es solo para desarrollo.
- `DB_USERNAME` y `DB_PASSWORD`: `postgres`/`postgres` por defecto.
- `compiler.api.url`: `http://localhost:8083` por defecto. Compiler debe estar disponible para calificar código.

El perfil predeterminado `dev` usa `jdbc:postgresql://localhost:5432/postgres?currentSchema=assessment_platform`, puerto `8082` y `ddl-auto=validate`. `prod` usa MySQL con `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` y `DB_PASSWORD`; no hay scripts MySQL aquí.

## Ejecución y API

Desde este directorio ejecute `./mvnw spring-boot:run` y `./mvnw test`; en Windows use `mvnw.cmd`. Las rutas empiezan por `/api/cuestionarios`, `/api/preguntas`, `/api/asignaciones`, `/api/intentos`, `/api/resultados` y `/api/usuarios`. Las operaciones de administración requieren `ADMIN`; las de examen y asignaciones propias requieren `CANDIDATO`.

Swagger: `http://localhost:8082/swagger-ui.html`; OpenAPI: `http://localhost:8082/v3/api-docs`.

## Problema conocido

`GET /api/intentos/{intentoId}/preguntas` devuelve un DTO que incluye `esCorrecta` y `expectedOutput`. Un candidato puede ver las respuestas de su intento durante el examen. Corrija esa salida antes de usar el sistema en evaluaciones reales.
