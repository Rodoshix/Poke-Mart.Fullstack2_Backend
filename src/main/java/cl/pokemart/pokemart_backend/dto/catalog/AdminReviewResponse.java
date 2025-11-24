package cl.pokemart.pokemart_backend.dto.catalog;

import cl.pokemart.pokemart_backend.model.catalog.ProductReview;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminReviewResponse {
    Long id;
    Long productId;
    String productName;
    String category;
    Integer rating;
    String comment;
    String author;
    String createdAt;

    public static AdminReviewResponse from(ProductReview review) {
        var product = review.getProduct();
        return AdminReviewResponse.builder()
                .id(review.getId())
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)
                .category(product != null && product.getCategory() != null ? product.getCategory().getName() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .author(review.getAuthorName())
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toString() : null)
                .build();
    }
}
