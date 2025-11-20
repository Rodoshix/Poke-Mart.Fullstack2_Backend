package cl.pokemart.pokemart_backend.config;

import cl.pokemart.pokemart_backend.model.catalog.Category;
import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.model.catalog.ProductOffer;
import cl.pokemart.pokemart_backend.model.user.Role;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.repository.catalog.CategoryRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductOfferRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductRepository;
import cl.pokemart.pokemart_backend.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductOfferRepository productOfferRepository;

    public DataInitializer(UserService userService,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           ProductOfferRepository productOfferRepository) {
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productOfferRepository = productOfferRepository;
    }

    @Override
    public void run(String... args) {
        try {
            User admin = userService.ensureUser(
                    "admin@pokemart.cl",
                    "admin",
                    "admin123",
                    "Admin",
                    "Pokemart",
                    "11.111.111-1",
                    "Av. Admin 123",
                    "Metropolitana",
                    "Santiago",
                    java.time.LocalDate.of(1990, 1, 1),
                    "+56911111111",
                    Role.ADMIN
            );
            User vendor = userService.ensureUser(
                    "vendedor@pokemart.cl",
                    "vendedor",
                    "vendedor123!",
                    "Vendedor",
                    "Pokemart",
                    "22.222.222-2",
                    "Av. Vendedor 456",
                    "Metropolitana",
                    "Santiago",
                    java.time.LocalDate.of(1992, 2, 2),
                    "+56922222222",
                    Role.VENDEDOR
            );
            seedCatalog(vendor);
        } catch (Exception e) {
            log.warn("No se pudo inicializar usuarios demo: {}", e.getMessage());
        }
    }

    private void seedCatalog(User seller) {
        try {
            Category ropa = ensureCategory("ropa", "Ropa");
            Category transporte = ensureCategory("transporte", "Transporte");
            Category tecnologia = ensureCategory("tecnologia", "Tecnología");

            Product gBien = ensureProduct("Gorra Liga Pokemon", "Gorra oficial de la Liga Pokemon", ropa, seller, "src/assets/img/tienda/productos/gorro-liga-pokemon.png", 900, 120);
            Product superBall = ensureProduct("Super Ball", "Mayor tasa de captura que la Pokeball.", tecnologia, seller, "src/assets/img/tienda/productos/super-ball.png", 600, 700);
            Product bicicleta = ensureProduct("Bicicleta de Montaña", "Para recorrer rutas largas.", transporte, seller, "src/assets/img/tienda/productos/bicicleta.png", 80000, 10);
            Product camiseta = ensureProduct("Camiseta Pikachu", "Camiseta temática Pikachu.", ropa, seller, "src/assets/img/tienda/productos/camiseta-pikachu.png", 1500, 200);

            ensureOffer(gBien, 35, java.time.LocalDateTime.now().plusMonths(2));
            ensureOffer(superBall, 25, java.time.LocalDateTime.now().plusWeeks(2));
            ensureOffer(camiseta, 15, java.time.LocalDateTime.now().plusWeeks(2));
        } catch (Exception e) {
            log.warn("No se pudo inicializar catalogo demo: {}", e.getMessage());
        }
    }

    private Category ensureCategory(String slug, String name) {
        return categoryRepository.findBySlugIgnoreCase(slug)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .slug(slug)
                        .name(name)
                        .build()));
    }

    private Product ensureProduct(String name, String description, Category category, User seller, String imageUrl, int price, int stock) {
        return productRepository.findAll().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> productRepository.save(Product.builder()
                        .name(name)
                        .description(description)
                        .category(category)
                        .seller(seller)
                        .imageUrl(imageUrl)
                        .price(java.math.BigDecimal.valueOf(price))
                        .stock(stock)
                        .active(true)
                        .build()));
    }

    private void ensureOffer(Product product, int discountPct, java.time.LocalDateTime endsAt) {
        boolean exists = productOfferRepository.findActive(java.time.LocalDateTime.now()).stream()
                .anyMatch(o -> o.getProduct().getId().equals(product.getId()));
        if (!exists) {
            productOfferRepository.save(ProductOffer.builder()
                    .product(product)
                    .discountPct(discountPct)
                    .endsAt(endsAt)
                    .active(true)
                    .build());
        }
    }
}
