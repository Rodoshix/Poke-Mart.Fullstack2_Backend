package cl.pokemart.pokemart_backend.dto.catalog;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ProductResponse {
    Long id;
    String nombre;
    String descripcion;
    BigDecimal precio;
    Integer stock;
    String imagenUrl;
    String categoria;
    OfferInfo offer;
    String vendedor;

    @Value
    @Builder
    public static class OfferInfo {
        Integer discountPct;
        String endsAt;
    }
}
