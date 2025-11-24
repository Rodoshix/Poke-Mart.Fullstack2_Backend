package cl.pokemart.pokemart_backend.dto.catalog;

import cl.pokemart.pokemart_backend.model.catalog.ProductOffer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminOfferResponse {
    Long id;
    Long productId;
    String productName;
    Integer discountPct;
    String endsAt;
    Boolean active;
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
