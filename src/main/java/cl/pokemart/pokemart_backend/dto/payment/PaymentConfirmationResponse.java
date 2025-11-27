package cl.pokemart.pokemart_backend.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Resultado de la confirmación de pago")
public class PaymentConfirmationResponse {
    @Schema(description = "Estado del pago/orden")
    String status;
    @Schema(description = "ID de la orden creada")
    Long orderId;
    @Schema(description = "ID del pago (string) en MP")
    String paymentId;
    @Schema(description = "ID de la preferencia asociada")
    String preferenceId;
    @Schema(description = "Referencia externa de la orden")
    String externalReference;
    @Schema(description = "Mensaje descriptivo")
    String message;
}
