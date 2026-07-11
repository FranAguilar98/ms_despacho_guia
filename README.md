# Sistema de Gestión de Pedidos y Guías de Despacho

## Arquitectura de código: interfaz + Impl (igual al ejemplo del profesor)

Se siguió el mismo patrón que `cl.duoc.cdy2204.msrabbitmq` (ProducirMensajeService
/Impl, ConsumirMensajeService/Impl, RabbitListenerControlService/Impl):

### `ms-guias-productor` (base: Cloud-Native-main, paquete `sistemadegestion.demo`)
- `dto/GuiaDespachoMensajeDTO.java` — DTO con Lombok (`@Data @AllArgsConstructor
  @NoArgsConstructor`), igual estilo que `BindingDTO`/`ProductoDTO` del profesor,
  con validaciones `@NotBlank`/`@PositiveOrZero`.
- `service/ProducirGuiaService.java` (interfaz) + `service/impl/ProducirGuiaServiceImpl.java`
  — publica en `cola.guias.despacho`. Si RabbitMQ no está disponible, lanza
  `GuiaDespachoPublishException` (no falla en silencio ni con un 500 genérico).
- `exception/GuiaDespachoPublishException.java` + handler agregado en el
  `GlobalExceptionHandler` ya existente (mismo estilo `ErrorResponse.builder()`
  que ya usabas para errores de S3).
- `config/RabbitMQConfig.java` — exchange, colas y bindings **declarados en
  Java con `@Bean`** (`Queue`, `DirectExchange`, `Binding`), igual que el
  ejemplo del profesor. La cola 1 tiene `x-dead-letter-exchange` apuntando al
  exchange de error: si el consumidor rechaza un mensaje, RabbitMQ lo reenvía
  solo a la cola 2, sin código Java de por medio.
- `config/SecurityConfig.java` (Azure AD B2C real) + `config/SecurityConfigDev.java`
  (`@Profile("dev")`, `permitAll()`, solo para probar S3 sin JWT).
- `AwsS3Controller` inyecta `ProducirGuiaService` (la interfaz, no la
  implementación) y publica tras subir la guía a S3.

### `ms-guias-consumidor` (proyecto nuevo, mismo paquete `sistemadegestion.demo`)
- `service/ConsumirGuiaService.java` (interfaz) + `service/impl/ConsumirGuiaServiceImpl.java`
  — contiene el `@RabbitListener` (id fijo `listenerGuiasDespacho`,
  `autoStartup=false`, ack manual: `basicAck`/`basicNack`, mismo patrón que
  `recibirMensajeConAckManual` del profesor).
- `service/RabbitListenerControlService.java` (interfaz) + `service/impl/RabbitListenerControlServiceImpl.java`
  — `pausarListener`/`reanudarListener`/`isListenerRunning`, copiado 1 a 1 del
  profesor (usa `RabbitListenerEndpointRegistry`).
- `controller/RabbitListenerAdminController.java` — mismos endpoints que el
  profesor (`/rabbit-listener/pausar/{id}`, `/reanudar/{id}`, `/status/{id}`),
  más atajos (`/rabbit-listener/guias/reanudar`, etc.) para no tener que
  acordarse del id del listener. **`POST /rabbit-listener/guias/reanudar` es
  el endpoint adicional que exige el enunciado** para empezar a consumir la
  cola 1.
- `controller/GuiaColaEstadoController.java` — `GET /api/consumo/guias/estado`,
  mensajes pendientes en cola 1 y cola 2.
- `entity/GuiaDespachoProcesada.java` + `repository/...` — tabla
  `guia_despacho_procesada` en Oracle Cloud.
- Mismo `SecurityConfig` real (Azure AD B2C, roles `ADMIN`/`DESCARGA`) que el
  productor, protegiendo `/api/consumo/guias/**` y `/rabbit-listener/**`.

## RabbitMQ: todo declarado en Java (como el profesor)

Ya no hay `definitions.json`. El exchange `exchange.guias.despacho`, la cola
`cola.guias.despacho` (con DLX), la cola de error `cola.guias.despacho.error`
y sus bindings se declaran con `@Bean` en `RabbitMQConfig` de **ambos**
microservicios — deben ser idénticos en nombre/argumentos en los dos, o
RabbitMQ tira `PRECONDITION_FAILED` al intentar declarar la misma cola con
argumentos distintos desde cada app.

## Flujo de prueba

```bash
docker compose -f docker-compose-rabbitmq.yml up -d
docker compose -f docker-compose.yml up --build -d
```

1. `POST /rabbit-listener/guias/reanudar` en el consumidor (rol ADMIN) —
   enciende el listener (está apagado por defecto).
2. Generar/subir una guía en el productor (`/guias/...`) → publica en cola 1.
3. El listener del consumidor la toma automáticamente y la guarda en Oracle.
4. `GET /api/consumo/guias/estado` para confirmar que la cola quedó en 0.
5. Para forzar un error y ver el DLX funcionando: `POST /rabbit-listener/guias/pausar`,
   mandar un mensaje inválido directo desde la UI de RabbitMQ a `cola.guias.despacho`,
   reanudar el listener → debería caer en `cola.guias.despacho.error`.

## Pendiente para el resto de la rúbrica

- **API Gateway**: enrutar hacia ambos microservicios y validar el JWT antes
  de reenviar.
- **Roles de Azure AD B2C en cuentas de consumidor**: resolver vía Microsoft
  Graph API (`POST /users/{id}/appRoleAssignments`).
