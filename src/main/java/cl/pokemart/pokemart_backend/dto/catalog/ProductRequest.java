package cl.pokemart.pokemart_backend.dto.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class ProductRequest {
    @NotBlank
    @Size(max = 200)
    String nombre;
    @Size(max = 1000)
    String descripcion;
    @NotNull
    BigDecimal precio;
    @NotNull
    @Min(0)
    Integer stock;
    @Min(0)
    Integer stockBase;
    String imagenUrl;
    @NotBlank
    @Size(max = 120)
    String categoriaSlug;
}
