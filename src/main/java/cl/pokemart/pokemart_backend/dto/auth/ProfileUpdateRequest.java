package cl.pokemart.pokemart_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class ProfileUpdateRequest {
    @Email
    @Size(max = 120)
    String email;

    @Size(min = 3, max = 60)
    String username;

    @Size(min = 6, max = 120)
    String password;

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

    @Size(max = 4_000_000) // admite data URI/base64 (~4MB)
    String avatarUrl;

    // ISO yyyy-MM-dd
    @Size(max = 20)
    String fechaNacimiento;
}
