package cl.pokemart.pokemart_backend.dto.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Schema(description = "Datos para crear o actualizar un producto")
public class ProductRequest {
    @Schema(description = "Nombre comercial del producto")
    @NotBlank
    @Size(max = 200)
    String nombre;
    @Schema(description = "Descripcion corta del producto")
    @Size(max = 1000)
    String descripcion;
    @Schema(description = "Precio de venta (CLP)")
    @NotNull
    BigDecimal precio;
    @Schema(description = "Stock disponible")
    @NotNull
    @Min(0)
    Integer stock;
    @Schema(description = "Stock base o de referencia")
    @Min(0)
    Integer stockBase;
    @Schema(description = "URL o data URL de la imagen principal")
    @Size(max = 1000)
    String imagenUrl;
    @Schema(description = "Slug de la categoria")
    @NotBlank
    @Size(max = 120)
    String categoriaSlug;
}

