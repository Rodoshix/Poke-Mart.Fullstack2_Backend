package cl.pokemart.pokemart_backend.dto.order;

import cl.pokemart.pokemart_backend.model.order.Order;
import cl.pokemart.pokemart_backend.model.order.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
@Schema(description = "Orden de compra resultante")
public class OrderResponse {
    @Schema(description = "ID de la orden")
    Long id;
    @Schema(description = "Numero de referencia de la orden")
    String numeroOrden;
    @Schema(description = "Nombre del cliente")
    String nombreCliente;
    @Schema(description = "Correo del cliente")
    String correoCliente;
    @Schema(description = "Telefono del cliente")
    String telefonoCliente;
    @Schema(description = "Direccion de envio")
    String direccionEnvio;
    @Schema(description = "Region de envio")
    String regionEnvio;
    @Schema(description = "Comuna de envio")
    String comunaEnvio;
    @Schema(description = "Referencia o tracking de envio")
    String referenciaEnvio;
    @Schema(description = "Metodo de pago")
    String metodoPago;
    @Schema(description = "Estado de la orden")
    String estado;
    @Schema(description = "Subtotal del pedido")
    BigDecimal subtotal;
    @Schema(description = "Costo de envio")
    BigDecimal costoEnvio;
    @Schema(description = "Descuento aplicado")
    BigDecimal descuento;
    @Schema(description = "Impuestos aplicados")
    BigDecimal impuestos;
    @Schema(description = "Total a pagar")
    BigDecimal total;
    @Schema(description = "Indica si se aplicaron ofertas")
    Boolean ofertasAplicadas;
    @Schema(description = "Fecha de creacion")
    String creadoEn;
    @Schema(description = "Fecha de ultima actualizacion")
    String actualizadoEn;
    @Schema(description = "Items de la orden")
    List<Item> items;

    @Value
    @Builder
    @Schema(description = "Linea de la orden")
    public static class Item {
        @Schema(description = "ID del producto")
        Long productoId;
        @Schema(description = "Nombre del producto")
        String nombreProducto;
        @Schema(description = "Cantidad solicitada")
        Integer cantidad;
        @Schema(description = "Precio unitario")
        BigDecimal precioUnitario;
        @Schema(description = "Total de la linea")
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
                .ofertasAplicadas(hasOffers(order))
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

    private static Boolean hasOffers(Order order) {
        if (order.getItems() == null) return Boolean.FALSE;
        return order.getItems().stream().anyMatch(item ->
                item.getProducto() != null
                        && item.getProducto().getPrice() != null
                        && item.getPrecioUnitario() != null
                        && item.getProducto().getPrice().compareTo(item.getPrecioUnitario()) > 0
        );
    }
}
