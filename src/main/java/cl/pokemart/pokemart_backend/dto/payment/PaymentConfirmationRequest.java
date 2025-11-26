package cl.pokemart.pokemart_backend.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class PaymentConfirmationRequest {
    @NotNull
    Long paymentId;
    String preferenceId;
    String externalReference;
}
