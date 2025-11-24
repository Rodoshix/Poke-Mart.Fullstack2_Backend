package cl.pokemart.pokemart_backend.controller.catalog;

import cl.pokemart.pokemart_backend.dto.catalog.ReviewRequest;
import cl.pokemart.pokemart_backend.dto.catalog.ReviewResponse;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.service.catalog.CatalogService;
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
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {

    private final CatalogService catalogService;

    public ReviewController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<ReviewResponse> list(@PathVariable Long productId) {
        return catalogService.listReviews(productId);
    }

    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','VENDEDOR')")
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
