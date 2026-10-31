# MotorLab · Especificación de la API REST (v1)

**Autor:** Pablo Ruiz Gandia · 2.º DAW
**Base de datos de referencia:** `motorlab_schema.sql`
**Stack propuesto:** Node.js + Express · MySQL · JWT · bcrypt

---

## 1. Convenciones generales

| Aspecto | Decisión |
|---|---|
| URL base | `/api/v1` |
| Formato | JSON (`Content-Type: application/json`), fechas en ISO 8601 (`2026-10-12T09:14:00`) |
| Autenticación | JWT en la cabecera `Authorization: Bearer <token>`; caducidad de 8 horas |
| Contraseñas | Mínimo 8 caracteres; se guardan con **bcrypt** (nunca en claro) |
| Roles | `cliente`, `mecanico`, `admin` (ver permisos en la sección 3) |
| Importes | Se calculan **siempre en el servidor**; el cliente nunca envía subtotal, IVA ni total |
| Paginación | `?pagina=1&limite=20` en los listados (por defecto 20, máximo 100) |
| Nombres | Recursos en plural y en minúsculas; campos en `snake_case` (igual que la BD) |

### 1.1 Códigos de estado usados

| Código | Cuándo |
|---|---|
| `200` | Consulta o actualización correcta |
| `201` | Recurso creado |
| `204` | Borrado correcto (sin cuerpo) |
| `400` | Datos mal formados o que no pasan la validación |
| `401` | Falta el token o ha caducado |
| `403` | El usuario no tiene permiso (rol incorrecto o recurso de otro cliente) |
| `404` | El recurso no existe (también se usa si un cliente pide la orden de otro, para no revelar que existe) |
| `409` | Conflicto con el estado actual (transición no permitida, matrícula duplicada, stock insuficiente) |
| `500` | Error interno (no se muestran detalles al usuario) |

### 1.2 Formato de error

```json
{
  "error": {
    "codigo": "VALIDACION",
    "mensaje": "Hay campos con errores",
    "detalles": [
      { "campo": "matricula", "mensaje": "Formato de matrícula no válido" }
    ]
  }
}
```

Códigos de error propios: `VALIDACION`, `NO_AUTENTICADO`, `SIN_PERMISO`, `NO_ENCONTRADO`, `TRANSICION_NO_PERMITIDA`, `MATRICULA_DUPLICADA`, `STOCK_INSUFICIENTE`, `PRESUPUESTO_NO_EDITABLE`.

---

## 2. Máquina de estados de una orden

```
solicitud_recibida ──► pendiente_presupuesto ──► presupuesto_enviado ──► presupuesto_autorizado
        │                      │                        │  └──► rechazada                 │
        └──────────────────────┴────────► cancelada ◄───┘                                  ▼
                                                                                        recibido
                                                                                           ▼
                                                              en_preparacion ◄──► esperando_pieza
                                                                     └──────► arreglando ◄───┘
                                                                                 ▼
                                                                               listo ──► entregado
```

| Desde | Hacia | Quién | Cómo se produce |
|---|---|---|---|
| `solicitud_recibida` | `pendiente_presupuesto` | admin | `PATCH /ordenes/:id/estado` |
| `pendiente_presupuesto` | `presupuesto_enviado` | admin | `POST /presupuestos/:id/enviar` (automático) |
| `presupuesto_enviado` | `presupuesto_autorizado` | cliente | `POST /presupuestos/:id/autorizar` (automático) |
| `presupuesto_enviado` | `rechazada` | cliente | `POST /presupuestos/:id/rechazar` (automático) |
| `presupuesto_autorizado` | `recibido` | admin | `PATCH /ordenes/:id/estado` (el vehículo llega al taller) |
| `recibido` | `en_preparacion` | admin, mecánico | `PATCH /ordenes/:id/estado` |
| `en_preparacion` | `esperando_pieza` o `arreglando` | admin, mecánico | `PATCH /ordenes/:id/estado` |
| `esperando_pieza` | `arreglando` | admin, mecánico | `PATCH /ordenes/:id/estado` |
| `arreglando` | `esperando_pieza` o `listo` | admin, mecánico | `PATCH /ordenes/:id/estado` |
| `listo` | `entregado` | admin | `PATCH /ordenes/:id/estado` |
| cualquiera antes de `recibido` | `cancelada` | admin; cliente (solo si aún no hay presupuesto autorizado) | `POST /ordenes/:id/cancelar` |

**Reglas asociadas a cada cambio de estado** (todas dentro de **una transacción**):

1. Se actualiza `ordenes_trabajo.estado`.
2. Se inserta una fila en `eventos_seguimiento` con la fecha y hora y el usuario que lo hizo.
3. Se crea una notificación para el cliente cuando el cambio le interesa (presupuesto listo, vehículo listo, cambios de ejecución).
4. **Stock:** al pasar a `arreglando` por primera vez se descuenta el stock de las piezas del presupuesto. Si alguna no tiene stock suficiente se responde `409 STOCK_INSUFICIENTE` y no cambia nada; el administrador debe registrar la entrada de la pieza (`PATCH /piezas/:id/stock`) y repetirlo.

> **Nota de diseño:** respecto al documento de propuesta, se añade el estado `presupuesto_autorizado` para distinguir «el cliente ya aceptó» de «el coche ya está en el taller».

### 2.1 Progreso que se muestra al cliente

| Estado | Progreso |
|---|---|
| Anteriores a `recibido` | 0 % |
| `recibido` | 20 % |
| `en_preparacion` | 40 % |
| `esperando_pieza` | 60 % |
| `arreglando` | 80 % |
| `listo` / `entregado` | 100 % |

---

## 3. Matriz de permisos

| Recurso / acción | Cliente | Mecánico | Admin |
|---|:---:|:---:|:---:|
| Registrarse / iniciar sesión | ✔ | ✔ | ✔ |
| Gestionar **sus** vehículos | ✔ | — | ✔ (todos) |
| Crear solicitud de reparación | ✔ | — | ✔ |
| Ver órdenes | solo las suyas | solo las asignadas | todas |
| Asignar mecánico/elevador, revisar solicitud | — | — | ✔ |
| Cambiar estado de ejecución | — | ✔ (asignadas) | ✔ |
| Crear/editar presupuesto y enviarlo | — | — | ✔ |
| Ver presupuesto | el suyo | — | ✔ |
| Autorizar / rechazar presupuesto | ✔ (el suyo) | — | — |
| Catálogo de piezas: consultar | — | ✔ | ✔ |
| Catálogo de piezas: crear/editar/stock | — | — | ✔ |
| Gestión de usuarios y panel de métricas | — | — | ✔ |

---

## 4. Endpoints

### 4.1 Autenticación

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/auth/registro` | público | Crea una cuenta de **cliente** |
| `POST` | `/auth/login` | público | Devuelve el token JWT |
| `GET` | `/auth/yo` | autenticado | Datos del usuario del token |

**`POST /auth/registro`**

```json
{
  "nombre": "Laura Gómez",
  "email": "laura@cliente.example",
  "telefono": "600000003",
  "password": "MiClaveSegura1",
  "acepta_privacidad": true
}
```

Respuesta `201`: `{ "id": 3, "nombre": "Laura Gómez", "email": "laura@cliente.example", "rol": "cliente" }`
Validación: email con formato válido y único (`409` si ya existe), contraseña ≥ 8 caracteres, `acepta_privacidad` debe ser `true` (se guarda la fecha en `consentimiento_en`).

**`POST /auth/login`**

```json
{ "email": "laura@cliente.example", "password": "MiClaveSegura1" }
```

Respuesta `200`:

```json
{
  "token": "eyJhbGciOi...",
  "usuario": { "id": 3, "nombre": "Laura Gómez", "rol": "cliente" }
}
```

Credenciales incorrectas: `401` con el mismo mensaje tanto si falla el email como la contraseña (no revelar cuál).

---

### 4.2 Vehículos

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/vehiculos` | cliente, admin | Lista los vehículos (el cliente solo ve los suyos) |
| `POST` | `/vehiculos` | cliente, admin | Da de alta un vehículo |
| `GET` | `/vehiculos/:id` | propietario, admin | Detalle |
| `PUT` | `/vehiculos/:id` | propietario, admin | Edita marca, modelo, versión, año o kilómetros |
| `DELETE` | `/vehiculos/:id` | propietario, admin | Baja lógica (`activo = 0`); `409` si tiene órdenes abiertas |
| `GET` | `/vehiculos/:id/historial` | propietario, admin | Órdenes anteriores del vehículo |

**`POST /vehiculos`**

```json
{
  "matricula": "4821 KLB",
  "marca": "Seat",
  "modelo": "León",
  "version": "1.5 TSI",
  "anio": 2019,
  "kilometros": 86400
}
```

Validación: la matrícula se normaliza (mayúsculas, sin espacios ni guiones) y debe cumplir `^[0-9]{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$`; `409 MATRICULA_DUPLICADA` si ya existe; `anio` entre 1980 y el año actual; `kilometros` entero ≥ 0.

---

### 4.3 Órdenes de trabajo (solicitudes)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/ordenes` | cliente, admin | Crea la solicitud de reparación |
| `GET` | `/ordenes` | todos (filtrado por rol) | Lista con filtros |
| `GET` | `/ordenes/:id` | cliente (suya), mecánico (asignada), admin | Detalle completo |
| `PATCH` | `/ordenes/:id/asignacion` | admin | Asigna mecánico, elevador y fecha estimada |
| `PATCH` | `/ordenes/:id/estado` | admin, mecánico | Cambia el estado (ver sección 2) |
| `POST` | `/ordenes/:id/cancelar` | cliente (suya), admin | Cancela la orden |
| `GET` | `/ordenes/:id/seguimiento` | cliente (suya), admin, mecánico | Línea de tiempo y progreso |

**`POST /ordenes`**

```json
{
  "vehiculo_id": 1,
  "tipo_servicio": "frenos",
  "sintoma": "Hace un ruido raro al frenar, el Seat León",
  "cuando_ocurre": "al_frenar",
  "conduccion_segura": "si",
  "urgencia": "media",
  "fecha_preferida": "2026-10-12",
  "franja_preferida": "manana",
  "contacto_preferido": "telefono",
  "acepta_privacidad": true
}
```

Respuesta `201`:

```json
{
  "id": 2481,
  "estado": "solicitud_recibida",
  "urgencia": "alta",
  "mensaje": "Solicitud recibida. El taller la revisará en breve."
}
```

Validación (todo se comprueba en el servidor, aunque el formulario ya valide):

- `vehiculo_id` debe pertenecer al cliente que hace la petición.
- `tipo_servicio`, `cuando_ocurre`, `conduccion_segura`, `urgencia`, `franja_preferida` y `contacto_preferido` solo admiten los valores del esquema.
- `sintoma`: mínimo 20 caracteres.
- `fecha_preferida`: fecha futura (hoy o posterior).
- `acepta_privacidad`: obligatorio y `true`.
- **Regla fija de urgencia:** si `conduccion_segura = "no"` o `tipo_servicio = "frenos"`, la urgencia queda en `alta` aunque el cliente haya indicado otra (lo garantiza el trigger de la BD).

**`GET /ordenes`** — filtros opcionales: `estado`, `urgencia`, `mecanico_id`, `q` (matrícula o nombre del cliente), `desde`, `hasta`, `pagina`, `limite`. Se apoya en la vista `v_ordenes_resumen`.

```json
{
  "datos": [
    {
      "orden_id": 2481,
      "estado": "arreglando",
      "progreso": 80,
      "urgencia": "alta",
      "matricula": "4821KLB",
      "marca": "Seat",
      "modelo": "León",
      "cliente_nombre": "Laura Gómez",
      "mecanico_nombre": "Javi",
      "presupuesto_estado": "autorizado",
      "presupuesto_total": "256.52"
    }
  ],
  "pagina": 1,
  "total": 1
}
```

**`PATCH /ordenes/:id/asignacion`** (admin)

```json
{ "mecanico_id": 2, "elevador": "Elevador 2", "fecha_estimada": "2026-10-12T18:30:00" }
```

**`PATCH /ordenes/:id/estado`**

```json
{ "estado": "esperando_pieza", "descripcion": "Pastillas pedidas · llegan 12:45" }
```

Respuesta `200` con la orden actualizada. Si la transición no está permitida: `409 TRANSICION_NO_PERMITIDA`. El mecánico solo puede usar `en_preparacion`, `esperando_pieza`, `arreglando` y `listo`, y solo en órdenes que tiene asignadas (`403` en caso contrario).

**`GET /ordenes/:id/seguimiento`**

```json
{
  "orden_id": 2481,
  "vehiculo": "Seat León 1.5 TSI · 4821KLB",
  "estado": "arreglando",
  "progreso": 80,
  "listo_estimado": "2026-10-12T18:30:00",
  "eventos": [
    { "estado": "recibido",        "descripcion": "Hemos recibido tu coche",          "fecha_hora": "2026-10-12T09:14:00" },
    { "estado": "en_preparacion",  "descripcion": "Diagnóstico y revisión",           "fecha_hora": "2026-10-12T10:02:00" },
    { "estado": "esperando_pieza", "descripcion": "Pastillas pedidas · llegan 12:45", "fecha_hora": "2026-10-12T11:30:00" },
    { "estado": "arreglando",      "descripcion": "Cambio de pastillas y discos",     "fecha_hora": "2026-10-12T13:05:00" }
  ]
}
```

---

### 4.4 Presupuestos

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/ordenes/:id/presupuesto` | admin | Crea el presupuesto (estado `borrador`) |
| `GET` | `/ordenes/:id/presupuesto` | cliente (suyo, si no es borrador), admin | Presupuesto con sus líneas y totales |
| `PATCH` | `/presupuestos/:id` | admin | Edita tiempo estimado, garantía (solo en `borrador`) |
| `POST` | `/presupuestos/:id/lineas` | admin | Añade una línea (solo en `borrador`) |
| `PUT` | `/presupuestos/:id/lineas/:lineaId` | admin | Modifica una línea |
| `DELETE` | `/presupuestos/:id/lineas/:lineaId` | admin | Elimina una línea |
| `POST` | `/presupuestos/:id/enviar` | admin | Pasa a `enviado` y avisa al cliente |
| `POST` | `/presupuestos/:id/autorizar` | cliente | El cliente acepta |
| `POST` | `/presupuestos/:id/rechazar` | cliente | El cliente rechaza (motivo opcional) |

Editar un presupuesto que ya no está en `borrador` devuelve `409 PRESUPUESTO_NO_EDITABLE`.

**`POST /presupuestos/:id/lineas`** — línea de pieza (el precio sale del catálogo, no lo envía el cliente web):

```json
{ "tipo": "pieza", "pieza_id": 2, "cantidad": 2 }
```

Línea de mano de obra:

```json
{ "tipo": "mano_obra", "concepto": "Mano de obra", "cantidad": 1.5, "precio_unitario": 28.00 }
```

Respuesta `201`: la línea creada y los totales ya recalculados por los triggers de la BD, con un aviso de stock (RF-12):

```json
{
  "linea": { "id": 6, "concepto": "Discos de freno delanteros", "cantidad": "2.00", "precio_unitario": "36.00", "importe": "72.00" },
  "totales": { "subtotal": "212.00", "iva": "44.52", "total": "256.52" },
  "aviso_stock": { "pieza_id": 2, "referencia": "DS-118", "stock": 1, "necesarias": 2 }
}
```

`aviso_stock` solo aparece si `stock < cantidad`; no impide guardar la línea.

**`GET /ordenes/:id/presupuesto`**

```json
{
  "id": 1,
  "estado": "enviado",
  "lineas": [
    { "concepto": "Pastillas de freno delanteras", "cantidad": "1.00", "precio_unitario": "86.00", "importe": "86.00" },
    { "concepto": "Discos de freno delanteros",    "cantidad": "2.00", "precio_unitario": "36.00", "importe": "72.00" },
    { "concepto": "Líquido de frenos DOT 4",       "cantidad": "1.00", "precio_unitario": "12.00", "importe": "12.00" },
    { "concepto": "Mano de obra",                  "cantidad": "1.50", "precio_unitario": "28.00", "importe": "42.00" }
  ],
  "subtotal": "212.00",
  "iva_porcentaje": "21.00",
  "iva": "44.52",
  "total": "256.52",
  "tiempo_estimado_horas": "2.0",
  "garantia_meses": 24
}
```

**`POST /presupuestos/:id/enviar`** (admin). Condiciones: estado `borrador` y al menos una línea (`400` si no). Efectos, en una transacción: presupuesto → `enviado` (`enviado_en`), orden → `presupuesto_enviado`, evento de seguimiento y notificación `presupuesto_listo` al cliente.

**`POST /presupuestos/:id/autorizar`** (cliente propietario). Condición: estado `enviado` (si no, `409`). Efectos: presupuesto → `autorizado` (`respondido_en`), orden → `presupuesto_autorizado`, evento y notificación al administrador. Para evitar dobles clics o peticiones simultáneas, se bloquea la fila con `SELECT ... FOR UPDATE` dentro de la transacción.

**`POST /presupuestos/:id/rechazar`** (cliente propietario)

```json
{ "motivo": "Es más caro de lo que esperaba" }
```

Efectos: presupuesto → `rechazado` (guarda `motivo_rechazo`), orden → `rechazada`, evento y notificación al administrador.

---

### 4.5 Catálogo de piezas

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/piezas` | mecánico, admin | Lista (filtros `q`, `solo_activas`, `stock_bajo=true`) |
| `POST` | `/piezas` | admin | Crea una pieza |
| `GET` | `/piezas/:id` | mecánico, admin | Detalle |
| `PUT` | `/piezas/:id` | admin | Edita referencia, nombre, precio |
| `DELETE` | `/piezas/:id` | admin | Baja lógica (`activa = 0`); no se borra si aparece en presupuestos |
| `PATCH` | `/piezas/:id/stock` | admin | Registra una entrada o ajuste de stock |

**`PATCH /piezas/:id/stock`**

```json
{ "delta": 4, "motivo": "Entrada de pedido" }
```

`delta` puede ser negativo para ajustes. Si el resultado fuera menor que 0: `409 STOCK_INSUFICIENTE` (lo refuerza el `CHECK` de la BD).

---

### 4.6 Notificaciones

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/notificaciones` | autenticado | Las del usuario (`?leida=false` para las pendientes) |
| `PATCH` | `/notificaciones/:id/leida` | propietario | Marca como leída |
| `POST` | `/notificaciones/marcar-todas` | autenticado | Marca todas como leídas |

La versión inicial usa **consulta periódica (polling)** cada 30–60 segundos desde el frontend; WebSockets o SSE quedan como mejora futura.

---

### 4.7 Usuarios y panel (administración)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/usuarios` | admin | Lista con filtro `rol` |
| `POST` | `/usuarios` | admin | Crea un mecánico u otro administrador |
| `PATCH` | `/usuarios/:id` | admin | Cambia rol o activa/desactiva (`activo`) |
| `GET` | `/panel/metricas` | admin | Métricas básicas (deseable, RF-15) |

**`GET /panel/metricas`**

```json
{
  "ordenes_por_estado": { "pendiente_presupuesto": 3, "arreglando": 2, "listo": 1 },
  "ingresos_mes": "1840.35",
  "presupuestos_pendientes_de_respuesta": 4
}
```

---

## 5. Seguridad

1. **Contraseñas:** bcrypt con coste ≥ 10; la API nunca devuelve `password_hash`.
2. **Autorización por recurso:** además del rol, se comprueba que el recurso es del usuario (un cliente no puede ver ni modificar órdenes ni vehículos de otro; se responde `404`).
3. **Consultas parametrizadas** en todo acceso a MySQL (nunca concatenar texto del usuario en el SQL) para evitar inyección SQL.
4. **Validación en servidor** de todos los campos (por ejemplo, con la librería `express-validator` o `zod`); el formulario del frontend es solo una ayuda.
5. **Límite de intentos** en `/auth/login` (por ejemplo, 5 por minuto y por IP) con `express-rate-limit`.
6. **Cabeceras y CORS:** `helmet` y CORS restringido al dominio del frontend.
7. **Transacciones** en todo lo que toca varias tablas (crear orden + evento, enviar, autorizar, cambiar estado).
8. **Mensajes de error genéricos** hacia el exterior y detalle solo en los registros del servidor.

## 6. Estructura de carpetas recomendada (Express)

```
motorlab-api/
├── src/
│   ├── app.js                 # configuración de Express, middlewares, rutas
│   ├── server.js              # arranque
│   ├── config/
│   │   └── db.js              # pool de conexiones MySQL (mysql2/promise)
│   ├── middlewares/
│   │   ├── auth.js            # verifica JWT y rol
│   │   ├── validar.js         # resultado de validaciones
│   │   └── errores.js         # manejador global de errores
│   ├── routes/                # una por recurso: auth, vehiculos, ordenes, presupuestos, piezas...
│   ├── controllers/           # reciben la petición y devuelven la respuesta
│   ├── services/              # lógica de negocio y transacciones (transiciones de estado, stock)
│   └── repositories/          # consultas SQL
├── .env.example               # DB_HOST, DB_USER, DB_PASS, DB_NAME, JWT_SECRET
└── package.json
```

## 7. Orden de implementación sugerido

1. Conexión a la BD, `auth` (registro, login, middleware JWT y roles).
2. `vehiculos` y `POST /ordenes` con validaciones y la regla de urgencia.
3. Listado y detalle de órdenes con filtros por rol.
4. `piezas` y `presupuestos` (líneas, cálculo y envío).
5. Autorizar/rechazar y transiciones de estado con eventos de seguimiento.
6. `seguimiento`, notificaciones y, al final, el panel de métricas.
