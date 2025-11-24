package cl.pokemart.pokemart_backend.controller.admin;

import cl.pokemart.pokemart_backend.dto.blog.AdminBlogResponse;
import cl.pokemart.pokemart_backend.dto.blog.BlogRequest;
import cl.pokemart.pokemart_backend.dto.blog.BlogStatusRequest;
import cl.pokemart.pokemart_backend.model.blog.BlogStatus;
import cl.pokemart.pokemart_backend.service.blog.BlogService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/blogs")
@Tag(name = "Admin - Blogs", description = "Gestión de blogs (admin)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBlogController {

    private final BlogService blogService;

    public AdminBlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @Operation(summary = "Listado admin de blogs", description = "Filtra por categoría, estado o texto.")
    @ApiResponse(responseCode = "200", description = "Listado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminBlogResponse.class)))
    @GetMapping
    public List<AdminBlogResponse> list(
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "q", required = false) String query
    ) {
        return blogService.listAdmin(categoria, estado, query);
    }

    @Operation(summary = "Crear blog", description = "Crea una nueva entrada de blog.")
    @PostMapping
    public AdminBlogResponse create(@Valid @RequestBody BlogRequest request) {
        return blogService.create(request);
    }

    @Operation(summary = "Actualizar blog", description = "Modifica título, contenido, estado e imagen.")
    @PutMapping("/{id}")
    public AdminBlogResponse update(@PathVariable Long id, @Valid @RequestBody BlogRequest request) {
        return blogService.update(id, request);
    }

    @Operation(summary = "Actualizar estado de blog", description = "Cambia el estado (PUBLISHED, DRAFT, HIDDEN).")
    @PatchMapping("/{id}/estado")
    public AdminBlogResponse updateStatus(@PathVariable Long id, @Valid @RequestBody BlogStatusRequest request) {
        try {
            BlogStatus status = BlogStatus.valueOf(request.getEstado().trim().toUpperCase());
            return blogService.updateStatus(id, status);
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Estado inválido");
        }
    }

    @Operation(summary = "Eliminar blog", description = "Elimina una entrada de blog por ID.")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        blogService.delete(id);
    }
}
