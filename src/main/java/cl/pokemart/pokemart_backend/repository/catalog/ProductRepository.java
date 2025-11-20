package cl.pokemart.pokemart_backend.repository.catalog;

import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p where p.active = true")
    List<Product> findAllActive();

    @Query("select p from Product p where p.active = true and lower(p.category.slug) = lower(:slug)")
    List<Product> findActiveByCategory(@Param("slug") String slug);

    @Query("select p from Product p where p.seller = :seller")
    List<Product> findBySeller(@Param("seller") User seller);

    @Query("select p from Product p where p.id = :id and p.active = true")
    Optional<Product> findActiveById(@Param("id") Long id);
}
