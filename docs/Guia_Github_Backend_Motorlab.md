# MotorLab · Guia del backend pas a pas amb GitHub

**Projecte:** MotorLab · **Autor:** Pablo Ruiz Gandia · **Stack:** Java 21 · Spring Boot · PostgreSQL · Spring Security + JWT
**Terminal:** Git Bash · **IDE:** Visual Studio Code · **Base de dades:** `motorlabdb` (proves: `motorlab_testdb`) · **Usuari:** `motorlab_usr`

Esta guia és diferent de `guia_backend_motorlab.md`: els prompts van en **ordre real d'execució** (el primer pas és crear el projecte Spring Boot) i el treball està partit en **29 apartats xicotets**. Cada apartat és una unitat que pots pujar a GitHub per separat, amb:

- una **issue** (títol i descripció),
- una **branca**,
- un **commit**,
- una **pull request** (títol i descripció, amb `Closes #N` per a tancar la seua issue).

## Regla d'or: una issue = una branca = un commit = una PR

Els passos depenen els uns dels altres, així que **fusiona cada PR abans de començar el pas següent**.

### Flux de cada pas

```bash
# 1) Parteix sempre de main actualitzada
git checkout main
git pull

# 2) Crea la branca del pas (el nom està a la guia)
git checkout -b feature/NN-nom-del-pas

# 3) Fes la feina del pas: executa els prompts en ordre, compila i fes la comprovació

# 4) Commit i push (el missatge està a la guia)
git add .
git commit -m "missatge del commit"
git push -u origin feature/NN-nom-del-pas

# 5) A GitHub: Compare & pull request → enganxa el títol i la descripció de la guia
#    Revisa els canvis, fes «Squash and merge» i esborra la branca

# 6) Torna a main i neteja
git checkout main
git pull
git branch -d feature/NN-nom-del-pas
```

## Com funcionen els números de les issues i de les PR

GitHub **compartix la numeració** entre issues i pull requests. Per això:

1. **Crea totes les issues abans** de començar (així l'issue del pas *N* és la **#N**). Si el repositori és nou i les crees en ordre, `Closes #N` de cada PR tanca la issue correcta.
2. Si el teu repositori ja té issues o PR, els números es desplacen: mira el número real de cada issue i canvia el `Closes #N` de la PR.
3. `Closes #N` només tanca la issue quan la PR es **fusiona a la branca per defecte** (`main`).

Tens dues maneres de crear les issues: copiar el títol i la descripció de cada pas a GitHub (*Issues → New issue*), o executar l'**Annex C** (un script amb GitHub CLI que les crea totes en ordre).

## Pas 0 · Requisits (una sola vegada, sense issue)

- Repositori de GitHub creat i **clonat**, amb un commit inicial a `main` (per exemple, un `README.md`). Branca per defecte: `main`.
- JDK 21 (o 17), Git, VS Code i PostgreSQL 17 o superior amb la contrasenya de `postgres` i `psql` al PATH.
- Opcional: GitHub CLI (`winget install --id GitHub.CLI`) i `gh auth login`, per a l'Annex C.

```bash
java -version
git --version
psql --version
```

## Com usar els prompts

1. Obri VS Code a l'arrel del repositori (`motorlab/`) i el xat de Copilot (o l'assistent que uses). Els prompts fan servir `#file:` de Copilot; si uses un altre assistent, adjunta els dos fitxers de `docs/`.
2. Comença **cada conversa nova** amb el **Prompt 0**. Al pas 1 el guardes a `.github/copilot-instructions.md`.
3. Executa els prompts **en ordre i d'un en un**. Els prompts es numeren per pas: el *Prompt 6.2* és el segon prompt del pas 6.
4. **Revisa el codi** abans d'acceptar-lo: ha de complir les regles del Prompt 0. Entendre'l és part de la defensa.
5. Després de cada prompt, compila: `./mvnw -q compile`. Si falla, usa el **Prompt de correcció**.

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

## Resum dels 29 apartats

| Pas | Issue | Branca | Commit |
|---|---|---|---|
| 1 | [Backend] Crear el projecte Spring Boot (a l'arrel del repositori) | `feature/01-projecte-spring-boot` | `chore(backend): crea el projecte Spring Boot amb les dependències base` |
| 2 | [Backend] Afegir la documentació i l'script SQL de PostgreSQL a docs/ | `feature/02-docs-script-sql` | `docs: afegeix la proposta, l'API i l'script SQL de PostgreSQL` |
| 3 | [Backend] Configuració base: PostgreSQL, JWT i Swagger | `feature/03-configuracio-base` | `feat(backend): configuració base amb PostgreSQL, JWT i Swagger` |
| 4 | [Backend] Excepcions, DTO comuns i manejador global d'errors | `feature/04-errors-dto-comuns` | `feat(backend): excepcions, DTO comuns i manejador global d'errors` |
| 5 | [Backend] Crear els enums del domini | `feature/05-enums-domini` | `feat(backend): enums del domini` |
| 6 | [Backend] Crear les entitats JPA i l'entitat de la vista d'ordres | `feature/06-entitats-jpa` | `feat(backend): entitats JPA i entitat de la vista d'ordres` |
| 7 | [Backend] Repositoris Spring Data i prova de fum amb les dades d'exemple | `feature/07-repositoris-prova-fum` | `feat(backend): repositoris Spring Data i prova de fum amb les dades d'exemple` |
| 8 | [Backend] Usuari autenticat, servei JWT i filtre d'autenticació | `feature/08-usuari-jwt` | `feat(security): usuari autenticat, servei JWT i filtre d'autenticació` |
| 9 | [Backend] Configuració de Spring Security (stateless, CORS i errors 401/403) | `feature/09-spring-security-config` | `feat(security): configuració stateless amb CORS i errors 401 i 403 en JSON` |
| 10 | [Backend] Registre, login i /auth/yo amb proves | `feature/10-registre-login` | `feat(auth): registre, login i /auth/yo amb proves` |
| 11 | [Backend] Gestió d'usuaris per a l'administrador i contrasenyes de desenvolupament | `feature/11-gestio-usuaris` | `feat(usuarios): gestió d'usuaris per a l'administrador i contrasenyes de desenvolupament` |
| 12 | [Backend] Validació i normalització de matrícules | `feature/12-validacio-matricula` | `feat(validation): anotació @MatriculaEspanola i utilitat de normalització` |
| 13 | [Backend] CRUD de vehicles | `feature/13-vehicles` | `feat(vehiculos): CRUD de vehicles` |
| 14 | [Backend] Serveis base de seguiment i notificacions | `feature/14-serveis-seguiment-avisos` | `feat(servicios): serveis base de seguiment i notificacions` |
| 15 | [Backend] Creació i consulta de sol·licituds de reparació (ordres) | `feature/15-sollicituds-reparacio` | `feat(ordenes): creació i consulta de sol·licituds de reparació` |
| 16 | [Backend] Historial d'ordres d'un vehicle | `feature/16-historial-vehicle` | `feat(vehiculos): historial d'ordres d'un vehicle` |
| 17 | [Backend] Màquina d'estats de les ordres i assignació de mecànic | `feature/17-maquina-estats` | `feat(ordenes): màquina d'estats i assignació de mecànic` |
| 18 | [Backend] Catàleg de peces i control d'stock | `feature/18-peces-stock` | `feat(piezas): catàleg de peces i control d'stock` |
| 19 | [Backend] Pressupostos: creació, consulta i edició de línies | `feature/19-pressupostos-linies` | `feat(presupuestos): creació de pressupostos i línies amb totals automàtics` |
| 20 | [Backend] Pressupostos: enviar, autoritzar i rebutjar | `feature/20-pressupostos-cicle` | `feat(presupuestos): enviar, autoritzar i rebutjar pressupostos` |
| 21 | [Backend] Canvi d'estat manual, cancel·lació i descompte d'stock | `feature/21-canvi-estat-stock` | `feat(ordenes): canvi d'estat manual, cancel·lació i descompte d'stock` |
| 22 | [Backend] Seguiment de la reparació per al client | `feature/22-seguiment-client` | `feat(ordenes): endpoint de seguiment de la reparació` |
| 23 | [Backend] Endpoints de notificacions | `feature/23-notificacions` | `feat(notificaciones): llistat, lectura i comptador de notificacions` |
| 24 | [Backend] Panell de mètriques per a l'administrador | `feature/24-panell-metriques` | `feat(panel): mètriques per a l'administrador` |
| 25 | [Backend] Prova d'integració del flux complet de la demostració | `feature/25-flux-complet` | `test(backend): prova del flux complet de la demostració` |
| 26 | [Backend] Documentar els endpoints amb Swagger | `feature/26-documentacio-swagger` | `docs(backend): documentació Swagger dels endpoints` |
| 27 | [Backend] README del projecte | `feature/27-readme-projecte` | `docs: README del projecte` |
| 28 | [Backend] Revisió de seguretat i correccions | `feature/28-revisio-seguretat` | `chore(security): revisió de seguretat i correccions` |
| 29 | [Backend] (Opcional) Límit d'intents al login | `feature/29-limit-intents-login` | `feat(security): límit d'intents al login` |

**Total: 116 classes de producció** (+ 1 opcional) **i 10 classes de prova**, repartides en els 29 apartats.

---

## Pas 1 · Crear el projecte Spring Boot

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Crear el projecte Spring Boot (a l'arrel del repositori)` |
| **Branca** | `feature/01-projecte-spring-boot` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Tindre el projecte Spring Boot creat a l'arrel del repositori, amb les dependències base, un `.gitignore` complet i les instruccions de context per a l'assistent d'IA.

## Tasques
- [ ] Generar el projecte amb Spring Initializr (Maven, Java 21, `com.motorlab`, artifact `motorlab-backend`) a l'arrel del repositori, al costat de `docs/`
- [ ] Completar el `.gitignore` de l'arrel (el projecte generat ja en porta un)
- [ ] Crear `.github/copilot-instructions.md` amb el Prompt 0
- [ ] Comprovar que el projecte compila

## Criteris d'acceptació
- [ ] `./mvnw -q compile` acaba sense errors
- [ ] Existeix `src/main/java/com/motorlab/MotorLabApplication.java`
- [ ] `git status` no mostra les carpetes `target/` ni `node_modules/`

## Classes
- `com.motorlab`: `MotorLabApplication`

## Depèn de
Cap (és la primera issue).

## Guia
Pas 1 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/01-projecte-spring-boot
```

### Què es crea

- **com.motorlab** (1): `MotorLabApplication`

### Execució (en ordre)

#### Crea el projecte (primer pas de tots)

**Opció A · VS Code:** `Ctrl+Shift+P` → *Spring Initializr: Create a Maven Project*. **Opció B · web:** start.spring.io.

| Camp | Valor |
|---|---|
| Project / Language | Maven · Java |
| Spring Boot | l'última versió estable (mai una `SNAPSHOT`) |
| Group | `com.motorlab` |
| Artifact | `motorlab-backend` |
| Name | `MotorLab` |
| Package name | `com.motorlab` |
| Packaging / Java | Jar · 21 (o 17) |
| Dependències | *Spring Web*, *Spring Data JPA*, *PostgreSQL Driver*, *Spring Security*, *Validation*, *Spring Boot Actuator*, *Spring Boot DevTools* |

Descomprimeix el `.zip` (amb l'Explorador de Windows) i deixa el contingut **directament a l'arrel del repositori** (`motorlab/`), de manera que `pom.xml` i `mvnw` queden al costat de `docs/`. Si Windows pregunta per sobreescriure fitxers, **conserva el teu `README.md`** i fusiona el `.gitignore` (vegeu més avall). Obri la carpeta `motorlab/` a VS Code i accepta instal·lar l'*Extension Pack for Java* i el *Spring Boot Extension Pack*.

L'estructura resultant ha de ser:

```text
motorlab/
├── .github/
│   └── copilot-instructions.md
├── .mvn/
├── docs/
├── src/
│   ├── main/java/com/motorlab/MotorLabApplication.java
│   ├── main/resources/
│   └── test/java/com/motorlab/
├── .gitignore
├── mvnw · mvnw.cmd · pom.xml
└── README.md
```

El futur frontend d'Angular anirà en una carpeta pròpia (`frontend/`) al costat de `src/`.

#### `.gitignore` de l'arrel

El projecte generat porta ja un `.gitignore` (amb `target/`, `HELP.md` i fitxers d'IDE). **No el substituïsques**: obri'l i **afig al final** estes línies:

```gitignore
# Node / Angular
node_modules/
dist/
.angular/

# Configuració local i secrets (no es pugen mai)
application-local.properties
.vscode/launch.json
.env

# Còpies de seguretat i IDE
backup_*.sql
.idea/
```

#### Instruccions per a l'assistent

Crea la carpeta `.github` i dins el fitxer `copilot-instructions.md` amb **el text complet del Prompt 0** (apartat «Prompt 0» d'esta guia). GitHub Copilot el llig automàticament en cada xat; si uses un altre assistent, enganxa el Prompt 0 al començament de cada conversa.

### Comprovació

```bash
./mvnw -q compile
git status
```
No arranques encara l'aplicació: fins al pas 3 no té configuració de base de dades.

### Commit, push i pull request

```bash
git add .
git commit -m "chore(backend): crea el projecte Spring Boot amb les dependències base"
git push -u origin feature/01-projecte-spring-boot
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Projecte Spring Boot base` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Generar el projecte amb Spring Initializr (Maven, Java 21, `com.motorlab`, artifact `motorlab-backend`) a l'arrel del repositori, al costat de `docs/`
- [x] Completar el `.gitignore` de l'arrel (el projecte generat ja en porta un)
- [x] Crear `.github/copilot-instructions.md` amb el Prompt 0
- [x] Comprovar que el projecte compila

## Com provar-ho
- `./mvnw -q compile` acaba sense errors
- Existeix `src/main/java/com/motorlab/MotorLabApplication.java`
- `git status` no mostra les carpetes `target/` ni `node_modules/`

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #1
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/01-projecte-spring-boot`

---

## Pas 2 · Documentació i script SQL

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Afegir la documentació i l'script SQL de PostgreSQL a docs/` |
| **Branca** | `feature/02-docs-script-sql` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Tindre al repositori la documentació del projecte i l'script que crea la base de dades, i deixar la base de dades local preparada.

## Tasques
- [ ] Copiar a `docs/` la proposta (Word i PDF), `motorlab_api.md`, el diagrama de la BBDD i `motorlab_schema.sql` (PostgreSQL)
- [ ] Crear en local l'usuari `motorlab_usr` i les bases de dades `motorlabdb` i `motorlab_testdb` (no es puja a Git)
- [ ] Carregar l'script a les dues bases de dades
- [ ] Comprovar les taules, els totals del pressupost i els triggers

## Criteris d'acceptació
- [ ] `\dt` mostra 8 taules a `motorlabdb` i a `motorlab_testdb`
- [ ] `SELECT subtotal, iva, total FROM presupuestos WHERE id = 1;` torna 212.00 | 44.52 | 256.52
- [ ] `SELECT tgname FROM pg_trigger WHERE NOT tgisinternal;` mostra 3 triggers

## Depèn de
#1

## Guia
Pas 2 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/02-docs-script-sql
```

### Execució (en ordre)

#### Copia els documents a `docs/`

| Fitxer | Destí |
|---|---|
| `propuesta-proyecto-motorlab.docx` i `.pdf` | `docs/` |
| `motorlab_api.md` | `docs/` |
| `motorlab_schema.sql` (el de PostgreSQL) | `docs/` |
| `diagrama-bbdd-motorlab.png` | `docs/` |
| `guia_backend_motorlab.md` i esta guia | `docs/` |
| `motorlab_schema_mysql.sql` (opcional, històric) | `docs/legacy/` |

#### Prepara la base de dades en local (això **no** es puja a Git)

Entra com a administrador de PostgreSQL i crea l'usuari i les dues bases de dades:

```bash
winpty psql -U postgres
```

```sql
CREATE USER motorlab_usr WITH PASSWORD 'motorlab_usr';
CREATE DATABASE motorlabdb      OWNER motorlab_usr ENCODING 'UTF8' TEMPLATE template0;
CREATE DATABASE motorlab_testdb OWNER motorlab_usr ENCODING 'UTF8' TEMPLATE template0;
\l
\q
```

Carrega l'script a les dues, des de l'arrel del repositori i amb el teu usuari (no amb `postgres`):

```bash
winpty psql -U motorlab_usr -d motorlabdb      -f docs/motorlab_schema.sql
winpty psql -U motorlab_usr -d motorlab_testdb -f docs/motorlab_schema.sql
```

La contrasenya igual que l'usuari només val per al teu ordinador. L'aplicació la llegirà de variables d'entorn: mai l'escrigues al codi ni la puges a Git.

### Comprovació

```bash
winpty psql -U motorlab_usr -d motorlabdb
```

```sql
\dt
SELECT subtotal, iva, total FROM presupuestos WHERE id = 1;
SELECT orden_id, estado, progreso, urgencia FROM v_ordenes_resumen;
SELECT tgname FROM pg_trigger WHERE NOT tgisinternal;
```

Ha de donar 8 taules, **212.00 | 44.52 | 256.52**, `arreglando | 80 | alta` i 3 triggers. Repeteix `\dt` a `motorlab_testdb`.

### Commit, push i pull request

```bash
git add .
git commit -m "docs: afegeix la proposta, l'API i l'script SQL de PostgreSQL"
git push -u origin feature/02-docs-script-sql
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Documentació i script SQL de PostgreSQL` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Copiar a `docs/` la proposta (Word i PDF), `motorlab_api.md`, el diagrama de la BBDD i `motorlab_schema.sql` (PostgreSQL)
- [x] Crear en local l'usuari `motorlab_usr` i les bases de dades `motorlabdb` i `motorlab_testdb` (no es puja a Git)
- [x] Carregar l'script a les dues bases de dades
- [x] Comprovar les taules, els totals del pressupost i els triggers

## Com provar-ho
- `\dt` mostra 8 taules a `motorlabdb` i a `motorlab_testdb`
- `SELECT subtotal, iva, total FROM presupuestos WHERE id = 1;` torna 212.00 | 44.52 | 256.52
- `SELECT tgname FROM pg_trigger WHERE NOT tgisinternal;` mostra 3 triggers

## Checklist
- [ ] Els fitxers estan a `docs/`
- [ ] L'script s'ha executat a `motorlabdb` i a `motorlab_testdb`
- [ ] No es puja cap contrasenya real, secret ni `.env`

Closes #2
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/02-docs-script-sql`

---

## Pas 3 · Configuració base

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Configuració base: PostgreSQL, JWT i Swagger` |
| **Branca** | `feature/03-configuracio-base` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Afegir les dependències de JWT i Swagger, configurar la connexió a PostgreSQL i crear les classes de configuració.

## Tasques
- [ ] Afegir `jjwt` i `springdoc-openapi` al `pom.xml`
- [ ] Crear `application.properties` (PostgreSQL, Jackson en snake_case, propietats de JWT)
- [ ] Crear `JwtProperties` i `OpenApiConfig`
- [ ] Definir les variables d'entorn en local (`export` o `.vscode/launch.json`)

## Criteris d'acceptació
- [ ] L'aplicació arranca i al log apareix `HikariPool-1 - Start completed` i `Started MotorLabApplication`
- [ ] No hi ha cap contrasenya escrita al codi ni a `application.properties`

## Classes
- `config`: `JwtProperties`, `OpenApiConfig`

## Depèn de
#2

## Guia
Pas 3 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/03-configuracio-base
```

### Què es crea

- **config** (2): `JwtProperties`, `OpenApiConfig`

### Execució (en ordre)

#### Prompt 3.1 · Dependències JWT i Swagger (pom.xml)

```text
Abre pom.xml (en la raíz del repositorio) y añade estas dependencias, sin cambiar nada más:
- io.jsonwebtoken: jjwt-api, jjwt-impl (scope runtime) y jjwt-jackson (scope runtime), en la última versión estable 0.12.x.
- org.springdoc: springdoc-openapi-starter-webmvc-ui, en la versión compatible con la versión de Spring Boot de este proyecto.
Dime qué versiones has elegido y por qué son compatibles. No inventes versiones: si no estás seguro, indícame dónde comprobarlas.
```

#### `application.properties` (a mà)

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

Posa `ddl-auto=none`: l'esquema, els triggers i les columnes generades els crea l'script SQL, i `create` o `update` podrien ensutzar-lo.

#### Prompt 3.2 · Configuració JWT i Swagger (config)

```text
En el paquete com.motorlab.config crea:

1. JwtProperties: un record con @ConfigurationProperties(prefix = "app.jwt") y dos campos: String secret e int expiracionHoras. Actívalo añadiendo @ConfigurationPropertiesScan a la clase MotorLabApplication.
2. OpenApiConfig: una clase @Configuration con un bean OpenAPI con título "MotorLab API", versión "v1", una descripción breve en castellano y un esquema de seguridad "bearerAuth" (HTTP, bearer, formato JWT) aplicado globalmente, para que Swagger UI muestre el botón Authorize.
```

### Comprovació

Defineix les variables d'entorn a Git Bash (valen només mentre el terminal estiga obert) i arranca:

```bash
export DB_USER=motorlab_usr DB_PASS=motorlab_usr
export JWT_SECRET="$(openssl rand -base64 48)"
./mvnw spring-boot:run
```

Al log ha d'aparéixer `HikariPool-1 - Start completed` i `Started MotorLabApplication`. És normal que les rutes tornen **401**: Spring Security protegeix tot per defecte fins al pas 9.

Si arranques des del botó *Run* de VS Code, les variables de Git Bash **no** s'apliquen. Crea `.vscode/launch.json` (ja està al `.gitignore`):

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

### Commit, push i pull request

```bash
git add .
git commit -m "feat(backend): configuració base amb PostgreSQL, JWT i Swagger"
git push -u origin feature/03-configuracio-base
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Configuració base (PostgreSQL, JWT i Swagger)` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Afegir `jjwt` i `springdoc-openapi` al `pom.xml`
- [x] Crear `application.properties` (PostgreSQL, Jackson en snake_case, propietats de JWT)
- [x] Crear `JwtProperties` i `OpenApiConfig`
- [x] Definir les variables d'entorn en local (`export` o `.vscode/launch.json`)

## Com provar-ho
- L'aplicació arranca i al log apareix `HikariPool-1 - Start completed` i `Started MotorLabApplication`
- No hi ha cap contrasenya escrita al codi ni a `application.properties`

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #3
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/03-configuracio-base`

---

## Pas 4 · Gestió d'errors i DTO comuns

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Excepcions, DTO comuns i manejador global d'errors` |
| **Branca** | `feature/04-errors-dto-comuns` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Tindre el format d'errors de l'API (`{"error": {...}}`) i les excepcions que usaran tots els serveis.

## Tasques
- [ ] Crear els DTO comuns: `ErrorDetalle`, `ErrorResponse` i `PageResponse`
- [ ] Crear la jerarquia d'excepcions (`ApiException` i 10 subclasses)
- [ ] Crear `GlobalExceptionHandler` amb `@RestControllerAdvice`

## Criteris d'acceptació
- [ ] `./mvnw -q compile` sense errors
- [ ] Cada excepció té el seu codi HTTP i el seu `codigo` segons `docs/motorlab_api.md`

## Classes
- `dto.common`: `ErrorDetalle`, `ErrorResponse`, `PageResponse`
- `exception`: `ApiException`, `RecursoNoEncontradoException`, `SinPermisoException`, `EmailDuplicadoException`, `MatriculaDuplicadaException`, `TransicionNoPermitidaException`, `StockInsuficienteException`, `PresupuestoNoEditableException`, `PresupuestoYaExisteException`, `SolicitudInvalidaException`, `ConflictoException`, `GlobalExceptionHandler`

## Depèn de
#3

## Guia
Pas 4 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/04-errors-dto-comuns
```

### Què es crea

- **dto.common** (3): `ErrorDetalle`, `ErrorResponse`, `PageResponse`
- **exception** (12): `ApiException`, `RecursoNoEncontradoException`, `SinPermisoException`, `EmailDuplicadoException`, `MatriculaDuplicadaException`, `TransicionNoPermitidaException`, `StockInsuficienteException`, `PresupuestoNoEditableException`, `PresupuestoYaExisteException`, `SolicitudInvalidaException`, `ConflictoException`, `GlobalExceptionHandler`

### Execució (en ordre)

#### Prompt 4.1 · DTO comunes, excepciones y manejador de errores

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

### Comprovació

```bash
./mvnw -q compile
```
Revisa que `PageResponse.pageable(...)` use pàgina base 1, límit per defecte 20 i màxim 100.

### Commit, push i pull request

```bash
git add .
git commit -m "feat(backend): excepcions, DTO comuns i manejador global d'errors"
git push -u origin feature/04-errors-dto-comuns
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Excepcions, DTO comuns i manejador d'errors` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear els DTO comuns: `ErrorDetalle`, `ErrorResponse` i `PageResponse`
- [x] Crear la jerarquia d'excepcions (`ApiException` i 10 subclasses)
- [x] Crear `GlobalExceptionHandler` amb `@RestControllerAdvice`

## Com provar-ho
- `./mvnw -q compile` sense errors
- Cada excepció té el seu codi HTTP i el seu `codigo` segons `docs/motorlab_api.md`

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #4
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/04-errors-dto-comuns`

---

## Pas 5 · Enums del domini

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Crear els enums del domini` |
| **Branca** | `feature/05-enums-domini` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Definir els 11 enums amb les constants en minúscula, idèntiques als valors dels `CHECK` de l'script SQL.

## Tasques
- [ ] Crear els enums `Rol`, `TipoServicio`, `CuandoOcurre`, `ConduccionSegura`, `Urgencia`, `FranjaPreferida`, `ContactoPreferido`, `EstadoOrden`, `EstadoPresupuesto`, `TipoLinea` i `TipoNotificacion`
- [ ] Afegir `EstadoOrden.progreso()` (20, 40, 60, 80, 100 o 0)

## Criteris d'acceptació
- [ ] `./mvnw -q compile` sense errors
- [ ] Els valors coincideixen lletra per lletra amb `docs/motorlab_schema.sql`

## Classes
- `entity.enums`: `Rol`, `TipoServicio`, `CuandoOcurre`, `ConduccionSegura`, `Urgencia`, `FranjaPreferida`, `ContactoPreferido`, `EstadoOrden`, `EstadoPresupuesto`, `TipoLinea`, `TipoNotificacion`

## Depèn de
#4

## Guia
Pas 5 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/05-enums-domini
```

### Què es crea

- **entity.enums** (11): `Rol`, `TipoServicio`, `CuandoOcurre`, `ConduccionSegura`, `Urgencia`, `FranjaPreferida`, `ContactoPreferido`, `EstadoOrden`, `EstadoPresupuesto`, `TipoLinea`, `TipoNotificacion`

### Execució (en ordre)

#### Prompt 5.1 · Enums

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

### Comprovació

```bash
./mvnw -q compile
```
Compara cada enum amb els `CHECK` del SQL (`grep -n "CHECK" docs/motorlab_schema.sql`).

### Commit, push i pull request

```bash
git add .
git commit -m "feat(backend): enums del domini"
git push -u origin feature/05-enums-domini
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Enums del domini` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear els enums `Rol`, `TipoServicio`, `CuandoOcurre`, `ConduccionSegura`, `Urgencia`, `FranjaPreferida`, `ContactoPreferido`, `EstadoOrden`, `EstadoPresupuesto`, `TipoLinea` i `TipoNotificacion`
- [x] Afegir `EstadoOrden.progreso()` (20, 40, 60, 80, 100 o 0)

## Com provar-ho
- `./mvnw -q compile` sense errors
- Els valors coincideixen lletra per lletra amb `docs/motorlab_schema.sql`

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #5
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/05-enums-domini`

---

## Pas 6 · Entitats JPA

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Crear les entitats JPA i l'entitat de la vista d'ordres` |
| **Branca** | `feature/06-entitats-jpa` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Mapejar les 8 taules i la vista `v_ordenes_resumen` amb JPA, seguint exactament l'script SQL.

## Tasques
- [ ] Aplicar les regles de mapatge JPA (LAZY, sense cascades, columnes gestionades per la BD)
- [ ] Crear `Usuario` i `Vehiculo`
- [ ] Crear `OrdenTrabajo`, `EventoSeguimiento` i `Notificacion`
- [ ] Crear `Pieza`, `Presupuesto`, `LineaPresupuesto` i `OrdenResumen` (vista, només lectura)

## Criteris d'acceptació
- [ ] `./mvnw -q compile` sense errors
- [ ] Cada entitat té `@Table` amb el nom exacte de la taula
- [ ] Les columnes que calculen els triggers tenen `insertable = false, updatable = false`

## Classes
- `entity`: `Usuario`, `Vehiculo`, `OrdenTrabajo`, `EventoSeguimiento`, `Notificacion`, `Pieza`, `Presupuesto`, `LineaPresupuesto`, `OrdenResumen`

## Depèn de
#5

## Guia
Pas 6 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/06-entitats-jpa
```

### Què es crea

- **entity** (9): `Usuario`, `Vehiculo`, `OrdenTrabajo`, `EventoSeguimiento`, `Notificacion`, `Pieza`, `Presupuesto`, `LineaPresupuesto`, `OrdenResumen`

### Execució (en ordre)

Enganxa **primer** el prompt de les regles de mapatge i després els tres de les entitats, **dins de la mateixa conversa** perquè l'assistent conserve les regles.

#### Prompt 6.1 · Regles de mapatge JPA (enganxa-les una vegada, abans dels tres prompts següents)

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

#### Prompt 6.2 · Entidades Usuario y Vehiculo

```text
Crea las entidades Usuario (tabla usuarios) y Vehiculo (tabla vehiculos) con las reglas de mapeo anteriores.
Usuario: id, nombre, email, telefono, passwordHash (columna password_hash), rol (Rol), activo, consentimientoEn, creadoEn.
Vehiculo: id, usuario (@ManyToOne a Usuario, columna usuario_id), matricula, marca, modelo, version (nullable), anio (Integer), kilometros (Integer), activo.
```

#### Prompt 6.3 · Entidades OrdenTrabajo, EventoSeguimiento y Notificacion

```text
Crea las entidades OrdenTrabajo (ordenes_trabajo), EventoSeguimiento (eventos_seguimiento) y Notificacion (notificaciones) con las reglas de mapeo anteriores.
OrdenTrabajo: id, vehiculo (ManyToOne), mecanico (ManyToOne a Usuario, columna mecanico_id, nullable), tipoServicio, sintoma (String largo), cuandoOcurre (nullable), conduccionSegura, urgencia, fechaPreferida (LocalDate), franjaPreferida, contactoPreferido, estado (EstadoOrden), elevador (nullable), fechaEntrada (nullable), fechaEstimada (nullable), creadoEn, actualizadoEn.
EventoSeguimiento: id, orden (ManyToOne), estado (EstadoOrden), descripcion (nullable), fechaHora (LocalDateTime, la rellena Java), usuario (ManyToOne a Usuario, nullable).
Notificacion: id, usuario (ManyToOne), orden (ManyToOne, nullable), tipo (TipoNotificacion), mensaje, leida (boolean), creadaEn.
```

#### Prompt 6.4 · Entidades Pieza, Presupuesto, LineaPresupuesto y OrdenResumen (vista)

```text
Crea las entidades Pieza (piezas), Presupuesto (presupuestos), LineaPresupuesto (lineas_presupuesto) y OrdenResumen con las reglas de mapeo anteriores.
Pieza: id, referencia, nombre, precio, stock (int), activa (boolean).
Presupuesto: id, orden (@OneToOne LAZY, columna orden_id), estado (EstadoPresupuesto), ivaPorcentaje, subtotal, iva, total, tiempoEstimadoHoras (nullable), garantiaMeses (Integer), motivoRechazo (nullable), enviadoEn (nullable), respondidoEn (nullable), creadoEn.
LineaPresupuesto: id, presupuesto (ManyToOne), tipo (TipoLinea), pieza (ManyToOne a Pieza, nullable), concepto, cantidad (BigDecimal), precioUnitario (BigDecimal), importe (solo lectura).
OrdenResumen: entidad de SOLO LECTURA (@Immutable de Hibernate) mapeada a la vista v_ordenes_resumen, con @Id en ordenId y los campos: ordenId, estado (EstadoOrden), progreso (int), urgencia, tipoServicio, sintoma, elevador, fechaEntrada, fechaEstimada, vehiculoId, matricula, marca, modelo, clienteId, clienteNombre, mecanicoId (nullable), mecanicoNombre (nullable), presupuestoEstado (EstadoPresupuesto, nullable), presupuestoTotal (BigDecimal, nullable). Mira la definición de la vista en docs/motorlab_schema.sql.
```

### Comprovació

```bash
./mvnw -q compile
```
La comprovació real arriba al pas 7, amb la prova de fum contra les dades d'exemple.

### Commit, push i pull request

```bash
git add .
git commit -m "feat(backend): entitats JPA i entitat de la vista d'ordres"
git push -u origin feature/06-entitats-jpa
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Entitats JPA i vista d'ordres` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Aplicar les regles de mapatge JPA (LAZY, sense cascades, columnes gestionades per la BD)
- [x] Crear `Usuario` i `Vehiculo`
- [x] Crear `OrdenTrabajo`, `EventoSeguimiento` i `Notificacion`
- [x] Crear `Pieza`, `Presupuesto`, `LineaPresupuesto` i `OrdenResumen` (vista, només lectura)

## Com provar-ho
- `./mvnw -q compile` sense errors
- Cada entitat té `@Table` amb el nom exacte de la taula
- Les columnes que calculen els triggers tenen `insertable = false, updatable = false`

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #6
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/06-entitats-jpa`

---

## Pas 7 · Repositoris i prova de fum

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Repositoris Spring Data i prova de fum amb les dades d'exemple` |
| **Branca** | `feature/07-repositoris-prova-fum` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Crear els repositoris i comprovar des de Java que es llegeixen les dades d'exemple de PostgreSQL.

## Tasques
- [ ] Crear `application-test.properties` (base de dades `motorlab_testdb`)
- [ ] Crear els 9 repositoris amb les consultes necessàries (bloquejos pessimistes inclosos)
- [ ] Crear `IntegracionBase` i `RepositoriosSmokeTest`

## Criteris d'acceptació
- [ ] `./mvnw test` en verd amb `DB_USER` i `DB_PASS` definides
- [ ] La prova de fum comprova 3 usuaris, el pressupost 256.52, el progrés 80 i la peça DS-118 amb stock 1

## Classes
- `repository`: `UsuarioRepository`, `VehiculoRepository`, `OrdenTrabajoRepository`, `OrdenResumenRepository`, `PresupuestoRepository`, `LineaPresupuestoRepository`, `PiezaRepository`, `EventoSeguimientoRepository`, `NotificacionRepository`
- proves `support`: `IntegracionBase`
- proves `repository`: `RepositoriosSmokeTest`

## Depèn de
#6

## Guia
Pas 7 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/07-repositoris-prova-fum
```

### Què es crea

- **repository** (9): `UsuarioRepository`, `VehiculoRepository`, `OrdenTrabajoRepository`, `OrdenResumenRepository`, `PresupuestoRepository`, `LineaPresupuestoRepository`, `PiezaRepository`, `EventoSeguimientoRepository`, `NotificacionRepository`
- **Proves** (`src/test/java/com/motorlab/...`): `support.IntegracionBase`, `repository.RepositoriosSmokeTest`

### Execució (en ordre)

#### Configuració de les proves (a mà)

Crea `src/test/resources/application-test.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/motorlab_testdb
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
app.jwt.secret=clave-solo-para-pruebas-de-mas-de-32-caracteres-0123456789
```

Este fitxer sí es puja a Git: no conté cap secret real.

#### Prompt 7.1 · Repositorios

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

#### Prompt 7.2 · Prueba de humo con los datos de ejemplo

```text
En src/test/java/com/motorlab crea:
1. support.IntegracionBase: clase abstracta con @SpringBootTest, @ActiveProfiles("test") y @Transactional, para que cada prueba haga rollback.
2. repository.RepositoriosSmokeTest (extiende IntegracionBase) que compruebe con los datos de ejemplo de docs/motorlab_schema.sql: hay 3 usuarios; findByEmail("laura@cliente.example") devuelve rol cliente; el presupuesto de la orden 2481 tiene subtotal 212.00, iva 44.52 y total 256.52 (compara BigDecimal con compareTo); la orden 2481 en OrdenResumenRepository tiene progreso 80 y urgencia alta; hay 4 líneas de presupuesto; y la pieza DS-118 tiene stock 1.
```

### Comprovació

```bash
export DB_USER=motorlab_usr DB_PASS=motorlab_usr
./mvnw test
```
La prova ha de passar en verd. Si falla amb errors de mapatge, usa el Prompt de correcció amb l'error complet.

### Commit, push i pull request

```bash
git add .
git commit -m "feat(backend): repositoris Spring Data i prova de fum amb les dades d'exemple"
git push -u origin feature/07-repositoris-prova-fum
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Repositoris i prova de fum` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `application-test.properties` (base de dades `motorlab_testdb`)
- [x] Crear els 9 repositoris amb les consultes necessàries (bloquejos pessimistes inclosos)
- [x] Crear `IntegracionBase` i `RepositoriosSmokeTest`

## Com provar-ho
- `./mvnw test` en verd amb `DB_USER` i `DB_PASS` definides
- La prova de fum comprova 3 usuaris, el pressupost 256.52, el progrés 80 i la peça DS-118 amb stock 1

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #7
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/07-repositoris-prova-fum`

---

## Pas 8 · Usuari autenticat i JWT

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Usuari autenticat, servei JWT i filtre d'autenticació` |
| **Branca** | `feature/08-usuari-jwt` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Tindre la base de seguretat: l'usuari autenticat, la generació i validació de tokens JWT i el filtre que els llig.

## Tasques
- [ ] Crear `UsuarioPrincipal`, `UsuarioDetailsService` i `SecurityUtils`
- [ ] Crear `JwtService` (jjwt 0.12.x) i `JwtAuthenticationFilter`

## Criteris d'acceptació
- [ ] `./mvnw -q compile` sense errors
- [ ] Un secret de menys de 32 caràcters fa fallar l'arrancada amb `IllegalStateException`

## Classes
- `security`: `UsuarioPrincipal`, `UsuarioDetailsService`, `SecurityUtils`, `JwtService`, `JwtAuthenticationFilter`

## Depèn de
#7

## Guia
Pas 8 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/08-usuari-jwt
```

### Què es crea

- **security** (5): `UsuarioPrincipal`, `UsuarioDetailsService`, `SecurityUtils`, `JwtService`, `JwtAuthenticationFilter`

### Execució (en ordre)

#### Prompt 8.1 · Usuario autenticado

```text
En el paquete com.motorlab.security crea:
1. UsuarioPrincipal, que implementa UserDetails. Guarda id, nombre, email, passwordHash, rol (Rol) y activo. getAuthorities() devuelve una sola authority "ROLE_" + rol.name().toUpperCase() (por ejemplo ROLE_ADMIN). getUsername() devuelve el email, isEnabled() devuelve activo y el resto de métodos devuelven true. Incluye un método estático desde(Usuario).
2. UsuarioDetailsService, que implementa UserDetailsService: loadUserByUsername(email) busca en UsuarioRepository y lanza UsernameNotFoundException si no existe. Añade también loadUserById(Integer) para el filtro JWT.
3. SecurityUtils: clase de utilidad con métodos estáticos usuarioActual() (UsuarioPrincipal del SecurityContext), idActual(), esAdmin(), esMecanico() y esCliente().
```

#### Prompt 8.2 · JWT: servicio y filtro

```text
En com.motorlab.security crea, usando jjwt 0.12.x:
1. JwtService (@Service), que usa JwtProperties. La clave es Keys.hmacShaKeyFor(secret.getBytes(UTF_8)); si el secreto tiene menos de 32 caracteres, lanza IllegalStateException al arrancar. Métodos: String generarToken(UsuarioPrincipal) con subject = id del usuario, claims rol y nombre, fecha de emisión y caducidad = ahora + expiracionHoras; Integer extraerUsuarioId(String token); y boolean esValido(String token), que devuelva false ante cualquier JwtException o IllegalArgumentException. Usa la API de jjwt 0.12 (Jwts.builder().subject(...).signWith(...), Jwts.parser().verifyWith(key).build().parseSignedClaims(token)).
2. JwtAuthenticationFilter (extiende OncePerRequestFilter): lee la cabecera Authorization: Bearer <token>; si el token es válido, carga el usuario por id con UsuarioDetailsService y, si está activo, coloca un UsernamePasswordAuthenticationToken en el SecurityContext. Ante cualquier fallo, continúa la cadena sin autenticar (el 401 lo devuelve el EntryPoint).
```

### Comprovació

```bash
./mvnw -q compile
```
Les proves del JWT arriben al pas 10, junt amb el login.

### Commit, push i pull request

```bash
git add .
git commit -m "feat(security): usuari autenticat, servei JWT i filtre d'autenticació"
git push -u origin feature/08-usuari-jwt
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Usuari autenticat i servei JWT` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `UsuarioPrincipal`, `UsuarioDetailsService` i `SecurityUtils`
- [x] Crear `JwtService` (jjwt 0.12.x) i `JwtAuthenticationFilter`

## Com provar-ho
- `./mvnw -q compile` sense errors
- Un secret de menys de 32 caràcters fa fallar l'arrancada amb `IllegalStateException`

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #8
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/08-usuari-jwt`

---

## Pas 9 · Configuració de Spring Security

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Configuració de Spring Security (stateless, CORS i errors 401/403)` |
| **Branca** | `feature/09-spring-security-config` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Protegir l'API: sessió sense estat, CORS per a Angular i respostes 401 i 403 en JSON.

## Tasques
- [ ] Crear `RestAuthenticationEntryPoint` i `RestAccessDeniedHandler`
- [ ] Crear `SecurityConfig` (CSRF desactivat, STATELESS, CORS per a `http://localhost:4200`, rutes públiques, filtre JWT)
- [ ] Definir els beans `PasswordEncoder` (BCrypt) i `AuthenticationManager`

## Criteris d'acceptació
- [ ] `GET /actuator/health` torna 200
- [ ] Qualsevol ruta protegida sense token torna 401 amb el cos d'error en JSON
- [ ] Swagger UI es veu a `http://localhost:8080/swagger-ui.html`

## Classes
- `security`: `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`
- `config`: `SecurityConfig`

## Depèn de
#8

## Guia
Pas 9 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/09-spring-security-config
```

### Què es crea

- **security** (2): `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`
- **config** (1): `SecurityConfig`

### Execució (en ordre)

#### Prompt 9.1 · Configuración de seguridad

```text
Crea:
1. security.RestAuthenticationEntryPoint (AuthenticationEntryPoint): responde 401 con Content-Type application/json y un ErrorResponse con código NO_AUTENTICADO y mensaje "Autenticación requerida o token caducado".
2. security.RestAccessDeniedHandler (AccessDeniedHandler): responde 403 con ErrorResponse SIN_PERMISO y mensaje "No tienes permiso para realizar esta acción". Ambos serializan con el ObjectMapper inyectado (para respetar snake_case).
3. config.SecurityConfig (@Configuration, @EnableWebSecurity, @EnableMethodSecurity) con un SecurityFilterChain: CSRF desactivado, sesión STATELESS, CORS para el origen http://localhost:4200 (métodos GET, POST, PUT, PATCH, DELETE y OPTIONS; cabeceras Authorization y Content-Type), manejo de excepciones con las dos clases anteriores, y reglas: permitAll para POST /api/v1/auth/registro, POST /api/v1/auth/login, /swagger-ui/**, /swagger-ui.html, /v3/api-docs/** y GET /actuator/health; el resto, authenticated. Añade JwtAuthenticationFilter antes de UsernamePasswordAuthenticationFilter. Define también los beans PasswordEncoder (BCryptPasswordEncoder) y AuthenticationManager (desde AuthenticationConfiguration). Evita dependencias circulares: el filtro no debe depender de SecurityConfig.
```

### Comprovació

```bash
export DB_USER=motorlab_usr DB_PASS=motorlab_usr
export JWT_SECRET="$(openssl rand -base64 48)"
./mvnw spring-boot:run
```
En un altre terminal de Git Bash:
```bash
curl -i http://localhost:8080/actuator/health     # 200
curl -i http://localhost:8080/api/v1/auth/yo      # 401 amb JSON
```

### Commit, push i pull request

```bash
git add .
git commit -m "feat(security): configuració stateless amb CORS i errors 401 i 403 en JSON"
git push -u origin feature/09-spring-security-config
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Configuració de Spring Security` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `RestAuthenticationEntryPoint` i `RestAccessDeniedHandler`
- [x] Crear `SecurityConfig` (CSRF desactivat, STATELESS, CORS per a `http://localhost:4200`, rutes públiques, filtre JWT)
- [x] Definir els beans `PasswordEncoder` (BCrypt) i `AuthenticationManager`

## Com provar-ho
- `GET /actuator/health` torna 200
- Qualsevol ruta protegida sense token torna 401 amb el cos d'error en JSON
- Swagger UI es veu a `http://localhost:8080/swagger-ui.html`

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #9
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/09-spring-security-config`

---

## Pas 10 · Registre i login

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Registre, login i /auth/yo amb proves` |
| **Branca** | `feature/10-registre-login` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Permetre que un client es registre, inicie sessió i consulte les seues dades, amb proves automàtiques.

## Tasques
- [ ] Crear `RegistroRequest`, `LoginRequest`, `LoginResponse`, `UsuarioResponse` i `UsuarioResumenResponse`
- [ ] Crear `UsuarioMapper`, `AuthService` i `AuthController`
- [ ] Crear `JwtServiceTest` i `AuthControllerTest`

## Criteris d'acceptació
- [ ] `./mvnw test` en verd
- [ ] A Swagger: `POST /auth/registro` (201), `POST /auth/login` (token) i `GET /auth/yo` amb el token (200)
- [ ] Un email repetit torna 409 `EMAIL_DUPLICADO` i unes credencials errònies tornen 401

## Classes
- `dto.auth`: `RegistroRequest`, `LoginRequest`, `LoginResponse`
- `dto.usuario`: `UsuarioResponse`, `UsuarioResumenResponse`
- `mapper`: `UsuarioMapper`
- `service`: `AuthService`
- `controller`: `AuthController`
- proves `security`: `JwtServiceTest`
- proves `controller`: `AuthControllerTest`

## Depèn de
#9

## Guia
Pas 10 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/10-registre-login
```

### Què es crea

- **dto.auth** (3): `RegistroRequest`, `LoginRequest`, `LoginResponse`
- **dto.usuario** (2): `UsuarioResponse`, `UsuarioResumenResponse`
- **mapper** (1): `UsuarioMapper`
- **service** (1): `AuthService`
- **controller** (1): `AuthController`
- **Proves** (`src/test/java/com/motorlab/...`): `security.JwtServiceTest`, `controller.AuthControllerTest`

### Execució (en ordre)

#### Prompt 10.1 · Registro, login y /auth/yo

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

#### Prompt 10.2 · Pruebas de seguridad

```text
En src/test/java/com/motorlab crea (ambas extienden IntegracionBase o usan @SpringBootTest + @ActiveProfiles("test") + @Transactional):
1. security.JwtServiceTest (prueba unitaria): un token generado es válido y devuelve el id correcto; un token manipulado no es válido; un token caducado (usa un JwtProperties con expiracionHoras = 0 o negativo) no es válido; un secreto de menos de 32 caracteres lanza IllegalStateException.
2. controller.AuthControllerTest (@AutoConfigureMockMvc): registro correcto (201); registro con email repetido (409 EMAIL_DUPLICADO); registro sin aceptar la privacidad (400 con detalles); login correcto devuelve token; login con contraseña errónea (401); GET /api/v1/auth/yo sin token (401) y con token (200).
```

### Comprovació

```bash
export DB_USER=motorlab_usr DB_PASS=motorlab_usr
./mvnw test
```
Després arranca l'aplicació i prova el registre i el login des de Swagger (`http://localhost:8080/swagger-ui.html`).

### Commit, push i pull request

```bash
git add .
git commit -m "feat(auth): registre, login i /auth/yo amb proves"
git push -u origin feature/10-registre-login
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Registre i login amb JWT` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `RegistroRequest`, `LoginRequest`, `LoginResponse`, `UsuarioResponse` i `UsuarioResumenResponse`
- [x] Crear `UsuarioMapper`, `AuthService` i `AuthController`
- [x] Crear `JwtServiceTest` i `AuthControllerTest`

## Com provar-ho
- `./mvnw test` en verd
- A Swagger: `POST /auth/registro` (201), `POST /auth/login` (token) i `GET /auth/yo` amb el token (200)
- Un email repetit torna 409 `EMAIL_DUPLICADO` i unes credencials errònies tornen 401

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #10
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/10-registre-login`

---

## Pas 11 · Gestió d'usuaris

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Gestió d'usuaris per a l'administrador i contrasenyes de desenvolupament` |
| **Branca** | `feature/11-gestio-usuaris` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Que l'administrador puga llistar, crear i modificar usuaris, i poder entrar amb els usuaris d'exemple en local.

## Tasques
- [ ] Crear `UsuarioCreateRequest`, `UsuarioUpdateRequest`, `UsuarioService` i `UsuarioController`
- [ ] Crear `InicializadorUsuarios` (només amb el perfil `local`)
- [ ] Crear `application-local.properties` (no es puja a Git)

## Criteris d'acceptació
- [ ] Amb el perfil `local`, `admin@taller.example` pot iniciar sessió
- [ ] Un token de client a `GET /usuarios` torna 403
- [ ] Un administrador no pot desactivar-se a si mateix

## Classes
- `dto.usuario`: `UsuarioCreateRequest`, `UsuarioUpdateRequest`
- `service`: `UsuarioService`
- `controller`: `UsuarioController`
- `config`: `InicializadorUsuarios`

## Depèn de
#10

## Guia
Pas 11 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/11-gestio-usuaris
```

### Què es crea

- **dto.usuario** (2): `UsuarioCreateRequest`, `UsuarioUpdateRequest`
- **service** (1): `UsuarioService`
- **controller** (1): `UsuarioController`
- **config** (1): `InicializadorUsuarios`

### Execució (en ordre)

#### Prompt 11.1 · Gestión de usuarios (administrador)

```text
Implementa la sección 4.7 de docs/motorlab_api.md (solo usuarios, sin el panel):
- dto.usuario.UsuarioCreateRequest: nombre, email, telefono, password (mismas validaciones que el registro) y rol (@NotNull, de tipo Rol).
- dto.usuario.UsuarioUpdateRequest: rol y activo, ambos opcionales (al menos uno obligatorio).
- service.UsuarioService: listar(Rol rol, pagina, limite) que devuelva PageResponse<UsuarioResponse>; crear (email único, contraseña con BCrypt); actualizar (un administrador no puede desactivarse ni cambiarse el rol a sí mismo: lanza SolicitudInvalidaException).
- controller.UsuarioController con @PreAuthorize("hasRole('ADMIN')") en la clase: GET /api/v1/usuarios?rol&pagina&limite, POST /api/v1/usuarios (201) y PATCH /api/v1/usuarios/{id}.
```

#### Prompt 11.2 · Contraseñas de los usuarios de ejemplo (solo desarrollo)

```text
Los usuarios de ejemplo de docs/motorlab_schema.sql tienen password_hash = 'REEMPLAZAR_POR_HASH_BCRYPT'. Crea config.InicializadorUsuarios, un CommandLineRunner con @Profile("local") y @Transactional que, al arrancar, busque los usuarios con ese valor y les ponga como contraseña el valor de la propiedad app.dev.password-semilla cifrado con BCrypt, registrando en el log cuáles ha actualizado. Explícame cómo activar el perfil "local" y dime qué contiene el archivo src/main/resources/application-local.properties (que NO se sube a Git).
```

#### Perfil `local` (a mà)

Crea `src/main/resources/application-local.properties` (ja està al `.gitignore`):

```properties
app.dev.password-semilla=MotorLab2026!
```

I arranca amb el perfil activat:

```bash
export SPRING_PROFILES_ACTIVE=local
```

### Comprovació

Arranca el backend amb el perfil `local` i, des de Swagger:
1. `POST /auth/login` amb `admin@taller.example` i la contrasenya de `application-local.properties` → reps un token.
2. **Authorize** amb el token i `GET /usuarios` → llista els usuaris.
3. Un token de client a `GET /usuarios` → **403**.

### Commit, push i pull request

```bash
git add .
git commit -m "feat(usuarios): gestió d'usuaris per a l'administrador i contrasenyes de desenvolupament"
git push -u origin feature/11-gestio-usuaris
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Gestió d'usuaris i usuaris d'exemple` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `UsuarioCreateRequest`, `UsuarioUpdateRequest`, `UsuarioService` i `UsuarioController`
- [x] Crear `InicializadorUsuarios` (només amb el perfil `local`)
- [x] Crear `application-local.properties` (no es puja a Git)

## Com provar-ho
- Amb el perfil `local`, `admin@taller.example` pot iniciar sessió
- Un token de client a `GET /usuarios` torna 403
- Un administrador no pot desactivar-se a si mateix

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #11
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/11-gestio-usuaris`

---

## Pas 12 · Validació de matrícula

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Validació i normalització de matrícules` |
| **Branca** | `feature/12-validacio-matricula` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Validar les matrícules espanyoles i guardar-les sempre normalitzades.

## Tasques
- [ ] Crear `MatriculaUtils.normalizar`
- [ ] Crear l'anotació `@MatriculaEspanola` i `MatriculaValidator`
- [ ] Crear `MatriculaValidatorTest`

## Criteris d'acceptació
- [ ] `./mvnw test` en verd
- [ ] «4821 KLB», «4821-klb» i «1234BCD» són vàlides; «4821 AEI», «123 ABC» i «4821KL» no ho són

## Classes
- `util`: `MatriculaUtils`
- `validation`: `MatriculaEspanola`, `MatriculaValidator`
- proves `validation`: `MatriculaValidatorTest`

## Depèn de
#11

## Guia
Pas 12 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/12-validacio-matricula
```

### Què es crea

- **util** (1): `MatriculaUtils`
- **validation** (2): `MatriculaEspanola`, `MatriculaValidator`
- **Proves** (`src/test/java/com/motorlab/...`): `validation.MatriculaValidatorTest`

### Execució (en ordre)

#### Prompt 12.1 · Validación y utilidad de matrícula

```text
Crea:
1. util.MatriculaUtils con un método estático normalizar(String) que haga trim, elimine espacios y guiones y pase a mayúsculas (null-safe).
2. validation.MatriculaEspanola: anotación de Bean Validation (aplicable a campos, parámetros y componentes de record) con mensaje por defecto "Formato de matrícula no válido".
3. validation.MatriculaValidator: ConstraintValidator que normaliza con MatriculaUtils y comprueba la expresión ^[0-9]{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$ (null se considera válido; la obligatoriedad se controla con @NotBlank).
4. Prueba src/test/java/.../validation/MatriculaValidatorTest: válidas "4821 KLB", "4821-klb", "1234BCD"; no válidas "4821 AEI" (vocales), "123 ABC", "4821KL", "4821 KLQ" y una cadena vacía.
```

### Comprovació

```bash
./mvnw test
```

### Commit, push i pull request

```bash
git add .
git commit -m "feat(validation): anotació @MatriculaEspanola i utilitat de normalització"
git push -u origin feature/12-validacio-matricula
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Validació de matrícules` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `MatriculaUtils.normalizar`
- [x] Crear l'anotació `@MatriculaEspanola` i `MatriculaValidator`
- [x] Crear `MatriculaValidatorTest`

## Com provar-ho
- `./mvnw test` en verd
- «4821 KLB», «4821-klb» i «1234BCD» són vàlides; «4821 AEI», «123 ABC» i «4821KL» no ho són

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #12
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/12-validacio-matricula`

---

## Pas 13 · Vehicles

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] CRUD de vehicles` |
| **Branca** | `feature/13-vehicles` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Que un client done d'alta, consulte, modifique i done de baixa els seus vehicles.

## Tasques
- [ ] Crear `VehiculoRequest`, `VehiculoResponse` i `VehiculoMapper`
- [ ] Crear `VehiculoService` (propietat, matrícula duplicada, baixa lògica)
- [ ] Crear `VehiculoController` (`/api/v1/vehiculos`)

## Criteris d'acceptació
- [ ] Un client crea un vehicle (201) i només veu els seus
- [ ] Una matrícula repetida torna 409 `MATRICULA_DUPLICADA`
- [ ] El vehicle d'un altre client torna 404

## Classes
- `dto.vehiculo`: `VehiculoRequest`, `VehiculoResponse`
- `mapper`: `VehiculoMapper`
- `service`: `VehiculoService`
- `controller`: `VehiculoController`

## Depèn de
#12

## Guia
Pas 13 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/13-vehicles
```

### Què es crea

- **dto.vehiculo** (2): `VehiculoRequest`, `VehiculoResponse`
- **mapper** (1): `VehiculoMapper`
- **service** (1): `VehiculoService`
- **controller** (1): `VehiculoController`

### Execució (en ordre)

#### Prompt 13.1 · Vehículos

```text
Implementa la sección 4.2 de docs/motorlab_api.md:
- dto.vehiculo.VehiculoRequest: matricula (@NotBlank y @MatriculaEspanola), marca (@NotBlank, máx. 50), modelo (@NotBlank, máx. 80), version (opcional, máx. 80), anio (@NotNull, @Min(1980)), kilometros (@NotNull, @PositiveOrZero) y usuarioId (opcional; solo lo usa un administrador).
- dto.vehiculo.VehiculoResponse(id, usuarioId, matricula, marca, modelo, version, anio, kilometros, activo) y mapper.VehiculoMapper.
- service.VehiculoService: listar (el cliente ve solo los suyos activos; el administrador, todos; devuelve PageResponse); crear (normaliza la matrícula, MatriculaDuplicadaException si existe, comprueba que anio no sea mayor que el año actual con SolicitudInvalidaException, el propietario es el usuario autenticado salvo que sea administrador y envíe usuarioId); obtener (si no es el propietario ni administrador, RecursoNoEncontradoException); actualizar (la matrícula no se puede cambiar); darDeBaja (baja lógica activo=false; ConflictoException si tiene órdenes que no estén en entregado, rechazada o cancelada).
- controller.VehiculoController (/api/v1/vehiculos) con @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')"): GET, POST (201), GET /{id}, PUT /{id}, DELETE /{id} (204). El endpoint GET /{id}/historial lo añadiremos en el prompt 16.1.
```

### Comprovació

Arranca el backend (perfil `local`) i, des de Swagger amb `laura@cliente.example`:
1. `POST /vehiculos` amb una matrícula nova → 201.
2. El mateix `POST` → **409**.
3. `GET /vehiculos` → només els seus.

### Commit, push i pull request

```bash
git add .
git commit -m "feat(vehiculos): CRUD de vehicles"
git push -u origin feature/13-vehicles
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `CRUD de vehicles` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `VehiculoRequest`, `VehiculoResponse` i `VehiculoMapper`
- [x] Crear `VehiculoService` (propietat, matrícula duplicada, baixa lògica)
- [x] Crear `VehiculoController` (`/api/v1/vehiculos`)

## Com provar-ho
- Un client crea un vehicle (201) i només veu els seus
- Una matrícula repetida torna 409 `MATRICULA_DUPLICADA`
- El vehicle d'un altre client torna 404

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #13
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/13-vehicles`

---

## Pas 14 · Seguiment i notificacions (serveis base)

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Serveis base de seguiment i notificacions` |
| **Branca** | `feature/14-serveis-seguiment-avisos` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Preparar els serveis que registren els esdeveniments d'una ordre i envien avisos, perquè els use la creació d'ordres.

## Tasques
- [ ] Crear `SeguimientoService` (registrar i llistar esdeveniments)
- [ ] Crear `NotificacionService` (notificar, notificar administradors i notificar el client)

## Criteris d'acceptació
- [ ] `./mvnw -q compile` sense errors

## Classes
- `service`: `SeguimientoService`, `NotificacionService`

## Depèn de
#13

## Guia
Pas 14 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/14-serveis-seguiment-avisos
```

### Què es crea

- **service** (2): `SeguimientoService`, `NotificacionService`

### Execució (en ordre)

#### Prompt 14.1 · Seguimiento y notificaciones (servicios base)

```text
Crea en com.motorlab.service:
1. SeguimientoService: EventoSeguimiento registrar(OrdenTrabajo orden, EstadoOrden estado, String descripcion, Usuario autor) (fechaHora = ahora) y List<EventoSeguimiento> listar(Integer ordenId).
2. NotificacionService: void notificar(Usuario destinatario, OrdenTrabajo orden, TipoNotificacion tipo, String mensaje); void notificarAdmins(OrdenTrabajo orden, TipoNotificacion tipo, String mensaje) (a todos los administradores activos); y void notificarCliente(OrdenTrabajo orden, TipoNotificacion tipo, String mensaje) (al propietario del vehículo de la orden).
Más adelante añadiremos los métodos de consulta de notificaciones.
```

### Comprovació

```bash
./mvnw -q compile
```

### Commit, push i pull request

```bash
git add .
git commit -m "feat(servicios): serveis base de seguiment i notificacions"
git push -u origin feature/14-serveis-seguiment-avisos
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Serveis base de seguiment i notificacions` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `SeguimientoService` (registrar i llistar esdeveniments)
- [x] Crear `NotificacionService` (notificar, notificar administradors i notificar el client)

## Com provar-ho
- `./mvnw -q compile` sense errors

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #14
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/14-serveis-seguiment-avisos`

---

## Pas 15 · Sol·licituds de reparació

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Creació i consulta de sol·licituds de reparació (ordres)` |
| **Branca** | `feature/15-sollicituds-reparacio` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Que un client envie una sol·licitud de reparació i que cadascú veja només les ordres que li corresponen.

## Tasques
- [ ] Crear els DTO de l'ordre i `PresupuestoResumenResponse`, `OrdenMapper` i `OrdenResumenSpecifications`
- [ ] Crear `OrdenService` (crear, llistar amb filtres per rol i obtenir) i `OrdenController`
- [ ] Crear `OrdenControllerTest`

## Criteris d'acceptació
- [ ] Una sol·licitud de **frenos** torna `urgencia: "alta"` (la fixa el trigger)
- [ ] Una sol·licitud amb síntoma de menys de 20 caràcters o data passada torna 400 amb detalls
- [ ] L'ordre d'un altre client torna 404

## Classes
- `dto.orden`: `OrdenCreateRequest`, `OrdenCreateResponse`, `OrdenResumenResponse`, `OrdenDetalleResponse`
- `dto.presupuesto`: `PresupuestoResumenResponse`
- `mapper`: `OrdenMapper`
- `repository`: `OrdenResumenSpecifications`
- `service`: `OrdenService`
- `controller`: `OrdenController`
- proves `controller`: `OrdenControllerTest`

## Depèn de
#14

## Guia
Pas 15 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/15-sollicituds-reparacio
```

### Què es crea

- **dto.orden** (4): `OrdenCreateRequest`, `OrdenCreateResponse`, `OrdenResumenResponse`, `OrdenDetalleResponse`
- **dto.presupuesto** (1): `PresupuestoResumenResponse`
- **mapper** (1): `OrdenMapper`
- **repository** (1): `OrdenResumenSpecifications`
- **service** (1): `OrdenService`
- **controller** (1): `OrdenController`
- **Proves** (`src/test/java/com/motorlab/...`): `controller.OrdenControllerTest`

### Execució (en ordre)

#### Prompt 15.1 · Solicitudes de reparación (órdenes)

```text
Implementa la sección 4.3 de docs/motorlab_api.md, excepto asignación, estado, cancelar y seguimiento (vienen en los pasos 17, 21 y 22):

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

#### Prompt 15.2 · Pruebas de órdenes

```text
Crea src/test/java/.../controller/OrdenControllerTest (@SpringBootTest, @AutoConfigureMockMvc, @ActiveProfiles("test"), @Transactional). Crea dentro de la prueba dos clientes, un mecánico y un administrador con contraseña cifrada, y comprueba:
- un cliente crea una solicitud de frenos con conduccionSegura = si y urgencia media: la respuesta (201) trae urgencia = alta;
- una solicitud con síntoma de menos de 20 caracteres o fecha pasada devuelve 400 con detalles;
- un cliente no puede crear una solicitud con el vehículo de otro cliente (404);
- el cliente B recibe 404 al pedir la orden del cliente A;
- GET /ordenes como cliente devuelve solo sus órdenes;
- un mecánico no asignado recibe 404 al pedir una orden.
```

### Comprovació

```bash
./mvnw test
```
I des de Swagger com a client: crea un vehicle i una sol·licitud de **frenos** → `urgencia: "alta"`. Amb un altre client, la mateixa ordre dona **404**.

### Commit, push i pull request

```bash
git add .
git commit -m "feat(ordenes): creació i consulta de sol·licituds de reparació"
git push -u origin feature/15-sollicituds-reparacio
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Sol·licituds de reparació` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear els DTO de l'ordre i `PresupuestoResumenResponse`, `OrdenMapper` i `OrdenResumenSpecifications`
- [x] Crear `OrdenService` (crear, llistar amb filtres per rol i obtenir) i `OrdenController`
- [x] Crear `OrdenControllerTest`

## Com provar-ho
- Una sol·licitud de **frenos** torna `urgencia: "alta"` (la fixa el trigger)
- Una sol·licitud amb síntoma de menys de 20 caràcters o data passada torna 400 amb detalls
- L'ordre d'un altre client torna 404

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #15
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/15-sollicituds-reparacio`

---

## Pas 16 · Historial del vehicle

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Historial d'ordres d'un vehicle` |
| **Branca** | `feature/16-historial-vehicle` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Consultar les ordres anteriors d'un vehicle.

## Tasques
- [ ] Afegir `GET /api/v1/vehiculos/{id}/historial` a `VehiculoController`

## Criteris d'acceptació
- [ ] El propietari i l'administrador veuen l'historial; un altre usuari rep 404

## Depèn de
#15

## Guia
Pas 16 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/16-historial-vehicle
```

### Execució (en ordre)

#### Prompt 16.1 · Historial del vehículo

```text
Añade GET /api/v1/vehiculos/{id}/historial en VehiculoController: devuelve PageResponse<OrdenResumenResponse> con las órdenes del vehículo (OrdenResumenSpecifications.delVehiculo), solo si el usuario es el propietario o administrador (si no, 404). Añade el método necesario en VehiculoService u OrdenService.
```

### Comprovació

```bash
./mvnw -q compile
```
Des de Swagger: `GET /vehiculos/{id}/historial` amb el propietari (200) i amb un altre client (404).

### Commit, push i pull request

```bash
git add .
git commit -m "feat(vehiculos): historial d'ordres d'un vehicle"
git push -u origin feature/16-historial-vehicle
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Historial del vehicle` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Afegir `GET /api/v1/vehiculos/{id}/historial` a `VehiculoController`

## Com provar-ho
- El propietari i l'administrador veuen l'historial; un altre usuari rep 404

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #16
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/16-historial-vehicle`

---

## Pas 17 · Màquina d'estats i assignació

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Màquina d'estats de les ordres i assignació de mecànic` |
| **Branca** | `feature/17-maquina-estats` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Implementar les transicions d'estat permeses i l'assignació de mecànic i elevador.

## Tasques
- [ ] Crear `TransicionesEstado` i `OrdenEstadoService`
- [ ] Crear `AsignacionRequest` i l'endpoint `PATCH /ordenes/{id}/asignacion`
- [ ] Crear `TransicionesEstadoTest`

## Criteris d'acceptació
- [ ] `./mvnw test` en verd
- [ ] Es comproven totes les transicions vàlides i les no vàlides (`listo` → `arreglando`, `recibido` → `listo`, etc.)
- [ ] Només un administrador pot assignar mecànic, i el mecànic ha d'existir i tindre rol `mecanico`

## Classes
- `dto.orden`: `AsignacionRequest`
- `service`: `TransicionesEstado`, `OrdenEstadoService`
- proves `service`: `TransicionesEstadoTest`

## Depèn de
#16

## Guia
Pas 17 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/17-maquina-estats
```

### Què es crea

- **dto.orden** (1): `AsignacionRequest`
- **service** (2): `TransicionesEstado`, `OrdenEstadoService`
- **Proves** (`src/test/java/com/motorlab/...`): `service.TransicionesEstadoTest`

### Execució (en ordre)

#### Prompt 17.1 · Transiciones de estado y asignación

```text
Implementa la máquina de estados de docs/motorlab_api.md (sección 2):

1. service.TransicionesEstado (@Component): contiene un Map<EstadoOrden, Set<EstadoOrden>> con las transiciones permitidas: solicitud_recibida -> {pendiente_presupuesto, cancelada}; pendiente_presupuesto -> {presupuesto_enviado, cancelada}; presupuesto_enviado -> {presupuesto_autorizado, rechazada, cancelada}; presupuesto_autorizado -> {recibido, cancelada}; recibido -> {en_preparacion}; en_preparacion -> {esperando_pieza, arreglando}; esperando_pieza -> {arreglando}; arreglando -> {esperando_pieza, listo}; listo -> {entregado}; entregado, rechazada y cancelada -> {}. Métodos: boolean permitida(desde, hasta) y Set<EstadoOrden> siguientes(desde). Define también dos constantes: ESTADOS_MECANICO = {en_preparacion, esperando_pieza, arreglando, listo} y ESTADOS_AUTOMATICOS = {presupuesto_enviado, presupuesto_autorizado, rechazada, cancelada} (estos solo se alcanzan desde los endpoints de presupuesto y cancelación).
2. service.OrdenEstadoService con el método OrdenTrabajo aplicarCambio(OrdenTrabajo orden, EstadoOrden nuevo, String descripcion, Usuario autor): comprueba la transición (si no es válida, TransicionNoPermitidaException con el mensaje "No se puede pasar de X a Y"); cambia el estado; si el nuevo estado es recibido y fechaEntrada es null, la rellena con la fecha y hora actual; guarda; registra el evento con SeguimientoService; y notifica al cliente (cambio_estado con un mensaje en castellano; vehiculo_listo cuando el estado es listo).
3. dto.orden.AsignacionRequest(mecanicoId @NotNull, elevador máx. 20 caracteres, fechaEstimada). En OrdenService añade asignar(id, AsignacionRequest): solo administrador; el mecánico debe existir, estar activo y tener rol mecanico (si no, SolicitudInvalidaException). Endpoint PATCH /api/v1/ordenes/{id}/asignacion con @PreAuthorize("hasRole('ADMIN')") que devuelve OrdenDetalleResponse.
4. Prueba service.TransicionesEstadoTest (unitaria): comprueba una por una todas las transiciones válidas y estas no válidas: listo -> arreglando, recibido -> listo, entregado -> cualquier estado, cancelada -> cualquier estado.
```

### Comprovació

```bash
./mvnw test
```

### Commit, push i pull request

```bash
git add .
git commit -m "feat(ordenes): màquina d'estats i assignació de mecànic"
git push -u origin feature/17-maquina-estats
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Màquina d'estats i assignació` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `TransicionesEstado` i `OrdenEstadoService`
- [x] Crear `AsignacionRequest` i l'endpoint `PATCH /ordenes/{id}/asignacion`
- [x] Crear `TransicionesEstadoTest`

## Com provar-ho
- `./mvnw test` en verd
- Es comproven totes les transicions vàlides i les no vàlides (`listo` → `arreglando`, `recibido` → `listo`, etc.)
- Només un administrador pot assignar mecànic, i el mecànic ha d'existir i tindre rol `mecanico`

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #17
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/17-maquina-estats`

---

## Pas 18 · Peces i stock

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Catàleg de peces i control d'stock` |
| **Branca** | `feature/18-peces-stock` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Gestionar les peces del catàleg i el seu stock, amb bloquejos per a evitar errors de concurrència.

## Tasques
- [ ] Crear `PiezaRequest`, `PiezaResponse`, `StockDeltaRequest` i `PiezaMapper`
- [ ] Crear `PiezaService` (CRUD, ajust de stock i descompte per pressupost) i `PiezaController`
- [ ] Crear `PiezaServiceTest`

## Criteris d'acceptació
- [ ] `./mvnw test` en verd
- [ ] Un ajust que deixaria l'stock per davall de 0 torna 409 `STOCK_INSUFICIENTE`
- [ ] Descomptar l'stock del pressupost d'exemple falla (DS-118 té 1 i en necessita 2) i no descompta cap altra peça

## Classes
- `dto.pieza`: `PiezaRequest`, `PiezaResponse`, `StockDeltaRequest`
- `mapper`: `PiezaMapper`
- `service`: `PiezaService`
- `controller`: `PiezaController`
- proves `service`: `PiezaServiceTest`

## Depèn de
#17

## Guia
Pas 18 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/18-peces-stock
```

### Què es crea

- **dto.pieza** (3): `PiezaRequest`, `PiezaResponse`, `StockDeltaRequest`
- **mapper** (1): `PiezaMapper`
- **service** (1): `PiezaService`
- **controller** (1): `PiezaController`
- **Proves** (`src/test/java/com/motorlab/...`): `service.PiezaServiceTest`

### Execució (en ordre)

#### Prompt 18.1 · Catálogo de piezas y stock

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

### Comprovació

```bash
./mvnw test
```

### Commit, push i pull request

```bash
git add .
git commit -m "feat(piezas): catàleg de peces i control d'stock"
git push -u origin feature/18-peces-stock
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Catàleg de peces i stock` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `PiezaRequest`, `PiezaResponse`, `StockDeltaRequest` i `PiezaMapper`
- [x] Crear `PiezaService` (CRUD, ajust de stock i descompte per pressupost) i `PiezaController`
- [x] Crear `PiezaServiceTest`

## Com provar-ho
- `./mvnw test` en verd
- Un ajust que deixaria l'stock per davall de 0 torna 409 `STOCK_INSUFICIENTE`
- Descomptar l'stock del pressupost d'exemple falla (DS-118 té 1 i en necessita 2) i no descompta cap altra peça

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #18
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/18-peces-stock`

---

## Pas 19 · Pressupostos: línies i totals

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Pressupostos: creació, consulta i edició de línies` |
| **Branca** | `feature/19-pressupostos-linies` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Crear un pressupost per a una ordre i gestionar-ne les línies, amb els totals calculats per la base de dades.

## Tasques
- [ ] Crear els DTO del pressupost (8 records) i `PresupuestoMapper`
- [ ] Crear `PresupuestoService` (crear, consultar, editar, afegir, modificar i eliminar línies)
- [ ] Crear `PresupuestoController` (`/ordenes/{id}/presupuesto` i `/presupuestos/{id}/...`)

## Criteris d'acceptació
- [ ] Afegir línies canvia subtotal, IVA i total (després d'un `refresh`)
- [ ] Es mostra un avís d'stock quan una peça no en té prou
- [ ] Editar un pressupost que no està en esborrany torna 409 `PRESUPUESTO_NO_EDITABLE`

## Classes
- `dto.presupuesto`: `PresupuestoUpdateRequest`, `LineaRequest`, `LineaResponse`, `TotalesResponse`, `AvisoStockResponse`, `LineaCreadaResponse`, `PresupuestoResponse`, `RechazoRequest`
- `mapper`: `PresupuestoMapper`
- `service`: `PresupuestoService`
- `controller`: `PresupuestoController`

## Depèn de
#18

## Guia
Pas 19 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/19-pressupostos-linies
```

### Què es crea

- **dto.presupuesto** (8): `PresupuestoUpdateRequest`, `LineaRequest`, `LineaResponse`, `TotalesResponse`, `AvisoStockResponse`, `LineaCreadaResponse`, `PresupuestoResponse`, `RechazoRequest`
- **mapper** (1): `PresupuestoMapper`
- **service** (1): `PresupuestoService`
- **controller** (1): `PresupuestoController`

### Execució (en ordre)

#### Prompt 19.1 · Presupuestos: crear, consultar y editar líneas

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

### Comprovació

Arranca el backend (perfil `local`) i, des de Swagger com a administrador:
1. Crea una ordre nova (com a client) i `POST /ordenes/{id}/presupuesto`.
2. Afig les 4 línies de la demostració i comprova **212.00 / 44.52 / 256.52** i l'avís de stock de DS-118.

### Commit, push i pull request

```bash
git add .
git commit -m "feat(presupuestos): creació de pressupostos i línies amb totals automàtics"
git push -u origin feature/19-pressupostos-linies
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Pressupostos: línies i totals` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear els DTO del pressupost (8 records) i `PresupuestoMapper`
- [x] Crear `PresupuestoService` (crear, consultar, editar, afegir, modificar i eliminar línies)
- [x] Crear `PresupuestoController` (`/ordenes/{id}/presupuesto` i `/presupuestos/{id}/...`)

## Com provar-ho
- Afegir línies canvia subtotal, IVA i total (després d'un `refresh`)
- Es mostra un avís d'stock quan una peça no en té prou
- Editar un pressupost que no està en esborrany torna 409 `PRESUPUESTO_NO_EDITABLE`

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #19
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/19-pressupostos-linies`

---

## Pas 20 · Pressupostos: enviar, autoritzar i rebutjar

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Pressupostos: enviar, autoritzar i rebutjar` |
| **Branca** | `feature/20-pressupostos-cicle` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Completar el cicle del pressupost amb les tres transicions, cadascuna dins d'una transacció, i provar-lo.

## Tasques
- [ ] Afegir `enviar`, `autorizar` i `rechazar` a `PresupuestoService` i `PresupuestoController`
- [ ] Bloquejar la fila amb `PESSIMISTIC_WRITE` en autoritzar
- [ ] Crear `PresupuestoServiceTest`

## Criteris d'acceptació
- [ ] `./mvnw test` en verd
- [ ] Enviar un pressupost sense línies torna 400; autoritzar dos cops torna 409
- [ ] Un altre client rep 404 en intentar autoritzar el pressupost d'un altre

## Classes
- proves `service`: `PresupuestoServiceTest`

## Depèn de
#19

## Guia
Pas 20 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/20-pressupostos-cicle
```

### Què es crea

- **Proves** (`src/test/java/com/motorlab/...`): `service.PresupuestoServiceTest`

### Execució (en ordre)

#### Prompt 20.1 · Presupuestos: enviar, autorizar y rechazar

```text
Añade a PresupuestoService y PresupuestoController (sección 4.4 de docs/motorlab_api.md). Cada método es @Transactional:

- enviar(id) [admin]: el presupuesto debe estar en borrador (si no, PresupuestoNoEditableException) y tener al menos una línea (si no, SolicitudInvalidaException "El presupuesto no tiene líneas"). Lo pasa a enviado (enviadoEn = ahora), pasa la orden a presupuesto_enviado con OrdenEstadoService ("Presupuesto enviado al cliente") y notifica al cliente (presupuesto_listo, con el total formateado en castellano, por ejemplo "Tu presupuesto está listo · 256,52 €").
- autorizar(id) [cliente propietario, si no 404]: bloquea la fila con findByIdForUpdate; solo si está enviado (si no, TransicionNoPermitidaException "El presupuesto no está pendiente de respuesta"). Lo pasa a autorizado (respondidoEn = ahora), la orden a presupuesto_autorizado ("Presupuesto autorizado por el cliente") y notifica a los administradores (presupuesto_respondido).
- rechazar(id, RechazoRequest) [cliente propietario]: igual que autorizar, pero el presupuesto pasa a rechazado con motivoRechazo y la orden a rechazada.

Rutas: POST /api/v1/presupuestos/{id}/enviar (admin), /autorizar y /rechazar (cliente). Todas devuelven PresupuestoResponse.
```

#### Prompt 20.2 · Pruebas del presupuesto

```text
Crea service.PresupuestoServiceTest (extiende IntegracionBase) con este caso, que reproduce la demostración:
1. Crea una orden nueva (frenos) para el cliente de ejemplo y, como administrador, crea el presupuesto.
2. Añade las líneas: pastillas BR-2231 (1), discos DS-118 (2), líquido LF-040 (1) y mano de obra 1,5 h a 28 EUR/h. Comprueba subtotal 212.00, IVA 44.52 y total 256.52 (compareTo), y que hay aviso de stock para DS-118.
3. Elimina la línea de mano de obra y comprueba que los totales cambian.
4. Intenta enviar un presupuesto sin líneas (SolicitudInvalidaException) y editar uno ya enviado (PresupuestoNoEditableException).
5. El cliente autoriza; un segundo intento da TransicionNoPermitidaException; otro cliente recibe RecursoNoEncontradoException.
```

### Comprovació

```bash
./mvnw test
```
Des de Swagger: l'administrador envia el pressupost, el client l'autoritza i l'ordre passa a `presupuesto_autorizado`. (Els endpoints per a consultar les notificacions arriben al pas 23.)

### Commit, push i pull request

```bash
git add .
git commit -m "feat(presupuestos): enviar, autoritzar i rebutjar pressupostos"
git push -u origin feature/20-pressupostos-cicle
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Pressupostos: enviar, autoritzar i rebutjar` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Afegir `enviar`, `autorizar` i `rechazar` a `PresupuestoService` i `PresupuestoController`
- [x] Bloquejar la fila amb `PESSIMISTIC_WRITE` en autoritzar
- [x] Crear `PresupuestoServiceTest`

## Com provar-ho
- `./mvnw test` en verd
- Enviar un pressupost sense línies torna 400; autoritzar dos cops torna 409
- Un altre client rep 404 en intentar autoritzar el pressupost d'un altre

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #20
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/20-pressupostos-cicle`

---

## Pas 21 · Canvi d'estat, cancel·lació i stock

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Canvi d'estat manual, cancel·lació i descompte d'stock` |
| **Branca** | `feature/21-canvi-estat-stock` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Fer avançar la reparació amb permisos per rol, permetre cancel·lar ordres i descomptar l'stock en passar a «arreglando».

## Tasques
- [ ] Crear `CambioEstadoRequest` i afegir `cambiarEstado` i `cancelar` a `OrdenService`
- [ ] Descomptar l'stock en passar a `arreglando` per primera vegada (i revertir si falta stock)
- [ ] Afegir `PATCH /ordenes/{id}/estado` i `POST /ordenes/{id}/cancelar`

## Criteris d'acceptació
- [ ] Un mecànic només pot usar `en_preparacion`, `esperando_pieza`, `arreglando` i `listo` en ordres assignades a ell
- [ ] Passar a `arreglando` sense stock torna 409 `STOCK_INSUFICIENTE` i l'estat no canvia
- [ ] El client pot cancelar fins a `presupuesto_enviado`; l'administrador també en `presupuesto_autorizado`

## Classes
- `dto.orden`: `CambioEstadoRequest`

## Depèn de
#20

## Guia
Pas 21 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/21-canvi-estat-stock
```

### Què es crea

- **dto.orden** (1): `CambioEstadoRequest`

### Execució (en ordre)

#### Prompt 21.1 · Cambio de estado manual y cancelación

```text
Implementa en OrdenService y OrdenController (sección 4.3 de docs/motorlab_api.md):

- dto.orden.CambioEstadoRequest(estado @NotNull, descripcion máx. 200).
- cambiarEstado(id, CambioEstadoRequest): carga la orden con obtenerEntidadAutorizada. El administrador puede aplicar cualquier transición válida salvo las de ESTADOS_AUTOMATICOS (estas se hacen desde presupuesto y cancelación). El mecánico solo puede usar ESTADOS_MECANICO y solo en órdenes asignadas a él (si no, SinPermisoException). Delega en OrdenEstadoService.aplicarCambio.
- Modifica OrdenEstadoService para que, al pasar a arreglando por primera vez (no existe un evento previo con estado arreglando: EventoSeguimientoRepository.existsByOrdenIdAndEstado), llame a PiezaService.descontarStockDePresupuesto del presupuesto de la orden, dentro de la misma transacción. Si falta stock, la excepción StockInsuficienteException revierte todo (el estado no cambia).
- cancelar(id): el cliente propietario puede cancelar si la orden está en solicitud_recibida, pendiente_presupuesto o presupuesto_enviado; el administrador también en presupuesto_autorizado (si no, SinPermisoException o TransicionNoPermitidaException). Si hay presupuesto en borrador o enviado, lo marca como rechazado con motivo "Orden cancelada". Aplica el estado cancelada con OrdenEstadoService.
- Rutas: PATCH /api/v1/ordenes/{id}/estado (@PreAuthorize ADMIN o MECANICO) y POST /api/v1/ordenes/{id}/cancelar (CLIENTE o ADMIN).
```

### Comprovació

```bash
./mvnw test
```
Si algun test anterior es trenca per este canvi, usa el Prompt de correcció.

### Commit, push i pull request

```bash
git add .
git commit -m "feat(ordenes): canvi d'estat manual, cancel·lació i descompte d'stock"
git push -u origin feature/21-canvi-estat-stock
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Canvi d'estat, cancel·lació i stock` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `CambioEstadoRequest` i afegir `cambiarEstado` i `cancelar` a `OrdenService`
- [x] Descomptar l'stock en passar a `arreglando` per primera vegada (i revertir si falta stock)
- [x] Afegir `PATCH /ordenes/{id}/estado` i `POST /ordenes/{id}/cancelar`

## Com provar-ho
- Un mecànic només pot usar `en_preparacion`, `esperando_pieza`, `arreglando` i `listo` en ordres assignades a ell
- Passar a `arreglando` sense stock torna 409 `STOCK_INSUFICIENTE` i l'estat no canvia
- El client pot cancelar fins a `presupuesto_enviado`; l'administrador també en `presupuesto_autorizado`

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #21
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/21-canvi-estat-stock`

---

## Pas 22 · Seguiment per al client

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Seguiment de la reparació per al client` |
| **Branca** | `feature/22-seguiment-client` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Mostrar al client la línia de temps de la reparació, el progrés i l'hora estimada.

## Tasques
- [ ] Crear `EventoResponse` i `SeguimientoResponse`
- [ ] Afegir `seguimiento(id)` a `OrdenService` i `GET /ordenes/{id}/seguimiento`

## Criteris d'acceptació
- [ ] `GET /ordenes/{id}/seguimiento` torna els esdeveniments en ordre, el progrés i `listo_estimado`
- [ ] Només el client propietari, el mecànic assignat i l'administrador hi tenen accés

## Classes
- `dto.orden`: `EventoResponse`, `SeguimientoResponse`

## Depèn de
#21

## Guia
Pas 22 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/22-seguiment-client
```

### Què es crea

- **dto.orden** (2): `EventoResponse`, `SeguimientoResponse`

### Execució (en ordre)

#### Prompt 22.1 · Seguimiento para el cliente

```text
Implementa GET /api/v1/ordenes/{id}/seguimiento (sección 4.3 de docs/motorlab_api.md):
- dto.orden.EventoResponse(estado, descripcion, fechaHora) y dto.orden.SeguimientoResponse(ordenId, vehiculo, estado, progreso, listoEstimado, eventos).
- vehiculo es un texto "Marca Modelo Versión · MATRÍCULA" (por ejemplo "Seat León 1.5 TSI · 4821KLB"). El progreso sale de EstadoOrden.progreso().
- OrdenService.seguimiento(id) usa obtenerEntidadAutorizada y SeguimientoService.listar; añade la conversión a OrdenMapper. Acceso: cliente propietario, mecánico asignado y administrador.
```

### Comprovació

```bash
./mvnw -q compile
```
Des de Swagger, amb l'ordre 2481 d'exemple: `GET /ordenes/2481/seguimiento` torna 4 esdeveniments i `progreso: 80`.

### Commit, push i pull request

```bash
git add .
git commit -m "feat(ordenes): endpoint de seguiment de la reparació"
git push -u origin feature/22-seguiment-client
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Seguiment per al client` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `EventoResponse` i `SeguimientoResponse`
- [x] Afegir `seguimiento(id)` a `OrdenService` i `GET /ordenes/{id}/seguimiento`

## Com provar-ho
- `GET /ordenes/{id}/seguimiento` torna els esdeveniments en ordre, el progrés i `listo_estimado`
- Només el client propietari, el mecànic assignat i l'administrador hi tenen accés

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #22
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/22-seguiment-client`

---

## Pas 23 · Notificacions

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Endpoints de notificacions` |
| **Branca** | `feature/23-notificacions` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Que cada usuari consulte i marque com a llegides les seues notificacions.

## Tasques
- [ ] Crear `NotificacionResponse` i `NotificacionMapper`
- [ ] Afegir a `NotificacionService` el llistat, marcar llegida, marcar totes i el comptador
- [ ] Crear `NotificacionController`

## Criteris d'acceptació
- [ ] `GET /notificaciones` llista les de l'usuari, de la més nova a la més antiga
- [ ] `GET /notificaciones/contador` torna `{"no_leidas": n}`
- [ ] Marcar com a llegida una notificació d'un altre usuari torna 404

## Classes
- `dto.notificacion`: `NotificacionResponse`
- `mapper`: `NotificacionMapper`
- `controller`: `NotificacionController`

## Depèn de
#22

## Guia
Pas 23 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/23-notificacions
```

### Què es crea

- **dto.notificacion** (1): `NotificacionResponse`
- **mapper** (1): `NotificacionMapper`
- **controller** (1): `NotificacionController`

### Execució (en ordre)

#### Prompt 23.1 · Notificaciones

```text
Implementa la sección 4.6 de docs/motorlab_api.md:
- dto.notificacion.NotificacionResponse(id, tipo, mensaje, ordenId, leida, creadaEn) y mapper.NotificacionMapper.
- En NotificacionService añade: listar(leida opcional, pagina, limite) del usuario autenticado (PageResponse, de más nueva a más antigua); marcarLeida(id) (si no es suya, RecursoNoEncontradoException); marcarTodas() que devuelva cuántas ha marcado; y contarNoLeidas().
- controller.NotificacionController (/api/v1/notificaciones, cualquier usuario autenticado): GET ?leida&pagina&limite, PATCH /{id}/leida, POST /marcar-todas y GET /contador (devuelve {"no_leidas": n}).
```

### Comprovació

```bash
./mvnw -q compile
```
Des de Swagger: crea una sol·licitud com a client i, com a administrador, `GET /notificaciones` mostra l'avís `solicitud_nueva`.

### Commit, push i pull request

```bash
git add .
git commit -m "feat(notificaciones): llistat, lectura i comptador de notificacions"
git push -u origin feature/23-notificacions
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Notificacions` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `NotificacionResponse` i `NotificacionMapper`
- [x] Afegir a `NotificacionService` el llistat, marcar llegida, marcar totes i el comptador
- [x] Crear `NotificacionController`

## Com provar-ho
- `GET /notificaciones` llista les de l'usuari, de la més nova a la més antiga
- `GET /notificaciones/contador` torna `{"no_leidas": n}`
- Marcar com a llegida una notificació d'un altre usuari torna 404

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #23
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/23-notificacions`

---

## Pas 24 · Panell de mètriques

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Panell de mètriques per a l'administrador` |
| **Branca** | `feature/24-panell-metriques` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Oferir les mètriques bàsiques del taller.

## Tasques
- [ ] Crear `MetricasResponse` i `PanelService`
- [ ] Crear `PanelController` (`GET /api/v1/panel/metricas`, només administrador)

## Criteris d'acceptació
- [ ] El resultat inclou ordres actives, ordres per estat, ingressos del mes, pressupostos sense resposta i peces amb stock baix
- [ ] Un client rep 403

## Classes
- `dto.panel`: `MetricasResponse`
- `service`: `PanelService`
- `controller`: `PanelController`

## Depèn de
#23

## Guia
Pas 24 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/24-panell-metriques
```

### Què es crea

- **dto.panel** (1): `MetricasResponse`
- **service** (1): `PanelService`
- **controller** (1): `PanelController`

### Execució (en ordre)

#### Prompt 24.1 · Panel de métricas

```text
Implementa GET /api/v1/panel/metricas (solo administrador):
- dto.panel.MetricasResponse(ordenesActivas, ordenesPorEstado (Map<EstadoOrden, Long>), ingresosMes (BigDecimal), presupuestosSinRespuesta, piezasStockBajo).
- service.PanelService: ordenesActivas = órdenes cuyo estado no es entregado, rechazada ni cancelada; ordenesPorEstado con contarPorEstado(); ingresosMes = sumarAutorizadosEntre(primer día del mes actual a las 00:00, primer día del mes siguiente); presupuestosSinRespuesta = presupuestos en estado enviado; piezasStockBajo con countByActivaTrueAndStockLessThanEqual(umbral).
- controller.PanelController con @PreAuthorize("hasRole('ADMIN')").
```

### Comprovació

```bash
./mvnw -q compile
```
Des de Swagger com a administrador: `GET /panel/metricas`.

### Commit, push i pull request

```bash
git add .
git commit -m "feat(panel): mètriques per a l'administrador"
git push -u origin feature/24-panell-metriques
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Panell de mètriques` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `MetricasResponse` i `PanelService`
- [x] Crear `PanelController` (`GET /api/v1/panel/metricas`, només administrador)

## Com provar-ho
- El resultat inclou ordres actives, ordres per estat, ingressos del mes, pressupostos sense resposta i peces amb stock baix
- Un client rep 403

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #24
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/24-panell-metriques`

---

## Pas 25 · Prova del flux complet

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Prova d'integració del flux complet de la demostració` |
| **Branca** | `feature/25-flux-complet` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Comprovar de principi a fi el cas del Seat León, amb estats, stock, notificacions i permisos.

## Tasques
- [ ] Crear `FlujoCompletoTest` que reproduïsca els 9 passos de la demostració
- [ ] Executar la demostració completa des de Swagger

## Criteris d'acceptació
- [ ] `./mvnw test` en verd
- [ ] La demostració de Swagger funciona amb els tres rols

## Classes
- proves `service`: `FlujoCompletoTest`

## Depèn de
#24

## Guia
Pas 25 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/25-flux-complet
```

### Què es crea

- **Proves** (`src/test/java/com/motorlab/...`): `service.FlujoCompletoTest`

### Execució (en ordre)

#### Prompt 25.1 · Prueba del flujo completo

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

### Comprovació

```bash
./mvnw test
```

**Demostració completa des de Swagger**

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

### Commit, push i pull request

```bash
git add .
git commit -m "test(backend): prova del flux complet de la demostració"
git push -u origin feature/25-flux-complet
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Prova del flux complet` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `FlujoCompletoTest` que reproduïsca els 9 passos de la demostració
- [x] Executar la demostració completa des de Swagger

## Com provar-ho
- `./mvnw test` en verd
- La demostració de Swagger funciona amb els tres rols

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #25
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/25-flux-complet`

---

## Pas 26 · Documentació Swagger

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Documentar els endpoints amb Swagger` |
| **Branca** | `feature/26-documentacio-swagger` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Que Swagger UI documente tots els endpoints amb resums i codis de resposta.

## Tasques
- [ ] Afegir `@Tag`, `@Operation` i `@ApiResponse` a tots els controladors
- [ ] Comprovar que Swagger mostra tots els endpoints de `docs/motorlab_api.md`

## Criteris d'acceptació
- [ ] Swagger UI mostra tots els endpoints amb resum en castellà i els codis 401, 403, 404 i 409

## Depèn de
#25

## Guia
Pas 26 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/26-documentacio-swagger
```

### Execució (en ordre)

#### Prompt 26.1 · Documentación Swagger

```text
Revisa todos los controladores y añade @Tag (nombre del recurso en castellano), @Operation(summary = "...") en cada método y @ApiResponse para los códigos 401, 403, 404 y 409 que correspondan, sin cambiar la lógica. Comprueba que Swagger UI muestra todos los endpoints de docs/motorlab_api.md y dime cuáles faltan o difieren.
```

### Comprovació

```bash
./mvnw spring-boot:run
```
Obri `http://localhost:8080/swagger-ui.html` i repassa que no falte cap endpoint.

### Commit, push i pull request

```bash
git add .
git commit -m "docs(backend): documentació Swagger dels endpoints"
git push -u origin feature/26-documentacio-swagger
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Documentació Swagger` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Afegir `@Tag`, `@Operation` i `@ApiResponse` a tots els controladors
- [x] Comprovar que Swagger mostra tots els endpoints de `docs/motorlab_api.md`

## Com provar-ho
- Swagger UI mostra tots els endpoints amb resum en castellà i els codis 401, 403, 404 i 409

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #26
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/26-documentacio-swagger`

---

## Pas 27 · README del projecte

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] README del projecte` |
| **Branca** | `feature/27-readme-projecte` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Documentar com instal·lar, configurar i executar el projecte.

## Tasques
- [ ] Actualitzar el `README.md` de l'arrel amb requisits, base de dades, variables d'entorn, execució, proves, Swagger i estructura de paquets

## Criteris d'acceptació
- [ ] Algú que clone el repositori pot arrancar el backend seguint només el README

## Depèn de
#26

## Guia
Pas 27 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/27-readme-projecte
```

### Execució (en ordre)

#### Prompt 27.1 · README del proyecto

```text
Actualiza el README.md de la raíz del repositorio (si ya existe, amplíalo sin borrar lo que haya) en castellano con: requisitos (JDK 21, PostgreSQL 17 o superior), cómo crear el usuario motorlab_usr y las bases de datos motorlabdb y motorlab_testdb con psql desde Git Bash y cargar docs/motorlab_schema.sql, variables de entorno (DB_USER, DB_PASS, JWT_SECRET, SPRING_PROFILES_ACTIVE) y cómo definirlas con export en Git Bash y en .vscode/launch.json, cómo arrancar (./mvnw spring-boot:run), cómo ejecutar los tests (contra motorlab_testdb), la URL de Swagger UI, los usuarios de ejemplo y la estructura de paquetes con una línea por paquete.
```

### Comprovació

Segueix el README tal com està escrit en una carpeta nova (o demana-ho a un company) i comprova que arranca.

### Commit, push i pull request

```bash
git add .
git commit -m "docs: README del projecte"
git push -u origin feature/27-readme-projecte
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `README del projecte` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Actualitzar el `README.md` de l'arrel amb requisits, base de dades, variables d'entorn, execució, proves, Swagger i estructura de paquets

## Com provar-ho
- Algú que clone el repositori pot arrancar el backend seguint només el README

## Checklist
- [ ] El README s'ha provat en una carpeta nova
- [ ] No es puja cap contrasenya real, secret ni `.env`

Closes #27
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/27-readme-projecte`

---

## Pas 28 · Revisió de seguretat

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] Revisió de seguretat i correccions` |
| **Branca** | `feature/28-revisio-seguretat` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Revisar tot el backend amb la llista de seguretat i corregir el que incomplisca.

## Tasques
- [ ] Executar la revisió de seguretat amb l'assistent
- [ ] Aplicar les correccions
- [ ] Executar `./mvnw clean verify`

## Criteris d'acceptació
- [ ] Cap resposta inclou `password_hash` ni una entitat JPA
- [ ] Tots els endpoints tenen `@PreAuthorize` o són públics a `SecurityConfig`
- [ ] No hi ha secrets ni contrasenyes al codi, als tests ni als fitxers pujats a Git
- [ ] `./mvnw clean verify` en verd

## Depèn de
#27

## Guia
Pas 28 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/28-revisio-seguretat
```

### Execució (en ordre)

#### Prompt 28.1 · Revisión de seguridad

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

Després de la revisió, **aplica les correccions** que et propose l'assistent (un prompt cada vegada) i torna a compilar.

### Comprovació

```bash
export DB_USER=motorlab_usr DB_PASS=motorlab_usr
./mvnw clean verify
git grep -n -i "password\|secret" -- ':!*.md' ':!*.sql'
```
Revisa que no apareguen contrasenyes reals ni secrets escrits al codi.

### Commit, push i pull request

```bash
git add .
git commit -m "chore(security): revisió de seguretat i correccions"
git push -u origin feature/28-revisio-seguretat
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Revisió de seguretat` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Executar la revisió de seguretat amb l'assistent
- [x] Aplicar les correccions
- [x] Executar `./mvnw clean verify`

## Com provar-ho
- Cap resposta inclou `password_hash` ni una entitat JPA
- Tots els endpoints tenen `@PreAuthorize` o són públics a `SecurityConfig`
- No hi ha secrets ni contrasenyes al codi, als tests ni als fitxers pujats a Git
- `./mvnw clean verify` en verd

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #28
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/28-revisio-seguretat`

---

## Pas 29 · (Opcional) Límit d'intents de login

### GitHub: issue i branca

| Camp | Valor |
|---|---|
| **Títol de la issue** | `[Backend] (Opcional) Límit d'intents al login` |
| **Branca** | `feature/29-limit-intents-login` |

**Descripció de la issue** (copia-la al camp *Description*):

```markdown
## Objectiu
Limitar a 5 els intents de login per minut i per IP.

## Tasques
- [ ] Crear `LimiteIntentosLoginFilter` i registrar-lo a `SecurityConfig`
- [ ] Afegir una prova que comprove el 429 al sisé intent

## Criteris d'acceptació
- [ ] El sisé intent de login en un minut torna 429 amb codi `DEMASIADOS_INTENTOS`

## Classes
- `security`: `LimiteIntentosLoginFilter`

## Depèn de
#28

## Guia
Pas 29 de `docs/guia_github_backend_motorlab.md`.
```

```bash
git checkout main
git pull
git checkout -b feature/29-limit-intents-login
```

### Què es crea

- **security** (1): `LimiteIntentosLoginFilter`

### Execució (en ordre)

#### Prompt 29.1 · (Opcional) Límite de intentos de login

```text
Crea security.LimiteIntentosLoginFilter (OncePerRequestFilter) que limite POST /api/v1/auth/login a 5 intentos por minuto y por IP, con un contador en memoria (ConcurrentHashMap con ventana de tiempo), y responda 429 con un ErrorResponse (código DEMASIADOS_INTENTOS). Regístralo en SecurityConfig antes del filtro JWT y añade una prueba que compruebe el 429 al sexto intento.
```

### Comprovació

```bash
./mvnw test
```

### Commit, push i pull request

```bash
git add .
git commit -m "feat(security): límit d'intents al login"
git push -u origin feature/29-limit-intents-login
```

| Camp | Valor |
|---|---|
| **Títol de la PR** | `Límit d'intents al login (opcional)` |
| **Branca base** | `main` |

**Descripció de la PR** (enganxa-la en crear-la; l'última línia tanca la issue):

```markdown
## Què inclou
- [x] Crear `LimiteIntentosLoginFilter` i registrar-lo a `SecurityConfig`
- [x] Afegir una prova que comprove el 429 al sisé intent

## Com provar-ho
- El sisé intent de login en un minut torna 429 amb codi `DEMASIADOS_INTENTOS`

## Checklist
- [ ] `./mvnw -q compile` sense errors
- [ ] Les proves passen (`./mvnw test`), si el pas en té
- [ ] No es puja cap contrasenya, secret ni `.env`

Closes #29
```

Després de fusionar: `git checkout main && git pull && git branch -d feature/29-limit-intents-login`

---

## Annex A · Llista de classes per paquets

Marca cada casella quan la classe estiga creada, revisada i compilant. Al costat, el pas (issue) on es genera. Ruta base: `src/main/java/com/motorlab/`.

### `com.motorlab` (1)

- [ ] `MotorLabApplication` · pas 1

### `config` (4)

- [ ] `JwtProperties` · pas 3
- [ ] `OpenApiConfig` · pas 3
- [ ] `SecurityConfig` · pas 9
- [ ] `InicializadorUsuarios` · pas 11

### `security` (7)

- [ ] `UsuarioPrincipal` · pas 8
- [ ] `UsuarioDetailsService` · pas 8
- [ ] `SecurityUtils` · pas 8
- [ ] `JwtService` · pas 8
- [ ] `JwtAuthenticationFilter` · pas 8
- [ ] `RestAuthenticationEntryPoint` · pas 9
- [ ] `RestAccessDeniedHandler` · pas 9
- [ ] `LimiteIntentosLoginFilter` · pas 29 · opcional

### `exception` (12)

- [ ] `ApiException` · pas 4
- [ ] `RecursoNoEncontradoException` · pas 4
- [ ] `SinPermisoException` · pas 4
- [ ] `EmailDuplicadoException` · pas 4
- [ ] `MatriculaDuplicadaException` · pas 4
- [ ] `TransicionNoPermitidaException` · pas 4
- [ ] `StockInsuficienteException` · pas 4
- [ ] `PresupuestoNoEditableException` · pas 4
- [ ] `PresupuestoYaExisteException` · pas 4
- [ ] `SolicitudInvalidaException` · pas 4
- [ ] `ConflictoException` · pas 4
- [ ] `GlobalExceptionHandler` · pas 4

### `entity.enums` (11)

- [ ] `Rol` · pas 5
- [ ] `TipoServicio` · pas 5
- [ ] `CuandoOcurre` · pas 5
- [ ] `ConduccionSegura` · pas 5
- [ ] `Urgencia` · pas 5
- [ ] `FranjaPreferida` · pas 5
- [ ] `ContactoPreferido` · pas 5
- [ ] `EstadoOrden` · pas 5
- [ ] `EstadoPresupuesto` · pas 5
- [ ] `TipoLinea` · pas 5
- [ ] `TipoNotificacion` · pas 5

### `entity` (9)

- [ ] `Usuario` · pas 6
- [ ] `Vehiculo` · pas 6
- [ ] `OrdenTrabajo` · pas 6
- [ ] `EventoSeguimiento` · pas 6
- [ ] `Notificacion` · pas 6
- [ ] `Pieza` · pas 6
- [ ] `Presupuesto` · pas 6
- [ ] `LineaPresupuesto` · pas 6
- [ ] `OrdenResumen` · pas 6

### `repository` (10)

- [ ] `UsuarioRepository` · pas 7
- [ ] `VehiculoRepository` · pas 7
- [ ] `OrdenTrabajoRepository` · pas 7
- [ ] `OrdenResumenRepository` · pas 7
- [ ] `PresupuestoRepository` · pas 7
- [ ] `LineaPresupuestoRepository` · pas 7
- [ ] `PiezaRepository` · pas 7
- [ ] `EventoSeguimientoRepository` · pas 7
- [ ] `NotificacionRepository` · pas 7
- [ ] `OrdenResumenSpecifications` · pas 15

### `dto.common` (3)

- [ ] `ErrorDetalle` · pas 4
- [ ] `ErrorResponse` · pas 4
- [ ] `PageResponse` · pas 4

### `dto.auth` (3)

- [ ] `RegistroRequest` · pas 10
- [ ] `LoginRequest` · pas 10
- [ ] `LoginResponse` · pas 10

### `dto.usuario` (4)

- [ ] `UsuarioResponse` · pas 10
- [ ] `UsuarioResumenResponse` · pas 10
- [ ] `UsuarioCreateRequest` · pas 11
- [ ] `UsuarioUpdateRequest` · pas 11

### `dto.vehiculo` (2)

- [ ] `VehiculoRequest` · pas 13
- [ ] `VehiculoResponse` · pas 13

### `dto.orden` (8)

- [ ] `OrdenCreateRequest` · pas 15
- [ ] `OrdenCreateResponse` · pas 15
- [ ] `OrdenResumenResponse` · pas 15
- [ ] `OrdenDetalleResponse` · pas 15
- [ ] `AsignacionRequest` · pas 17
- [ ] `CambioEstadoRequest` · pas 21
- [ ] `EventoResponse` · pas 22
- [ ] `SeguimientoResponse` · pas 22

### `dto.presupuesto` (9)

- [ ] `PresupuestoResumenResponse` · pas 15
- [ ] `PresupuestoUpdateRequest` · pas 19
- [ ] `LineaRequest` · pas 19
- [ ] `LineaResponse` · pas 19
- [ ] `TotalesResponse` · pas 19
- [ ] `AvisoStockResponse` · pas 19
- [ ] `LineaCreadaResponse` · pas 19
- [ ] `PresupuestoResponse` · pas 19
- [ ] `RechazoRequest` · pas 19

### `dto.pieza` (3)

- [ ] `PiezaRequest` · pas 18
- [ ] `PiezaResponse` · pas 18
- [ ] `StockDeltaRequest` · pas 18

### `dto.notificacion` (1)

- [ ] `NotificacionResponse` · pas 23

### `dto.panel` (1)

- [ ] `MetricasResponse` · pas 24

### `mapper` (6)

- [ ] `UsuarioMapper` · pas 10
- [ ] `VehiculoMapper` · pas 13
- [ ] `OrdenMapper` · pas 15
- [ ] `PiezaMapper` · pas 18
- [ ] `PresupuestoMapper` · pas 19
- [ ] `NotificacionMapper` · pas 23

### `validation` (2)

- [ ] `MatriculaEspanola` · pas 12
- [ ] `MatriculaValidator` · pas 12

### `util` (1)

- [ ] `MatriculaUtils` · pas 12

### `service` (11)

- [ ] `AuthService` · pas 10
- [ ] `UsuarioService` · pas 11
- [ ] `VehiculoService` · pas 13
- [ ] `SeguimientoService` · pas 14
- [ ] `NotificacionService` · pas 14
- [ ] `OrdenService` · pas 15
- [ ] `TransicionesEstado` · pas 17
- [ ] `OrdenEstadoService` · pas 17
- [ ] `PiezaService` · pas 18
- [ ] `PresupuestoService` · pas 19
- [ ] `PanelService` · pas 24

### `controller` (8)

- [ ] `AuthController` · pas 10
- [ ] `UsuarioController` · pas 11
- [ ] `VehiculoController` · pas 13
- [ ] `OrdenController` · pas 15
- [ ] `PiezaController` · pas 18
- [ ] `PresupuestoController` · pas 19
- [ ] `NotificacionController` · pas 23
- [ ] `PanelController` · pas 24

### Proves · `src/test/java/com/motorlab/` (10)

- [ ] `support.IntegracionBase` · pas 7
- [ ] `repository.RepositoriosSmokeTest` · pas 7
- [ ] `security.JwtServiceTest` · pas 10
- [ ] `controller.AuthControllerTest` · pas 10
- [ ] `validation.MatriculaValidatorTest` · pas 12
- [ ] `controller.OrdenControllerTest` · pas 15
- [ ] `service.TransicionesEstadoTest` · pas 17
- [ ] `service.PiezaServiceTest` · pas 18
- [ ] `service.PresupuestoServiceTest` · pas 20
- [ ] `service.FlujoCompletoTest` · pas 25

### Recursos de configuració

- [ ] `.gitignore (arrel)` · pas 1
- [ ] `.github/copilot-instructions.md` · pas 1
- [ ] `src/main/resources/application.properties` · pas 3
- [ ] `.vscode/launch.json (no es puja a Git)` · pas 3
- [ ] `src/test/resources/application-test.properties` · pas 7
- [ ] `src/main/resources/application-local.properties (no es puja a Git)` · pas 11
- [ ] `README.md (arrel)` · pas 27

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

---

## Annex C · Crear totes les issues amb GitHub CLI (opcional)

Si tens GitHub CLI instal·lat i has fet `gh auth login`, el script `crear_issues_motorlab.sh` crea les 29 issues **en ordre** (de la #1 a la #29 en un repositori nou). Executa'l des de l'arrel del repositori, a Git Bash:

```bash
bash crear_issues_motorlab.sh
```

Si no el tens, copia a mà el títol i la descripció de cada pas. Després, a cada PR, canvia el `Closes #N` només si els números reals no coincidixen.
