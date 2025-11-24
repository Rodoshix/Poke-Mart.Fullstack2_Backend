package cl.pokemart.pokemart_backend.repository.blog;

import cl.pokemart.pokemart_backend.model.blog.BlogPost;
import cl.pokemart.pokemart_backend.model.blog.BlogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    Optional<BlogPost> findBySlugAndStatus(String slug, BlogStatus status);

    boolean existsBySlug(String slug);

    List<BlogPost> findByStatusOrderByPublishedAtDesc(BlogStatus status);

    @Query("""
            select b from BlogPost b
            where (:status is null or b.status = :status)
              and (:category is null or lower(b.category) = lower(:category))
              and (:q is null or lower(b.title) like lower(concat('%', coalesce(:q, ''), '%'))
                   or lower(b.summary) like lower(concat('%', coalesce(:q, ''), '%')))
            order by b.createdAt desc
            """)
    List<BlogPost> searchAdmin(@Param("status") BlogStatus status,
                               @Param("category") String category,
                               @Param("q") String query);
}
