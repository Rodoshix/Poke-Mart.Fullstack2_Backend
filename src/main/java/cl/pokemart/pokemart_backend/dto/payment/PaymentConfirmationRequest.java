package cl.pokemart.pokemart_backend.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
@Schema(description = "Solicitud para confirmar un pago de Mercado Pago")
public class PaymentConfirmationRequest {
    @Schema(description = "ID del pago en Mercado Pago")
    @NotNull
    Long paymentId;
    @Schema(description = "ID de la preferencia (opcional)")
    String preferenceId;
    @Schema(description = "Referencia externa de la orden")
    String externalReference;
}
