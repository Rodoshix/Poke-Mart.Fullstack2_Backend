package cl.pokemart.pokemart_backend.dto.catalog;

import cl.pokemart.pokemart_backend.model.catalog.ProductReview;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Reseña gestionada desde admin")
public class AdminReviewResponse {
    @Schema(description = "ID de la reseña")
    Long id;
    @Schema(description = "ID del producto reseñado")
    Long productId;
    @Schema(description = "Nombre del producto reseñado")
    String productName;
    @Schema(description = "Rating otorgado (1-5)")
    Integer rating;
    @Schema(description = "Comentario")
    String comment;
    @Schema(description = "Autor de la reseña")
    String author;
    @Schema(description = "Fecha de creación")
    String createdAt;
    @Schema(description = "Correo del autor (si existe)")
    String authorEmail;

    public static AdminReviewResponse from(ProductReview review) {
        return AdminReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .productName(review.getProduct() != null ? review.getProduct().getName() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .author(review.getAuthorName())
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toString() : null)
                .authorEmail(review.getUser() != null ? review.getUser().getEmail() : null)
                .build();
    }
}
