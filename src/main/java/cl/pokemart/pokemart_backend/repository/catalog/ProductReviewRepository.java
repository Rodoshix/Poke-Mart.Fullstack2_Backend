package cl.pokemart.pokemart_backend.repository.catalog;

import cl.pokemart.pokemart_backend.model.catalog.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    @Query("select r from ProductReview r join fetch r.product p left join fetch p.category c left join fetch r.user u where p.id = :productId order by r.createdAt desc")
    List<ProductReview> findByProductId(@Param("productId") Long productId);

    @Query("select r from ProductReview r join fetch r.product p left join fetch p.category c left join fetch r.user u order by r.createdAt desc")
    List<ProductReview> findAllWithProduct();

    @Query("select avg(r.rating) from ProductReview r where r.product.id = :productId")
    Optional<Double> averageRating(@Param("productId") Long productId);

    long countByProductId(Long productId);
}
