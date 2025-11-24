package cl.pokemart.pokemart_backend.dto.auth;

import cl.pokemart.pokemart_backend.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Perfil básico del usuario autenticado")
public class Profile {
    @Schema(description = "ID del usuario")
    Long id;
    @Schema(description = "Nombre de usuario (login)")
    String username;
    @Schema(description = "Rol de la cuenta (ADMIN, VENDEDOR, CLIENTE)")
    String role;
    @Schema(description = "Nombre real")
    String nombre;
    @Schema(description = "Apellido")
    String apellido;
    @Schema(description = "Correo electrónico")
    String email;
    @Schema(description = "RUT/RUN del usuario")
    String rut;
    @Schema(description = "Dirección principal")
    String direccion;
    @Schema(description = "Región de residencia")
    String region;
    @Schema(description = "Comuna de residencia")
    String comuna;
    @Schema(description = "Fecha de nacimiento en ISO-8601 (yyyy-MM-dd)")
    String fechaNacimiento;
    @Schema(description = "Teléfono de contacto")
    String telefono;
    @Schema(description = "Avatar o foto de perfil")
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
