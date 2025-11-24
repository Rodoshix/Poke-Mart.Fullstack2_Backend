package cl.pokemart.pokemart_backend.controller.catalog;

import cl.pokemart.pokemart_backend.dto.catalog.ProductRequest;
import cl.pokemart.pokemart_backend.dto.catalog.ProductResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CatalogService catalogService;

    public ProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<ProductResponse> list(@RequestParam(value = "category", required = false) String category) {
        return catalogService.listProducts(category);
    }

    @GetMapping("/{id}")
    public ProductResponse getOne(@PathVariable Long id) {
        return catalogService.getProduct(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @GetMapping("/manage")
    public List<ProductResponse> listForManagement(@RequestParam(value = "includeInactive", defaultValue = "true") boolean includeInactive,
                                                   Authentication auth) {
        return catalogService.listProductsForManagement(includeInactive, currentUser(auth));
    }

    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @GetMapping("/manage/{id}")
    public ProductResponse getOneForManagement(@PathVariable Long id, Authentication auth) {
        return catalogService.getProductForManagement(id, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ProductResponse create(@Valid @RequestBody ProductRequest request, Authentication auth) {
        return catalogService.createProduct(request, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request, Authentication auth) {
        return catalogService.updateProduct(id, request, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ProductResponse updateStatus(@PathVariable Long id, @RequestParam("active") boolean active, Authentication auth) {
        return catalogService.setProductActive(id, active, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id,
                       @RequestParam(value = "hard", defaultValue = "false") boolean hardDelete,
                       Authentication auth) {
        catalogService.deleteProduct(id, currentUser(auth), hardDelete);
    }

    private User currentUser(Authentication auth) {
        Object principal = auth != null ? auth.getPrincipal() : null;
        if (principal instanceof User user) {
            return user;
        }
        return null;
    }
}
