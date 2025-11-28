package cl.pokemart.pokemart_backend.repository.catalog;

import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.model.catalog.ProductStockBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

public interface ProductStockBaseRepository extends JpaRepository<ProductStockBase, Long> {
    Optional<ProductStockBase> findByProduct(Product product);
    Optional<ProductStockBase> findByProductId(Long productId);
    List<ProductStockBase> findByProductIdIn(Collection<Long> productIds);
    void deleteByProduct(Product product);
}
