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
    String numeroOrden;
    String nombreCliente;
    String correoCliente;
    String telefonoCliente;
    String direccionEnvio;
    String regionEnvio;
    String comunaEnvio;
    String referenciaEnvio;
    String metodoPago;
    String estado;
    BigDecimal subtotal;
    BigDecimal costoEnvio;
    BigDecimal descuento;
    BigDecimal impuestos;
    BigDecimal total;
    String creadoEn;
    String actualizadoEn;
    List<Item> items;

    @Value
    @Builder
    public static class Item {
        Long productoId;
        String nombreProducto;
        Integer cantidad;
        BigDecimal precioUnitario;
        BigDecimal totalLinea;
    }

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .numeroOrden(order.getNumeroOrden())
                .nombreCliente(order.getNombreCliente())
                .correoCliente(order.getCorreoCliente())
                .telefonoCliente(order.getTelefonoCliente())
                .direccionEnvio(order.getDireccionEnvio())
                .regionEnvio(order.getRegionEnvio())
                .comunaEnvio(order.getComunaEnvio())
                .referenciaEnvio(order.getReferenciaEnvio())
                .metodoPago(order.getMetodoPago())
                .estado(order.getEstado() != null ? order.getEstado().name() : null)
                .subtotal(order.getSubtotal())
                .costoEnvio(order.getCostoEnvio())
                .descuento(order.getDescuento())
                .impuestos(order.getImpuestos())
                .total(order.getTotal())
                .creadoEn(order.getCreadoEn() != null ? order.getCreadoEn().toString() : null)
                .actualizadoEn(order.getActualizadoEn() != null ? order.getActualizadoEn().toString() : null)
                .items(order.getItems() == null ? List.of() : order.getItems().stream().map(OrderResponse::mapItem).toList())
                .build();
    }

    private static Item mapItem(OrderItem item) {
        return Item.builder()
                .productoId(item.getProducto() != null ? item.getProducto().getId() : null)
                .nombreProducto(item.getNombreProducto())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .totalLinea(item.getTotalLinea())
                .build();
    }
}
