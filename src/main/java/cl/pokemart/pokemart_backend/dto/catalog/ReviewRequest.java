package cl.pokemart.pokemart_backend.dto.catalog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class ReviewRequest {
    @NotNull
    @Min(1)
    @Max(5)
    Integer rating;

    @NotBlank
    @Size(min = 4, max = 1200)
    String comment;
}
