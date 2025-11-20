package cl.pokemart.pokemart_backend.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class OrderItemRequest {
    @NotNull
    Long productId;
    @NotNull
    @Min(1)
    Integer quantity;
}
