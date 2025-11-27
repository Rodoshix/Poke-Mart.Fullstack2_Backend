package cl.pokemart.pokemart_backend.controller.catalog;

import cl.pokemart.pokemart_backend.dto.catalog.ReviewRequest;
import cl.pokemart.pokemart_backend.dto.catalog.ReviewResponse;
import cl.pokemart.pokemart_backend.dto.common.ApiErrorExamples;
import cl.pokemart.pokemart_backend.dto.common.ErrorResponse;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.service.catalog.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
@Tag(name = "Reviews", description = "Reseñas públicas por producto")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Body invalido", value = """
                        {
                          "status": 400,
                          "error": "Bad Request",
                          "message": "El comentario es obligatorio",
                          "path": "/api/v1/products/1/reviews",
                          "timestamp": "2025-11-27T10:15:30Z"
                        }
                        """)
        })),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Sin token", value = ApiErrorExamples.REVIEW_FORBIDDEN)
        })),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "No autorizado", value = ApiErrorExamples.REVIEW_FORBIDDEN)
        })),
        @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Producto no existe", value = ApiErrorExamples.REVIEW_NOT_FOUND)
        })),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Fallo interno", value = ApiErrorExamples.PUBLIC_OFFERS_ERROR)
        }))
})
public class ReviewController {

    private final CatalogService catalogService;

    public ReviewController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(summary = "Listar reseñas", description = "Devuelve las reseñas de un producto.")
    @ApiResponse(responseCode = "200", description = "Listado de reseñas",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewResponse.class)))
    @GetMapping
    public List<ReviewResponse> list(@PathVariable Long productId) {
        return catalogService.listReviews(productId);
    }

    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','VENDEDOR')")
    @Operation(summary = "Crear reseña", description = "Agrega una reseña a un producto (requiere autenticación).")
    @ApiResponse(responseCode = "200", description = "Reseña creada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewResponse.class)))
    @PostMapping
    public ReviewResponse create(@PathVariable Long productId, @Valid @RequestBody ReviewRequest request, Authentication auth) {
        return catalogService.addReview(productId, request, currentUser(auth));
    }

    private User currentUser(Authentication auth) {
        Object principal = auth != null ? auth.getPrincipal() : null;
        if (principal instanceof User user) {
            return user;
        }
        return null;
    }
}
