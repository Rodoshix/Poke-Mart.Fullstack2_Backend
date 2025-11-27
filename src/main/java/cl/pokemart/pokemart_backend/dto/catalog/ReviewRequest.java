package cl.pokemart.pokemart_backend.dto.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
@Schema(description = "Solicitud para crear una reseña de producto")
public class ReviewRequest {
    @Schema(description = "Rating (1-5)")
    @NotNull
    @Min(1)
    @Max(5)
    Integer rating;

    @Schema(description = "Comentario de la reseña")
    @NotBlank
    @Size(min = 4, max = 1200)
    String comment;
}
