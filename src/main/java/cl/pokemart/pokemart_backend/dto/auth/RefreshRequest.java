package cl.pokemart.pokemart_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class RefreshRequest {
    @NotBlank
    String refreshToken;
}
