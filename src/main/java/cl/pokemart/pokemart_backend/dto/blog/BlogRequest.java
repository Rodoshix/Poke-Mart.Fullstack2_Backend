package cl.pokemart.pokemart_backend.dto.blog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Datos para crear o actualizar un post de blog")
public class BlogRequest {

    @NotBlank
    @Size(max = 180)
    @Schema(description = "Titulo del post", example = "Nuevo evento en Galar", requiredMode = Schema.RequiredMode.REQUIRED)
    private String titulo;

    @NotBlank
    @Size(max = 480)
    @Schema(description = "Resumen del articulo", example = "Descubre los nuevos retos y recompensas.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String descripcion;

    @Schema(description = "Contenido completo en HTML o markdown")
    private String contenido;

    @Size(max = 120)
    @Schema(description = "Categoria asignada", example = "eventos")
    private String categoria;

    @Size(max = 500)
    @Schema(description = "URL de la imagen destacada", example = "https://cdn.pokemart.com/blogs/galar.jpg")
    private String imagen;

    @Schema(description = "Etiquetas asociadas", example = "[\"galar\",\"evento\",\"recompensas\"]")
    private List<String> etiquetas;

    /**
     * Estado esperado: DRAFT o PUBLISHED. Si es nulo, se mantiene el actual.
     */
    @Schema(description = "Estado del post", example = "DRAFT")
    private String estado;
}
