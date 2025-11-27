package cl.pokemart.pokemart_backend.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
@Schema(description = "Actualizacion de orden por parte de un administrador")
public class AdminOrderUpdateRequest {
    @Size(max = 300)
    @Schema(description = "Referencia o tracking del envio", example = "CL123456789PE")
    String referenciaEnvio;

    @Size(max = 1000)
    @Schema(description = "Notas internas o comentario al cliente", example = "Despacho programado para manana")
    String notas;

    @Size(max = 20)
    @Schema(description = "Estado nuevo de la orden", example = "SHIPPED")
    String estado; // debe ser uno de OrderStatus
}
