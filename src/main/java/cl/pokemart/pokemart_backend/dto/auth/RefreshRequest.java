package cl.pokemart.pokemart_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
@Schema(description = "Solicitud para renovar tokens a partir de un refresh token valido")
public class RefreshRequest {
    @Schema(description = "Refresh token emitido previamente")
    @NotBlank
    String refreshToken;
}
