package cl.pokemart.pokemart_backend.repository.order;

import cl.pokemart.pokemart_backend.model.order.Order;
import cl.pokemart.pokemart_backend.model.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"items", "items.producto", "cliente"})
    List<Order> findAllByOrderByCreadoEnDesc();

    @EntityGraph(attributePaths = {"items", "items.producto", "cliente"})
    Optional<Order> findWithItemsById(Long id);

    Optional<Order> findTopByOrderByIdDesc();

    List<Order> findByCliente(User cliente);
}
