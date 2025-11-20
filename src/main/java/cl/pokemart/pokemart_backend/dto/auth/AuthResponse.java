package cl.pokemart.pokemart_backend.dto.auth;

import cl.pokemart.pokemart_backend.model.user.User;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String token;
    String refreshToken;
    long expiresAt;
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
