package cl.pokemart.pokemart_backend.repository.catalog;

import cl.pokemart.pokemart_backend.model.catalog.ProductOffer;
import cl.pokemart.pokemart_backend.model.catalog.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductOfferRepository extends JpaRepository<ProductOffer, Long> {
    @Query("select o from ProductOffer o join fetch o.product p where o.active = true and (o.endsAt is null or o.endsAt > :now) and p.active = true")
    List<ProductOffer> findActive(LocalDateTime now);

    List<ProductOffer> findByProduct(Product product);
}
