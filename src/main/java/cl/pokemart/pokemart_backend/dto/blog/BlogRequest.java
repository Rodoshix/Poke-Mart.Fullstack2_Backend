package cl.pokemart.pokemart_backend.dto.blog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BlogRequest {

    @NotBlank
    @Size(max = 180)
    private String titulo;

    @NotBlank
    @Size(max = 480)
    private String descripcion;

    private String contenido;

    @Size(max = 120)
    private String categoria;

    @Size(max = 500)
    private String imagen;

    private List<String> etiquetas;

    /**
     * Estado esperado: DRAFT o PUBLISHED. Si es nulo, se mantiene el actual.
     */
    private String estado;
}
