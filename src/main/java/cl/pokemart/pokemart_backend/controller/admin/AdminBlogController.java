package cl.pokemart.pokemart_backend.controller.admin;

import cl.pokemart.pokemart_backend.dto.blog.AdminBlogResponse;
import cl.pokemart.pokemart_backend.dto.blog.BlogRequest;
import cl.pokemart.pokemart_backend.dto.blog.BlogStatusRequest;
import cl.pokemart.pokemart_backend.dto.common.ApiErrorExamples;
import cl.pokemart.pokemart_backend.dto.common.ErrorResponse;
import cl.pokemart.pokemart_backend.model.blog.BlogStatus;
import cl.pokemart.pokemart_backend.service.blog.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/blogs")
@Tag(name = "Admin - Blogs", description = "Gestión de blogs (admin)")
@PreAuthorize("hasRole('ADMIN')")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Categoria invalida", value = ApiErrorExamples.BLOG_BAD_REQUEST)
        })),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Sin token", value = """
                        {
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "No autenticado",
                          "path": "/api/v1/admin/blogs",
                          "timestamp": "2025-11-27T10:15:30Z"
                        }
                        """)
        })),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Rol insuficiente", value = ApiErrorExamples.OFFER_FORBIDDEN)
        })),
        @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Blog no existe", value = ApiErrorExamples.BLOG_NOT_FOUND)
        })),
        @ApiResponse(responseCode = "409", description = "Conflicto o regla de negocio", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Slug duplicado", value = """
                        {
                          "status": 409,
                          "error": "Conflict",
                          "message": "Ya existe un blog con ese titulo/slug",
                          "path": "/api/v1/admin/blogs",
                          "timestamp": "2025-11-27T10:15:30Z"
                        }
                        """)
        })),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Fallo interno", value = ApiErrorExamples.PUBLIC_OFFERS_ERROR)
        }))
})
public class AdminBlogController {

    private final BlogService blogService;

    public AdminBlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @Operation(summary = "Listado admin de blogs", description = "Filtra por categoría, estado o texto.")
    @ApiResponse(responseCode = "200", description = "Listado de blogs",
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
    @ApiResponse(responseCode = "201", description = "Blog creado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminBlogResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminBlogResponse create(@Valid @RequestBody BlogRequest request) {
        return blogService.create(request);
    }

    @Operation(summary = "Actualizar blog", description = "Modifica título, contenido, estado e imagen.")
    @ApiResponse(responseCode = "200", description = "Blog actualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminBlogResponse.class)))
    @PutMapping("/{id}")
    public AdminBlogResponse update(@PathVariable Long id, @Valid @RequestBody BlogRequest request) {
        return blogService.update(id, request);
    }

    @Operation(summary = "Actualizar estado de blog", description = "Cambia el estado (PUBLISHED, DRAFT, HIDDEN).")
    @ApiResponse(responseCode = "200", description = "Estado actualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminBlogResponse.class)))
    @PatchMapping("/{id}/estado")
    public AdminBlogResponse updateStatus(@PathVariable Long id, @Valid @RequestBody BlogStatusRequest request) {
        try {
            BlogStatus status = BlogStatus.valueOf(request.getEstado().trim().toUpperCase());
            return blogService.updateStatus(id, status);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
        }
    }

    @Operation(summary = "Eliminar blog", description = "Elimina una entrada de blog por ID.")
    @ApiResponse(responseCode = "204", description = "Eliminado")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        blogService.delete(id);
    }
}
