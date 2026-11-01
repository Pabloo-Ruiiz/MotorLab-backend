# MotorLab · Guia del backend (Spring Boot) amb prompts

**Projecte:** MotorLab · **Autor:** Pablo Ruiz Gandia · **Stack:** Java 21 · Spring Boot · PostgreSQL · Spring Security + JWT
**Terminal:** Git Bash · **IDE:** Visual Studio Code

Esta guia substitueix els passos del backend de la guia anterior (passos 3 a 8 i la part de backend del 10). El pas 2 (base de dades PostgreSQL) i el pas 9 (frontend Angular) no s'inclouen ací: els tens al missatge del xat. Les dades de connexió que usa esta guia són: base de dades `motorlabdb`, base de proves `motorlab_testdb`, usuari `motorlab_usr`.

Per cada pas tens:
1. **Què es crea**: les classes del pas, ordenades per paquets.
2. **Els prompts** per a generar-les amb l'assistent d'IA, en l'ordre en què s'han d'executar.
3. **La comprovació** per a saber si pots passar al pas següent.

Al final tens l'**Annex A**: la llista completa de classes per paquets, amb caselles per a anar marcant. L'**Annex B** recull les decisions que difereixen de `motorlab_api.md`.

## Com usar els prompts

1. Obri VS Code a l'**arrel del repositori** (`motorlab/`) i el xat de Copilot (o l'assistent que uses). Els prompts fan servir `#file:` de Copilot; si uses un altre assistent, adjunta els dos fitxers de `docs/`.
2. Comença **cada conversa nova** amb el **Prompt 0**. Una manera còmoda: guarda'l a `.github/copilot-instructions.md`, que Copilot llig automàticament.
3. Executa els prompts **en ordre i d'un en un**.
4. **Revisa el codi** abans d'acceptar-lo: ha de complir les regles del Prompt 0 (sense Lombok, DTO en lloc d'entitats, etc.). Entendre'l és part de la defensa.
5. Després de cada prompt, compila des de Git Bash: `./mvnw -q compile`. Si falla, usa el **Prompt de correcció**.
6. Al final de cada pas: fes la comprovació i un commit (`git add . && git commit -m "..."`).

### Prompt 0 · Context del projecte (comença cada conversa amb ell)

```text
Estoy desarrollando MotorLab: una API REST para gestionar las reparaciones de un taller mecánico (solicitudes de los clientes, órdenes de trabajo, presupuestos con IVA, stock de piezas y seguimiento).

Stack: Java 21, Spring Boot 3.x, Maven, Spring Web, Spring Data JPA (Hibernate), PostgreSQL, Spring Security con JWT (jjwt 0.12.x), Jakarta Validation y springdoc-openapi.

Proyecto: aplicación Spring Boot (Maven) en la raíz del repositorio (pom.xml, mvnw y src/ en la raíz; la documentación está en docs/), paquete raíz com.motorlab con los subpaquetes config, security, entity (y entity.enums), repository, dto (con subpaquetes por dominio: common, auth, usuario, vehiculo, orden, presupuesto, pieza, notificacion, panel), mapper, service, controller, exception, validation y util.

Reglas que debes cumplir SIEMPRE:
1. Nombres de clases, métodos, variables y comentarios en castellano.
2. Sin Lombok: getters y setters explícitos en las entidades. Los DTO son records de Java.
3. Inyección de dependencias por constructor (nunca @Autowired en campos).
4. Los controladores solo reciben la petición, llaman a un servicio y devuelven un DTO. Nunca devuelven entidades ni el campo password_hash.
5. La lógica de negocio va en los servicios, con @Transactional (readOnly = true en las consultas).
6. La base de datos PostgreSQL YA existe: la crea el script docs/motorlab_schema.sql y spring.jpa.hibernate.ddl-auto=none. Las entidades deben mapear exactamente las tablas y columnas de ese script.
7. El JSON usa snake_case (spring.jackson.property-naming-strategy=SNAKE_CASE). Los importes son BigDecimal.
8. Los enums de Java tienen las constantes en minúscula, idénticas a los valores permitidos por el CHECK de la columna en PostgreSQL (por ejemplo solicitud_recibida), para que coincidan con la base de datos y con el JSON.
9. Todas las rutas empiezan por /api/v1 y siguen docs/motorlab_api.md. Los errores usan el formato {"error": {"codigo": "...", "mensaje": "...", "detalles": [{"campo": "...", "mensaje": "..."}]}}.
10. Antes de generar nada, lee #file:docs/motorlab_schema.sql y #file:docs/motorlab_api.md. Si algo no está claro, pregúntame en lugar de inventarlo.

Cuando termines, dime qué archivos has creado o modificado.
```

### Prompt de correcció (quan alguna cosa no compila o falla)

```text
Al compilar (./mvnw -q compile) me sale este error:

<pega aquí el error completo>

Explícame la causa en dos líneas y corrige solo los archivos afectados, respetando las reglas del proyecto.
```

### Resum per paquets

| Paquet | Classes |
|---|---|
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

---

## Pas 3 · Crear el projecte i la infraestructura comuna

**Objectiu:** un projecte Spring Boot que arranque, connecte a PostgreSQL i tinga ja la base d'errors i configuració.

**Què es crea**

- **com.motorlab** (1): `MotorLabApplication`
- **config** (2): `JwtProperties`, `OpenApiConfig`
- **exception** (12): `ApiException`, `RecursoNoEncontradoException`, `SinPermisoException`, `EmailDuplicadoException`, `MatriculaDuplicadaException`, `TransicionNoPermitidaException`, `StockInsuficienteException`, `PresupuestoNoEditableException`, `PresupuestoYaExisteException`, `SolicitudInvalidaException`, `ConflictoException`, `GlobalExceptionHandler`
- **dto.common** (3): `ErrorDetalle`, `ErrorResponse`, `PageResponse`

### 3.0 Crear el projecte (a mà)

A **start.spring.io** (o amb l'extensió *Spring Initializr* de VS Code: `Ctrl+Shift+P` → *Spring Initializr: Create a Maven Project*) tria:

- Maven · Java 21 (o 17) · Packaging *Jar*
- Group `com.motorlab` · Artifact `motorlab-backend` · Package name `com.motorlab`
- Dependències: *Spring Web*, *Spring Data JPA*, *PostgreSQL Driver*, *Spring Security*, *Validation*, *Spring Boot Actuator*, *Spring Boot DevTools*

Descomprimeix el contingut **a l'arrel del repositori** (`pom.xml`, `mvnw` i `src/` al costat de `docs/`) i instal·la a VS Code l'*Extension Pack for Java* i el *Spring Boot Extension Pack*. Des de Git Bash, a l'arrel:

```bash
./mvnw -q compile
```

Afig al `.gitignore` de l'arrel (el projecte generat ja en porta un): `application-local.properties` i `.vscode/launch.json`.

#### Prompt 3.1 · Dependències JWT i Swagger (pom.xml)

```text
Abre pom.xml (en la raíz del repositorio) y añade estas dependencias, sin cambiar nada más:
- io.jsonwebtoken: jjwt-api, jjwt-impl (scope runtime) y jjwt-jackson (scope runtime), en la última versión estable 0.12.x.
- org.springdoc: springdoc-openapi-starter-webmvc-ui, en la versión compatible con la versión de Spring Boot de este proyecto.
Dime qué versiones has elegido y por qué son compatibles. No inventes versiones: si no estás seguro, indícame dónde comprobarlas.
```

Comprova les versions a *mvnrepository.com* abans de continuar.

### 3.2 Configuració (`application.properties`, a mà)

Copia-ho a `src/main/resources/application.properties`:

```properties
spring.application.name=motorlab-backend

spring.datasource.url=jdbc:postgresql://localhost:5432/motorlabdb
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}

spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
spring.jackson.property-naming-strategy=SNAKE_CASE

app.jwt.secret=${JWT_SECRET}
app.jwt.expiracion-horas=8
app.stock-bajo-umbral=2

management.endpoints.web.exposure.include=health
springdoc.swagger-ui.path=/swagger-ui.html
```

Les variables d'entorn les defineixes a Git Bash abans d'arrancar (valen només mentre el terminal estiga obert):

```bash
export DB_USER=motorlab_usr DB_PASS=motorlab_usr
export JWT_SECRET="$(openssl rand -base64 48)"
./mvnw spring-boot:run
```

Si arranques des del botó *Run* de VS Code en lloc del terminal, aquestes variables **no** s'apliquen. Defineix-les a `.vscode/launch.json` (ja està al `.gitignore`):

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

```text
En el paquete com.motorlab.config crea:

1. JwtProperties: un record con @ConfigurationProperties(prefix = "app.jwt") y dos campos: String secret e int expiracionHoras. Actívalo añadiendo @ConfigurationPropertiesScan a la clase MotorLabApplication.
2. OpenApiConfig: una clase @Configuration con un bean OpenAPI con título "MotorLab API", versión "v1", una descripción breve en castellano y un esquema de seguridad "bearerAuth" (HTTP, bearer, formato JWT) aplicado globalmente, para que Swagger UI muestre el botón Authorize.
```

#### Prompt 3.4 · DTO comunes, excepciones y manejador de errores

```text
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

Al log ha d'aparéixer `HikariPool-1 - Start completed` (connecta a PostgreSQL) i `Started MotorLabApplication`. És normal que `/actuator/health` torne **401** fins al pas 5, perquè Spring Security protegeix tot per defecte.

**Commit:** `feat(backend): projecte base, configuració i gestió d'errors`

---

## Pas 4 · Entitats i repositoris (JPA)

**Objectiu:** mapejar les 8 taules i la vista, i poder llegir les dades d'exemple des de Java.

**Què es crea**

- **entity.enums** (11): `Rol`, `TipoServicio`, `CuandoOcurre`, `ConduccionSegura`, `Urgencia`, `FranjaPreferida`, `ContactoPreferido`, `EstadoOrden`, `EstadoPresupuesto`, `TipoLinea`, `TipoNotificacion`
- **entity** (9): `Usuario`, `Vehiculo`, `OrdenTrabajo`, `EventoSeguimiento`, `Notificacion`, `Pieza`, `Presupuesto`, `LineaPresupuesto`, `OrdenResumen`
- **repository** (9): `UsuarioRepository`, `VehiculoRepository`, `OrdenTrabajoRepository`, `OrdenResumenRepository`, `PresupuestoRepository`, `LineaPresupuestoRepository`, `PiezaRepository`, `EventoSeguimientoRepository`, `NotificacionRepository`
- **Proves** (`src/test/java/com/motorlab/...`): `support.IntegracionBase`, `repository.RepositoriosSmokeTest`

### 4.0 Base de dades de proves (Git Bash)

Les proves d'integració s'executen contra `motorlab_testdb`, perquè no toquen les dades de `motorlabdb`. Si ja has creat les dues bases de dades i has carregat l'script a cadascuna (pas 2 del xat), no cal fer res més ací. Si no, des de l'arrel del repositori:

```bash
winpty psql -U motorlab_usr -d motorlab_testdb -f docs/motorlab_schema.sql
```

Crea `src/test/resources/application-test.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/motorlab_testdb
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
app.jwt.secret=clave-solo-para-pruebas-de-mas-de-32-caracteres-0123456789
```

#### Prompt 4.1 · Enums

```text
En el paquete com.motorlab.entity.enums crea estos enums, con las constantes en MINÚSCULA exactamente como los valores permitidos por el CHECK de la columna en PostgreSQL (añade un comentario que explique que es intencionado):
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

```text
Para crear las entidades de com.motorlab.entity aplica SIEMPRE estas reglas de mapeo:
- Cada entidad lleva @Entity y @Table(name = "...") con el nombre EXACTO de la tabla de docs/motorlab_schema.sql (usuarios, vehiculos, ordenes_trabajo, eventos_seguimiento, notificaciones, piezas, presupuestos, lineas_presupuesto).
- Clave primaria: @Id + @GeneratedValue(strategy = GenerationType.IDENTITY), tipo Integer.
- Los atributos en camelCase se mapean solos a columnas en snake_case. Usa @Column solo cuando haga falta (insertable/updatable, nullable, length).
- Relaciones: solo @ManyToOne(fetch = FetchType.LAZY) con @JoinColumn(name = "..."), y @OneToOne unidireccional LAZY en Presupuesto.orden. Sin colecciones @OneToMany ni cascades.
- Enums con @Enumerated(EnumType.STRING). BOOLEAN = boolean, NUMERIC = BigDecimal, DATE = LocalDate, TIMESTAMP = LocalDateTime. Las columnas "enum" son VARCHAR con CHECK en PostgreSQL, así que se leen como texto.
- Columnas que rellena la base de datos: márcalas con @Column(insertable = false, updatable = false): usuarios.consentimiento_en y creado_en, ordenes_trabajo.creado_en y actualizado_en, presupuestos.subtotal, iva, total y creado_en (los calculan triggers), lineas_presupuesto.importe (columna generada) y notificaciones.creada_en.
- Valores por defecto de columnas NOT NULL con DEFAULT: inicialízalos en Java (por ejemplo, estado = borrador, ivaPorcentaje = 21.00, garantiaMeses = 24, activo = true, stock = 0, estado de la orden = solicitud_recibida, rol = cliente), porque Hibernate inserta todas las columnas.
- @DynamicUpdate en OrdenTrabajo y Presupuesto, para que solo actualicen las columnas modificadas y no pisen lo que calculan los triggers.
- Sin Lombok: constructor vacío y getters/setters explícitos.
Confírmame que has entendido las reglas y espera el siguiente prompt.
```

#### Prompt 4.3 · Entidades Usuario y Vehiculo

```text
Crea las entidades Usuario (tabla usuarios) y Vehiculo (tabla vehiculos) con las reglas de mapeo anteriores.
Usuario: id, nombre, email, telefono, passwordHash (columna password_hash), rol (Rol), activo, consentimientoEn, creadoEn.
Vehiculo: id, usuario (@ManyToOne a Usuario, columna usuario_id), matricula, marca, modelo, version (nullable), anio (Integer), kilometros (Integer), activo.
```

#### Prompt 4.4 · Entidades OrdenTrabajo, EventoSeguimiento y Notificacion

```text
Crea las entidades OrdenTrabajo (ordenes_trabajo), EventoSeguimiento (eventos_seguimiento) y Notificacion (notificaciones) con las reglas de mapeo anteriores.
OrdenTrabajo: id, vehiculo (ManyToOne), mecanico (ManyToOne a Usuario, columna mecanico_id, nullable), tipoServicio, sintoma (String largo), cuandoOcurre (nullable), conduccionSegura, urgencia, fechaPreferida (LocalDate), franjaPreferida, contactoPreferido, estado (EstadoOrden), elevador (nullable), fechaEntrada (nullable), fechaEstimada (nullable), creadoEn, actualizadoEn.
EventoSeguimiento: id, orden (ManyToOne), estado (EstadoOrden), descripcion (nullable), fechaHora (LocalDateTime, la rellena Java), usuario (ManyToOne a Usuario, nullable).
Notificacion: id, usuario (ManyToOne), orden (ManyToOne, nullable), tipo (TipoNotificacion), mensaje, leida (boolean), creadaEn.
```

#### Prompt 4.5 · Entidades Pieza, Presupuesto, LineaPresupuesto y OrdenResumen (vista)

```text
Crea las entidades Pieza (piezas), Presupuesto (presupuestos), LineaPresupuesto (lineas_presupuesto) y OrdenResumen con las reglas de mapeo anteriores.
Pieza: id, referencia, nombre, precio, stock (int), activa (boolean).
Presupuesto: id, orden (@OneToOne LAZY, columna orden_id), estado (EstadoPresupuesto), ivaPorcentaje, subtotal, iva, total, tiempoEstimadoHoras (nullable), garantiaMeses (Integer), motivoRechazo (nullable), enviadoEn (nullable), respondidoEn (nullable), creadoEn.
LineaPresupuesto: id, presupuesto (ManyToOne), tipo (TipoLinea), pieza (ManyToOne a Pieza, nullable), concepto, cantidad (BigDecimal), precioUnitario (BigDecimal), importe (solo lectura).
OrdenResumen: entidad de SOLO LECTURA (@Immutable de Hibernate) mapeada a la vista v_ordenes_resumen, con @Id en ordenId y los campos: ordenId, estado (EstadoOrden), progreso (int), urgencia, tipoServicio, sintoma, elevador, fechaEntrada, fechaEstimada, vehiculoId, matricula, marca, modelo, clienteId, clienteNombre, mecanicoId (nullable), mecanicoNombre (nullable), presupuestoEstado (EstadoPresupuesto, nullable), presupuestoTotal (BigDecimal, nullable). Mira la definición de la vista en docs/motorlab_schema.sql.
```

#### Prompt 4.6 · Repositorios

```text
En el paquete com.motorlab.repository crea los repositorios Spring Data JPA (todos con clave Integer) con estos métodos:

- UsuarioRepository: Optional<Usuario> findByEmail(String); boolean existsByEmail(String); Page<Usuario> findByRol(Rol, Pageable); List<Usuario> findByRolAndActivoTrue(Rol).
- VehiculoRepository: Page<Vehiculo> findByUsuarioIdAndActivoTrue(Integer, Pageable); Page<Vehiculo> findByActivoTrue(Pageable); boolean existsByMatricula(String).
- OrdenTrabajoRepository: List<OrdenTrabajo> findByVehiculoIdOrderByIdDesc(Integer); boolean existsByVehiculoIdAndEstadoNotIn(Integer, Collection<EstadoOrden>); y una consulta @Query que devuelva el número de órdenes agrupadas por estado (List<Object[]> contarPorEstado()).
- OrdenResumenRepository: extiende JpaRepository<OrdenResumen, Integer> y JpaSpecificationExecutor<OrdenResumen>.
- PresupuestoRepository: Optional<Presupuesto> findByOrdenId(Integer); un método findByIdForUpdate(Integer id) con @Lock(PESSIMISTIC_WRITE) y @Query; long countByEstado(EstadoPresupuesto); y un @Query BigDecimal sumarAutorizadosEntre(LocalDateTime desde, LocalDateTime hasta) que sume total de los presupuestos autorizados cuyo respondidoEn esté en el rango [desde, hasta), con coalesce a cero.
- LineaPresupuestoRepository: List<LineaPresupuesto> findByPresupuestoIdOrderByIdAsc(Integer); Optional<LineaPresupuesto> findByIdAndPresupuestoId(Integer, Integer); long countByPresupuestoId(Integer).
- PiezaRepository: Optional<Pieza> findByReferencia(String); boolean existsByReferencia(String); findByIdForUpdate(Integer) con @Lock(PESSIMISTIC_WRITE); long countByActivaTrueAndStockLessThanEqual(int); y un @Query Page<Pieza> buscar(String q, boolean soloActivas, boolean stockBajo, int umbral, Pageable) que filtre por referencia o nombre (sin distinguir mayúsculas). IMPORTANTE en PostgreSQL: q nunca debe ser null (un parámetro null dentro de lower() falla), así que el servicio pasa una cadena vacía cuando no hay filtro y la condición es (:q = '' or lower(p.nombre) like lower(concat('%', :q, '%')) or lower(p.referencia) like lower(concat('%', :q, '%'))), por activa si soloActivas y por stock <= umbral si stockBajo.
- EventoSeguimientoRepository: List<EventoSeguimiento> findByOrdenIdOrderByFechaHoraAscIdAsc(Integer); boolean existsByOrdenIdAndEstado(Integer, EstadoOrden).
- NotificacionRepository: Page<Notificacion> findByUsuarioIdOrderByIdDesc(Integer, Pageable); Page<Notificacion> findByUsuarioIdAndLeidaOrderByIdDesc(Integer, boolean, Pageable); Optional<Notificacion> findByIdAndUsuarioId(Integer, Integer); long countByUsuarioIdAndLeidaFalse(Integer); y un @Modifying @Query int marcarTodasLeidas(Integer usuarioId) con clearAutomatically = true.
```

#### Prompt 4.7 · Prueba de humo con los datos de ejemplo

```text
En src/test/java/com/motorlab crea:
1. support.IntegracionBase: clase abstracta con @SpringBootTest, @ActiveProfiles("test") y @Transactional, para que cada prueba haga rollback.
2. repository.RepositoriosSmokeTest (extiende IntegracionBase) que compruebe con los datos de ejemplo de docs/motorlab_schema.sql: hay 3 usuarios; findByEmail("laura@cliente.example") devuelve rol cliente; el presupuesto de la orden 2481 tiene subtotal 212.00, iva 44.52 y total 256.52 (compara BigDecimal con compareTo); la orden 2481 en OrdenResumenRepository tiene progreso 80 y urgencia alta; hay 4 líneas de presupuesto; y la pieza DS-118 tiene stock 1.
```

**Comprovació**

```bash
export DB_USER=motorlab_usr DB_PASS=motorlab_usr
./mvnw test
```

La prova de humo ha de passar en verd. Si falla amb errors de mapatge (columna no trobada, tipus incorrecte), usa el Prompt de correcció amb l'error complet.

**Commit:** `feat(backend): entitats, repositoris i prova de fum`

---

## Pas 5 · Seguretat i autenticació

**Objectiu:** registre, login amb JWT, rols i gestió d'usuaris per a l'administrador.

**Què es crea**

- **config** (2): `SecurityConfig`, `InicializadorUsuarios`
- **security** (7): `UsuarioPrincipal`, `UsuarioDetailsService`, `SecurityUtils`, `JwtService`, `JwtAuthenticationFilter`, `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`
- **dto.auth** (3): `RegistroRequest`, `LoginRequest`, `LoginResponse`
- **dto.usuario** (4): `UsuarioResponse`, `UsuarioResumenResponse`, `UsuarioCreateRequest`, `UsuarioUpdateRequest`
- **mapper** (1): `UsuarioMapper`
- **service** (2): `AuthService`, `UsuarioService`
- **controller** (2): `AuthController`, `UsuarioController`
- **Proves** (`src/test/java/com/motorlab/...`): `security.JwtServiceTest`, `controller.AuthControllerTest`

#### Prompt 5.1 · Usuario autenticado

```text
En el paquete com.motorlab.security crea:
1. UsuarioPrincipal, que implementa UserDetails. Guarda id, nombre, email, passwordHash, rol (Rol) y activo. getAuthorities() devuelve una sola authority "ROLE_" + rol.name().toUpperCase() (por ejemplo ROLE_ADMIN). getUsername() devuelve el email, isEnabled() devuelve activo y el resto de métodos devuelven true. Incluye un método estático desde(Usuario).
2. UsuarioDetailsService, que implementa UserDetailsService: loadUserByUsername(email) busca en UsuarioRepository y lanza UsernameNotFoundException si no existe. Añade también loadUserById(Integer) para el filtro JWT.
3. SecurityUtils: clase de utilidad con métodos estáticos usuarioActual() (UsuarioPrincipal del SecurityContext), idActual(), esAdmin(), esMecanico() y esCliente().
```

#### Prompt 5.2 · JWT: servicio y filtro

```text
En com.motorlab.security crea, usando jjwt 0.12.x:
1. JwtService (@Service), que usa JwtProperties. La clave es Keys.hmacShaKeyFor(secret.getBytes(UTF_8)); si el secreto tiene menos de 32 caracteres, lanza IllegalStateException al arrancar. Métodos: String generarToken(UsuarioPrincipal) con subject = id del usuario, claims rol y nombre, fecha de emisión y caducidad = ahora + expiracionHoras; Integer extraerUsuarioId(String token); y boolean esValido(String token), que devuelva false ante cualquier JwtException o IllegalArgumentException. Usa la API de jjwt 0.12 (Jwts.builder().subject(...).signWith(...), Jwts.parser().verifyWith(key).build().parseSignedClaims(token)).
2. JwtAuthenticationFilter (extiende OncePerRequestFilter): lee la cabecera Authorization: Bearer <token>; si el token es válido, carga el usuario por id con UsuarioDetailsService y, si está activo, coloca un UsernamePasswordAuthenticationToken en el SecurityContext. Ante cualquier fallo, continúa la cadena sin autenticar (el 401 lo devuelve el EntryPoint).
```

#### Prompt 5.3 · Configuración de seguridad

```text
Crea:
1. security.RestAuthenticationEntryPoint (AuthenticationEntryPoint): responde 401 con Content-Type application/json y un ErrorResponse con código NO_AUTENTICADO y mensaje "Autenticación requerida o token caducado".
2. security.RestAccessDeniedHandler (AccessDeniedHandler): responde 403 con ErrorResponse SIN_PERMISO y mensaje "No tienes permiso para realizar esta acción". Ambos serializan con el ObjectMapper inyectado (para respetar snake_case).
3. config.SecurityConfig (@Configuration, @EnableWebSecurity, @EnableMethodSecurity) con un SecurityFilterChain: CSRF desactivado, sesión STATELESS, CORS para el origen http://localhost:4200 (métodos GET, POST, PUT, PATCH, DELETE y OPTIONS; cabeceras Authorization y Content-Type), manejo de excepciones con las dos clases anteriores, y reglas: permitAll para POST /api/v1/auth/registro, POST /api/v1/auth/login, /swagger-ui/**, /swagger-ui.html, /v3/api-docs/** y GET /actuator/health; el resto, authenticated. Añade JwtAuthenticationFilter antes de UsernamePasswordAuthenticationFilter. Define también los beans PasswordEncoder (BCryptPasswordEncoder) y AuthenticationManager (desde AuthenticationConfiguration). Evita dependencias circulares: el filtro no debe depender de SecurityConfig.
```

#### Prompt 5.4 · Registro, login y /auth/yo

```text
Implementa la autenticación según docs/motorlab_api.md (sección 4.1):

DTO (records con validación Jakarta):
- dto.auth.RegistroRequest: nombre (@NotBlank, máx. 100), email (@NotBlank, @Email, máx. 150), telefono (@NotBlank, solo dígitos, espacios y +, entre 9 y 20 caracteres), password (@NotBlank, entre 8 y 72 caracteres) y aceptaPrivacidad (@AssertTrue con mensaje en castellano).
- dto.auth.LoginRequest (email y password, @NotBlank) y dto.auth.LoginResponse(String token, UsuarioResponse usuario).
- dto.usuario.UsuarioResponse(id, nombre, email, telefono, rol, activo) y dto.usuario.UsuarioResumenResponse(id, nombre, telefono).
- mapper.UsuarioMapper (@Component) con aResponse(Usuario) y aResumen(Usuario).

service.AuthService:
- registrar(RegistroRequest): normaliza el email (trim y minúsculas); si ya existe lanza EmailDuplicadoException; crea un Usuario con rol cliente y activo, con la contraseña cifrada con BCrypt; devuelve UsuarioResponse.
- login(LoginRequest): autentica con AuthenticationManager; ante credenciales incorrectas o usuario inactivo lanza BadCredentialsException con el mismo mensaje (no revelar cuál falló); devuelve el token y el usuario.
- yo(): devuelve el UsuarioResponse del usuario autenticado.

controller.AuthController (@RequestMapping("/api/v1/auth")): POST /registro (201), POST /login (200) y GET /yo. Añade @Tag y @Operation de springdoc.
```

#### Prompt 5.5 · Gestión de usuarios (administrador)

```text
Implementa la sección 4.7 de docs/motorlab_api.md (solo usuarios, sin el panel):
- dto.usuario.UsuarioCreateRequest: nombre, email, telefono, password (mismas validaciones que el registro) y rol (@NotNull, de tipo Rol).
- dto.usuario.UsuarioUpdateRequest: rol y activo, ambos opcionales (al menos uno obligatorio).
- service.UsuarioService: listar(Rol rol, pagina, limite) que devuelva PageResponse<UsuarioResponse>; crear (email único, contraseña con BCrypt); actualizar (un administrador no puede desactivarse ni cambiarse el rol a sí mismo: lanza SolicitudInvalidaException).
- controller.UsuarioController con @PreAuthorize("hasRole('ADMIN')") en la clase: GET /api/v1/usuarios?rol&pagina&limite, POST /api/v1/usuarios (201) y PATCH /api/v1/usuarios/{id}.
```

#### Prompt 5.6 · Contraseñas de los usuarios de ejemplo (solo desarrollo)

```text
Los usuarios de ejemplo de docs/motorlab_schema.sql tienen password_hash = 'REEMPLAZAR_POR_HASH_BCRYPT'. Crea config.InicializadorUsuarios, un CommandLineRunner con @Profile("local") y @Transactional que, al arrancar, busque los usuarios con ese valor y les ponga como contraseña el valor de la propiedad app.dev.password-semilla cifrado con BCrypt, registrando en el log cuáles ha actualizado. Explícame cómo activar el perfil "local" y dime qué contiene el archivo src/main/resources/application-local.properties (que NO se sube a Git).
```

Crea `application-local.properties` amb `app.dev.password-semilla=MotorLab2026!` (o la que vulgues) i arranca amb `export SPRING_PROFILES_ACTIVE=local`.

#### Prompt 5.7 · Pruebas de seguridad

```text
En src/test/java/com/motorlab crea (ambas extienden IntegracionBase o usan @SpringBootTest + @ActiveProfiles("test") + @Transactional):
1. security.JwtServiceTest (prueba unitaria): un token generado es válido y devuelve el id correcto; un token manipulado no es válido; un token caducado (usa un JwtProperties con expiracionHoras = 0 o negativo) no es válido; un secreto de menos de 32 caracteres lanza IllegalStateException.
2. controller.AuthControllerTest (@AutoConfigureMockMvc): registro correcto (201); registro con email repetido (409 EMAIL_DUPLICADO); registro sin aceptar la privacidad (400 con detalles); login correcto devuelve token; login con contraseña errónea (401); GET /api/v1/auth/yo sin token (401) y con token (200).
```

**Comprovació**

Arranca amb el perfil `local` i obri `http://localhost:8080/swagger-ui.html`:
1. `POST /auth/login` amb `admin@taller.example` i la contrasenya de `application-local.properties` → reps un token.
2. Prem **Authorize**, enganxa el token i prova `GET /auth/yo` → torna l'administrador.
3. Un token de client a `GET /usuarios` → **403**.

**Commit:** `feat(backend): autenticació JWT, rols i gestió d'usuaris`

---

## Pas 6 · Vehicles i sol·licituds

**Objectiu:** que un client done d'alta els seus vehicles i envie sol·licituds de reparació; que cadascú veja només el que li correspon.

**Què es crea**

- **repository** (1): `OrdenResumenSpecifications`
- **dto.vehiculo** (2): `VehiculoRequest`, `VehiculoResponse`
- **dto.orden** (4): `OrdenCreateRequest`, `OrdenCreateResponse`, `OrdenResumenResponse`, `OrdenDetalleResponse`
- **dto.presupuesto** (1): `PresupuestoResumenResponse`
- **mapper** (2): `VehiculoMapper`, `OrdenMapper`
- **validation** (2): `MatriculaEspanola`, `MatriculaValidator`
- **util** (1): `MatriculaUtils`
- **service** (4): `VehiculoService`, `OrdenService`, `SeguimientoService`, `NotificacionService`
- **controller** (2): `VehiculoController`, `OrdenController`
- **Proves** (`src/test/java/com/motorlab/...`): `controller.OrdenControllerTest`, `validation.MatriculaValidatorTest`

#### Prompt 6.1 · Validación y utilidad de matrícula

```text
Crea:
1. util.MatriculaUtils con un método estático normalizar(String) que haga trim, elimine espacios y guiones y pase a mayúsculas (null-safe).
2. validation.MatriculaEspanola: anotación de Bean Validation (aplicable a campos, parámetros y componentes de record) con mensaje por defecto "Formato de matrícula no válido".
3. validation.MatriculaValidator: ConstraintValidator que normaliza con MatriculaUtils y comprueba la expresión ^[0-9]{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$ (null se considera válido; la obligatoriedad se controla con @NotBlank).
4. Prueba src/test/java/.../validation/MatriculaValidatorTest: válidas "4821 KLB", "4821-klb", "1234BCD"; no válidas "4821 AEI" (vocales), "123 ABC", "4821KL", "4821 KLQ" y una cadena vacía.
```

#### Prompt 6.2 · Vehículos

```text
Implementa la sección 4.2 de docs/motorlab_api.md:
- dto.vehiculo.VehiculoRequest: matricula (@NotBlank y @MatriculaEspanola), marca (@NotBlank, máx. 50), modelo (@NotBlank, máx. 80), version (opcional, máx. 80), anio (@NotNull, @Min(1980)), kilometros (@NotNull, @PositiveOrZero) y usuarioId (opcional; solo lo usa un administrador).
- dto.vehiculo.VehiculoResponse(id, usuarioId, matricula, marca, modelo, version, anio, kilometros, activo) y mapper.VehiculoMapper.
- service.VehiculoService: listar (el cliente ve solo los suyos activos; el administrador, todos; devuelve PageResponse); crear (normaliza la matrícula, MatriculaDuplicadaException si existe, comprueba que anio no sea mayor que el año actual con SolicitudInvalidaException, el propietario es el usuario autenticado salvo que sea administrador y envíe usuarioId); obtener (si no es el propietario ni administrador, RecursoNoEncontradoException); actualizar (la matrícula no se puede cambiar); darDeBaja (baja lógica activo=false; ConflictoException si tiene órdenes que no estén en entregado, rechazada o cancelada).
- controller.VehiculoController (/api/v1/vehiculos) con @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')"): GET, POST (201), GET /{id}, PUT /{id}, DELETE /{id} (204). El endpoint GET /{id}/historial lo añadiremos en el prompt 6.5.
```

#### Prompt 6.3 · Seguimiento y notificaciones (servicios base)

```text
Crea en com.motorlab.service:
1. SeguimientoService: EventoSeguimiento registrar(OrdenTrabajo orden, EstadoOrden estado, String descripcion, Usuario autor) (fechaHora = ahora) y List<EventoSeguimiento> listar(Integer ordenId).
2. NotificacionService: void notificar(Usuario destinatario, OrdenTrabajo orden, TipoNotificacion tipo, String mensaje); void notificarAdmins(OrdenTrabajo orden, TipoNotificacion tipo, String mensaje) (a todos los administradores activos); y void notificarCliente(OrdenTrabajo orden, TipoNotificacion tipo, String mensaje) (al propietario del vehículo de la orden).
Más adelante añadiremos los métodos de consulta de notificaciones.
```

#### Prompt 6.4 · Solicitudes de reparación (órdenes)

```text
Implementa la sección 4.3 de docs/motorlab_api.md, excepto asignación, estado, cancelar y seguimiento (vienen en los pasos 7 y 8):

DTO:
- dto.orden.OrdenCreateRequest: vehiculoId (@NotNull), tipoServicio (@NotNull), sintoma (@NotBlank, entre 20 y 2000 caracteres), cuandoOcurre (opcional), conduccionSegura (@NotNull), urgencia (@NotNull), fechaPreferida (@NotNull, @FutureOrPresent), franjaPreferida (@NotNull), contactoPreferido (@NotNull) y aceptaPrivacidad (@AssertTrue).
- dto.orden.OrdenCreateResponse(id, estado, urgencia, mensaje).
- dto.orden.OrdenResumenResponse (a partir de OrdenResumen): ordenId, estado, progreso, urgencia, tipoServicio, matricula, marca, modelo, clienteNombre, mecanicoNombre, elevador, fechaEntrada, fechaEstimada, presupuestoEstado y presupuestoTotal.
- dto.orden.OrdenDetalleResponse: id, estado, progreso, urgencia, tipoServicio, sintoma, cuandoOcurre, conduccionSegura, fechaPreferida, franjaPreferida, contactoPreferido, elevador, fechaEntrada, fechaEstimada, vehiculo (VehiculoResponse), cliente (UsuarioResumenResponse), mecanico (UsuarioResumenResponse, nullable) y presupuesto (PresupuestoResumenResponse, nullable).
- dto.presupuesto.PresupuestoResumenResponse(id, estado, total).
- mapper.OrdenMapper con los métodos de conversión necesarios.

repository.OrdenResumenSpecifications: clase de utilidad con métodos estáticos que devuelvan Specification<OrdenResumen>: delCliente(id), delMecanico(id), delVehiculo(id), conEstado(estado), conUrgencia(urgencia), conMecanico(id) y buscar(String q) (matrícula o nombre del cliente, sin distinguir mayúsculas). Cada método devuelve null-safe (si el parámetro es null, no filtra).

service.OrdenService:
- crear(OrdenCreateRequest): el cliente solo puede usar sus vehículos (si no, RecursoNoEncontradoException); un administrador puede usar cualquiera. Guarda la orden con estado solicitud_recibida y haz flush + refresh de la entidad (EntityManager) para leer la urgencia que fija el trigger de la base de datos. Registra un evento de seguimiento ("Solicitud recibida") y notifica a los administradores (solicitud_nueva). Devuelve OrdenCreateResponse.
- listar(estado, urgencia, mecanicoId, q, pagina, limite): usa OrdenResumenSpecifications y filtra por rol (el cliente ve solo sus órdenes, el mecánico solo las asignadas a él, el administrador todas). Devuelve PageResponse<OrdenResumenResponse>.
- obtener(id): devuelve OrdenDetalleResponse. Si el usuario no tiene permiso, lanza RecursoNoEncontradoException (no revelar que existe). Crea un método público obtenerEntidadAutorizada(id) que reutilizaremos en otros servicios.

controller.OrdenController (/api/v1/ordenes): POST (201, @PreAuthorize cliente o admin), GET y GET /{id}.
```

#### Prompt 6.5 · Historial del vehículo

```text
Añade GET /api/v1/vehiculos/{id}/historial en VehiculoController: devuelve PageResponse<OrdenResumenResponse> con las órdenes del vehículo (OrdenResumenSpecifications.delVehiculo), solo si el usuario es el propietario o administrador (si no, 404). Añade el método necesario en VehiculoService u OrdenService.
```

#### Prompt 6.6 · Pruebas de órdenes

```text
Crea src/test/java/.../controller/OrdenControllerTest (@SpringBootTest, @AutoConfigureMockMvc, @ActiveProfiles("test"), @Transactional). Crea dentro de la prueba dos clientes, un mecánico y un administrador con contraseña cifrada, y comprueba:
- un cliente crea una solicitud de frenos con conduccionSegura = si y urgencia media: la respuesta (201) trae urgencia = alta;
- una solicitud con síntoma de menos de 20 caracteres o fecha pasada devuelve 400 con detalles;
- un cliente no puede crear una solicitud con el vehículo de otro cliente (404);
- el cliente B recibe 404 al pedir la orden del cliente A;
- GET /ordenes como cliente devuelve solo sus órdenes;
- un mecánico no asignado recibe 404 al pedir una orden.
```

**Comprovació**

Des de Swagger: com a client (`laura@cliente.example`) dona d'alta un vehicle i crea una sol·licitud de **frenos** → torna `urgencia: "alta"`. Amb un altre client, la mateixa ordre dona **404**.

**Commit:** `feat(backend): vehicles i sol·licituds de reparació`

---

## Pas 7 · Ordres, pressupostos i stock

**Objectiu:** la màquina d'estats, el catàleg de peces i el cicle del pressupost (crear, enviar, autoritzar o rebutjar).

**Què es crea**

- **dto.orden** (1): `AsignacionRequest`
- **dto.presupuesto** (8): `PresupuestoUpdateRequest`, `LineaRequest`, `LineaResponse`, `TotalesResponse`, `AvisoStockResponse`, `LineaCreadaResponse`, `PresupuestoResponse`, `RechazoRequest`
- **dto.pieza** (3): `PiezaRequest`, `PiezaResponse`, `StockDeltaRequest`
- **mapper** (2): `PiezaMapper`, `PresupuestoMapper`
- **service** (4): `TransicionesEstado`, `OrdenEstadoService`, `PiezaService`, `PresupuestoService`
- **controller** (2): `PiezaController`, `PresupuestoController`
- **Proves** (`src/test/java/com/motorlab/...`): `service.TransicionesEstadoTest`, `service.PiezaServiceTest`, `service.PresupuestoServiceTest`

#### Prompt 7.1 · Transiciones de estado y asignación

```text
Implementa la máquina de estados de docs/motorlab_api.md (sección 2):

1. service.TransicionesEstado (@Component): contiene un Map<EstadoOrden, Set<EstadoOrden>> con las transiciones permitidas: solicitud_recibida -> {pendiente_presupuesto, cancelada}; pendiente_presupuesto -> {presupuesto_enviado, cancelada}; presupuesto_enviado -> {presupuesto_autorizado, rechazada, cancelada}; presupuesto_autorizado -> {recibido, cancelada}; recibido -> {en_preparacion}; en_preparacion -> {esperando_pieza, arreglando}; esperando_pieza -> {arreglando}; arreglando -> {esperando_pieza, listo}; listo -> {entregado}; entregado, rechazada y cancelada -> {}. Métodos: boolean permitida(desde, hasta) y Set<EstadoOrden> siguientes(desde). Define también dos constantes: ESTADOS_MECANICO = {en_preparacion, esperando_pieza, arreglando, listo} y ESTADOS_AUTOMATICOS = {presupuesto_enviado, presupuesto_autorizado, rechazada, cancelada} (estos solo se alcanzan desde los endpoints de presupuesto y cancelación).
2. service.OrdenEstadoService con el método OrdenTrabajo aplicarCambio(OrdenTrabajo orden, EstadoOrden nuevo, String descripcion, Usuario autor): comprueba la transición (si no es válida, TransicionNoPermitidaException con el mensaje "No se puede pasar de X a Y"); cambia el estado; si el nuevo estado es recibido y fechaEntrada es null, la rellena con la fecha y hora actual; guarda; registra el evento con SeguimientoService; y notifica al cliente (cambio_estado con un mensaje en castellano; vehiculo_listo cuando el estado es listo).
3. dto.orden.AsignacionRequest(mecanicoId @NotNull, elevador máx. 20 caracteres, fechaEstimada). En OrdenService añade asignar(id, AsignacionRequest): solo administrador; el mecánico debe existir, estar activo y tener rol mecanico (si no, SolicitudInvalidaException). Endpoint PATCH /api/v1/ordenes/{id}/asignacion con @PreAuthorize("hasRole('ADMIN')") que devuelve OrdenDetalleResponse.
4. Prueba service.TransicionesEstadoTest (unitaria): comprueba una por una todas las transiciones válidas y estas no válidas: listo -> arreglando, recibido -> listo, entregado -> cualquier estado, cancelada -> cualquier estado.
```

#### Prompt 7.2 · Catálogo de piezas y stock

```text
Implementa la sección 4.5 de docs/motorlab_api.md:
- dto.pieza.PiezaRequest: referencia (@NotBlank, máx. 30), nombre (@NotBlank, máx. 120), precio (@NotNull, @PositiveOrZero) y stock (opcional, >= 0).
- dto.pieza.PiezaResponse(id, referencia, nombre, precio, stock, activa, stockBajo), donde stockBajo = stock <= la propiedad app.stock-bajo-umbral (usa @Value).
- dto.pieza.StockDeltaRequest(delta @NotNull y distinto de 0, motivo máx. 200).
- mapper.PiezaMapper.
- service.PiezaService: listar(q, soloActivas, stockBajo, pagina, limite) (convierte q null en cadena vacía antes de llamar al repositorio); crear (referencia única, si no ConflictoException); obtener; actualizar (referencia, nombre y precio, no el stock); darDeBaja (activa = false); ajustarStock(id, delta, motivo) con bloqueo pesimista (findByIdForUpdate) que lance StockInsuficienteException si el resultado fuera menor que 0 y registre el motivo en el log; y descontarStockDePresupuesto(Integer presupuestoId): suma las cantidades por pieza de las líneas de tipo pieza, bloquea las piezas en orden de id (para evitar interbloqueos), comprueba que hay stock suficiente en todas (si no, StockInsuficienteException con un detalle por pieza, "Stock 1, necesarias 2") y descuenta.
- controller.PiezaController (/api/v1/piezas): GET y GET /{id} para mecánico y admin; POST (201), PUT, DELETE (204) y PATCH /{id}/stock solo para admin.
- Prueba service.PiezaServiceTest: ajustar stock por debajo de cero lanza excepción; un ajuste positivo suma; descontarStockDePresupuesto del presupuesto de ejemplo falla porque DS-118 solo tiene stock 1 y necesita 2, y no descuenta nada de las demás piezas.
```

#### Prompt 7.3 · Presupuestos: crear, consultar y editar líneas

```text
Implementa la sección 4.4 de docs/motorlab_api.md (hasta las líneas, sin enviar/autorizar/rechazar):

DTO (records): PresupuestoUpdateRequest(tiempoEstimadoHoras opcional > 0, garantiaMeses opcional entre 0 y 120); LineaRequest(tipo @NotNull, piezaId, concepto, cantidad @NotNull y > 0, precioUnitario); LineaResponse(id, tipo, piezaId, referencia, concepto, cantidad, precioUnitario, importe); TotalesResponse(subtotal, ivaPorcentaje, iva, total); AvisoStockResponse(piezaId, referencia, stock, necesarias); LineaCreadaResponse(linea, totales, avisoStock nullable); PresupuestoResponse(id, ordenId, estado, lineas, subtotal, ivaPorcentaje, iva, total, tiempoEstimadoHoras, garantiaMeses, motivoRechazo, enviadoEn, respondidoEn, avisosStock); RechazoRequest(motivo opcional, máx. 255). Crea mapper.PresupuestoMapper.

service.PresupuestoService:
- crear(ordenId): solo administrador; la orden debe estar en solicitud_recibida o pendiente_presupuesto (si no, TransicionNoPermitidaException); si ya hay presupuesto, PresupuestoYaExisteException. Crea el presupuesto en borrador y, si la orden estaba en solicitud_recibida, la pasa a pendiente_presupuesto con OrdenEstadoService ("Solicitud revisada, preparando presupuesto").
- obtenerPorOrden(ordenId): el administrador ve cualquiera; el mecánico, el de sus órdenes asignadas; el cliente, el suyo solo si NO está en borrador (si no, RecursoNoEncontradoException). Los avisos de stock solo se rellenan para administrador y mecánico.
- actualizar(id, PresupuestoUpdateRequest): solo en borrador (si no, PresupuestoNoEditableException).
- anadirLinea / modificarLinea / eliminarLinea: solo administrador y solo en borrador. Línea de tipo pieza: exige piezaId (pieza activa), el concepto y el precio se copian del catálogo y la cantidad debe ser un entero. Línea de mano_obra: exige concepto y precioUnitario (tarifa por hora), sin pieza. Los errores de validación cruzada devuelven SolicitudInvalidaException con detalles. Después de guardar una línea haz flush + refresh del Presupuesto (EntityManager), porque los triggers de la base de datos recalculan subtotal, IVA y total. Calcula avisoStock si el stock de la pieza es menor que la cantidad total necesaria de esa pieza en el presupuesto. eliminarLinea devuelve los TotalesResponse.

controller.PresupuestoController: POST /api/v1/ordenes/{ordenId}/presupuesto (201, admin); GET /api/v1/ordenes/{ordenId}/presupuesto; PATCH /api/v1/presupuestos/{id}; POST /api/v1/presupuestos/{id}/lineas (201); PUT y DELETE /api/v1/presupuestos/{id}/lineas/{lineaId} (DELETE devuelve 200 con los totales).
```

#### Prompt 7.4 · Presupuestos: enviar, autorizar y rechazar

```text
Añade a PresupuestoService y PresupuestoController (sección 4.4 de docs/motorlab_api.md). Cada método es @Transactional:

- enviar(id) [admin]: el presupuesto debe estar en borrador (si no, PresupuestoNoEditableException) y tener al menos una línea (si no, SolicitudInvalidaException "El presupuesto no tiene líneas"). Lo pasa a enviado (enviadoEn = ahora), pasa la orden a presupuesto_enviado con OrdenEstadoService ("Presupuesto enviado al cliente") y notifica al cliente (presupuesto_listo, con el total formateado en castellano, por ejemplo "Tu presupuesto está listo · 256,52 €").
- autorizar(id) [cliente propietario, si no 404]: bloquea la fila con findByIdForUpdate; solo si está enviado (si no, TransicionNoPermitidaException "El presupuesto no está pendiente de respuesta"). Lo pasa a autorizado (respondidoEn = ahora), la orden a presupuesto_autorizado ("Presupuesto autorizado por el cliente") y notifica a los administradores (presupuesto_respondido).
- rechazar(id, RechazoRequest) [cliente propietario]: igual que autorizar, pero el presupuesto pasa a rechazado con motivoRechazo y la orden a rechazada.

Rutas: POST /api/v1/presupuestos/{id}/enviar (admin), /autorizar y /rechazar (cliente). Todas devuelven PresupuestoResponse.
```

#### Prompt 7.5 · Pruebas del presupuesto

```text
Crea service.PresupuestoServiceTest (extiende IntegracionBase) con este caso, que reproduce la demostración:
1. Crea una orden nueva (frenos) para el cliente de ejemplo y, como administrador, crea el presupuesto.
2. Añade las líneas: pastillas BR-2231 (1), discos DS-118 (2), líquido LF-040 (1) y mano de obra 1,5 h a 28 EUR/h. Comprueba subtotal 212.00, IVA 44.52 y total 256.52 (compareTo), y que hay aviso de stock para DS-118.
3. Elimina la línea de mano de obra y comprueba que los totales cambian.
4. Intenta enviar un presupuesto sin líneas (SolicitudInvalidaException) y editar uno ya enviado (PresupuestoNoEditableException).
5. El cliente autoriza; un segundo intento da TransicionNoPermitidaException; otro cliente recibe RecursoNoEncontradoException.
```

**Comprovació**

Des de Swagger, amb l'administrador: crea el pressupost d'una ordre nova (la 2481 ja en té un), afig les 4 línies i comprova **212.00 / 44.52 / 256.52**. Envia'l; amb el client autoritza'l i mira que l'ordre passa a `presupuesto_autorizado`.

**Commit:** `feat(backend): màquina d'estats, peces i pressupostos`

---

## Pas 8 · Estats, seguiment i avisos

**Objectiu:** que el taller faça avançar la reparació, que el client la vegi en temps real i que s'actualitze el stock.

**Què es crea**

- **dto.orden** (3): `CambioEstadoRequest`, `EventoResponse`, `SeguimientoResponse`
- **dto.notificacion** (1): `NotificacionResponse`
- **dto.panel** (1): `MetricasResponse`
- **mapper** (1): `NotificacionMapper`
- **service** (1): `PanelService`
- **controller** (2): `NotificacionController`, `PanelController`
- **Proves** (`src/test/java/com/motorlab/...`): `service.FlujoCompletoTest`

#### Prompt 8.1 · Cambio de estado manual y cancelación

```text
Implementa en OrdenService y OrdenController (sección 4.3 de docs/motorlab_api.md):

- dto.orden.CambioEstadoRequest(estado @NotNull, descripcion máx. 200).
- cambiarEstado(id, CambioEstadoRequest): carga la orden con obtenerEntidadAutorizada. El administrador puede aplicar cualquier transición válida salvo las de ESTADOS_AUTOMATICOS (estas se hacen desde presupuesto y cancelación). El mecánico solo puede usar ESTADOS_MECANICO y solo en órdenes asignadas a él (si no, SinPermisoException). Delega en OrdenEstadoService.aplicarCambio.
- Modifica OrdenEstadoService para que, al pasar a arreglando por primera vez (no existe un evento previo con estado arreglando: EventoSeguimientoRepository.existsByOrdenIdAndEstado), llame a PiezaService.descontarStockDePresupuesto del presupuesto de la orden, dentro de la misma transacción. Si falta stock, la excepción StockInsuficienteException revierte todo (el estado no cambia).
- cancelar(id): el cliente propietario puede cancelar si la orden está en solicitud_recibida, pendiente_presupuesto o presupuesto_enviado; el administrador también en presupuesto_autorizado (si no, SinPermisoException o TransicionNoPermitidaException). Si hay presupuesto en borrador o enviado, lo marca como rechazado con motivo "Orden cancelada". Aplica el estado cancelada con OrdenEstadoService.
- Rutas: PATCH /api/v1/ordenes/{id}/estado (@PreAuthorize ADMIN o MECANICO) y POST /api/v1/ordenes/{id}/cancelar (CLIENTE o ADMIN).
```

#### Prompt 8.2 · Seguimiento para el cliente

```text
Implementa GET /api/v1/ordenes/{id}/seguimiento (sección 4.3 de docs/motorlab_api.md):
- dto.orden.EventoResponse(estado, descripcion, fechaHora) y dto.orden.SeguimientoResponse(ordenId, vehiculo, estado, progreso, listoEstimado, eventos).
- vehiculo es un texto "Marca Modelo Versión · MATRÍCULA" (por ejemplo "Seat León 1.5 TSI · 4821KLB"). El progreso sale de EstadoOrden.progreso().
- OrdenService.seguimiento(id) usa obtenerEntidadAutorizada y SeguimientoService.listar; añade la conversión a OrdenMapper. Acceso: cliente propietario, mecánico asignado y administrador.
```

#### Prompt 8.3 · Notificaciones

```text
Implementa la sección 4.6 de docs/motorlab_api.md:
- dto.notificacion.NotificacionResponse(id, tipo, mensaje, ordenId, leida, creadaEn) y mapper.NotificacionMapper.
- En NotificacionService añade: listar(leida opcional, pagina, limite) del usuario autenticado (PageResponse, de más nueva a más antigua); marcarLeida(id) (si no es suya, RecursoNoEncontradoException); marcarTodas() que devuelva cuántas ha marcado; y contarNoLeidas().
- controller.NotificacionController (/api/v1/notificaciones, cualquier usuario autenticado): GET ?leida&pagina&limite, PATCH /{id}/leida, POST /marcar-todas y GET /contador (devuelve {"no_leidas": n}).
```

#### Prompt 8.4 · Panel de métricas

```text
Implementa GET /api/v1/panel/metricas (solo administrador):
- dto.panel.MetricasResponse(ordenesActivas, ordenesPorEstado (Map<EstadoOrden, Long>), ingresosMes (BigDecimal), presupuestosSinRespuesta, piezasStockBajo).
- service.PanelService: ordenesActivas = órdenes cuyo estado no es entregado, rechazada ni cancelada; ordenesPorEstado con contarPorEstado(); ingresosMes = sumarAutorizadosEntre(primer día del mes actual a las 00:00, primer día del mes siguiente); presupuestosSinRespuesta = presupuestos en estado enviado; piezasStockBajo con countByActivaTrueAndStockLessThanEqual(umbral).
- controller.PanelController con @PreAuthorize("hasRole('ADMIN')").
```

#### Prompt 8.5 · Prueba del flujo completo

```text
Crea service.FlujoCompletoTest (extiende IntegracionBase, con MockMvc o llamando a los servicios) que reproduzca la demostración de principio a fin, comprobando el estado y el progreso en cada paso:
1. El cliente crea una solicitud de frenos (urgencia alta) y se notifica a los administradores.
2. El administrador crea el presupuesto, añade las líneas, asigna mecánico y elevador y lo envía; el cliente recibe la notificación presupuesto_listo.
3. El cliente autoriza.
4. El administrador pasa a recibido (fechaEntrada se rellena) y el mecánico a en_preparacion y esperando_pieza.
5. Intentar pasar a arreglando falla con STOCK_INSUFICIENTE (DS-118) y el estado sigue en esperando_pieza.
6. El administrador registra una entrada de stock de 4 unidades de DS-118; ahora el mecánico pasa a arreglando y el stock de las tres piezas baja según el presupuesto.
7. El mecánico pasa a listo (el cliente recibe vehiculo_listo) y el administrador a entregado.
8. El seguimiento del cliente muestra los eventos en orden y progreso 100.
9. Un mecánico que intenta usar recibido recibe 403, y cualquier transición desde entregado da 409.
```

**Comprovació: la demostració completa des de Swagger**

| # | Qui | Petició |
|---|---|---|
| 1 | client | `POST /ordenes` (frenos) → `urgencia: alta` |
| 2 | admin | `POST /ordenes/{id}/presupuesto` i 4 vegades `POST /presupuestos/{id}/lineas` |
| 3 | admin | `PATCH /ordenes/{id}/asignacion` i `POST /presupuestos/{id}/enviar` |
| 4 | client | `GET /notificaciones` i `POST /presupuestos/{id}/autorizar` |
| 5 | admin | `PATCH /ordenes/{id}/estado` → `recibido` |
| 6 | mecànic | `en_preparacion`, `esperando_pieza`; després `arreglando` → **409 STOCK_INSUFICIENTE** |
| 7 | admin | `PATCH /piezas/{id}/stock` amb `delta: 4` |
| 8 | mecànic | `arreglando` i `listo` |
| 9 | client | `GET /ordenes/{id}/seguimiento` → línia de temps i `progreso: 100` |

**Commit:** `feat(backend): estats, seguiment, avisos i panell`

---

## Pas 10 (part backend) · Documentació, seguretat i revisió final

**Què es crea**

- **security** (1): `LimiteIntentosLoginFilter`

#### Prompt 10.1 · Documentación Swagger

```text
Revisa todos los controladores y añade @Tag (nombre del recurso en castellano), @Operation(summary = "...") en cada método y @ApiResponse para los códigos 401, 403, 404 y 409 que correspondan, sin cambiar la lógica. Comprueba que Swagger UI muestra todos los endpoints de docs/motorlab_api.md y dime cuáles faltan o difieren.
```

#### Prompt 10.2 · README del proyecto

```text
Actualiza el README.md de la raíz del repositorio (si ya existe, amplíalo sin borrar lo que haya) en castellano con: requisitos (JDK 21, PostgreSQL 17 o superior), cómo crear el usuario motorlab_usr y las bases de datos motorlabdb y motorlab_testdb con psql desde Git Bash y cargar docs/motorlab_schema.sql, variables de entorno (DB_USER, DB_PASS, JWT_SECRET, SPRING_PROFILES_ACTIVE) y cómo definirlas con export en Git Bash y en .vscode/launch.json, cómo arrancar (./mvnw spring-boot:run), cómo ejecutar los tests (contra motorlab_testdb), la URL de Swagger UI, los usuarios de ejemplo y la estructura de paquetes con una línea por paquete.
```

#### Prompt 10.3 · Revisión de seguridad

```text
Revisa todo el proyecto con esta lista y dime, archivo por archivo, qué incumple y cómo corregirlo:
1. Ninguna respuesta incluye password_hash ni devuelve una entidad JPA.
2. Todos los endpoints tienen @PreAuthorize o están en la lista pública de SecurityConfig.
3. Los recursos de un cliente (vehículos, órdenes, presupuestos, notificaciones) comprueban la propiedad y devuelven 404 si no es suyo.
4. Toda entrada pasa por validación Jakarta y no hay consultas con concatenación de texto.
5. No hay secretos, contraseñas ni tokens escritos en el código, en los tests ni en archivos subidos a Git.
6. Los mensajes de error no revelan trazas ni datos internos y el login no distingue entre email inexistente y contraseña errónea.
7. Los logs no contienen contraseñas ni tokens.
```

#### Prompt 10.4 · (Opcional) Límite de intentos de login

```text
Crea security.LimiteIntentosLoginFilter (OncePerRequestFilter) que limite POST /api/v1/auth/login a 5 intentos por minuto y por IP, con un contador en memoria (ConcurrentHashMap con ventana de tiempo), y responda 429 con un ErrorResponse (código DEMASIADOS_INTENTOS). Regístralo en SecurityConfig antes del filtro JWT y añade una prueba que compruebe el 429 al sexto intento.
```

**Comprovació final**

```bash
export DB_USER=motorlab_usr DB_PASS=motorlab_usr
./mvnw clean verify
```

Totes les proves en verd, el README complet i la demostració del pas 8 funcionant. **Commit:** `docs(backend): swagger, readme i revisió de seguretat`

---

## Annex A · Llista de classes per paquets

Marca cada casella quan la classe estiga creada, revisada i compilant. Al costat de cada classe, el pas on es genera. Ruta base: `src/main/java/com/motorlab/`.

**Total: 116 classes de producció** (+ 1 opcional) **i 10 classes de prova**.

### `com.motorlab` (1)

- [ ] `MotorLabApplication` · pas 3

### `config` (4)

- [ ] `JwtProperties` · pas 3
- [ ] `OpenApiConfig` · pas 3
- [ ] `SecurityConfig` · pas 5
- [ ] `InicializadorUsuarios` · pas 5

### `security` (7)

- [ ] `UsuarioPrincipal` · pas 5
- [ ] `UsuarioDetailsService` · pas 5
- [ ] `SecurityUtils` · pas 5
- [ ] `JwtService` · pas 5
- [ ] `JwtAuthenticationFilter` · pas 5
- [ ] `RestAuthenticationEntryPoint` · pas 5
- [ ] `RestAccessDeniedHandler` · pas 5
- [ ] `LimiteIntentosLoginFilter` · pas 10 · opcional

### `exception` (12)

- [ ] `ApiException` · pas 3
- [ ] `RecursoNoEncontradoException` · pas 3
- [ ] `SinPermisoException` · pas 3
- [ ] `EmailDuplicadoException` · pas 3
- [ ] `MatriculaDuplicadaException` · pas 3
- [ ] `TransicionNoPermitidaException` · pas 3
- [ ] `StockInsuficienteException` · pas 3
- [ ] `PresupuestoNoEditableException` · pas 3
- [ ] `PresupuestoYaExisteException` · pas 3
- [ ] `SolicitudInvalidaException` · pas 3
- [ ] `ConflictoException` · pas 3
- [ ] `GlobalExceptionHandler` · pas 3

### `entity.enums` (11)

- [ ] `Rol` · pas 4
- [ ] `TipoServicio` · pas 4
- [ ] `CuandoOcurre` · pas 4
- [ ] `ConduccionSegura` · pas 4
- [ ] `Urgencia` · pas 4
- [ ] `FranjaPreferida` · pas 4
- [ ] `ContactoPreferido` · pas 4
- [ ] `EstadoOrden` · pas 4
- [ ] `EstadoPresupuesto` · pas 4
- [ ] `TipoLinea` · pas 4
- [ ] `TipoNotificacion` · pas 4

### `entity` (9)

- [ ] `Usuario` · pas 4
- [ ] `Vehiculo` · pas 4
- [ ] `OrdenTrabajo` · pas 4
- [ ] `EventoSeguimiento` · pas 4
- [ ] `Notificacion` · pas 4
- [ ] `Pieza` · pas 4
- [ ] `Presupuesto` · pas 4
- [ ] `LineaPresupuesto` · pas 4
- [ ] `OrdenResumen` · pas 4

### `repository` (10)

- [ ] `UsuarioRepository` · pas 4
- [ ] `VehiculoRepository` · pas 4
- [ ] `OrdenTrabajoRepository` · pas 4
- [ ] `OrdenResumenRepository` · pas 4
- [ ] `PresupuestoRepository` · pas 4
- [ ] `LineaPresupuestoRepository` · pas 4
- [ ] `PiezaRepository` · pas 4
- [ ] `EventoSeguimientoRepository` · pas 4
- [ ] `NotificacionRepository` · pas 4
- [ ] `OrdenResumenSpecifications` · pas 6

### `dto.common` (3)

- [ ] `ErrorDetalle` · pas 3
- [ ] `ErrorResponse` · pas 3
- [ ] `PageResponse` · pas 3

### `dto.auth` (3)

- [ ] `RegistroRequest` · pas 5
- [ ] `LoginRequest` · pas 5
- [ ] `LoginResponse` · pas 5

### `dto.usuario` (4)

- [ ] `UsuarioResponse` · pas 5
- [ ] `UsuarioResumenResponse` · pas 5
- [ ] `UsuarioCreateRequest` · pas 5
- [ ] `UsuarioUpdateRequest` · pas 5

### `dto.vehiculo` (2)

- [ ] `VehiculoRequest` · pas 6
- [ ] `VehiculoResponse` · pas 6

### `dto.orden` (8)

- [ ] `OrdenCreateRequest` · pas 6
- [ ] `OrdenCreateResponse` · pas 6
- [ ] `OrdenResumenResponse` · pas 6
- [ ] `OrdenDetalleResponse` · pas 6
- [ ] `AsignacionRequest` · pas 7
- [ ] `CambioEstadoRequest` · pas 8
- [ ] `EventoResponse` · pas 8
- [ ] `SeguimientoResponse` · pas 8

### `dto.presupuesto` (9)

- [ ] `PresupuestoResumenResponse` · pas 6
- [ ] `PresupuestoUpdateRequest` · pas 7
- [ ] `LineaRequest` · pas 7
- [ ] `LineaResponse` · pas 7
- [ ] `TotalesResponse` · pas 7
- [ ] `AvisoStockResponse` · pas 7
- [ ] `LineaCreadaResponse` · pas 7
- [ ] `PresupuestoResponse` · pas 7
- [ ] `RechazoRequest` · pas 7

### `dto.pieza` (3)

- [ ] `PiezaRequest` · pas 7
- [ ] `PiezaResponse` · pas 7
- [ ] `StockDeltaRequest` · pas 7

### `dto.notificacion` (1)

- [ ] `NotificacionResponse` · pas 8

### `dto.panel` (1)

- [ ] `MetricasResponse` · pas 8

### `mapper` (6)

- [ ] `UsuarioMapper` · pas 5
- [ ] `VehiculoMapper` · pas 6
- [ ] `OrdenMapper` · pas 6
- [ ] `PiezaMapper` · pas 7
- [ ] `PresupuestoMapper` · pas 7
- [ ] `NotificacionMapper` · pas 8

### `validation` (2)

- [ ] `MatriculaEspanola` · pas 6
- [ ] `MatriculaValidator` · pas 6

### `util` (1)

- [ ] `MatriculaUtils` · pas 6

### `service` (11)

- [ ] `AuthService` · pas 5
- [ ] `UsuarioService` · pas 5
- [ ] `VehiculoService` · pas 6
- [ ] `OrdenService` · pas 6
- [ ] `SeguimientoService` · pas 6
- [ ] `NotificacionService` · pas 6
- [ ] `TransicionesEstado` · pas 7
- [ ] `OrdenEstadoService` · pas 7
- [ ] `PiezaService` · pas 7
- [ ] `PresupuestoService` · pas 7
- [ ] `PanelService` · pas 8

### `controller` (8)

- [ ] `AuthController` · pas 5
- [ ] `UsuarioController` · pas 5
- [ ] `VehiculoController` · pas 6
- [ ] `OrdenController` · pas 6
- [ ] `PiezaController` · pas 7
- [ ] `PresupuestoController` · pas 7
- [ ] `NotificacionController` · pas 8
- [ ] `PanelController` · pas 8

### Proves · `src/test/java/com/motorlab/` (10)

- [ ] `support.IntegracionBase` · pas 4
- [ ] `repository.RepositoriosSmokeTest` · pas 4
- [ ] `security.JwtServiceTest` · pas 5
- [ ] `controller.AuthControllerTest` · pas 5
- [ ] `controller.OrdenControllerTest` · pas 6
- [ ] `validation.MatriculaValidatorTest` · pas 6
- [ ] `service.TransicionesEstadoTest` · pas 7
- [ ] `service.PiezaServiceTest` · pas 7
- [ ] `service.PresupuestoServiceTest` · pas 7
- [ ] `service.FlujoCompletoTest` · pas 8

### Recursos de configuració

- [ ] `src/main/resources/application.properties` · pas 3
- [ ] `src/main/resources/application-local.properties (no es puja a Git)` · pas 5
- [ ] `src/test/resources/application-test.properties` · pas 4
- [ ] `.vscode/launch.json (no es puja a Git)` · pas 3
- [ ] `README.md (arrel)` · pas 10

---

## Annex B · Decisions que difereixen de `motorlab_api.md`

Actualitza el document d'API amb estos canvis (o digues-m'ho i ho faig jo):

1. **Enums en minúscula** a Java (`solicitud_recibida`), perquè coincidisquen amb els valors del `CHECK` de PostgreSQL i amb el JSON.
2. **Imports com a número JSON** (86.00), no com a text com als exemples de l'API.
3. **Codis d'error nous:** `EMAIL_DUPLICADO`, `PRESUPUESTO_YA_EXISTE` i `CONFLICTO` (a més dels que ja hi havia).
4. **Paginació amb base 1** (`pagina`, `limite`) i resposta `datos`, `pagina`, `limite`, `total`, `total_paginas`, també a `/vehiculos` i `/usuarios`.
5. **No s'implementen els filtres `desde` i `hasta`** de `GET /ordenes`: la vista no té data de creació.
6. **El mecànic pot consultar el pressupost** de les seues ordres assignades (la pantalla del mecànic ho necessita; la matriu de permisos deia que no).
7. **`DELETE` d'una línia** del pressupost torna **200 amb els totals** en lloc d'una resposta buida.
8. **Endpoint extra:** `GET /notificaciones/contador`. Les mètriques afig `ordenes_activas` i `piezas_stock_bajo`.
9. **Crear el pressupost** passa l'ordre automàticament a `pendiente_presupuesto`.
10. **Cancelar una ordre** amb pressupost en esborrany o enviat el marca com a rebutjat («Orden cancelada»).
11. **El motiu d'un ajust de stock** només es guarda al log: no hi ha taula de moviments.
12. **PostgreSQL:** els «ENUM» són `VARCHAR` amb `CHECK` (no tipus enum natius), i els ids d'exemple obliguen a avançar les seqüències al final de l'script.
13. **Stock baix** = stock menor o igual que `app.stock-bajo-umbral` (2 per defecte).
