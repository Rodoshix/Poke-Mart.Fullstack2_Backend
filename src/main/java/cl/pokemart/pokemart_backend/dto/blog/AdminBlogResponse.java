package cl.pokemart.pokemart_backend.dto.blog;

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
public class AdminBlogResponse {
    private Long id;
    private String slug;
    private String titulo;
    private String descripcion;
    private String contenido;
    private String categoria;
    private List<String> etiquetas;
    private String imagen;
    private String estado;
    private String autor;
    private LocalDateTime fechaPublicacion;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
