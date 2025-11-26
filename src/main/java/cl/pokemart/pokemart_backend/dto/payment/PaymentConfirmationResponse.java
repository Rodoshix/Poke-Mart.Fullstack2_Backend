package cl.pokemart.pokemart_backend.dto.payment;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentConfirmationResponse {
    String status;
    Long orderId;
    String paymentId;
    String preferenceId;
    String externalReference;
    String message;
}
