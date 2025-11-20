package cl.pokemart.pokemart_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class LoginRequest {
    @NotBlank
    String identifier;
    @NotBlank
    String password;
}
