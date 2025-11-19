package cl.pokemart.pokemart_backend.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class RefreshRequest {
    @NotBlank
    String refreshToken;
}
