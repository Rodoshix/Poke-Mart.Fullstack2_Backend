package cl.pokemart.pokemart_backend.dto.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
@Schema(description = "Solicitud para crear/actualizar ofertas")
public class AdminOfferRequest {
    @Schema(description = "ID del producto al que aplica la oferta")
    @NotNull
    Long productId;
    @Schema(description = "Porcentaje de descuento (1-99)")
    @NotNull
    @Min(1)
    @Max(99)
    Integer discountPct;
    @Schema(description = "Fecha de expiración ISO-8601")
    String endsAt; // ISO-8601 (yyyy-MM-dd'T'HH:mm[:ss])
    @Schema(description = "Si la oferta está activa")
    Boolean active;
}
