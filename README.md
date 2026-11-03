# MotorLab-backend
Aplicació web per a la gestió de reparacions d’un taller mecànic: sol·licituds, pressupostos i seguiment en temps real per al client.

## Requisitos

- JDK 21.
- PostgreSQL 17 o superior.
- Git Bash (para ejecutar los comandos `psql` y Maven Wrapper).

La aplicación usa Spring Boot, Spring Web, Spring Data JPA, Spring Security con JWT y PostgreSQL. La base de datos no se crea automáticamente: Hibernate está configurado con `ddl-auto=none`.

## Configuración de PostgreSQL

Desde Git Bash, crea el usuario y las dos bases de datos conectándote con un usuario administrador de PostgreSQL:

```bash
winpty psql -U postgres -d postgres -c "CREATE USER motorlab_usr WITH PASSWORD 'motorlab_usr';"
winpty psql -U postgres -d postgres -c "CREATE DATABASE motorlabdb OWNER motorlab_usr ENCODING 'UTF8' TEMPLATE template0;"
winpty psql -U postgres -d postgres -c "CREATE DATABASE motorlab_testdb OWNER motorlab_usr ENCODING 'UTF8' TEMPLATE template0;"
```

Si el usuario o las bases de datos ya existen, no repitas esas órdenes o elimina previamente los objetos con las herramientas de administración de PostgreSQL. A continuación, carga el esquema y los datos de ejemplo en ambas bases:

```bash
winpty psql -U motorlab_usr -d motorlabdb -f docs/motorlab_schema.sql
winpty psql -U motorlab_usr -d motorlab_testdb -f docs/motorlab_schema.sql
```

El script [docs/motorlab_schema.sql](docs/motorlab_schema.sql) recrea las tablas y contiene datos de demostración, por lo que su ejecución debe hacerse sobre una base de datos de desarrollo o de pruebas.

## Variables de entorno

La aplicación necesita estas variables:

| Variable | Descripción |
|---|---|
| `DB_USER` | Usuario de PostgreSQL, normalmente `motorlab_usr`. |
| `DB_PASS` | Contraseña de `motorlab_usr`. |
| `JWT_SECRET` | Secreto JWT largo y aleatorio (más de 32 caracteres). |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring; usa `local` para inicializar la contraseña de los usuarios de ejemplo. |

### Git Bash

Define las variables en la terminal antes de arrancar la aplicación. Solo estarán disponibles en esa sesión:

```bash
export DB_USER=motorlab_usr
export DB_PASS=motorlab_usr
export JWT_SECRET="$(openssl rand -base64 48)"
export SPRING_PROFILES_ACTIVE=local
```

### VS Code

Para arrancar desde el depurador de VS Code, crea `.vscode/launch.json` (este archivo debe mantenerse fuera del control de versiones si contiene credenciales):

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "MotorLab",
      "request": "launch",
      "mainClass": "com.motorlab.MotorlabBackendApplication",
      "projectName": "motorlab-backend",
      "env": {
        "DB_USER": "motorlab_usr",
        "DB_PASS": "motorlab_usr",
        "JWT_SECRET": "sustituye-este-valor-por-un-secreto-largo-y-aleatorio",
        "SPRING_PROFILES_ACTIVE": "local"
      }
    }
  ]
}
```

## Arrancar la aplicación

Con PostgreSQL iniciado y las variables definidas:

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080/api/v1`.

## Ejecutar los tests

Los tests de integración utilizan la base de datos independiente `motorlab_testdb`. Carga el esquema en ella y define las variables de conexión antes de ejecutar:

```bash
export DB_USER=motorlab_usr
export DB_PASS=motorlab_usr
./mvnw test
```

## Swagger UI

La documentación interactiva está disponible en:

<http://localhost:8080/swagger-ui.html>

La especificación OpenAPI se puede consultar en <http://localhost:8080/v3/api-docs>.

## Usuarios de ejemplo

El script SQL crea estos usuarios:

| Rol | Email | Contraseña local |
|---|---|---|
| Administrador | `admin@taller.example` | `MotorLab2026!` |
| Mecánico | `javi@taller.example` | `MotorLab2026!` |
| Cliente | `laura@cliente.example` | `MotorLab2026!` |

La contraseña se genera a partir de `app.dev.password-semilla` cuando se arranca con el perfil `local`; no se almacena en claro en la base de datos.

## Estructura de paquetes

- `com.motorlab`: clase principal de arranque de la aplicación.
- `com.motorlab.config`: configuración de Spring, JWT, OpenAPI e inicialización local.
- `com.motorlab.security`: autenticación JWT, autorización y principal de usuario.
- `com.motorlab.entity`: entidades JPA y proyección de órdenes.
- `com.motorlab.entity.enums`: enumeraciones cuyos valores coinciden con PostgreSQL y el JSON.
- `com.motorlab.repository`: repositorios y especificaciones de acceso a datos.
- `com.motorlab.dto`: objetos de transferencia de datos.
- `com.motorlab.dto.common`: respuestas paginadas y formato común de errores.
- `com.motorlab.dto.auth`: peticiones y respuestas de autenticación.
- `com.motorlab.dto.usuario`: DTO de gestión de usuarios.
- `com.motorlab.dto.vehiculo`: DTO de vehículos.
- `com.motorlab.dto.orden`: DTO de órdenes y seguimiento.
- `com.motorlab.dto.presupuesto`: DTO de presupuestos y sus líneas.
- `com.motorlab.dto.pieza`: DTO del catálogo y stock de piezas.
- `com.motorlab.dto.notificacion`: DTO de notificaciones.
- `com.motorlab.dto.panel`: DTO de métricas del panel.
- `com.motorlab.mapper`: conversión entre entidades y DTO.
- `com.motorlab.service`: lógica de negocio y transacciones.
- `com.motorlab.controller`: controladores REST de `/api/v1`.
- `com.motorlab.exception`: excepciones de dominio y manejador global de errores.
- `com.motorlab.validation`: validadores personalizados.
- `com.motorlab.util`: utilidades reutilizables.
