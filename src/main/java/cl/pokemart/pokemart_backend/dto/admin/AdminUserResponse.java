package cl.pokemart.pokemart_backend.dto.admin;

import cl.pokemart.pokemart_backend.model.user.User;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminUserResponse {
    Long id;
    String username;
    String email;
    String role;
    Boolean active;
    String createdAt;
    String nombre;
    String apellido;
    String rut;
    String direccion;
    String region;
    String comuna;
    String fechaNacimiento;
    String telefono;
    String avatarUrl;

    public static AdminUserResponse from(User user) {
        var profile = user.getProfile();
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .active(user.getActive())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .nombre(profile != null ? profile.getNombre() : null)
                .apellido(profile != null ? profile.getApellido() : null)
                .rut(profile != null ? profile.getRut() : null)
                .direccion(profile != null ? profile.getDireccion() : null)
                .region(profile != null ? profile.getRegion() : null)
                .comuna(profile != null ? profile.getComuna() : null)
                .fechaNacimiento(profile != null && profile.getFechaNacimiento() != null
                        ? profile.getFechaNacimiento().toString()
                        : null)
                .telefono(profile != null ? profile.getTelefono() : null)
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
