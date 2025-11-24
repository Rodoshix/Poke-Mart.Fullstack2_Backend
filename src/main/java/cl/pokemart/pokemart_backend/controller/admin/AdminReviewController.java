package cl.pokemart.pokemart_backend.controller.admin;

import cl.pokemart.pokemart_backend.dto.catalog.AdminReviewResponse;
import cl.pokemart.pokemart_backend.service.catalog.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@Tag(name = "Admin - Reviews", description = "Gestión de reseñas (admin)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final CatalogService catalogService;

    public AdminReviewController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(summary = "Listado de reseñas", description = "Devuelve todas las reseñas registradas.")
    @ApiResponse(responseCode = "200", description = "Listado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminReviewResponse.class)))
    @GetMapping
    public List<AdminReviewResponse> list() {
        return catalogService.listReviewsAdmin();
    }

    @Operation(summary = "Eliminar reseña", description = "Elimina una reseña por su ID.")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        catalogService.deleteReview(id);
    }
}
