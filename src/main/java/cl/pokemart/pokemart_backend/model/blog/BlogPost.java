package cl.pokemart.pokemart_backend.model.blog;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "blog_posts")
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 480)
    private String summary;

    @Lob
    @Column(name = "content_body")
    private String content;

    @Column(length = 120)
    private String category;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BlogStatus status;

    @Column(name = "author_name", length = 120)
    private String authorName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = BlogStatus.DRAFT;
        }
        if (this.status == BlogStatus.PUBLISHED && this.publishedAt == null) {
            this.publishedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
