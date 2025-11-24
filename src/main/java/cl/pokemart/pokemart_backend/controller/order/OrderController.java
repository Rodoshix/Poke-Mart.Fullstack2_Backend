package cl.pokemart.pokemart_backend.controller.order;

import cl.pokemart.pokemart_backend.dto.order.OrderRequest;
import cl.pokemart.pokemart_backend.dto.order.OrderResponse;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Creación de órdenes de compra")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Crear orden", description = "Genera una orden a partir del carrito y datos del cliente.")
    @ApiResponse(responseCode = "200", description = "Orden creada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class)))
    @PostMapping
    public OrderResponse create(@Valid @RequestBody OrderRequest request, Authentication authentication) {
        User user = authentication != null && authentication.getPrincipal() instanceof User principal ? principal : null;
        return orderService.createOrder(request, user);
    }
}
