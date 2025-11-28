package cl.pokemart.pokemart_backend.dto.blog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Cambio de estado de un post de blog")
public class BlogStatusRequest {
    @NotBlank
    @Schema(description = "Nuevo estado del post", example = "PUBLISHED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String estado;
}
