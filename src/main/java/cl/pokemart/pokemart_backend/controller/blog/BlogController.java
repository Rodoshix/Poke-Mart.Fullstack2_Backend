package cl.pokemart.pokemart_backend.controller.blog;

import cl.pokemart.pokemart_backend.dto.blog.BlogResponse;
import cl.pokemart.pokemart_backend.service.blog.BlogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blogs")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public List<BlogResponse> list(
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "q", required = false) String query
    ) {
        return blogService.listPublic(categoria, query);
    }

    @GetMapping("/{slug}")
    public BlogResponse detail(@PathVariable String slug) {
        return blogService.getPublic(slug);
    }
}
