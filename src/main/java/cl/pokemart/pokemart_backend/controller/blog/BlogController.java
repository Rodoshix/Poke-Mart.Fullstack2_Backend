package cl.pokemart.pokemart_backend.controller.blog;

import cl.pokemart.pokemart_backend.dto.blog.BlogResponse;
import cl.pokemart.pokemart_backend.service.blog.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blogs")
@Tag(name = "Blog", description = "Entradas de blog públicas")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @Operation(summary = "Lista blogs", description = "Listado paginado/filtrado de entradas públicas.")
    @ApiResponse(responseCode = "200", description = "Listado de blogs",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BlogResponse.class)))
    @GetMapping
    public List<BlogResponse> list(
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "q", required = false) String query
    ) {
        return blogService.listPublic(categoria, query);
    }

    @Operation(summary = "Detalle de blog", description = "Devuelve el detalle público de una entrada por su slug.")
    @ApiResponse(responseCode = "200", description = "Entrada encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BlogResponse.class)))
    @GetMapping("/{slug}")
    public BlogResponse detail(@PathVariable String slug) {
        return blogService.getPublic(slug);
    }
}
