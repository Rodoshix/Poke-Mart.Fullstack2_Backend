package cl.pokemart.pokemart_backend.auth;

import cl.pokemart.pokemart_backend.user.User;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Profile {
    Long id;
    String username;
    String role;
    String nombre;
    String apellido;
    String email;
    String avatarUrl;

    public static Profile fromUser(User user) {
        return Profile.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .nombre(user.getFirstName())
                .apellido(user.getLastName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
