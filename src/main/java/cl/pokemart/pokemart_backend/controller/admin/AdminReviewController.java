package cl.pokemart.pokemart_backend.controller.admin;

import cl.pokemart.pokemart_backend.dto.catalog.AdminReviewResponse;
import cl.pokemart.pokemart_backend.dto.common.ApiErrorExamples;
import cl.pokemart.pokemart_backend.dto.common.ErrorResponse;
import cl.pokemart.pokemart_backend.service.catalog.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@Tag(name = "Admin - Reviews", description = "Gestión de reseñas (admin)")
@PreAuthorize("hasRole('ADMIN')")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Id invalido", value = """
                        {
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Id de reseña inválido",
                          "path": "/api/v1/admin/reviews/abc",
                          "timestamp": "2025-11-27T10:15:30Z"
                        }
                        """)
        })),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Sin token", value = ApiErrorExamples.PROFILE_UNAUTHORIZED)
        })),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Rol insuficiente", value = ApiErrorExamples.OFFER_FORBIDDEN)
        })),
        @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Reseña no existe", value = """
                        {
                          "status": 404,
                          "error": "Not Found",
                          "message": "Review no encontrada",
                          "path": "/api/v1/admin/reviews/123",
                          "timestamp": "2025-11-27T10:15:30Z"
                        }
                        """)
        })),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Fallo interno", value = ApiErrorExamples.PUBLIC_OFFERS_ERROR)
        }))
})
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
    @ApiResponse(responseCode = "204", description = "Eliminada")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        catalogService.deleteReview(id);
    }
}
