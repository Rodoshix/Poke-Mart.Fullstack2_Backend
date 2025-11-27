package cl.pokemart.pokemart_backend.controller.admin;

import cl.pokemart.pokemart_backend.dto.catalog.AdminOfferRequest;
import cl.pokemart.pokemart_backend.dto.catalog.AdminOfferResponse;
import cl.pokemart.pokemart_backend.dto.common.ErrorResponse;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.service.catalog.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/offers")
@Tag(name = "Admin - Offers", description = "Gestión de ofertas (admin/vendedor)")
@PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflicto o regla de negocio", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class AdminOfferController {

    private final CatalogService catalogService;

    public AdminOfferController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(summary = "Listado admin de ofertas", description = "Incluye ofertas inactivas para gestión.")
    @ApiResponse(responseCode = "200", description = "Listado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminOfferResponse.class)))
    @GetMapping
    public List<AdminOfferResponse> list(@RequestParam(value = "includeInactive", defaultValue = "true") boolean includeInactive,
                                         Authentication auth) {
        return catalogService.listOffersForManagement(includeInactive, currentUser(auth));
    }

    @Operation(summary = "Detalle de oferta", description = "Obtiene una oferta por ID.")
    @ApiResponse(responseCode = "200", description = "Oferta encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminOfferResponse.class)))
    @GetMapping("/{id}")
    public AdminOfferResponse getOne(@PathVariable Long id, Authentication auth) {
        return catalogService.getOfferForManagement(id, currentUser(auth));
    }

    @Operation(summary = "Crear oferta", description = "Crea una nueva oferta para un producto.")
    @ApiResponse(responseCode = "201", description = "Oferta creada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminOfferResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminOfferResponse create(@Valid @RequestBody AdminOfferRequest request, Authentication auth) {
        return catalogService.createOffer(request, currentUser(auth));
    }

    @Operation(summary = "Actualizar oferta", description = "Modifica una oferta existente.")
    @ApiResponse(responseCode = "200", description = "Oferta actualizada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminOfferResponse.class)))
    @PutMapping("/{id}")
    public AdminOfferResponse update(@PathVariable Long id,
                                     @Valid @RequestBody AdminOfferRequest request,
                                     Authentication auth) {
        return catalogService.updateOffer(id, request, currentUser(auth));
    }

    @Operation(summary = "Eliminar oferta", description = "Eliminar o desactivar una oferta.")
    @ApiResponse(responseCode = "204", description = "Oferta eliminada/desactivada")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestParam(value = "hard", defaultValue = "false") boolean hard,
                       Authentication auth) {
        catalogService.deleteOffer(id, hard, currentUser(auth));
    }

    private User currentUser(Authentication auth) {
        Object principal = auth != null ? auth.getPrincipal() : null;
        if (principal instanceof User user) {
            return user;
        }
        return null;
    }
}
