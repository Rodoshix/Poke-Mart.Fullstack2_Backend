package cl.pokemart.pokemart_backend.repository.catalog;

import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.model.catalog.ProductStockBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductStockBaseRepository extends JpaRepository<ProductStockBase, Long> {
    Optional<ProductStockBase> findByProduct(Product product);
    Optional<ProductStockBase> findByProductId(Long productId);
}
