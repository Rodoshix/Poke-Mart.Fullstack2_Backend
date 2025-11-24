package cl.pokemart.pokemart_backend.dto.order;

import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class AdminOrderUpdateRequest {
    @Size(max = 300)
    String referenciaEnvio;

    @Size(max = 1000)
    String notas;

    @Size(max = 20)
    String estado; // debe ser uno de OrderStatus
}
