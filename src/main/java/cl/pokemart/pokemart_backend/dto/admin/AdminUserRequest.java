package cl.pokemart.pokemart_backend.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class AdminUserRequest {
    @NotBlank
    @Email
    @Size(max = 120)
    String email;

    @NotBlank
    @Size(max = 60)
    String username;

    @Size(min = 6, max = 120)
    String password;

    @NotBlank
    String role; // ADMIN, VENDEDOR, CLIENTE

    @Size(max = 100)
    String nombre;
    @Size(max = 100)
    String apellido;
    @Size(max = 20)
    String rut;
    @Size(max = 300)
    String direccion;
    @Size(max = 100)
    String region;
    @Size(max = 100)
    String comuna;
    @Size(max = 30)
    String telefono;
    @Size(max = 200)
    String avatarUrl;
    String fechaNacimiento; // ISO yyyy-MM-dd
    Boolean active;
}
