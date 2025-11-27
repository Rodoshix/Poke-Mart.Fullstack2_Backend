package cl.pokemart.pokemart_backend.controller.catalog;

import cl.pokemart.pokemart_backend.dto.catalog.ProductRequest;
import cl.pokemart.pokemart_backend.dto.catalog.ProductResponse;
import cl.pokemart.pokemart_backend.dto.common.ApiErrorExamples;
import cl.pokemart.pokemart_backend.dto.common.ErrorResponse;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.service.catalog.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product", description = "Catálogo público y gestión de productos")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Categoria invalida", value = ApiErrorExamples.BLOG_BAD_REQUEST)
        })),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Sin token", value = ApiErrorExamples.PROFILE_UNAUTHORIZED)
        })),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Rol insuficiente", value = ApiErrorExamples.OFFER_FORBIDDEN)
        })),
        @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Producto no existe", value = ApiErrorExamples.REVIEW_NOT_FOUND)
        })),
        @ApiResponse(responseCode = "409", description = "Conflicto o regla de negocio", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Nombre duplicado", value = """
                        {
                          "status": 409,
                          "error": "Conflict",
                          "message": "Ya existe un producto con ese nombre",
                          "path": "/api/v1/products",
                          "timestamp": "2025-11-27T10:15:30Z"
                        }
                        """)
        })),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Fallo interno", value = ApiErrorExamples.PUBLIC_OFFERS_ERROR)
        }))
})
public class ProductController {

    private final CatalogService catalogService;

    public ProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(
            summary = "Lista productos públicos",
            description = "Entrega el catálogo visible para clientes. Se puede filtrar por categoría."
    )
    @ApiResponse(responseCode = "200", description = "Listado de productos",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class)))
    @GetMapping
    public List<ProductResponse> list(@RequestParam(value = "category", required = false) String category) {
        return catalogService.listProducts(category);
    }

    @Operation(
            summary = "Detalle de producto público",
            description = "Obtiene la ficha de un producto visible por su identificador."
    )
    @ApiResponse(responseCode = "200", description = "Producto encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class)))
    @GetMapping("/{id}")
    public ProductResponse getOne(@Parameter(description = "ID del producto", required = true) @PathVariable Long id) {
        return catalogService.getProduct(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @Operation(
            summary = "Lista de gestión (admin/vendedor)",
            description = "Incluye productos inactivos y datos internos como stock base."
    )
    @GetMapping("/manage")
    public List<ProductResponse> listForManagement(@RequestParam(value = "includeInactive", defaultValue = "true") boolean includeInactive,
                                                   Authentication auth) {
        return catalogService.listProductsForManagement(includeInactive, currentUser(auth));
    }

    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @Operation(summary = "Detalle para gestión", description = "Detalle extendido de un producto para admin/vendedor.")
    @GetMapping("/manage/{id}")
    public ProductResponse getOneForManagement(@PathVariable Long id, Authentication auth) {
        return catalogService.getProductForManagement(id, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear producto", description = "Crea un nuevo producto en el catálogo.")
    @ApiResponse(responseCode = "200", description = "Producto creado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class)))
    @PostMapping
    public ProductResponse create(@Valid @RequestBody ProductRequest request, Authentication auth) {
        return catalogService.createProduct(request, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar producto", description = "Modifica un producto existente.")
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request, Authentication auth) {
        return catalogService.updateProduct(id, request, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar estado del producto", description = "Activa o desactiva un producto.")
    @PatchMapping("/{id}/status")
    public ProductResponse updateStatus(@PathVariable Long id, @RequestParam("active") boolean active, Authentication auth) {
        return catalogService.setProductActive(id, active, currentUser(auth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar producto", description = "Elimina lógicamente o físicamente un producto.")
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
