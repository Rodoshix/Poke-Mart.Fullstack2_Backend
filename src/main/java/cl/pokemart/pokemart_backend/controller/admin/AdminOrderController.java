package cl.pokemart.pokemart_backend.controller.admin;

import cl.pokemart.pokemart_backend.dto.common.ErrorResponse;
import cl.pokemart.pokemart_backend.dto.order.AdminOrderUpdateRequest;
import cl.pokemart.pokemart_backend.dto.order.OrderResponse;
import cl.pokemart.pokemart_backend.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
@Tag(name = "Admin - Orders", description = "Gestión de órdenes (admin/vendedor)")
@PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflicto o regla de negocio", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Listado admin de órdenes", description = "Devuelve todas las órdenes para gestión.")
    @ApiResponse(responseCode = "200", description = "Listado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class)))
    @GetMapping
    public List<OrderResponse> list() {
        return orderService.listForAdmin();
    }

    @Operation(summary = "Detalle de orden (admin)", description = "Obtiene el detalle de una orden para administración.")
    @ApiResponse(responseCode = "200", description = "Detalle de orden",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class)))
    @GetMapping("/{id}")
    public OrderResponse getOne(@PathVariable Long id) {
        return orderService.getForAdmin(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar orden (admin)", description = "Permite actualizar estado, notas y referencia de envío.")
    @ApiResponse(responseCode = "200", description = "Orden actualizada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class)))
    @PatchMapping("/{id}")
    public OrderResponse update(@PathVariable Long id, @Valid @RequestBody AdminOrderUpdateRequest request) {
        return orderService.updateOrderAdmin(id, request.getEstado(), request.getNotas(), request.getReferenciaEnvio());
    }
}
