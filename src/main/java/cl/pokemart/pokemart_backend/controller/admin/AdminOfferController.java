package cl.pokemart.pokemart_backend.controller.admin;

import cl.pokemart.pokemart_backend.dto.catalog.AdminOfferRequest;
import cl.pokemart.pokemart_backend.dto.catalog.AdminOfferResponse;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.service.catalog.CatalogService;
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
@RequestMapping("/api/admin/offers")
@PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
public class AdminOfferController {

    private final CatalogService catalogService;

    public AdminOfferController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<AdminOfferResponse> list(@RequestParam(value = "includeInactive", defaultValue = "true") boolean includeInactive,
                                         Authentication auth) {
        return catalogService.listOffersForManagement(includeInactive, currentUser(auth));
    }

    @GetMapping("/{id}")
    public AdminOfferResponse getOne(@PathVariable Long id, Authentication auth) {
        return catalogService.getOfferForManagement(id, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public AdminOfferResponse create(@Valid @RequestBody AdminOfferRequest request, Authentication auth) {
        return catalogService.createOffer(request, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public AdminOfferResponse update(@PathVariable Long id, @Valid @RequestBody AdminOfferRequest request, Authentication auth) {
        return catalogService.updateOffer(id, request, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public AdminOfferResponse updateStatus(@PathVariable Long id, @RequestParam("active") boolean active, Authentication auth) {
        return catalogService.setOfferActive(id, active, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
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
