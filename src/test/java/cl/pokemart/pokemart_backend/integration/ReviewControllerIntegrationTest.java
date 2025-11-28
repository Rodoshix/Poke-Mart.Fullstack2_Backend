package cl.pokemart.pokemart_backend.integration;

import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.repository.catalog.ProductRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    private Long productId;

    @BeforeEach
    void setup() {
        List<Product> products = productRepository.findAll();
        assertThat(products).isNotEmpty();
        productId = products.get(0).getId();
    }

    @Test
    void shouldListReviewsForProduct() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}/reviews", productId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(Matchers.greaterThanOrEqualTo(0)));
    }

    @Test
    void shouldRejectReviewWithoutAuth() throws Exception {
        String payload = """
                {
                  "rating": 5,
                  "comment": "Excelente"
                }
                """;
        mockMvc.perform(post("/api/v1/products/{id}/reviews", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(s).isIn(401, 403);
                });
    }
}
