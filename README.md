# Poke-Mart.Fullstack2_Backend

API REST (Spring Boot) para la tienda Pokemart. Usa Oracle DB (wallet) y JWT para autenticación.

## Variables de entorno requeridas
- `DB_URL`: URL JDBC, ej. `jdbc:oracle:thin:@pokemartbdd_high?TNS_ADMIN=/ruta/Wallet_PokeMartBDD`.
- `DB_USERNAME` / `DB_PASSWORD`
- `DB_DRIVER` (opcional, por defecto `oracle.jdbc.OracleDriver`)
- `JWT_SECRET`: clave de al menos 256 bits.
- `JWT_EXPIRATION_MS` / `JWT_REFRESH_EXPIRATION_MS` (opcionales)
- `UPLOADS_DIR` (opcional, por defecto `uploads`)
- `UPLOAD_MAX_FILE`, `UPLOAD_MAX_REQUEST` (opcionales)
- **Mercado Pago (sandbox)**:
  - `MERCADOPAGO_ACCESS_TOKEN`
  - `MERCADOPAGO_INTEGRATOR_ID` (opcional)
  - `MERCADOPAGO_NOTIFICATION_URL` (webhook público -> `/api/v1/payments/mp/webhook`)
  - `MERCADOPAGO_SUCCESS_URL` / `MERCADOPAGO_FAILURE_URL` / `MERCADOPAGO_PENDING_URL` (redirecciones del checkout)

Opcional: `SPRING_PROFILES_ACTIVE` para diferenciar dev/prod y `TNS_ADMIN` si lo pasas por variable de entorno.

## Configuración rápida
1. Copia `application.example.properties` a `src/main/resources/application.properties` solo para desarrollo local y ajusta los placeholders (no subas secretos al repo).
2. Exporta las variables anteriores en el host o servicio (Oracle Cloud) antes de arrancar.
3. Ejecuta:
   ```bash
   ./gradlew bootRun
   ```
4. Swagger UI: `http(s)://<host>:<port>/swagger-ui/index.html`

## Seeds
`DataInitializer` carga:
- usuarios demo (admin/vendedor) y perfil básico de cliente.
- catálogo desde `productos.json`, ofertas desde `ofertas.json`, reviews y blogs demo.
- órdenes demo si no existen.

Para desactivar o ajustar seeding, modifica `DataInitializer` o usa un perfil separado.

## Despliegue en Oracle Cloud (wallet) y uploads

1. Descarga el wallet de tu Autonomous DB desde OCI Console (`DB Connection` → `Download Wallet`) y descompr��melo en una ruta accesible, por ejemplo `/opt/oci/Wallet_PokeMartBDD` o `C:\wallets\Wallet_PokeMartBDD`.
2. Exporta `TNS_ADMIN` apuntando a esa carpeta (donde est�� `tnsnames.ora`).
3. Define `DB_URL` usando el alias del wallet, por ejemplo `jdbc:oracle:thin:@pokemartbdd_high?TNS_ADMIN=/opt/oci/Wallet_PokeMartBDD` (ajusta la ruta en Windows). Variables requeridas: `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER` (opcional si usas `oracle.jdbc.OracleDriver`).
4. Define secretos de seguridad: `JWT_SECRET`, `JWT_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS`.
5. Configura uploads: crea el directorio configurado en `UPLOADS_DIR` (por defecto `uploads`) con permisos de escritura para el usuario que corre la app. En contenedores/OCI compute, monta un volumen o carpeta persistente y expórtala como `UPLOADS_DIR`.
6. Asegura que el servicio tenga acceso al wallet (monta la carpeta o inyecta el zip descomprimido) y exporta las variables anteriores en el entorno antes de arrancar la app.

## Mercado Pago (sandbox)
Flujo implementado:
- El frontend solicita `/api/v1/payments/mp/preference` con los datos de envío y carrito.
- El backend crea una `preference` en Mercado Pago usando precios/stock de la BD y guarda un `PaymentIntent` (snapshot del pedido).
- Mercado Pago redirige al usuario y puede enviar notificaciones al webhook `/api/v1/payments/mp/webhook`.
- Si usas webhook: cuando el pago se marca `approved`, el backend crea la orden y descuenta stock; si se rechaza/cancela, no se crea la orden.
- Si prefieres retorno (sin webhook): en la URL de éxito el frontend recibe `payment_id` y llama a `POST /api/v1/payments/mp/confirm`, que consulta el pago en MP y crea la orden solo si está `approved`.

Configura estas variables/propiedades antes de probar:
- `MERCADOPAGO_ACCESS_TOKEN`: token sandbox.
- `MERCADOPAGO_INTEGRATOR_ID` (opcional).
- `MERCADOPAGO_NOTIFICATION_URL`: URL pública que apunte al webhook.
- `MERCADOPAGO_SUCCESS_URL`, `MERCADOPAGO_FAILURE_URL`, `MERCADOPAGO_PENDING_URL`: rutas del frontend para redirecciones de MP.
