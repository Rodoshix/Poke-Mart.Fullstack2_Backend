package cl.pokemart.pokemart_backend.service.payment;

import cl.pokemart.pokemart_backend.config.MercadoPagoProperties;
import cl.pokemart.pokemart_backend.dto.order.OrderItemRequest;
import cl.pokemart.pokemart_backend.dto.order.OrderRequest;
import cl.pokemart.pokemart_backend.dto.order.OrderResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
@Service
public class PaymentService {

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

        PreferenceRequest prefReq = PreferenceRequest.builder()
                .items(items)
                .payer(PreferencePayerRequest.builder()
                        .name(request.getNombre())
                        .surname(request.getApellido())
                        .email(request.getCorreo())
                        .build())
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(properties.getSuccessUrl())
                        .failure(properties.getFailureUrl())
                        .pending(properties.getPendingUrl())
                        .build())
                .autoReturn("approved")
                .notificationUrl(properties.getNotificationUrl())
                .externalReference(externalReference)
                .metadata(java.util.Map.of("intentId", externalReference))
                .build();

        PreferenceClient client = new PreferenceClient();
        var preference = client.create(prefReq);

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
        Payment payment = paymentClient.get(paymentId);
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

    private void ensureAccessToken() {
        if (!StringUtils.hasText(properties.getAccessToken())) {
            throw new IllegalStateException("Configura el access token de Mercado Pago (sandbox) en mercadopago.access-token");
        }
        MercadoPagoConfig.setAccessToken(properties.getAccessToken());
        if (StringUtils.hasText(properties.getIntegratorId())) {
            MercadoPagoConfig.setIntegratorId(properties.getIntegratorId());
        }
    }
}
