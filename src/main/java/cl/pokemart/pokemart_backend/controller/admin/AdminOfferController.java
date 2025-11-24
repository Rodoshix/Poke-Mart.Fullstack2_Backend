package cl.pokemart.pokemart_backend.controller.admin;

import cl.pokemart.pokemart_backend.dto.catalog.AdminOfferRequest;
import cl.pokemart.pokemart_backend.dto.catalog.AdminOfferResponse;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.service.catalog.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/offers")
@Tag(name = "Admin - Offers", description = "Gestión de ofertas (admin/vendedor)")
@PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
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

    @Operation(summary = "Detalle de oferta", description = "Detalle extendido de una oferta para administración.")
    @GetMapping("/{id}")
    public AdminOfferResponse getOne(@PathVariable Long id, Authentication auth) {
        return catalogService.getOfferForManagement(id, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear oferta", description = "Crea una oferta para un producto.")
    @PostMapping
    public AdminOfferResponse create(@Valid @RequestBody AdminOfferRequest request, Authentication auth) {
        return catalogService.createOffer(request, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar oferta", description = "Modifica una oferta existente.")
    @PutMapping("/{id}")
    public AdminOfferResponse update(@PathVariable Long id, @Valid @RequestBody AdminOfferRequest request, Authentication auth) {
        return catalogService.updateOffer(id, request, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar/Desactivar oferta", description = "Cambia el estado activo de una oferta.")
    @PutMapping("/{id}/status")
    public AdminOfferResponse updateStatus(@PathVariable Long id, @RequestParam("active") boolean active, Authentication auth) {
        return catalogService.setOfferActive(id, active, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar oferta", description = "Elimina lógicamente o físicamente una oferta.")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestParam(value = "hard", defaultValue = "false") boolean hard, Authentication auth) {
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
