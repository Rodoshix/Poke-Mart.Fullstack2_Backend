package cl.pokemart.pokemart_backend.controller.catalog;

import cl.pokemart.pokemart_backend.dto.catalog.ProductResponse;
import cl.pokemart.pokemart_backend.dto.common.ErrorResponse;
import cl.pokemart.pokemart_backend.service.catalog.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/offers")
@Tag(name = "Offers", description = "Listado de productos con oferta vigente")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class OfferController {

    private final CatalogService catalogService;

    public OfferController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(summary = "Ofertas activas", description = "Lista productos con descuentos vigentes.")
    @ApiResponse(responseCode = "200", description = "Listado de ofertas",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class)))
    @GetMapping
    public List<ProductResponse> listOffers() {
        return catalogService.listActiveOffers();
    }
}
