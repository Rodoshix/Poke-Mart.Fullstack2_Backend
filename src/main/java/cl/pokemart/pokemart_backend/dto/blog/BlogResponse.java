package cl.pokemart.pokemart_backend.dto.blog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Articulo de blog disponible para el cliente")
public class BlogResponse {
    @Schema(description = "Identificador interno", example = "12")
    private Long id;

    @Schema(description = "Slug publico del post", example = "evento-galar-2025")
    private String slug;

    @Schema(description = "Titulo del post", example = "Nuevo evento en Galar")
    private String titulo;

    @Schema(description = "Resumen corto", example = "Descubre los nuevos retos y recompensas.")
    private String descripcion;

    @Schema(description = "Contenido HTML/markdown del articulo")
    private String contenido;

    @Schema(description = "Categoria asignada", example = "eventos")
    private String categoria;

    @Schema(description = "Etiquetas del post", example = "[\"galar\",\"evento\",\"recompensas\"]")
    private List<String> etiquetas;

    @Schema(description = "URL de la imagen destacada", example = "https://cdn.pokemart.com/blogs/galar.jpg")
    private String imagen;

    @Schema(description = "Estado actual", example = "PUBLISHED")
    private String estado;

    @Schema(description = "Autor visible", example = "Prof. Oak")
    private String autor;

    @Schema(description = "Fecha de publicacion", example = "2025-11-27T08:30:00")
    private LocalDateTime fechaPublicacion;
}
