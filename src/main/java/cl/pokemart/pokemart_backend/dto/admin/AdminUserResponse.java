package cl.pokemart.pokemart_backend.dto.admin;

import cl.pokemart.pokemart_backend.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Respuesta para usuarios gestionados desde admin")
public class AdminUserResponse {
    @Schema(description = "ID del usuario")
    Long id;
    @Schema(description = "Nombre de usuario")
    String username;
    @Schema(description = "Correo")
    String email;
    @Schema(description = "Rol asignado")
    String role;
    @Schema(description = "Si está activo")
    Boolean active;
    @Schema(description = "Fecha de creación")
    String createdAt;
    @Schema(description = "Nombre")
    String nombre;
    @Schema(description = "Apellido")
    String apellido;
    @Schema(description = "RUT o identificador")
    String rut;
    @Schema(description = "Dirección")
    String direccion;
    @Schema(description = "Región")
    String region;
    @Schema(description = "Comuna")
    String comuna;
    @Schema(description = "Fecha de nacimiento")
    String fechaNacimiento;
    @Schema(description = "Teléfono")
    String telefono;
    @Schema(description = "Avatar URL/base64")
    String avatarUrl;
    @Schema(description = "Último inicio de sesión")
    String lastLoginAt;

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
                .lastLoginAt(user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null)
                .build();
    }
}
