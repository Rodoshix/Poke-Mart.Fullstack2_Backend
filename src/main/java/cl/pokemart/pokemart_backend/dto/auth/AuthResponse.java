package cl.pokemart.pokemart_backend.dto.auth;

import cl.pokemart.pokemart_backend.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Respuesta de autenticación (JWT + perfil)")
public class AuthResponse {
    @Schema(description = "Token JWT de acceso")
    String token;
    @Schema(description = "Token de refresco para renovar sesión")
    String refreshToken;
    @Schema(description = "Fecha de expiración del token (epoch millis)")
    long expiresAt;
    @Schema(description = "Perfil del usuario autenticado")
    Profile profile;

    public static AuthResponse of(String token, long expiresAt, String refreshToken, User user) {
        return AuthResponse.builder()
                .token(token)
                .expiresAt(expiresAt)
                .refreshToken(refreshToken)
                .profile(Profile.fromUser(user))
                .build();
    }
}
