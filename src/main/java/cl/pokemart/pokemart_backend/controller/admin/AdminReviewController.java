package cl.pokemart.pokemart_backend.controller.admin;

import cl.pokemart.pokemart_backend.dto.catalog.AdminReviewResponse;
import cl.pokemart.pokemart_backend.service.catalog.CatalogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final CatalogService catalogService;

    public AdminReviewController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<AdminReviewResponse> list() {
        return catalogService.listReviewsAdmin();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        catalogService.deleteReview(id);
    }
}
