package cl.pokemart.pokemart_backend.integration;

import cl.pokemart.pokemart_backend.dto.order.OrderRequest;
import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.repository.catalog.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long firstProductId;

    @BeforeEach
    void setup() {
        List<Product> products = productRepository.findAll();
        assertThat(products).isNotEmpty();
        firstProductId = products.get(0).getId();
    }

    @Test
    void shouldCreateOrderAsGuest() throws Exception {
        String payload = objectMapper.writeValueAsString(new OrderRequest(
                "Tester",
                "Invitado",
                "tester@example.com",
                "+56999999999",
                "Kanto",
                "Ciudad Central",
                "Calle 1",
                "Depto 2",
                "Orden de prueba",
                "credit",
                BigDecimal.valueOf(1500),
                List.of(new cl.pokemart.pokemart_backend.dto.order.OrderItemRequest(firstProductId, 1))
        ));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.total").isNumber());
    }
}
