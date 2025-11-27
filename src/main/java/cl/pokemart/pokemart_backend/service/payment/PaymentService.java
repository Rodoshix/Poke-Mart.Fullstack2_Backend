package cl.pokemart.pokemart_backend.service.payment;

import cl.pokemart.pokemart_backend.config.MercadoPagoProperties;
import cl.pokemart.pokemart_backend.dto.order.OrderItemRequest;
import cl.pokemart.pokemart_backend.dto.order.OrderRequest;
import cl.pokemart.pokemart_backend.dto.order.OrderResponse;
import cl.pokemart.pokemart_backend.dto.payment.PaymentConfirmationRequest;
import cl.pokemart.pokemart_backend.dto.payment.PaymentConfirmationResponse;
import cl.pokemart.pokemart_backend.dto.payment.PaymentPreferenceRequest;
import cl.pokemart.pokemart_backend.dto.payment.PaymentPreferenceResponse;
import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.model.payment.PaymentIntent;
import cl.pokemart.pokemart_backend.model.payment.PaymentIntentStatus;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.repository.catalog.ProductRepository;
import cl.pokemart.pokemart_backend.repository.payment.PaymentIntentRepository;
import cl.pokemart.pokemart_backend.service.order.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final MercadoPagoProperties properties;
    private final PaymentIntentRepository paymentIntentRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public PaymentService(MercadoPagoProperties properties,
                          PaymentIntentRepository paymentIntentRepository,
                          ProductRepository productRepository,
                          OrderService orderService,
                          ObjectMapper objectMapper) {
        this.properties = properties;
        this.paymentIntentRepository = paymentIntentRepository;
        this.productRepository = productRepository;
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PaymentPreferenceResponse createPreference(PaymentPreferenceRequest request, User currentUser) {
        ensureAccessToken();

        // Tomar precios actuales de la BD para evitar montos manipulados desde el frontend
        List<PreferenceItemRequest> items = request.getItems().stream().map(itemReq -> {
            Product product = productRepository.findById(itemReq.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no disponible: " + itemReq.getProductoId()));
            BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
            return PreferenceItemRequest.builder()
                    .id(String.valueOf(product.getId()))
                    .title(product.getName())
                    .quantity(itemReq.getCantidad())
                    .currencyId("CLP")
                    .unitPrice(unitPrice)
                    .build();
        }).toList();

        OrderRequest orderSnapshot = toOrderRequest(request);
        String payloadJson = serialize(orderSnapshot);

        PaymentIntent intent = PaymentIntent.builder()
                .status(PaymentIntentStatus.PENDIENTE)
                .payloadJson(payloadJson)
                .user(currentUser)
                .build();
        PaymentIntent saved = paymentIntentRepository.save(intent);

        String externalReference = String.valueOf(saved.getId());
        saved.setExternalReference(externalReference);

        var prefBuilder = PreferenceRequest.builder()
                .items(items)
                .payer(PreferencePayerRequest.builder()
                        .name(request.getNombre())
                        .surname(request.getApellido())
                        .email(request.getCorreo())
                        .build())
                .externalReference(externalReference)
                .metadata(java.util.Map.of("intentId", externalReference));

        var backUrls = buildBackUrls();
        if (backUrls != null) {
            prefBuilder.backUrls(backUrls);
            if (isHttps(backUrls.getSuccess()) && isHttps(backUrls.getFailure()) && isHttps(backUrls.getPending())) {
                prefBuilder.autoReturn("approved");
            }
        }

        if (StringUtils.hasText(properties.getNotificationUrl())) {
            prefBuilder.notificationUrl(properties.getNotificationUrl());
        }

        PreferenceRequest prefReq = prefBuilder.build();

        PreferenceClient client = new PreferenceClient();
        var preference = createPreferenceSafe(client, prefReq);

        saved.setPreferenceId(preference.getId());
        paymentIntentRepository.save(saved);

        return PaymentPreferenceResponse.builder()
                .preferenceId(preference.getId())
                .initPoint(preference.getInitPoint())
                .sandboxInitPoint(preference.getSandboxInitPoint())
                .build();
    }

    @Transactional
    public void handlePaymentNotification(Long paymentId) {
        ensureAccessToken();
        PaymentClient paymentClient = new PaymentClient();
        Payment payment = getPaymentSafe(paymentClient, paymentId);
        if (payment == null) return;

        String externalReference = payment.getExternalReference();
        if (!StringUtils.hasText(externalReference)) return;

        PaymentIntent intent = paymentIntentRepository.findByExternalReference(externalReference)
                .orElse(null);
        if (intent == null) return;

        if (PaymentIntentStatus.APROBADO.equals(intent.getStatus())) {
            return; // ya procesado
        }

        String status = payment.getStatus();
        intent.setPaymentId(String.valueOf(paymentId));

        if ("approved".equalsIgnoreCase(status)) {
            OrderResponse order = persistOrder(intent);
            intent.setOrderId(order.getId());
            intent.setStatus(PaymentIntentStatus.APROBADO);
        } else if ("rejected".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
            intent.setStatus(PaymentIntentStatus.FALLIDO);
        }

        paymentIntentRepository.save(intent);
    }

    @Transactional
    public PaymentConfirmationResponse confirmPayment(PaymentConfirmationRequest request) {
        ensureAccessToken();
        if (request == null || request.getPaymentId() == null) {
            throw new IllegalArgumentException("paymentId es requerido");
        }

        PaymentClient paymentClient = new PaymentClient();
        Payment payment = getPaymentSafe(paymentClient, request.getPaymentId());
        if (payment == null) {
            throw new IllegalArgumentException("Pago no encontrado en Mercado Pago");
        }

        PaymentIntent intent = findIntent(request.getPreferenceId(), request.getExternalReference(), payment);
        if (intent == null) {
            throw new IllegalStateException("No se encontró la intención de pago asociada");
        }

        intent.setPaymentId(String.valueOf(request.getPaymentId()));
        String status = payment.getStatus();

        if ("approved".equalsIgnoreCase(status)) {
            if (!PaymentIntentStatus.APROBADO.equals(intent.getStatus())) {
                OrderResponse order = persistOrder(intent);
                intent.setOrderId(order.getId());
                intent.setStatus(PaymentIntentStatus.APROBADO);
            }
            paymentIntentRepository.save(intent);
            return PaymentConfirmationResponse.builder()
                    .status("approved")
                    .orderId(intent.getOrderId())
                    .paymentId(intent.getPaymentId())
                    .preferenceId(intent.getPreferenceId())
                    .externalReference(intent.getExternalReference())
                    .message("Pago aprobado")
                    .build();
        }

        if ("rejected".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
            intent.setStatus(PaymentIntentStatus.FALLIDO);
            paymentIntentRepository.save(intent);
            return PaymentConfirmationResponse.builder()
                    .status("rejected")
                    .paymentId(intent.getPaymentId())
                    .preferenceId(intent.getPreferenceId())
                    .externalReference(intent.getExternalReference())
                    .message("Pago rechazado o cancelado")
                    .build();
        }

        intent.setStatus(PaymentIntentStatus.PENDIENTE);
        paymentIntentRepository.save(intent);
        return PaymentConfirmationResponse.builder()
                .status(status != null ? status.toLowerCase() : "pending")
                .paymentId(intent.getPaymentId())
                .preferenceId(intent.getPreferenceId())
                .externalReference(intent.getExternalReference())
                .message("Pago pendiente")
                .build();
    }

    private PaymentIntent findIntent(String preferenceId, String externalReference, Payment payment) {
        String pref = preferenceId;
        String extRef = externalReference;
        if (!StringUtils.hasText(extRef)) {
            extRef = payment.getExternalReference();
        }
        if (!StringUtils.hasText(pref) && payment.getMetadata() != null) {
            Object metaPref = payment.getMetadata().get("preference_id");
            if (metaPref != null) {
                pref = String.valueOf(metaPref);
            }
        }

        if (StringUtils.hasText(pref)) {
            Optional<PaymentIntent> byPref = paymentIntentRepository.findByPreferenceId(pref);
            if (byPref.isPresent()) return byPref.get();
        }
        if (StringUtils.hasText(extRef)) {
            Optional<PaymentIntent> byExt = paymentIntentRepository.findByExternalReference(extRef);
            if (byExt.isPresent()) return byExt.get();
        }
        return null;
    }

    private OrderResponse persistOrder(PaymentIntent intent) {
        PaymentPreferenceRequest paymentReq = deserialize(intent.getPayloadJson(), PaymentPreferenceRequest.class);
        if (paymentReq == null) {
            throw new IllegalStateException("No se pudo recuperar el payload de la preferencia");
        }
        OrderRequest orderRequest = toOrderRequest(paymentReq).withMetodoPago("mercado_pago");
        return orderService.createOrder(orderRequest, intent.getUser());
    }

    private OrderRequest toOrderRequest(PaymentPreferenceRequest request) {
        List<OrderItemRequest> items = request.getItems().stream()
                .map(it -> new OrderItemRequest(it.getProductoId(), it.getCantidad()))
                .toList();
        return new OrderRequest(
                request.getNombre(),
                request.getApellido(),
                request.getCorreo(),
                request.getTelefono(),
                request.getRegion(),
                request.getComuna(),
                request.getCalle(),
                request.getDepartamento(),
                request.getNotas(),
                "mercado_pago",
                request.getCostoEnvio(),
                items
        );
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el payload", e);
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            return null;
        }
    }

    private Payment getPaymentSafe(PaymentClient client, Long paymentId) {
        try {
            return client.get(paymentId);
        } catch (com.mercadopago.exceptions.MPApiException e) {
            String details = e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage();
            int status = e.getApiResponse() != null ? e.getApiResponse().getStatusCode() : 0;
            log.error("Error MP al obtener pago {} (status {}): {}", paymentId, status, details);
            throw new IllegalStateException("MP API error al obtener el pago (status " + status + "): " + details, e);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo obtener el pago desde Mercado Pago", e);
        }
    }

    private com.mercadopago.resources.preference.Preference createPreferenceSafe(PreferenceClient client, PreferenceRequest request) {
        try {
            return client.create(request);
        } catch (com.mercadopago.exceptions.MPApiException e) {
            String details = e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage();
            int status = e.getApiResponse() != null ? e.getApiResponse().getStatusCode() : 0;
            log.error("Error MP al crear preferencia (status {}): {}", status, details);
            throw new IllegalStateException("MP API error al crear la preferencia (status " + status + "): " + details, e);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo crear la preferencia en Mercado Pago", e);
        }
    }
    
    private void ensureAccessToken() {
        if (!StringUtils.hasText(properties.getAccessToken())) {
            throw new IllegalStateException("Configura el access token de Mercado Pago (sandbox) en mercadopago.access-token");
        }
        MercadoPagoConfig.setAccessToken(properties.getAccessToken());
        if (StringUtils.hasText(properties.getIntegratorId())) {
            MercadoPagoConfig.setIntegratorId(properties.getIntegratorId());
        }
    }

    private PreferenceBackUrlsRequest buildBackUrls() {
        String success = properties.getSuccessUrl();
        String failure = properties.getFailureUrl();
        String pending = properties.getPendingUrl();

        if (!StringUtils.hasText(success) || !StringUtils.hasText(failure) || !StringUtils.hasText(pending)) {
            return null;
        }

        return PreferenceBackUrlsRequest.builder()
                .success(success)
                .failure(failure)
                .pending(pending)
                .build();
    }

    private boolean isHttps(String url) {
        return StringUtils.hasText(url) && url.trim().toLowerCase().startsWith("https://");
    }

}
