package cl.pokemart.pokemart_backend.controller.payment;

import cl.pokemart.pokemart_backend.dto.payment.PaymentConfirmationRequest;
import cl.pokemart.pokemart_backend.dto.payment.PaymentConfirmationResponse;
import cl.pokemart.pokemart_backend.dto.payment.PaymentPreferenceRequest;
import cl.pokemart.pokemart_backend.dto.payment.PaymentPreferenceResponse;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/mp")
@Tag(name = "Payments", description = "Integración con Mercado Pago")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Crear preferencia de Mercado Pago", description = "Genera un init_point en modo sandbox para iniciar el flujo de pago.")
    @ApiResponse(responseCode = "200", description = "Preference creada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentPreferenceResponse.class)))
    @PostMapping("/preference")
    public PaymentPreferenceResponse createPreference(@Valid @RequestBody PaymentPreferenceRequest request,
                                                      Authentication authentication) {
        User user = authentication != null && authentication.getPrincipal() instanceof User principal ? principal : null;
        return paymentService.createPreference(request, user);
    }

    @Operation(summary = "Webhook Mercado Pago", description = "Recibe notificaciones de pagos y crea la orden cuando el pago es aprobado.")
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestParam(name = "type", required = false) String type,
                                        @RequestParam(name = "data.id", required = false) Long dataId,
                                        @RequestBody(required = false) Map<String, Object> body) {
        if (dataId != null) {
            paymentService.handlePaymentNotification(dataId);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/webhook")
    public ResponseEntity<Void> webhookGet(@RequestParam(name = "type", required = false) String type,
                                           @RequestParam(name = "data.id", required = false) Long dataId) {
        if (dataId != null) {
            paymentService.handlePaymentNotification(dataId);
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Confirmar pago (retorno)", description = "Consulta el pago en Mercado Pago y crea la orden si el pago fue aprobado.")
    @PostMapping("/confirm")
    public PaymentConfirmationResponse confirm(@Valid @RequestBody PaymentConfirmationRequest request) {
        return paymentService.confirmPayment(request);
    }
}
