package cl.pokemart.pokemart_backend.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
@Schema(description = "Payload para crear/actualizar usuarios desde admin")
public class AdminUserRequest {
    @Schema(description = "Correo del usuario")
    @NotBlank
    @Email
    @Size(max = 120)
    String email;

    @Schema(description = "Nombre de usuario")
    @NotBlank
    @Size(max = 60)
    String username;

    @Schema(description = "Contraseña (opcional en update)")
    @Size(min = 6, max = 120)
    String password;

    @Schema(description = "Rol: ADMIN, VENDEDOR o CLIENTE")
    @NotBlank
    String role; // ADMIN, VENDEDOR, CLIENTE

    @Schema(description = "Nombre real")
    @Size(max = 100)
    String nombre;
    @Schema(description = "Apellido")
    @Size(max = 100)
    String apellido;
    @Schema(description = "RUT o identificador")
    @Size(max = 20)
    String rut;
    @Schema(description = "Dirección")
    @Size(max = 300)
    String direccion;
    @Schema(description = "Región")
    @Size(max = 100)
    String region;
    @Schema(description = "Comuna o ciudad")
    @Size(max = 100)
    String comuna;
    @Schema(description = "Teléfono de contacto")
    @Size(max = 30)
    String telefono;
    @Schema(description = "Avatar en base64/URL")
    @Size(max = 4_000_000) // admite data URI/base64 (~4MB)
    String avatarUrl;
    @Schema(description = "Fecha de nacimiento (yyyy-MM-dd)")
    String fechaNacimiento; // ISO yyyy-MM-dd
    @Schema(description = "Si el usuario está activo")
    Boolean active;
}
