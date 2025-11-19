package cl.pokemart.pokemart_backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class RegisterRequest {
    @Email
    @NotBlank
    String email;
    @NotBlank
    @Size(min = 3, max = 60)
    String username;
    @NotBlank
    @Size(min = 6, max = 100)
    String password;
    String nombre;
    String apellido;
}
