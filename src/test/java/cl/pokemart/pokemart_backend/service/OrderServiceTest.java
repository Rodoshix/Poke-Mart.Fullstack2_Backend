package cl.pokemart.pokemart_backend.service;

import cl.pokemart.pokemart_backend.dto.order.OrderItemRequest;
import cl.pokemart.pokemart_backend.dto.order.OrderRequest;
import cl.pokemart.pokemart_backend.dto.order.OrderResponse;
import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.model.catalog.ProductOffer;
import cl.pokemart.pokemart_backend.model.catalog.Category;
import cl.pokemart.pokemart_backend.model.order.Order;
import cl.pokemart.pokemart_backend.repository.catalog.ProductOfferRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductRepository;
import cl.pokemart.pokemart_backend.repository.order.OrderRepository;
import cl.pokemart.pokemart_backend.repository.user.UserRepository;
import cl.pokemart.pokemart_backend.service.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductOfferRepository productOfferRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private Product product;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        product = Product.builder()
                .id(1L)
                .name("Poke Ball")
                .price(BigDecimal.valueOf(1000))
                .stock(10)
                .active(true)
                .category(Category.builder().id(1L).slug("pokeballs").build())
                .build();

        // Simular persistencia para asignar ID al guardar
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            if (o.getId() == null) {
                o.setId(99L);
            }
            return o;
        });
    }

    @Test
    void shouldApplyOfferDiscountWhenActive() {
        // Offer 10% descuento
        ProductOffer offer = ProductOffer.builder()
                .id(5L)
                .product(product)
                .discountPct(10)
                .active(true)
                .endsAt(LocalDateTime.now().plusDays(1))
                .build();

        when(productRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(productOfferRepository.findActive(any(LocalDateTime.class))).thenReturn(List.of(offer));

        OrderRequest request = new OrderRequest(
                "Tester",
                "Unit",
                "tester@example.com",
                "+56900000000",
                "Kanto",
                "Ciudad Central",
                "Calle 1",
                "Depto 2",
                "Notas",
                "credit",
                BigDecimal.ZERO,
                List.of(new OrderItemRequest(1L, 2))
        );

        OrderResponse response = orderService.createOrder(request, null);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        assertThat(response.getId()).isNotNull();
        // 10% descuento sobre 1000 => 900 cada uno, 2 unidades = 1800 total
        assertThat(response.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(1800));
    }
}
