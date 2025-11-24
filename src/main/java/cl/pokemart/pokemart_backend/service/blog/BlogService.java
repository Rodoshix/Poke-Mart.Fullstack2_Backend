package cl.pokemart.pokemart_backend.service.blog;

import cl.pokemart.pokemart_backend.dto.blog.AdminBlogResponse;
import cl.pokemart.pokemart_backend.dto.blog.BlogRequest;
import cl.pokemart.pokemart_backend.dto.blog.BlogResponse;
import cl.pokemart.pokemart_backend.model.blog.BlogPost;
import cl.pokemart.pokemart_backend.model.blog.BlogStatus;
import cl.pokemart.pokemart_backend.repository.blog.BlogPostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BlogService {

    private final BlogPostRepository blogPostRepository;

    public BlogService(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    public List<BlogResponse> listPublic(String category, String query) {
        String cat = normalize(category);
        String q = normalize(query);
        return blogPostRepository.findByStatusOrderByPublishedAtDesc(BlogStatus.PUBLISHED).stream()
                .filter(p -> cat == null || normalize(p.getCategory()) != null && normalize(p.getCategory()).equals(cat))
                .filter(p -> matchesQuery(p, q))
                .map(this::toResponse)
                .toList();
    }

    public BlogResponse getPublic(String slug) {
        return blogPostRepository.findBySlugAndStatus(slug, BlogStatus.PUBLISHED)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog no encontrado"));
    }

    public List<AdminBlogResponse> listAdmin(String category, String estado, String query) {
        BlogStatus status = parseStatusOptional(estado);
        String cat = normalize(category);
        String q = normalize(query);
        List<BlogPost> posts = blogPostRepository.searchAdmin(status, cat, q);
        return posts.stream().map(this::toAdminResponse).toList();
    }

    public AdminBlogResponse create(BlogRequest request) {
        BlogPost entity = new BlogPost();
        mapRequestToEntity(request, entity);
        entity.setSlug(buildUniqueSlug(request.getTitulo()));
        applyStatus(entity, parseStatusOptional(request.getEstado()));
        blogPostRepository.save(entity);
        return toAdminResponse(entity);
    }

    public AdminBlogResponse update(Long id, BlogRequest request) {
        BlogPost entity = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog no encontrado"));
        mapRequestToEntity(request, entity);
        if (!StringUtils.hasText(entity.getSlug())) {
            entity.setSlug(buildUniqueSlug(request.getTitulo()));
        }
        applyStatus(entity, parseStatusOptional(request.getEstado()));
        blogPostRepository.save(entity);
        return toAdminResponse(entity);
    }

    public AdminBlogResponse updateStatus(Long id, BlogStatus status) {
        BlogPost entity = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog no encontrado"));
        applyStatus(entity, status);
        blogPostRepository.save(entity);
        return toAdminResponse(entity);
    }

    public void delete(Long id) {
        if (!blogPostRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog no encontrado");
        }
        blogPostRepository.deleteById(id);
    }

    private void mapRequestToEntity(BlogRequest request, BlogPost entity) {
        entity.setTitle(request.getTitulo());
        entity.setSummary(request.getDescripcion());
        entity.setContent(request.getContenido());
        entity.setCategory(trimToNull(request.getCategoria()));
        entity.setImageUrl(trimToNull(request.getImagen()));
        entity.setAuthorName("Equipo Pokemart");
        entity.setTags(joinTags(request.getEtiquetas()));
    }

    private void applyStatus(BlogPost entity, BlogStatus status) {
        BlogStatus finalStatus = status != null ? status : Optional.ofNullable(entity.getStatus()).orElse(BlogStatus.DRAFT);
        entity.setStatus(finalStatus);
        if (finalStatus == BlogStatus.PUBLISHED && entity.getPublishedAt() == null) {
            entity.setPublishedAt(LocalDateTime.now());
        }
        if (finalStatus == BlogStatus.DRAFT) {
            // mantenemos publishedAt si existía; solo cambiar estado
        }
    }

    private String joinTags(List<String> etiquetas) {
        if (etiquetas == null || etiquetas.isEmpty()) return null;
        return etiquetas.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining(","));
    }

    private List<String> splitTags(String tags) {
        if (!StringUtils.hasText(tags)) return Collections.emptyList();
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private BlogResponse toResponse(BlogPost entity) {
        return BlogResponse.builder()
                .id(entity.getId())
                .slug(entity.getSlug())
                .titulo(entity.getTitle())
                .descripcion(entity.getSummary())
                .contenido(entity.getContent())
                .categoria(entity.getCategory())
                .etiquetas(splitTags(entity.getTags()))
                .imagen(entity.getImageUrl())
                .estado(entity.getStatus() != null ? entity.getStatus().name() : null)
                .autor(entity.getAuthorName())
                .fechaPublicacion(entity.getPublishedAt())
                .build();
    }

    private AdminBlogResponse toAdminResponse(BlogPost entity) {
        return AdminBlogResponse.builder()
                .id(entity.getId())
                .slug(entity.getSlug())
                .titulo(entity.getTitle())
                .descripcion(entity.getSummary())
                .contenido(entity.getContent())
                .categoria(entity.getCategory())
                .etiquetas(splitTags(entity.getTags()))
                .imagen(entity.getImageUrl())
                .estado(entity.getStatus() != null ? entity.getStatus().name() : null)
                .autor(entity.getAuthorName())
                .fechaPublicacion(entity.getPublishedAt())
                .creadoEn(entity.getCreatedAt())
                .actualizadoEn(entity.getUpdatedAt())
                .build();
    }

    private boolean matchesQuery(BlogPost p, String q) {
        if (q == null) return true;
        String title = normalize(p.getTitle());
        String summary = normalize(p.getSummary());
        return (title != null && title.contains(q)) || (summary != null && summary.contains(q));
    }

    private BlogStatus parseStatusOptional(String estado) {
        if (!StringUtils.hasText(estado)) return null;
        try {
            return BlogStatus.valueOf(estado.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
        }
    }

    private String buildUniqueSlug(String titulo) {
        String base = slugify(titulo);
        String candidate = base;
        int counter = 2;
        while (blogPostRepository.existsBySlug(candidate)) {
            candidate = base + "-" + counter;
            counter++;
        }
        return candidate;
    }

    private String slugify(String input) {
        if (!StringUtils.hasText(input)) {
            return "blog-" + UUID.randomUUID().toString().substring(0, 8);
        }
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]+", "");
        String slug = normalized.replaceAll("[^a-zA-Z0-9\\-]", "").toLowerCase(Locale.ROOT);
        slug = slug.replaceAll("-{2,}", "-");
        return slug.endsWith("-") ? slug.substring(0, slug.length() - 1) : slug;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) return null;
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
