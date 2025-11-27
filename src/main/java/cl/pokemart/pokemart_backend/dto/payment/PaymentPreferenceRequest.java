package cl.pokemart.pokemart_backend.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Schema(description = "Preferencia de pago para Mercado Pago, incluye datos de env?o y items")
public class PaymentPreferenceRequest {
    @Schema(description = "Titulo de la preferencia")
    @NotBlank
    String title;

    @Schema(description = "Moneda (ej: CLP)")
    @NotBlank
    String currency;

    @Schema(description = "URL de retorno al front")
    @NotBlank
    String backUrl;

    @Schema(description = "Nombre del comprador")
    @NotBlank
    @Size(max = 120)
    String nombre;

    @Schema(description = "Apellido del comprador")
    @NotBlank
    @Size(max = 120)
    String apellido;

    @Schema(description = "Correo del comprador")
    @NotBlank
    @Email
    @Size(max = 180)
    String correo;

    @Schema(description = "Telefono de contacto")
    @Size(max = 60)
    String telefono;

    @Schema(description = "Region de envio")
    @Size(max = 120)
    String region;

    @Schema(description = "Comuna de envio")
    @Size(max = 120)
    String comuna;

    @Schema(description = "Calle y numero")
    @NotBlank
    @Size(max = 300)
    String calle;

    @Schema(description = "Departamento u observacion de direccion")
    @Size(max = 300)
    String departamento;

    @Schema(description = "Notas adicionales")
    @Size(max = 1000)
    String notas;

    @Schema(description = "Costo de envio aplicado")
    @NotNull
    BigDecimal costoEnvio;

    @Schema(description = "Items incluidos en la preferencia")
    @NotEmpty
    List<@Valid PaymentItemRequest> items;

    @Schema(description = "Descuento aplicado (opcional)")
    @Size(max = 120)
    String coupon;
}
