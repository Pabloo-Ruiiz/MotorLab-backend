Estoy desarrollando MotorLab: una API REST para gestionar las reparaciones de un taller mecánico (solicitudes de los clientes, órdenes de trabajo, presupuestos con IVA, stock de piezas y seguimiento).



Stack: Java 21, Spring Boot 3.x, Maven, Spring Web, Spring Data JPA (Hibernate), PostgreSQL 15+, Spring Security con JWT (jjwt 0.12.x), Jakarta Validation y springdoc-openapi.



Proyecto: ubicado en la RAÍZ del repositorio, paquete raíz com.motorlab con los subpaquetes config, security, entity (y entity.enums), repository, dto (con subpaquetes por dominio: common, auth, usuario, vehiculo, orden, presupuesto, pieza, notificacion, panel), mapper, service, controller, exception, validation y util.



Reglas que debes cumplir SIEMPRE:

1\. Nombres de clases, métodos, variables y comentarios en castellano.

2\. Sin Lombok: getters y setters explícitos en las entidades. Los DTO son records de Java.

3\. Inyección de dependencias por constructor (nunca @Autowired en campos).

4\. Los controladores solo reciben la petición, llaman a un servicio y devuelven un DTO. Nunca devuelven entidades ni el campo password\_hash.

5\. La lógica de negocio va en los servicios, con @Transactional (readOnly = true en las consultas).

6\. La base de datos PostgreSQL YA existe: la crea el script docs/motorlab\_schema.sql y spring.jpa.hibernate.ddl-auto=none. Las entidades deben mapear exactamente las tablas y columnas de ese script.

7\. El JSON usa snake\_case (spring.jackson.property-naming-strategy=SNAKE\_CASE). Los importes son BigDecimal.

8\. Los enums de Java tienen las constantes en minúscula, idénticas a los valores CHECK / VARCHAR de PostgreSQL (por ejemplo solicitud\_recibida), para que coincidan con la base de datos y con el JSON.

9\. Todas las rutas empiezan por /api/v1 y siguen docs/motorlab\_api.md. Los errores usan el formato {"error": {"codigo": "...", "mensaje": "...", "detalles": \[{"campo": "...", "mensaje": "..."}]}}.

10\. Antes de generar nada, lee #file:docs/motorlab\_schema.sql y #file:docs/motorlab\_api.md. Si algo no está claro, pregúntame en lugar de inventarlo.



Cuando termines, dime qué archivos has creado o modificado.



