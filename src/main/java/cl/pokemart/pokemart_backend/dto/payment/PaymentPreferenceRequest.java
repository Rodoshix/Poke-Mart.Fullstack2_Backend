package cl.pokemart.pokemart_backend.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Schema(description = "Preferencia de pago para Mercado Pago")
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
    String buyerName;
    @Schema(description = "Correo del comprador")
    @NotBlank
    String buyerEmail;
    @Schema(description = "Total a pagar")
    @NotNull
    BigDecimal total;
    @Schema(description = "Items incluidos en la preferencia")
    @NotEmpty
    List<@Valid PaymentItemRequest> items;
    @Schema(description = "Descuento aplicado (opcional)")
    @Size(max = 120)
    String coupon;
}
