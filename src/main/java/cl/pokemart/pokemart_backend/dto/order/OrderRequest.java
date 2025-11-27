package cl.pokemart.pokemart_backend.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Value;
import lombok.With;

import java.math.BigDecimal;
import java.util.List;

@Value
@With
@Schema(description = "Datos necesarios para crear una orden de compra")
public class OrderRequest {
    @Schema(description = "Nombre del cliente")
    @NotBlank
    @Size(max = 120)
    String nombre;
    @Schema(description = "Apellido del cliente")
    @NotBlank
    @Size(max = 120)
    String apellido;
    @Schema(description = "Correo de contacto")
    @NotBlank
    @Email
    String correo;
    @Schema(description = "Telefono de contacto")
    @Size(max = 50)
    String telefono;
    @Schema(description = "Region de envio")
    @Size(max = 120)
    String region;
    @Schema(description = "Comuna o ciudad de envio")
    @Size(max = 120)
    String comuna;
    @Schema(description = "Calle y numero")
    @NotBlank
    @Size(max = 300)
    String calle;
    @Schema(description = "Departamento u observaciones de direccion")
    @Size(max = 300)
    String departamento;
    @Schema(description = "Notas adicionales")
    @Size(max = 1000)
    String notas;
    @Schema(description = "Metodo de pago seleccionado")
    @Size(max = 60)
    String metodoPago;
    @Schema(description = "Costo de envio aplicado")
    @PositiveOrZero
    BigDecimal costoEnvio;

    @Schema(description = "Items del carrito", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    List<@Valid OrderItemRequest> items;
}
