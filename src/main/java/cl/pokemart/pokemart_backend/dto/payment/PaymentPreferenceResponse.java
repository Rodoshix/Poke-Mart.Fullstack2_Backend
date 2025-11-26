package cl.pokemart.pokemart_backend.dto.payment;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentPreferenceResponse {
    String preferenceId;
    String initPoint;
    String sandboxInitPoint;
}
