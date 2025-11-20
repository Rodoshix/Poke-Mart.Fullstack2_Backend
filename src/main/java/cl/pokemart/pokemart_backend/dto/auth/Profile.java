package cl.pokemart.pokemart_backend.dto.auth;

import cl.pokemart.pokemart_backend.model.user.User;
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
    String rut;
    String direccion;
    String region;
    String comuna;
    String fechaNacimiento;
    String telefono;
    String avatarUrl;

    public static Profile fromUser(User user) {
        var prof = user.getProfile();
        return Profile.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .nombre(prof != null ? prof.getNombre() : null)
                .apellido(prof != null ? prof.getApellido() : null)
                .email(user.getEmail())
                .rut(prof != null ? prof.getRut() : null)
                .direccion(prof != null ? prof.getDireccion() : null)
                .region(prof != null ? prof.getRegion() : null)
                .comuna(prof != null ? prof.getComuna() : null)
                .fechaNacimiento(prof != null && prof.getFechaNacimiento() != null
                        ? prof.getFechaNacimiento().toString()
                        : null)
                .telefono(prof != null ? prof.getTelefono() : null)
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
