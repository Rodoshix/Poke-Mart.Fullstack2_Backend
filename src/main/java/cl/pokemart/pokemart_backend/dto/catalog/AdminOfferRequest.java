package cl.pokemart.pokemart_backend.dto.catalog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class AdminOfferRequest {
    @NotNull
    Long productId;
    @NotNull
    @Min(1)
    @Max(99)
    Integer discountPct;
    String endsAt; // ISO-8601 (yyyy-MM-dd'T'HH:mm[:ss])
    Boolean active;
}
