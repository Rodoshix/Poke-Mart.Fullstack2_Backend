package cl.pokemart.pokemart_backend.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
@Schema(description = "Item incluido en la preferencia de pago")
public class PaymentItemRequest {
    @Schema(description = "ID del producto")
    @NotNull
    Long productoId;

    @Schema(description = "Cantidad solicitada")
    @Min(1)
    Integer cantidad;
}
