package cl.pokemart.pokemart_backend.service.order;

import cl.pokemart.pokemart_backend.dto.order.OrderItemRequest;
import cl.pokemart.pokemart_backend.dto.order.OrderRequest;
import cl.pokemart.pokemart_backend.dto.order.OrderResponse;
import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.model.catalog.ProductOffer;
import cl.pokemart.pokemart_backend.model.order.Order;
import cl.pokemart.pokemart_backend.model.order.OrderItem;
import cl.pokemart.pokemart_backend.model.order.OrderStatus;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.repository.catalog.ProductOfferRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductRepository;
import cl.pokemart.pokemart_backend.repository.order.OrderRepository;
import cl.pokemart.pokemart_backend.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductOfferRepository productOfferRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        ProductOfferRepository productOfferRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productOfferRepository = productOfferRepository;
        this.userRepository = userRepository;
    }

    public OrderResponse createOrder(OrderRequest request, User currentUser) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener al menos un producto");
        }

        Order order = Order.builder()
                .cliente(resolveCustomer(request, currentUser))
                .nombreCliente((request.getNombre() + " " + request.getApellido()).trim())
                .correoCliente(request.getCorreo())
                .telefonoCliente(request.getTelefono())
                .direccionEnvio(joinAddress(request.getCalle(), request.getDepartamento()))
                .regionEnvio(request.getRegion())
                .comunaEnvio(request.getComuna())
                .referenciaEnvio(valueOrDefault(request.getNotas(), "Sin referencias"))
                .metodoPago(request.getMetodoPago())
                .estado(OrderStatus.CREADA)
                .subtotal(BigDecimal.ZERO)
                .costoEnvio(BigDecimal.ZERO)
                .descuento(BigDecimal.ZERO)
                .impuestos(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .notas(valueOrDefault(request.getNotas(), "Sin notas registradas"))
                .build();

        List<OrderItem> items = new java.util.ArrayList<>(request.getItems().stream()
                .map(itemReq -> toOrderItem(order, itemReq))
                .toList());

        BigDecimal subtotal = items.stream()
                .map(OrderItem::getTotalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setItems(items);
        order.setSubtotal(subtotal);
        order.setCostoEnvio(BigDecimal.ZERO);
        order.setDescuento(BigDecimal.ZERO);
        order.setImpuestos(BigDecimal.ZERO);
        order.setTotal(subtotal);

        Order saved = orderRepository.save(order);
        saved.setNumeroOrden(generateOrderNumber(saved.getId()));
        Order persisted = orderRepository.save(saved);
        return OrderResponse.from(persisted);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listForAdmin() {
        return orderRepository.findAllByOrderByCreadoEnDesc().stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getForAdmin(Long id) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada"));
        return OrderResponse.from(order);
    }

    public OrderResponse updateOrderAdmin(Long id, String estado, String notas, String referenciaEnvio) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada"));

        if (StringUtils.hasText(estado)) {
            try {
                order.setEstado(OrderStatus.valueOf(estado.trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Estado de orden invalido");
            }
        }
        if (notas != null) {
            order.setNotas(notas);
        }
        if (referenciaEnvio != null) {
            order.setReferenciaEnvio(referenciaEnvio);
        }

        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }

    private OrderItem toOrderItem(Order order, OrderItemRequest itemReq) {
        Product product = productRepository.findById(itemReq.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + itemReq.getProductoId()));

        int quantity = Math.max(1, itemReq.getCantidad());
        BigDecimal unitPrice = resolvePrice(product);
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        if (product.getStock() != null && product.getStock() > 0) {
            int nextStock = Math.max(0, product.getStock() - quantity);
            product.setStock(nextStock);
        }

        return OrderItem.builder()
                .orden(order)
                .producto(product)
                .nombreProducto(product.getName())
                .precioUnitario(unitPrice)
                .cantidad(quantity)
                .totalLinea(lineTotal)
                .build();
    }

    private BigDecimal resolvePrice(Product product) {
        Optional<ProductOffer> offerOpt = findActiveOffer(product);
        BigDecimal base = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        if (offerOpt.isEmpty()) return base;
        int pct = offerOpt.get().getDiscountPct() != null ? offerOpt.get().getDiscountPct() : 0;
        if (pct <= 0 || pct >= 100) return base;
        BigDecimal multiplier = BigDecimal.valueOf(100 - pct).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return base.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    private Optional<ProductOffer> findActiveOffer(Product product) {
        var offers = productOfferRepository.findActive(LocalDateTime.now());
        if (offers == null) return Optional.empty();
        return offers.stream()
                .filter(o -> o.getProduct() != null && o.getProduct().getId().equals(product.getId()) && !o.isExpired())
                .findFirst();
    }

    private User resolveCustomer(OrderRequest request, User currentUser) {
        if (currentUser == null) return null;
        return userRepository.findById(currentUser.getId()).orElse(null);
    }

    private String generateOrderNumber(Long id) {
        if (id == null) {
            var last = orderRepository.findTopByOrderByIdDesc().map(Order::getId).orElse(1000L);
            return String.format("ORD-%04d", last + 1);
        }
        return String.format("ORD-%04d", id);
    }

    private String joinAddress(String calle, String depto) {
        if (depto == null || depto.isBlank()) return calle;
        return calle + ", " + depto;
    }

    private String valueOrDefault(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}
