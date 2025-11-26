package cl.pokemart.pokemart_backend.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class PaymentItemRequest {
    @NotNull
    Long productoId;

    @Min(1)
    Integer cantidad;
}
