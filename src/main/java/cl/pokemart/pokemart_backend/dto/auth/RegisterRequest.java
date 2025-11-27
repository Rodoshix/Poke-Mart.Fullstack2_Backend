package cl.pokemart.pokemart_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
@Schema(description = "Datos para registrar un nuevo cliente")
public class RegisterRequest {
    @Schema(description = "Correo del usuario")
    @Email
    @NotBlank
    String email;
    @Schema(description = "Nombre de usuario a registrar")
    @NotBlank
    @Size(min = 3, max = 60)
    String username;
    @Schema(description = "Contrasena de acceso")
    @NotBlank
    @Size(min = 6, max = 100)
    String password;
    @Schema(description = "Nombre real")
    String nombre;
    @Schema(description = "Apellido")
    String apellido;
    @Schema(description = "Rut o identificador")
    String rut;
    @Schema(description = "Direccion del usuario")
    String direccion;
    @Schema(description = "Region del usuario")
    String region;
    @Schema(description = "Comuna o ciudad")
    String comuna;
    @Schema(description = "Fecha de nacimiento (yyyy-MM-dd)")
    String fechaNacimiento; // ISO date string
    @Schema(description = "Telefono de contacto")
    String telefono;
}
