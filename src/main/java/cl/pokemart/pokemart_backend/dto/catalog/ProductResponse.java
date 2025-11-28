package cl.pokemart.pokemart_backend.dto.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
@Schema(description = "Detalle de un producto publicado en el catálogo")
public class ProductResponse {
    @Schema(description = "Identificador del producto")
    Long id;
    @Schema(description = "Nombre comercial del producto")
    String nombre;
    @Schema(description = "Descripción corta del producto")
    String descripcion;
    @Schema(description = "Precio de venta (CLP)")
    BigDecimal precio;
    @Schema(description = "Stock disponible actual")
    Integer stock;
    @Schema(description = "Stock base o de referencia para reportes")
    Integer stockBase;
    @Schema(description = "URL absoluta o relativa de la imagen principal")
    String imagenUrl;
    @Schema(description = "Categoría legible del producto")
    String categoria;
    @Schema(description = "Información de oferta activa, si existe")
    OfferInfo offer;
    @Schema(description = "Nombre del vendedor o tienda")
    String vendedor;
    @Schema(description = "Cantidad de reseñas registradas")
    Integer reviewCount;
    @Schema(description = "Promedio de reseñas (1-5)")
    Double reviewAvg;
    @Schema(description = "Indica si el producto está activo/visible")
    Boolean active;

    @Value
    @Builder
    @Schema(description = "Oferta aplicada al producto")
    public static class OfferInfo {
        @Schema(description = "Porcentaje de descuento aplicado (0-100)")
        Integer discountPct;
        @Schema(description = "Fecha/hora de expiración de la oferta en ISO-8601 (opcional)")
        String endsAt;
    }
}
