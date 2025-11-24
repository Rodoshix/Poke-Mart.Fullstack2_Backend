package cl.pokemart.pokemart_backend.controller.catalog;

import cl.pokemart.pokemart_backend.dto.catalog.ProductResponse;
import cl.pokemart.pokemart_backend.service.catalog.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/offers")
public class OfferController {

    private final CatalogService catalogService;

    public OfferController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<ProductResponse> listOffers() {
        return catalogService.listActiveOffers();
    }
}
