package cl.pokemart.pokemart_backend.controller.order;

import cl.pokemart.pokemart_backend.dto.order.OrderRequest;
import cl.pokemart.pokemart_backend.dto.order.OrderResponse;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.service.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse create(@Valid @RequestBody OrderRequest request, Authentication authentication) {
        User user = authentication != null && authentication.getPrincipal() instanceof User principal ? principal : null;
        return orderService.createOrder(request, user);
    }
}
