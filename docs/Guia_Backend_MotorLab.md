# MotorLab · Guia del backend (Spring Boot) amb prompts

**Projecte:** MotorLab · **Autor:** Pablo Ruiz Gandia · **Stack:** Java 21 · Spring Boot 3.x · PostgreSQL 15+ · Spring Security + JWT
**Terminal:** Git Bash · **IDE:** Visual Studio Code

Esta guia substitueix els passos del backend de la guia anterior (passos 3 a 8 i la part de backend del 10) adaptada a **PostgreSQL** (`motorlabdb` i `motorlab_testdb`, usuari `motorlab_usr`). Tota la lògica i estructura del projecte es troba a la **l'arrel del repositori** (sense carpeta `backend/`). El pas 2 (base de dades) i el pas 9 (frontend Angular) no s'inclouen ací.

Per cada pas tens:

1. **Què es crea**: les classes del pas, ordenades per paquets.

2. **Els prompts** per a generar-les amb l'assistent d me d'IA, en l'ordre en què s'han d'executar.

3. **La comprovació** per a saber si pots passar al pas següent.

Al final tens l'**Annex A**: la llista completa de classes per paquets, amb caselles per a anar marcant. L'**Annex B** recull les decisions que difereixen de `motorlab_api.md`.

## Com usar els prompts

1. Obri VS Code a l'**arrel del repositori** (`motorlab/`) i el xat de Copilot (o l'assistent que uses). Els prompts fan servir `#file:` de Copilot; si uses un altre assistent, adjunta els dos fitxers de `docs/`.

2. Comença **cada conversa nova** amb el **Prompt 0**. Guarda'l a `.github/copilot-instructions.md`, que Copilot llig automàticament.

3. Executa els prompts **en ordre i d'un en un**.

4. **Revisa el codi** abans d'acceptar-lo: ha de complir les regles del Prompt 0 (sense Lombok, DTO en lloc d'entitats, etc.). Entendre'l és part de la defensa.

5. Després de cada prompt, compila des de la terminal a l'arrel: `./mvnw -q compile`. Si falla, usa el **Prompt de correcció**.

6. Al final de cada pas: fes la comprovació i un commit (`git add . && git commit -m "..."`).

### Prompt 0 · Context del projecte (comença cada conversa amb ell)

```
Estoy desarrollando MotorLab: una API REST para gestionar las reparaciones de un taller mecánico (solicitudes de los clientes, órdenes de trabajo, presupuestos con IVA, stock de piezas y seguimiento).

Stack: Java 21, Spring Boot 3.x, Maven, Spring Web, Spring Data JPA (Hibernate), PostgreSQL 15+, Spring Security con JWT (jjwt 0.12.x), Jakarta Validation y springdoc-openapi.

Proyecto: ubicado en la RAÍZ del repositorio, paquete raíz com.motorlab con los subpaquetes config, security, entity (y entity.enums), repository, dto (con subpaquetes por dominio: common, auth, usuario, vehiculo, orden, presupuesto, pieza, notificacion, panel), mapper, service, controller, exception, validation y util.

Reglas que debes cumplir SIEMPRE:
1. Nombres de clases, métodos, variables y comentarios en castellano.
2. Sin Lombok: getters y setters explícitos en las entidades. Los DTO son records de Java.
3. Inyección de dependencias por constructor (nunca @Autowired en campos).
4. Los controladores solo reciben la petición, llaman a un servicio y devuelven un DTO. Nunca devuelven entidades ni el campo password_hash.
5. La lógica de negocio va en los servicios, con @Transactional (readOnly = true en las consultas).
6. La base de datos PostgreSQL YA existe: la crea el script docs/motorlab_schema.sql y spring.jpa.hibernate.ddl-auto=none. Las entidades deben mapear exactamente las tablas y columnas de ese script.
7. El JSON usa snake_case (spring.jackson.property-naming-strategy=SNAKE_CASE). Los importes son BigDecimal.
8. Los enums de Java tienen las constantes en minúscula, idénticas a los valores ENUM / CHECK de PostgreSQL (por ejemplo solicitud_recibida), para que coincidan con la base de datos y con el JSON.
9. Todas las rutas empiezan por /api/v1 y siguen docs/motorlab_api.md. Los errores usan el formato {"error": {"codigo": "...", "mensaje": "...", "detalles": [{"campo": "...", "mensaje": "..."}]}}.
10. Antes de generar nada, lee #file:docs/motorlab_schema.sql y #file:docs/motorlab_api.md. Si algo no está claro, pregúntame en lugar de inventarlo.

Cuando termines, dime qué archivos has creado o modificado.
```

### Prompt de correcció (quan alguna cosa no compila o falla)

```
Al compilar (./mvnw -q compile) me sale este error:

<pega aquí el error completo>

Explícame la causa en dos líneas y corrige solo los archivos afectados, respetando las reglas del proyecto.
```

### Resum per paquets

| Paquet | Classes | 
| ----- | ----- | 
| `com.motorlab` | 1 | 
| `config` | 4 | 
| `security` | 7 | 
| `exception` | 12 | 
| `entity.enums` | 11 | 
| `entity` | 9 | 
| `repository` | 10 | 
| `dto.common` | 3 | 
| `dto.auth` | 3 | 
| `dto.usuario` | 4 | 
| `dto.vehiculo` | 2 | 
| `dto.orden` | 8 | 
| `dto.presupuesto` | 9 | 
| `dto.pieza` | 3 | 
| `dto.notificacion` | 1 | 
| `dto.panel` | 1 | 
| `mapper` | 6 | 
| `validation` | 2 | 
| `util` | 1 | 
| `service` | 11 | 
| `controller` | 8 | 
| **Total classes de producció** | **116** | 
| Proves (`src/test/java`) | 10 | 

A més hi ha una classe **opcional** (`LimiteIntentosLoginFilter`, pas 10) i la configuració `application*.properties`.

## Pas 3 · Crear el projecte i la infraestructura comuna

**Objectiu:** un projecte Spring Boot a l'arrel que arranque, connecte a PostgreSQL (`motorlabdb`) i tinga la base d'errors i configuració.

**Què es crea**

* **com.motorlab** (1): `MotorLabApplication`

* **config** (2): `JwtProperties`, `OpenApiConfig`

* **exception** (12): `ApiException`, `RecursoNoEncontradoException`, `SinPermisoException`, `EmailDuplicadoException`, `MatriculaDuplicadaException`, `TransicionNoPermitidaException`, `StockInsuficienteException`, `PresupuestoNoEditableException`, `PresupuestoYaExisteException`, `SolicitudInvalidaException`, `ConflictoException`, `GlobalExceptionHandler`

* **dto.common** (3): `ErrorDetalle`, `ErrorResponse`, `PageResponse`

### 3.0 Crear el projecte (a mà)

A **start.spring.io** (o amb l'extensió *Spring Initializr* de VS Code):

* Maven · Java 21 · Packaging *Jar*

* Group `com.motorlab` · Artifact `motorlab-backend` · Package name `com.motorlab`

* Dependències: *Spring Web*, *Spring Data JPA*, **PostgreSQL Driver**, *Spring Security*, *Validation*, *Spring Boot Actuator*, *Spring Boot DevTools*

Descomprimeix el contingut directament a la **raíz del repositori** i des de Git Bash:

```bash
./mvnw -q compile
```

Afig al `.gitignore` de l'arrel: `target/`, `src/main/resources/application-local.properties` i `.vscode/launch.json`.

#### Prompt 3.1 · Dependències JWT i Swagger (pom.xml)

```
Abre pom.xml (en la raíz) y añade estas dependencias, sin cambiar nada más:
- io.jsonwebtoken: jjwt-api, jjwt-impl (scope runtime) y jjwt-jackson (scope runtime), en la última versión estable 0.12.x.
- org.springdoc: springdoc-openapi-starter-webmvc-ui, en la versión compatible con la versión de Spring Boot de este proyecto.
Dime qué versiones has elegido y por qué son compatibles.
```

### 3.2 Configuració (`application.properties`, a mà)

Copia-ho a `src/main/resources/application.properties`:

```properties
spring.application.name=motorlab-backend

spring.datasource.url=jdbc:postgresql://localhost:5432/motorlabdb
spring.datasource.username=${DB_USER:motorlab_usr}
spring.datasource.password=${DB_PASS:motorlab_usr}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
spring.jackson.property-naming-strategy=SNAKE_CASE

app.jwt.secret=${JWT_SECRET}
app.jwt.expiracion-horas=8
app.stock-bajo-umbral=2

management.endpoints.web.exposure.include=health
springdoc.swagger-ui.path=/swagger-ui.html
```

Variables d'entorn a Git Bash abans d'arrancar des de l'arrel:

```bash
export DB_USER=motorlab_usr DB_PASS=motorlab_usr
export JWT_SECRET="$(openssl rand -base64 48)"
./mvnw spring-boot:run
```

O en `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "MotorLab",
      "request": "launch",
      "mainClass": "com.motorlab.MotorLabApplication",
      "projectName": "motorlab-backend",
      "env": {
        "DB_USER": "motorlab_usr",
        "DB_PASS": "motorlab_usr",
        "JWT_SECRET": "un_text_llarg_i_aleatori_de_mes_de_32_caracters",
        "SPRING_PROFILES_ACTIVE": "local"
      }
    }
  ]
}
```

#### Prompt 3.3 · Configuració JWT i Swagger (config)

```
En el paquete com.motorlab.config crea:

1. JwtProperties: un record con @ConfigurationProperties(prefix = "app.jwt") y dos campos: String secret e int expiracionHoras. Actívalo añadiendo @ConfigurationPropertiesScan a la clase MotorLabApplication.
2. OpenApiConfig: una clase @Configuration con un bean OpenAPI con título "MotorLab API", versión "v1", una descripción breve en castellano y un esquema de seguridad "bearerAuth" (HTTP, bearer, formato JWT) aplicado globalmente, para que Swagger UI muestre el botón Authorize.
```

#### Prompt 3.4 · DTO comunes, excepciones y manejador de errores

```
Crea lo siguiente, respetando el formato de errores de docs/motorlab_api.md (sección 1.2):

Paquete com.motorlab.dto.common (records):
- ErrorDetalle(String campo, String mensaje).
- ErrorResponse(Cuerpo error), con un record anidado Cuerpo(String codigo, String mensaje, List<ErrorDetalle> detalles). Añade un método estático de fábrica ErrorResponse.de(codigo, mensaje, detalles).
- PageResponse<T>(List<T> datos, int pagina, int limite, long total, int totalPaginas), con un método estático de(Page<T> page) (pagina empieza en 1) y otro estático pageable(Integer pagina, Integer limite) que devuelva un Pageable con pagina por defecto 1, límite por defecto 20 y máximo 100.

Paquete com.motorlab.exception:
- ApiException: clase abstracta que extiende RuntimeException, con HttpStatus estado, String codigo y List<ErrorDetalle> detalles (opcional).
- Subclases (todas con mensaje configurable): RecursoNoEncontradoException (404, NO_ENCONTRADO), SinPermisoException (403, SIN_PERMISO), EmailDuplicadoException (409, EMAIL_DUPLICADO), MatriculaDuplicadaException (409, MATRICULA_DUPLICADA), TransicionNoPermitidaException (409, TRANSICION_NO_PERMITIDA), StockInsuficienteException (409, STOCK_INSUFICIENTE, con lista de detalles), PresupuestoNoEditableException (409, PRESUPUESTO_NO_EDITABLE), PresupuestoYaExisteException (409, PRESUPUESTO_YA_EXISTE), SolicitudInvalidaException (400, VALIDACION, con lista de detalles opcional) y ConflictoException (409, CONFLICTO).
- GlobalExceptionHandler (@RestControllerAdvice) que devuelva siempre un ErrorResponse para: ApiException; MethodArgumentNotValidException y ConstraintViolationException (400 VALIDACION, un detalle por campo, con el nombre del campo en snake_case); HttpMessageNotReadableException y MethodArgumentTypeMismatchException (400 VALIDACION); AuthenticationException (401 NO_AUTENTICADO, mensaje "Credenciales incorrectas" si es BadCredentialsException); AccessDeniedException (403 SIN_PERMISO); DataIntegrityViolationException (409 CONFLICTO); y Exception (500, mensaje genérico "Error interno del servidor", registrando la traza en el log y sin exponerla al cliente).
```

**Comprovació**

```bash
export DB_USER=motorlab_usr DB_PASS=motorlab_usr JWT_SECRET="$(openssl rand -base64 48)"
./mvnw spring-boot:run
```

Al log ha d'aparéixer `HikariPool-1 - Start completed` (connecta a PostgreSQL) i `Started MotorLabApplication`.

**Commit:** `feat(backend): projecte base, configuració i gestió d'errors`

## Pas 4 · Entitats i repositoris (JPA)

**Objectiu:** mapejar les 8 taules i la vista en PostgreSQL, i poder llegir les dades d'exemple des de Java.

**Què es crea**

* **entity.enums** (11): `Rol`, `TipoServicio`, `CuandoOcurre`, `ConduccionSegura`, `Urgencia`, `FranjaPreferida`, `ContactoPreferido`, `EstadoOrden`, `EstadoPresupuesto`, `TipoLinea`, `TipoNotificacion`

* **entity** (9): `Usuario`, `Vehiculo`, `OrdenTrabajo`, `EventoSeguimiento`, `Notificacion`, `Pieza`, `Presupuesto`, `LineaPresupuesto`, `OrdenResumen`

* **repository** (9): `UsuarioRepository`, `VehiculoRepository`, `OrdenTrabajoRepository`, `OrdenResumenRepository`, `PresupuestoRepository`, `LineaPresupuestoRepository`, `PiezaRepository`, `EventoSeguimientoRepository`, `NotificacionRepository`

* **Proves** (`src/test/java/com/motorlab/...`): `support.IntegracionBase`, `repository.RepositoriosSmokeTest`

### 4.0 Base de dades de proves (`motorlab_testdb`)

Des de Git Bash:

```bash
psql -U motorlab_usr -d motorlab_testdb -f docs/motorlab_schema.sql
```

Crea `src/test/resources/application-test.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/motorlab_testdb
spring.datasource.username=${DB_USER:motorlab_usr}
spring.datasource.password=${DB_PASS:motorlab_usr}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
app.jwt.secret=clave-solo-para-pruebas-de-mas-de-32-caracteres-0123456789
```

#### Prompt 4.1 · Enums

```
En el paquete com.motorlab.entity.enums crea estos enums, con las constantes en MINÚSCULA exactamente como los valores CHECK / VARCHAR de PostgreSQL (añade un comentario que explique que es intencionado):
- Rol: cliente, mecanico, admin
- TipoServicio: frenos, motor, aceite_filtros, neumaticos, electricidad, itv, otro
- CuandoOcurre: al_frenar, al_arrancar, en_marcha, siempre, otro
- ConduccionSegura: si, no, no_se
- Urgencia: baja, media, alta
- FranjaPreferida: manana, tarde
- ContactoPreferido: telefono, email
- EstadoOrden: solicitud_recibida, pendiente_presupuesto, presupuesto_enviado, presupuesto_autorizado, recibido, en_preparacion, esperando_pieza, arreglando, listo, entregado, rechazada, cancelada. Añade un método int progreso() que devuelva 20 para recibido, 40 para en_preparacion, 60 para esperando_pieza, 80 para arreglando, 100 para listo y entregado, y 0 para el resto.
- EstadoPresupuesto: borrador, enviado, autorizado, rechazado
- TipoLinea: pieza, mano_obra
- TipoNotificacion: solicitud_nueva, presupuesto_listo, presupuesto_respondido, cambio_estado, vehiculo_listo
Comprueba que coinciden letra por letra con docs/motorlab_schema.sql.
```

#### Prompt 4.2 · Reglas de mapeo JPA (pégalo UNA vez antes de 4.3, 4.4 y 4.5)

```
Para crear las entidades de com.motorlab.entity aplica SIEMPRE estas reglas de mapeo:
- Cada entidad lleva @Entity y @Table(name = "...") con el nombre EXACTO de la tabla de docs/motorlab_schema.sql (usuarios, vehiculos, ordenes_trabajo, eventos_seguimiento, notificaciones, piezas, presupuestos, lineas_presupuesto).
- Clave primaria: @Id + @GeneratedValue(strategy = GenerationType.IDENTITY), tipo Integer.
- Los atributos en camelCase se mapean solos a columnas en snake_case. Usa @Column solo cuando haga falta (insertable/updatable, nullable, length).
- Relaciones: solo @ManyToOne(fetch = FetchType.LAZY) con @JoinColumn(name = "..."), y @OneToOne unidireccional LAZY en Presupuesto.orden. Sin colecciones @OneToMany ni cascades.
- Enums con @Enumerated(EnumType.STRING). BOOLEAN = boolean, NUMERIC / DECIMAL = BigDecimal, DATE = LocalDate, TIMESTAMP = LocalDateTime.
- Columnas que rellena la base de datos: márcalas con @Column(insertable = false, updatable = false): usuarios.consentimiento_en y creado_en, ordenes_trabajo.creado_en y actualizado_en, presupuestos.subtotal, iva, total y creado_en (los calculan triggers PL/pgSQL), lineas_presupuesto.importe (columna generada STORED) y notificaciones.creada_en.
- Valores por defecto de columnas NOT NULL con DEFAULT: inicialízalos en Java (por ejemplo, estado = borrador, ivaPorcentaje = 21.00, garantiaMeses = 24, activo = true, stock = 0, estado de la orden = solicitud_recibida, rol = cliente).
- @DynamicUpdate en OrdenTrabajo y Presupuesto.
- Sin Lombok: constructor vacío y getters/setters explícitos.
Confírmame que has entendido las reglas y espera el siguiente prompt.
```

#### Prompt 4.3 · Entidades Usuario y Vehiculo

```
Crea las entidades Usuario (tabla usuarios) y Vehiculo (tabla vehiculos) con las reglas de mapeo anteriores.
Usuario: id, nombre, email, telefono, passwordHash (columna password_hash), rol (Rol), activo, consentimientoEn, creadoEn.
Vehiculo: id, usuario (@ManyToOne a Usuario, columna usuario_id), matricula, marca, modelo, version (nullable), anio (Integer), kilometros (Integer), activo.
```

#### Prompt 4.4 · Entidades OrdenTrabajo, EventoSeguimiento y Notificacion

```
Crea las entidades OrdenTrabajo (ordenes_trabajo), EventoSeguimiento (eventos_seguimiento) y Notificacion (notificaciones) con las reglas de mapeo anteriores.
OrdenTrabajo: id, vehiculo (ManyToOne), mecanico (ManyToOne a Usuario, columna mecanico_id, nullable), tipoServicio, sintoma (String largo), cuandoOcurre (nullable), conduccionSegura, urgencia, fechaPreferida (LocalDate), franjaPreferida, contactoPreferido, estado (EstadoOrden), elevador (nullable), fechaEntrada (nullable), fechaEstimada (nullable), creadoEn, actualizadoEn.
EventoSeguimiento: id, orden (ManyToOne), estado (EstadoOrden), descripcion (nullable), fechaHora (LocalDateTime, la rellena Java), usuario (ManyToOne a Usuario, nullable).
Notificacion: id, usuario (ManyToOne), orden (ManyToOne, nullable), tipo (TipoNotificacion), mensaje, leida (boolean), creadaEn.
```

#### Prompt 4.5 · Entidades Pieza, Presupuesto, LineaPresupuesto y OrdenResumen (vista)

```
Crea las entidades Pieza (piezas), Presupuesto (presupuestos), LineaPresupuesto (lineas_presupuesto) y OrdenResumen con las reglas de mapeo anteriores.
Pieza: id, referencia, nombre, precio, stock (int), activa (boolean).
Presupuesto: id, orden (@OneToOne LAZY, columna orden_id), estado (EstadoPresupuesto), ivaPorcentaje, subtotal, iva, total, tiempoEstimadoHoras (nullable), garantiaMeses (Integer), motivoRechazo (nullable), enviadoEn (nullable), respondidoEn (nullable), creadoEn.
LineaPresupuesto: id, presupuesto (ManyToOne), tipo (TipoLinea), pieza (ManyToOne a Pieza, nullable), concepto, cantidad (BigDecimal), precioUnitario (BigDecimal), importe (solo lectura).
OrdenResumen: entidad de SOLO LECTURA (@Immutable de Hibernate) mapeada a la vista v_ordenes_resumen, con @Id en ordenId y los campos: ordenId, estado (EstadoOrden), progreso (int), urgencia, tipoServicio, sintoma, elevador, fechaEntrada, fechaEstimada, vehiculoId, matricula, marca, modelo, clienteId, clienteNombre, mecanicoId (nullable), mecanicoNombre (nullable), presupuestoEstado (EstadoPresupuesto, nullable), presupuestoTotal (BigDecimal, nullable).
```

#### Prompt 4.6 · Repositorios

```
En el paquete com.motorlab.repository crea los repositorios Spring Data JPA (todos con clave Integer) con estos métodos:

- UsuarioRepository: Optional<Usuario> findByEmail(String); boolean existsByEmail(String); Page<Usuario> findByRol(Rol, Pageable); List<Usuario> findByRolAndActivoTrue(Rol).
- VehiculoRepository: Page<Vehiculo> findByUsuarioIdAndActivoTrue(Integer, Pageable); Page<Vehiculo> findByActivoTrue(Pageable); boolean existsByMatricula(String).
- OrdenTrabajoRepository: List<OrdenTrabajo> findByVehiculoIdOrderByIdDesc(Integer); boolean existsByVehiculoIdAndEstadoNotIn(Integer, Collection<EstadoOrden>); y una consulta @Query que devuelva el número de órdenes agrupadas por estado (List<Object[]> contarPorEstado()).
- OrdenResumenRepository: extiende JpaRepository<OrdenResumen, Integer> y JpaSpecificationExecutor<OrdenResumen>.
- PresupuestoRepository: Optional<Presupuesto> findByOrdenId(Integer); un método findByIdForUpdate(Integer id) con @Lock(PESSIMISTIC_WRITE) y @Query; long countByEstado(EstadoPresupuesto); y un @Query BigDecimal sumarAutorizadosEntre(LocalDateTime desde, LocalDateTime hasta) que sume total de los presupuestos autorizados cuyo respondidoEn esté en el rango [desde, hasta), con coalesce a cero.
- LineaPresupuestoRepository: List<LineaPresupuesto> findByPresupuestoIdOrderByIdAsc(Integer); Optional<LineaPresupuesto> findByIdAndPresupuestoId(Integer, Integer); long countByPresupuestoId(Integer).
- PiezaRepository: Optional<Pieza> findByReferencia(String); boolean existsByReferencia(String); findByIdForUpdate(Integer) con @Lock(PESSIMISTIC_WRITE); long countByActivaTrueAndStockLessThanEqual(int); y un @Query Page<Pieza> buscar(String q, boolean soloActivas, boolean stockBajo, int umbral, Pageable) que filtre por referencia o nombre (sin distinguir mayúsculas, q puede ser null), por activa si soloActivas y por stock <= umbral si stockBajo.
- EventoSeguimientoRepository: List<EventoSeguimiento> findByOrdenIdOrderByFechaHoraAscIdAsc(Integer); boolean existsByOrdenIdAndEstado(Integer, EstadoOrden).
- NotificacionRepository: Page<Notificacion> findByUsuarioIdOrderByIdDesc(Integer, Pageable); Page<Notificacion> findByUsuarioIdAndLeidaOrderByIdDesc(Integer, boolean, Pageable); Optional<Notificacion> findByIdAndUsuarioId(Integer, Integer); long countByUsuarioIdAndLeidaFalse(Integer); y un @Modifying @Query int marcarTodasLeidas(Integer usuarioId) con clearAutomatically = true.
```

#### Prompt 4.7 · Prueba de humo con los datos de ejemplo

```
En src/test/java/com/motorlab crea:
1. support.IntegracionBase: clase abstracta con @SpringBootTest, @ActiveProfiles("test") y @Transactional, para que cada prueba haga rollback.
2. repository.RepositoriosSmokeTest (extiende IntegracionBase) que compruebe con los datos de ejemplo de docs/motorlab_schema.sql: hay 3 usuarios; findByEmail("laura@cliente.example") devuelve rol cliente; el presupuesto de la orden 2481 tiene subtotal 212.00, iva 44.52 y total 256.52 (compara BigDecimal con compareTo); la orden 2481 en OrdenResumenRepository tiene progreso 80 y urgencia alta; hay 4 líneas de presupuesto; y la pieza DS-118 tiene stock 1.
```

**Comprovació**

```bash
./mvnw test
```

**Commit:** `feat(backend): entitats, repositoris i prova de fum`

## Pas 5 · Seguretat i autenticació

**Objectiu:** registre, login amb JWT, rols i gestió d'usuaris per a l'administrador.

**Què es crea**

* **config** (2): `SecurityConfig`, `InicializadorUsuarios`

* **security** (7): `UsuarioPrincipal`, `UsuarioDetailsService`, `SecurityUtils`, `JwtService`, `JwtAuthenticationFilter`, `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`

* **dto.auth** (3): `RegistroRequest`, `LoginRequest`, `LoginResponse`

* **dto.usuario** (4): `UsuarioResponse`, `UsuarioResumenResponse`, `UsuarioCreateRequest`, `UsuarioUpdateRequest`

* **mapper** (1): `UsuarioMapper`

* **service** (2): `AuthService`, `UsuarioService`

* **controller** (2): `AuthController`, `UsuarioController`

* **Proves** (`src/test/java/com/motorlab/...`): `security.JwtServiceTest`, `controller.AuthControllerTest`

#### Prompt 5.1 · Usuario autenticado

```
En el paquete com.motorlab.security crea:
1. UsuarioPrincipal, que implementa UserDetails. Guarda id, nombre, email, passwordHash, rol (Rol) y activo. getAuthorities() devuelve una sola authority "ROLE_" + rol.name().toUpperCase(). getUsername() devuelve el email, isEnabled() devuelve activo y el resto de métodos devuelven true. Incluye un método estático desde(Usuario).
2. UsuarioDetailsService, que implementa UserDetailsService: loadUserByUsername(email) busca en UsuarioRepository y lanza UsernameNotFoundException si no existe. Añade también loadUserById(Integer) para el filtro JWT.
3. SecurityUtils: clase de utilidad con métodos estáticos usuarioActual(), idActual(), esAdmin(), esMecanico() y esCliente().
```

#### Prompt 5.2 · JWT: servicio y filtro

```
En com.motorlab.security crea, usando jjwt 0.12.x:
1. JwtService (@Service), que usa JwtProperties. La clave es Keys.hmacShaKeyFor(secret.getBytes(UTF_8)); si el secreto tiene menos de 32 caracteres, lanza IllegalStateException al arrancar. Métodos: String generarToken(UsuarioPrincipal); Integer extraerUsuarioId(String token); y boolean esValido(String token).
2. JwtAuthenticationFilter (extiende OncePerRequestFilter): lee la cabecera Authorization: Bearer <token>; si el token es válido, carga el usuario por id con UsuarioDetailsService y coloca un UsernamePasswordAuthenticationToken en el SecurityContext.
```

#### Prompt 5.3 · Configuración de seguridad

```
Crea:
1. security.RestAuthenticationEntryPoint (AuthenticationEntryPoint): responde 401 con Content-Type application/json y un ErrorResponse con código NO_AUTENTICADO.
2. security.RestAccessDeniedHandler (AccessDeniedHandler): responde 403 con ErrorResponse SIN_PERMISO.
3. config.SecurityConfig (@Configuration, @EnableWebSecurity, @EnableMethodSecurity) con un SecurityFilterChain: CSRF desactivado, sesión STATELESS, CORS para http://localhost:4200, manejo de excepciones con las dos clases anteriores, y reglas: permitAll para POST /api/v1/auth/registro, POST /api/v1/auth/login, /swagger-ui/**, /swagger-ui.html, /v3/api-docs/** y GET /actuator/health; el resto, authenticated. Añade JwtAuthenticationFilter antes de UsernamePasswordAuthenticationFilter. Beans PasswordEncoder (BCryptPasswordEncoder) y AuthenticationManager.
```

#### Prompt 5.4 · Registro, login y /auth/yo

```
Implementa la autenticación según docs/motorlab_api.md (sección 4.1):

DTO (records con validación Jakarta):
- dto.auth.RegistroRequest: nombre, email, telefono, password y aceptaPrivacidad (@AssertTrue).
- dto.auth.LoginRequest (email y password) y dto.auth.LoginResponse(String token, UsuarioResponse usuario).
- dto.usuario.UsuarioResponse(id, nombre, email, telefono, rol, activo) y dto.usuario.UsuarioResumenResponse(id, nombre, telefono).
- mapper.UsuarioMapper (@Component) con aResponse(Usuario) y aResumen(Usuario).

service.AuthService:
- registrar(RegistroRequest): normaliza el email; EmailDuplicadoException si existe; Usuario con rol cliente y activo, clave cifrada con BCrypt.
- login(LoginRequest): autentica con AuthenticationManager; credenciales incorrectas lanza BadCredentialsException.
- yo(): devuelve el UsuarioResponse del usuario autenticado.

controller.AuthController (@RequestMapping("/api/v1/auth")): POST /registro (201), POST /login (200) y GET /yo.
```

#### Prompt 5.5 · Gestión de usuarios (administrador)

```
Implementa la sección 4.7 de docs/motorlab_api.md (solo usuarios):
- dto.usuario.UsuarioCreateRequest y UsuarioUpdateRequest.
- service.UsuarioService: listar(Rol, pagina, limite); crear; actualizar (un administrador no puede desactivarse ni cambiarse el rol a sí mismo).
- controller.UsuarioController con @PreAuthorize("hasRole('ADMIN')"): GET /api/v1/usuarios, POST /api/v1/usuarios (201) y PATCH /api/v1/usuarios/{id}.
```

#### Prompt 5.6 · Contraseñas de los usuarios de ejemplo

```
Los usuarios de ejemplo de docs/motorlab_schema.sql tienen password_hash = 'REEMPLAZAR_POR_HASH_BCRYPT'. Crea config.InicializadorUsuarios, un CommandLineRunner con @Profile("local") y @Transactional que, al arrancar, busque los usuarios con ese valor y les ponga como contraseña el valor de app.dev.password-semilla cifrado con BCrypt.
```

Crea `src/main/resources/application-local.properties` amb `app.dev.password-semilla=MotorLab2026!` i arranca amb `export SPRING_PROFILES_ACTIVE=local`.

#### Prompt 5.7 · Pruebas de seguridad

```
En src/test/java/com/motorlab crea:
1. security.JwtServiceTest: generación y validación de tokens.
2. controller.AuthControllerTest (@AutoConfigureMockMvc): registro correcto (201), email repetido (409), login correcto i login erróneo (401).
```

**Comprovació**

Arranca amb perfil `local` i entra a `http://localhost:8080/swagger-ui.html`. Prova login amb `admin@taller.example`.

**Commit:** `feat(backend): autenticació JWT, rols i gestió d'usuaris`

## Pas 6 · Vehicles i sol·licituds

**Objectiu:** que un client done d'alta els seus vehicles i envie sol·licituds de reparació.

**Què es crea**

* **repository** (1): `OrdenResumenSpecifications`

* **dto.vehiculo** (2): `VehiculoRequest`, `VehiculoResponse`

* **dto.orden** (4): `OrdenCreateRequest`, `OrdenCreateResponse`, `OrdenResumenResponse`, `OrdenDetalleResponse`

* **dto.presupuesto** (1): `PresupuestoResumenResponse`

* **mapper** (2): `VehiculoMapper`, `OrdenMapper`

* **validation** (2): `MatriculaEspanola`, `MatriculaValidator`

* **util** (1): `MatriculaUtils`

* **service** (4): `VehiculoService`, `OrdenService`, `SeguimientoService`, `NotificacionService`

* **controller** (2): `VehiculoController`, `OrdenController`

* **Proves**: `controller.OrdenControllerTest`, `validation.MatriculaValidatorTest`

#### Prompt 6.1 · Validación y utilidad de matrícula

```
Crea:
1. util.MatriculaUtils: normalizar(String) (trim, mayúsculas, sin espacios/guiones).
2. validation.MatriculaEspanola y validation.MatriculaValidator: valida formato ^[0-9]{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$.
3. Prueba MatriculaValidatorTest: comprobar formatos válidos e inválidos.
```

#### Prompt 6.2 · Vehículos

```
Implementa la sección 4.2 de docs/motorlab_api.md:
- DTOs de vehículo, VehiculoMapper.
- service.VehiculoService: listar; crear; obtener; actualizar; darDeBaja (baja lógica activo=false; ConflictoException si tiene órdenes abiertas).
- controller.VehiculoController (/api/v1/vehiculos) con @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')"): GET, POST, GET /{id}, PUT /{id}, DELETE /{id}.
```

#### Prompt 6.3 · Seguimiento y notificaciones (servicios base)

```
Crea en com.motorlab.service:
1. SeguimientoService: EventoSeguimiento registrar(...) y List<EventoSeguimiento> listar(ordenId).
2. NotificacionService: notificar(...), notificarAdmins(...) y notificarCliente(...).
```

#### Prompt 6.4 · Solicitudes de reparación (órdenes)

```
Implementa la sección 4.3 de docs/motorlab_api.md:
- DTOs de orden (OrdenCreateRequest, OrdenCreateResponse, OrdenResumenResponse, OrdenDetalleResponse, PresupuestoResumenResponse) y OrdenMapper.
- repository.OrdenResumenSpecifications: Specifications para filtrar por cliente, mecánico, vehículo, estado, urgencia y término de búsqueda (q).
- service.OrdenService: crear (estado solicitud_recibida, flush + refresh para obtener la urgencia fijada por el trigger de PostgreSQL, notifica admins); listar (filtrado por rol); obtener (obtenerEntidadAutorizada).
- controller.OrdenController (/api/v1/ordenes): POST, GET y GET /{id}.
```

#### Prompt 6.5 · Historial del vehículo

```
Añade GET /api/v1/vehiculos/{id}/historial en VehiculoController: devuelve PageResponse<OrdenResumenResponse> con las órdenes del vehículo.
```

#### Prompt 6.6 · Pruebas de órdenes

```
Crea controller.OrdenControllerTest: comprueba creación de solicitudes, regla de urgencia por frenos, validación de síntomas y restricciones de acceso por propietario.
```

**Comprovació**

Crea una sol·licitud de frenos des de Swagger i comprova que PostgreSQL aplica el trigger i assigna `urgencia: alta`.

**Commit:** `feat(backend): vehicles i sol·licituds de reparació`

## Pas 7 · Ordres, pressupostos i stock

**Objectiu:** màquina d'estats, catàleg de peces i cicle del pressupost.

**Què es crea**

* **dto.orden** (1): `AsignacionRequest`

* **dto.presupuesto** (8): `PresupuestoUpdateRequest`, `LineaRequest`, `LineaResponse`, `TotalesResponse`, `AvisoStockResponse`, `LineaCreadaResponse`, `PresupuestoResponse`, `RechazoRequest`

* **dto.pieza** (3): `PiezaRequest`, `PiezaResponse`, `StockDeltaRequest`

* **mapper** (2): `PiezaMapper`, `PresupuestoMapper`

* **service** (4): `TransicionesEstado`, `OrdenEstadoService`, `PiezaService`, `PresupuestoService`

* **controller** (2): `PiezaController`, `PresupuestoController`

* **Proves**: `service.TransicionesEstadoTest`, `service.PiezaServiceTest`, `service.PresupuestoServiceTest`

#### Prompt 7.1 · Transiciones de estado y asignación

```
Implementa la máquina de estados de docs/motorlab_api.md (sección 2):
1. service.TransicionesEstado (@Component): transiciones permitidas entre EstadoOrden.
2. service.OrdenEstadoService: aplicarCambio(orden, nuevoEstado, descripcion, autor) con registro de evento y notificación al cliente.
3. dto.orden.AsignacionRequest. En OrdenService añade asignar(id, AsignacionRequest) [admin]. Ruta PATCH /api/v1/ordenes/{id}/asignacion.
4. Prueba service.TransicionesEstadoTest.
```

#### Prompt 7.2 · Catálogo de piezas y stock

```
Implementa la sección 4.5 de docs/motorlab_api.md:
- DTOs de piezas, PiezaMapper.
- service.PiezaService: listar; crear; obtener; actualizar; darDeBaja; ajustarStock(id, delta, motivo) con bloqueo pesimista; descontarStockDePresupuesto(presupuestoId) (bloquea piezas y descuenta stock o lanza StockInsuficienteException).
- controller.PiezaController (/api/v1/piezas).
- Prueba service.PiezaServiceTest.
```

#### Prompt 7.3 · Presupuestos: crear, consultar y editar líneas

```
Implementa la sección 4.4 de docs/motorlab_api.md:
- DTOs de presupuesto y PresupuestoMapper.
- service.PresupuestoService: crear(ordenId); obtenerPorOrden; actualizar; anadirLinea / modificarLinea / eliminarLinea (después de modificar líneas haz flush + refresh para leer subtotal, iva y total calculados por las funciones PL/pgSQL recalcular_presupuesto).
- controller.PresupuestoController.
```

#### Prompt 7.4 · Presupuestos: enviar, autorizar y rechazar

```
Añade a PresupuestoService y PresupuestoController:
- enviar(id) [admin]: pasa a enviado, orden a presupuesto_enviado y notifica cliente.
- autorizar(id) [cliente]: usa findByIdForUpdate, pasa a autorizado, orden a presupuesto_autorizado.
- rechazar(id, RechazoRequest) [cliente]: pasa a rechazado, orden a rechazada.
```

#### Prompt 7.5 · Pruebas del presupuesto

```
Crea service.PresupuestoServiceTest: reproduce el caso de prueba con las 4 líneas, comprobando subtotal 212.00, IVA 44.52 y total 256.52, aviso de stock y flujo de autorización.
```

**Comprovació**

Crea el pressupost per a una nova ordre i verifica els calculs realitzats pels triggers de PostgreSQL.

**Commit:** `feat(backend): màquina d'estats, peces i pressupostos`

## Pas 8 · Estats, seguiment i avisos

**Objectiu:** avanç de reparació, seguiment en temps real i descompte automàtic de stock.

**Què es crea**

* **dto.orden** (3): `CambioEstadoRequest`, `EventoResponse`, `SeguimientoResponse`

* **dto.notificacion** (1): `NotificacionResponse`

* **dto.panel** (1): `MetricasResponse`

* **mapper** (1): `NotificacionMapper`

* **service** (1): `PanelService`

* **controller** (2): `NotificacionController`, `PanelController`

* **Proves**: `service.FlujoCompletoTest`

#### Prompt 8.1 · Cambio de estado manual y cancelación

```
Implementa en OrdenService y OrdenController:
- dto.orden.CambioEstadoRequest.
- cambiarEstado(id, CambioEstadoRequest): al pasar a arreglando por primera vez llama a PiezaService.descontarStockDePresupuesto. Si falta stock, revierte el cambio.
- cancelar(id): cancela la orden y rechaza presupuestos no autorizados.
- Rutas: PATCH /api/v1/ordenes/{id}/estado y POST /api/v1/ordenes/{id}/cancelar.
```

#### Prompt 8.2 · Seguimiento para el cliente

```
Implementa GET /api/v1/ordenes/{id}/seguimiento según la sección 4.3 de docs/motorlab_api.md.
```

#### Prompt 8.3 · Notificaciones

```
Implementa la sección 4.6 de docs/motorlab_api.md en NotificacionService y NotificacionController (/api/v1/notificaciones).
```

#### Prompt 8.4 · Panel de métricas

```
Implementa GET /api/v1/panel/metricas en PanelService y PanelController (@PreAuthorize ADMIN).
```

#### Prompt 8.5 · Prueba del flujo completo

```
Crea service.FlujoCompletoTest que ejecute el ciclo de vida completo: solicitud -> presupuesto -> autorización -> recibido -> esperando_pieza -> ajuste de stock -> arreglando -> listo -> entregado.
```

**Comprovació**

Executa `./mvnw test` per validar tots els tests integrats amb `motorlab_testdb`.

**Commit:** `feat(backend): estats, seguiment, avisos i panell`

## Pas 10 (part backend) · Documentació, seguretat i revisió final

**Què es crea**

* **security** (1): `LimiteIntentosLoginFilter`

#### Prompt 10.1 · Documentación Swagger

```
Revisa todos los controladores y añade @Tag y @Operation en castellano.
```

#### Prompt 10.2 · README del backend

```
Genera README.md en la raíz con instrucciones de configuración para PostgreSQL 15+, variables de entorno, psql y ./mvnw spring-boot:run.
```

#### Prompt 10.3 · Revisión de seguridad

```
Revisa la seguridad general del proyecto y corrige vulnerabilidades o accesos no autorizados.
```

#### Prompt 10.4 · (Opcional) Límite de intentos de login

```
Crea security.LimiteIntentosLoginFilter (5 intentos por minuto/IP).
```

**Comprovació final**

```bash
export DB_USER=motorlab_usr DB_PASS=motorlab_usr
./mvnw clean verify
```

**Commit:** `docs(backend): swagger, readme i revisió de seguretat`

## Annex A · Llista de classes per paquets

Marca cada casella quan la classe estiga creada, revisada i compilant.

### `com.motorlab` (1)

* \[ \] `MotorLabApplication` · pas 3

### `config` (4)

* \[ \] `JwtProperties` · pas 3

* \[ \] `OpenApiConfig` · pas 3

* \[ \] `SecurityConfig` · pas 5

* \[ \] `InicializadorUsuarios` · pas 5

### `security` (7)

* \[ \] `UsuarioPrincipal` · pas 5

* \[ \] `UsuarioDetailsService` · pas 5

* \[ \] `SecurityUtils` · pas 5

* \[ \] `JwtService` · pas 5

* \[ \] `JwtAuthenticationFilter` · pas 5

* \[ \] `RestAuthenticationEntryPoint` · pas 5

* \[ \] `RestAccessDeniedHandler` · pas 5

* \[ \] `LimiteIntentosLoginFilter` · pas 10 · opcional

### `exception` (12)

* \[ \] `ApiException` · pas 3

* \[ \] `RecursoNoEncontradoException` · pas 3

* \[ \] `SinPermisoException` · pas 3

* \[ \] `EmailDuplicadoException` · pas 3

* \[ \] `MatriculaDuplicadaException` · pas 3

* \[ \] `TransicionNoPermitidaException` · pas 3

* \[ \] `StockInsuficienteException` · pas 3

* \[ \] `PresupuestoNoEditableException` · pas 3

* \[ \] `PresupuestoYaExisteException` · pas 3

* \[ \] `SolicitudInvalidaException` · pas 3

* \[ \] `ConflictoException` · pas 3

* \[ \] `GlobalExceptionHandler` · pas 3

### `entity.enums` (11)

* \[ \] `Rol` · pas 4

* \[ \] `TipoServicio` · pas 4

* \[ \] `CuandoOcurre` · pas 4

* \[ \] `ConduccionSegura` · pas 4

* \[ \] `Urgencia` · pas 4

* \[ \] `FranjaPreferida` · pas 4

* \[ \] `ContactoPreferido` · pas 4

* \[ \] `EstadoOrden` · pas 4

* \[ \] `EstadoPresupuesto` · pas 4

* \[ \] `TipoLinea` · pas 4

* \[ \] `TipoNotificacion` · pas 4

### `entity` (9)

* \[ \] `Usuario` · pas 4

* \[ \] `Vehiculo` · pas 4

* \[ \] `OrdenTrabajo` · pas 4

* \[ \] `EventoSeguimiento` · pas 4

* \[ \] `Notificacion` · pas 4

* \[ \] `Pieza` · pas 4

* \[ \] `Presupuesto` · pas 4

* \[ \] `LineaPresupuesto` · pas 4

* \[ \] `OrdenResumen` · pas 4

### `repository` (10)

* \[ \] `UsuarioRepository` · pas 4

* \[ \] `VehiculoRepository` · pas 4

* \[ \] `OrdenTrabajoRepository` · pas 4

* \[ \] `OrdenResumenRepository` · pas 4

* \[ \] `PresupuestoRepository` · pas 4

* \[ \] `LineaPresupuestoRepository` · pas 4

* \[ \] `PiezaRepository` · pas 4

* \[ \] `EventoSeguimientoRepository` · pas 4

* \[ \] `NotificacionRepository` · pas 4

* \[ \] `OrdenResumenSpecifications` · pas 6

### `dto.common` (3)

* \[ \] `ErrorDetalle` · pas 3

* \[ \] `ErrorResponse` · pas 3

* \[ \] `PageResponse` · pas 3

### `dto.auth` (3)

* \[ \] `RegistroRequest` · pas 5

* \[ \] `LoginRequest` · pas 5

* \[ \] `LoginResponse` · pas 5

### `dto.usuario` (4)

* \[ \] `UsuarioResponse` · pas 5

* \[ \] `UsuarioResumenResponse` · pas 5

* \[ \] `UsuarioCreateRequest` · pas 5

* \[ \] `UsuarioUpdateRequest` · pas 5

### `dto.vehiculo` (2)

* \[ \] `VehiculoRequest` · pas 6

* \[ \] `VehiculoResponse` · pas 6

### `dto.orden` (8)

* \[ \] `OrdenCreateRequest` · pas 6

* \[ \] `OrdenCreateResponse` · pas 6

* \[ \] `OrdenResumenResponse` · pas 6

* \[ \] `OrdenDetalleResponse` · pas 6

* \[ \] `AsignacionRequest` · pas 7

* \[ \] `CambioEstadoRequest` · pas 8

* \[ \] `EventoResponse` · pas 8

* \[ \] `SeguimientoResponse` · pas 8

### `dto.presupuesto` (9)

* \[ \] `PresupuestoResumenResponse` · pas 6

* \[ \] `PresupuestoUpdateRequest` · pas 7

* \[ \] `LineaRequest` · pas 7

* \[ \] `LineaResponse` · pas 7

* \[ \] `TotalesResponse` · pas 7

* \[ \] `AvisoStockResponse` · pas 7

* \[ \] `LineaCreadaResponse` · pas 7

* \[ \] `PresupuestoResponse` · pas 7

* \[ \] `RechazoRequest` · pas 7

### `dto.pieza` (3)

* \[ \] `PiezaRequest` · pas 7

* \[ \] `PiezaResponse` · pas 7

* \[ \] `StockDeltaRequest` · pas 7

### `dto.notificacion` (1)

* \[ \] `NotificacionResponse` · pas 8

### `dto.panel` (1)

* \[ \] `MetricasResponse` · pas 8

### `mapper` (6)

* \[ \] `UsuarioMapper` · pas 5

* \[ \] `VehiculoMapper` · pas 6

* \[ \] `OrdenMapper` · pas 6

* \[ \] `PiezaMapper` · pas 7

* \[ \] `PresupuestoMapper` · pas 7

* \[ \] `NotificacionMapper` · pas 8

### `validation` (2)

* \[ \] `MatriculaEspanola` · pas 6

* \[ \] `MatriculaValidator` · pas 6

### `util` (1)

* \[ \] `MatriculaUtils` · pas 6

### `service` (11)

* \[ \] `AuthService` · pas 5

* \[ \] `UsuarioService` · pas 5

* \[ \] `VehiculoService` · pas 6

* \[ \] `OrdenService` · pas 6

* \[ \] `SeguimientoService` · pas 6

* \[ \] `NotificacionService` · pas 6

* \[ \] `TransicionesEstado` · pas 7

* \[ \] `OrdenEstadoService` · pas 7

* \[ \] `PiezaService` · pas 7

* \[ \] `PresupuestoService` · pas 7

* \[ \] `PanelService` · pas 8

### `controller` (8)

* \[ \] `AuthController` · pas 5

* \[ \] `UsuarioController` · pas 5

* \[ \] `VehiculoController` · pas 6

* \[ \] `OrdenController` · pas 6

* \[ \] `PiezaController` · pas 7

* \[ \] `PresupuestoController` · pas 7

* \[ \] `NotificacionController` · pas 8

* \[ \] `PanelController` · pas 8

### Proves · `src/test/java/com/motorlab/` (10)

* \[ \] `support.IntegracionBase` · pas 4

* \[ \] `repository.RepositoriosSmokeTest` · pas 4

* \[ \] `security.JwtServiceTest` · pas 5

* \[ \] `controller.AuthControllerTest` · pas 5

* \[ \] `controller.OrdenControllerTest` · pas 6

* \[ \] `validation.MatriculaValidatorTest` · pas 6

* \[ \] `service.TransicionesEstadoTest` · pas 7

* \[ \] `service.PiezaServiceTest` · pas 7

* \[ \] `service.PresupuestoServiceTest` · pas 7

* \[ \] `service.FlujoCompletoTest` · pas 8

## Annex B · Decisions que difereixen de `motorlab_api.md`

 1. **Projecte a la raíz**: Tota la font Java, recursos i Maven estan directament a la raíz del repositori.

 2. **Motor de BD**: Us de PostgreSQL 15+.

 3. **Enums en minúscula** a Java (`solicitud_recibida`), compatibles amb les restriccions `CHECK` de PostgreSQL.

 4. **Imports com a número JSON** (86.00).

 5. **Codis d'error nous:** `EMAIL_DUPLICADO`, `PRESUPUESTO_YA_EXISTE` i `CONFLICTO`.

 6. **Paginació amb base 1** (`pagina`, `limite`).

 7. **El mecànic pot consultar el pressupost** de les seues ordres assignades.

 8. **`DELETE` d'una línia** del pressupost torna **200 amb els totals**.

 9. **Crear el pressupost** passa l'ordre automàticament a `pendiente_presupuesto`.

10. **Cancelar una ordre** amb pressupost en esborrany o enviat el marca com a rebutjat.

11. **Stock baix** = stock menor o igual que `app.stock-bajo-umbral` (2 per defecte).