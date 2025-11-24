package cl.pokemart.pokemart_backend.dto.catalog;

import cl.pokemart.pokemart_backend.model.catalog.ProductReview;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReviewResponse {
    Long id;
    Integer rating;
    String comment;
    String author;
    String createdAt;

    public static ReviewResponse from(ProductReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .author(review.getAuthorName())
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toString() : null)
                .build();
    }
}
