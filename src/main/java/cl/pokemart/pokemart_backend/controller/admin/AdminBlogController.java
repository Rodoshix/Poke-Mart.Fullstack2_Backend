package cl.pokemart.pokemart_backend.controller.admin;

import cl.pokemart.pokemart_backend.dto.blog.AdminBlogResponse;
import cl.pokemart.pokemart_backend.dto.blog.BlogRequest;
import cl.pokemart.pokemart_backend.dto.blog.BlogStatusRequest;
import cl.pokemart.pokemart_backend.model.blog.BlogStatus;
import cl.pokemart.pokemart_backend.service.blog.BlogService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/blogs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBlogController {

    private final BlogService blogService;

    public AdminBlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public List<AdminBlogResponse> list(
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "q", required = false) String query
    ) {
        return blogService.listAdmin(categoria, estado, query);
    }

    @PostMapping
    public AdminBlogResponse create(@Valid @RequestBody BlogRequest request) {
        return blogService.create(request);
    }

    @PutMapping("/{id}")
    public AdminBlogResponse update(@PathVariable Long id, @Valid @RequestBody BlogRequest request) {
        return blogService.update(id, request);
    }

    @PatchMapping("/{id}/estado")
    public AdminBlogResponse updateStatus(@PathVariable Long id, @Valid @RequestBody BlogStatusRequest request) {
        try {
            BlogStatus status = BlogStatus.valueOf(request.getEstado().trim().toUpperCase());
            return blogService.updateStatus(id, status);
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Estado inválido");
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        blogService.delete(id);
    }
}
