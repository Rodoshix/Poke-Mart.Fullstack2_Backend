package cl.pokemart.pokemart_backend.repository.catalog;

import cl.pokemart.pokemart_backend.model.catalog.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    @Query("select r from ProductReview r join fetch r.product p left join fetch p.category c left join fetch r.user u where p.id = :productId order by r.createdAt desc")
    List<ProductReview> findByProductId(@Param("productId") Long productId);

    @Query("select r from ProductReview r join fetch r.product p left join fetch p.category c left join fetch r.user u order by r.createdAt desc")
    List<ProductReview> findAllWithProduct();

    @Query("select distinct lower(c.name) from ProductReview r join r.product p left join p.category c where c.name is not null")
    List<String> findDistinctCategoriesInReviews();

    @Query(
            value = """
                    select r from ProductReview r
                    join fetch r.product p
                    left join fetch p.category c
                    left join fetch r.user u
                    where (:category is null or lower(c.name) = lower(:category))
                      and (:productId is null or p.id = :productId)
                    order by r.createdAt desc
                    """,
            countQuery = """
                    select count(r) from ProductReview r
                    join r.product p
                    left join p.category c
                    where (:category is null or lower(c.name) = lower(:category))
                      and (:productId is null or p.id = :productId)
                    """
    )
    Page<ProductReview> findForAdmin(@Param("category") String category, @Param("productId") Long productId, Pageable pageable);

    @Query("select avg(r.rating) from ProductReview r where r.product.id = :productId")
    Optional<Double> averageRating(@Param("productId") Long productId);

    long countByProductId(Long productId);

    @Query("select r.product.id, count(r), avg(r.rating) from ProductReview r where r.product.id in :productIds group by r.product.id")
    List<Object[]> aggregateByProductIds(@Param("productIds") List<Long> productIds);
}
