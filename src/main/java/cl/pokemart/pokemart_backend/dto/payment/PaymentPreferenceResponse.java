package cl.pokemart.pokemart_backend.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Respuesta al crear preferencia de pago")
public class PaymentPreferenceResponse {
    @Schema(description = "ID de la preferencia en Mercado Pago")
    String preferenceId;
    @Schema(description = "URL de checkout (producción o sandbox)")
    String initPoint;
    @Schema(description = "URL de checkout sandbox")
    String sandboxInitPoint;
}
