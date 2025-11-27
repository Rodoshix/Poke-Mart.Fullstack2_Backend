package cl.pokemart.pokemart_backend.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
@Schema(description = "Item solicitado dentro de una orden")
public class OrderItemRequest {
    @NotNull
    @Schema(description = "ID del producto", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    Long productoId;

    @NotNull
    @Min(1)
    @Schema(description = "Cantidad solicitada", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer cantidad;
}
