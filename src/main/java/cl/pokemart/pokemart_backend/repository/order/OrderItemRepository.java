package cl.pokemart.pokemart_backend.repository.order;

import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.model.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByProducto(Product product);
}
