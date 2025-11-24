package cl.pokemart.pokemart_backend.dto.blog;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlogStatusRequest {
    @NotBlank
    private String estado;
}
