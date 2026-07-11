# AWS API Gateway — Guía de configuración

Todos los endpoints de `ms-guias-productor` y `ms-guias-consumidor` deben
quedar registrados en el API Gateway y securitizados con el mismo JWT de
Azure AD B2C que ya validan los microservicios. API Gateway valida el token
**antes** de reenviar la petición al backend (defensa adicional, no
reemplaza la seguridad de cada microservicio — ambas capas se mantienen).

## 0. Prerrequisitos

- Los dos contenedores (`ms-guias-productor` puerto 8080, `ms-guias-consumidor`
  puerto 8081) corriendo en tu EC2.
- El Security Group del EC2 debe permitir tráfico entrante en 8080 y 8081
  **solo desde el Security Group de API Gateway** (no público) — así nadie
  puede saltarse el Gateway y pegarle directo al EC2.
- Tener a mano: `issuer-uri` y `jwk-set-uri` de tu tenant Azure AD B2C (los
  mismos valores que están en `application.properties` de ambos
  microservicios).

## 1. Crear la API

AWS Console → **API Gateway** → **Create API** → **HTTP API** (más simple y
barata que REST API; soporta JWT Authorizer nativo, que es justo lo que
necesitamos para Azure AD B2C).

- Nombre: `guias-despacho-api`
- Integraciones: las vas a crear ruta por ruta en el paso 3, apuntando al EC2.

## 2. Crear el JWT Authorizer (Azure AD B2C)

API Gateway → tu API → **Authorization** → **Create and attach an authorizer**
→ tipo **JWT**.

| Campo | Valor |
|---|---|
| Nombre | `AzureADB2CAuthorizer` |
| Issuer URL | el mismo `spring.security.oauth2.resourceserver.jwt.issuer-uri` de tus microservicios (ej. `https://democloudnative.b2clogin.com/.../B2C_1_acceso/v2.0/`) |
| Audience | tu `azure.client-id` (el mismo que valida `SecurityConfig` en Java) |
| Identity source | `$request.header.Authorization` |

Este único authorizer se reutiliza en **todas** las rutas (no hay que crear
uno por endpoint).

## 3. Rutas a registrar

Todas las rutas usan `AzureADB2CAuthorizer`. La columna "Rol requerido" es
la que ya valida `@PreAuthorize` en el microservicio — API Gateway solo
verifica que el JWT sea válido (firma + issuer + audience); la autorización
fina por rol la sigue haciendo Spring Security del lado del microservicio.

### `ms-guias-productor` → integración: `http://<EC2_IP>:8080`

| Método | Ruta en API Gateway | Ruta destino (backend) | Rol requerido |
|---|---|---|---|
| POST | `/guias/{bucket}/generar` | `/guias/{bucket}/generar` | ADMIN |
| POST | `/guias/{bucket}/subir` | `/guias/{bucket}/subir` | ADMIN |
| GET | `/guias/{bucket}/object` | `/guias/{bucket}/object` | DESCARGA, ADMIN |
| PUT | `/guias/{bucket}/object` | `/guias/{bucket}/object` | ADMIN |
| DELETE | `/guias/{bucket}/object` | `/guias/{bucket}/object` | ADMIN |
| GET | `/guias/{bucket}/filtrar` | `/guias/{bucket}/filtrar` | ADMIN |
| GET | `/guias/{bucket}/objects` | `/guias/{bucket}/objects` | ADMIN |

### `ms-guias-consumidor` → integración: `http://<EC2_IP>:8081`

| Método | Ruta en API Gateway | Ruta destino (backend) | Rol requerido |
|---|---|---|---|
| GET | `/consumo/estado` | `/api/consumo/guias/estado` | ADMIN |
| POST | `/consumo/listener/pausar/{id}` | `/rabbit-listener/pausar/{id}` | ADMIN |
| POST | `/consumo/listener/reanudar/{id}` | `/rabbit-listener/reanudar/{id}` | ADMIN |
| GET | `/consumo/listener/status/{id}` | `/rabbit-listener/status/{id}` | ADMIN |
| GET | `/consumo/listener/guias/status` | `/rabbit-listener/guias/status` | ADMIN |
| POST | `/consumo/listener/guias/reanudar` | `/rabbit-listener/guias/reanudar` | ADMIN |
| POST | `/consumo/listener/guias/pausar` | `/rabbit-listener/guias/pausar` | ADMIN |

> Nota: las rutas del lado de API Gateway (columna 2) las puedes dejar
> idénticas a las del backend si prefieres no reescribir el path — el
> renombrado de arriba (`/consumo/...`) es solo un ejemplo de cómo
> "limpiar" las URLs públicas. Si tu profesor pide que las rutas públicas
> sean idénticas a los `@RequestMapping` del código, usa la misma ruta en
> ambas columnas.

## 4. Deploy y URL de invocación

**Deploy** → crea un **stage** (ej. `prod`) → API Gateway te da una URL tipo:
```
https://abc123xyz.execute-api.us-east-1.amazonaws.com/prod
```
Esa es la URL que usas en Postman de ahora en adelante — **no** la IP del EC2
directamente (por eso el paso 0 de cerrar el Security Group público).

## 5. Cuando cambie la IP del EC2

Si el EC2 se reinicia y cambia de IP pública (a menos que tengas Elastic IP):
API Gateway → tu API → **Integrations** → selecciona cada integración → edita
el "Integration target" con la nueva IP. Solo hay **2 integraciones** que
tocar (una por microservicio), no una por ruta — todas las rutas de un mismo
microservicio comparten la misma integración.

## 6. Probar con Postman

1. Obtener un JWT real de Azure AD B2C (login contra tu user flow
   `B2C_1_acceso`).
2. Header `Authorization: Bearer <token>` contra la URL del stage
   (ej. `https://abc123xyz.execute-api.us-east-1.amazonaws.com/prod/guias/despachog15/objects`).
3. Si el token no es válido (expiró, audience incorrecta, etc.) → API Gateway
   responde `401` **sin que la petición llegue al EC2** — así confirmas que
   el authorizer está funcionando antes de que el microservicio entre en juego.
