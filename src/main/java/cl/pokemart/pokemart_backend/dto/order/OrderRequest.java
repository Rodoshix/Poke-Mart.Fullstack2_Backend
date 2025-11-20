package cl.pokemart.pokemart_backend.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.List;

@Value
public class OrderRequest {
    @NotBlank
    @Size(max = 120)
    String nombre;
    @NotBlank
    @Size(max = 120)
    String apellido;
    @NotBlank
    @Email
    String email;
    @Size(max = 50)
    String telefono;
    @Size(max = 120)
    String region;
    @Size(max = 120)
    String comuna;
    @NotBlank
    @Size(max = 300)
    String calle;
    @Size(max = 300)
    String departamento;
    @Size(max = 1000)
    String notas;
    @Size(max = 60)
    String paymentMethod;

    @NotEmpty
    List<@Valid OrderItemRequest> items;
}
