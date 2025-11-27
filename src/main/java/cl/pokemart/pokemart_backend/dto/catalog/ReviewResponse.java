package cl.pokemart.pokemart_backend.dto.catalog;

import cl.pokemart.pokemart_backend.model.catalog.ProductReview;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Reseña pública de un producto")
public class ReviewResponse {
    @Schema(description = "ID de la reseña")
    Long id;
    @Schema(description = "Rating otorgado")
    Integer rating;
    @Schema(description = "Comentario")
    String comment;
    @Schema(description = "Autor de la reseña")
    String author;
    @Schema(description = "Fecha de creación")
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
