package cl.pokemart.pokemart_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
@Schema(description = "Credenciales para iniciar sesion (email/username + password)")
public class LoginRequest {
    @Schema(description = "Email o username del usuario")
    @NotBlank
    String identifier;
    @Schema(description = "Contrasena del usuario")
    @NotBlank
    String password;
}
