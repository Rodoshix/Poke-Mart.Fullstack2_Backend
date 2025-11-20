package cl.pokemart.pokemart_backend.dto.order;

import cl.pokemart.pokemart_backend.model.order.Order;
import cl.pokemart.pokemart_backend.model.order.OrderItem;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class OrderResponse {
    Long id;
    String orderNumber;
    String customerName;
    String customerEmail;
    String customerPhone;
    String shippingStreet;
    String shippingRegion;
    String shippingComuna;
    String shippingReference;
    String paymentMethod;
    String status;
    BigDecimal subtotal;
    BigDecimal shipping;
    BigDecimal discount;
    BigDecimal taxes;
    BigDecimal total;
    String createdAt;
    String updatedAt;
    List<Item> items;

    @Value
    @Builder
    public static class Item {
        Long productId;
        String productName;
        Integer quantity;
        BigDecimal unitPrice;
        BigDecimal lineTotal;
    }

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .shippingStreet(order.getShippingStreet())
                .shippingRegion(order.getShippingRegion())
                .shippingComuna(order.getShippingComuna())
                .shippingReference(order.getShippingReference())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .subtotal(order.getSubtotal())
                .shipping(order.getShipping())
                .discount(order.getDiscount())
                .taxes(order.getTaxes())
                .total(order.getTotal())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null)
                .updatedAt(order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : null)
                .items(order.getItems() == null ? List.of() : order.getItems().stream().map(OrderResponse::mapItem).toList())
                .build();
    }

    private static Item mapItem(OrderItem item) {
        return Item.builder()
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
