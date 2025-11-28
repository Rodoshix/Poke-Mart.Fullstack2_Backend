package cl.pokemart.pokemart_backend.dto.catalog;

import cl.pokemart.pokemart_backend.model.catalog.ProductOffer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Oferta administrada (admin)")
public class AdminOfferResponse {
    @Schema(description = "ID de la oferta")
    Long id;
    @Schema(description = "ID del producto")
    Long productId;
    @Schema(description = "Nombre del producto")
    String productName;
    @Schema(description = "Porcentaje de descuento")
    Integer discountPct;
    @Schema(description = "Fecha/hora de fin (ISO-8601)")
    String endsAt;
    @Schema(description = "Si está activa")
    Boolean active;
    @Schema(description = "Indicador de expiración")
    Boolean expired;

    public static AdminOfferResponse from(ProductOffer offer) {
        var product = offer.getProduct();
        boolean expired = offer.isExpired();
        return AdminOfferResponse.builder()
                .id(offer.getId())
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)
                .discountPct(offer.getDiscountPct())
                .endsAt(offer.getEndsAt() != null ? offer.getEndsAt().toString() : null)
                .active(offer.getActive())
                .expired(expired)
                .build();
    }
}
