package cl.pokemart.pokemart_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
@Schema(description = "Datos para actualizar el perfil del usuario autenticado")
public class ProfileUpdateRequest {
    @Schema(description = "Correo del usuario")
    @Email
    @Size(max = 120)
    String email;

    @Schema(description = "Username")
    @Size(min = 3, max = 60)
    String username;

    @Schema(description = "Contrasena nueva (opcional)")
    @Size(min = 6, max = 120)
    String password;

    @Schema(description = "Nombre")
    @Size(max = 100)
    String nombre;

    @Schema(description = "Apellido")
    @Size(max = 100)
    String apellido;

    @Schema(description = "RUT o identificador")
    @Size(max = 20)
    String rut;

    @Schema(description = "Direccion")
    @Size(max = 300)
    String direccion;

    @Schema(description = "Region")
    @Size(max = 100)
    String region;

    @Schema(description = "Comuna o ciudad")
    @Size(max = 100)
    String comuna;

    @Schema(description = "Telefono de contacto")
    @Size(max = 30)
    String telefono;

    @Schema(description = "Avatar en base64/URL")
    @Size(max = 4_000_000) // admite data URI/base64 (~4MB)
    String avatarUrl;

    // ISO yyyy-MM-dd
    @Schema(description = "Fecha de nacimiento (yyyy-MM-dd)")
    @Size(max = 20)
    String fechaNacimiento;
}
