package cl.pokemart.pokemart_backend.config;

import cl.pokemart.pokemart_backend.dto.order.OrderItemRequest;
import cl.pokemart.pokemart_backend.dto.order.OrderRequest;
import cl.pokemart.pokemart_backend.model.catalog.Category;
import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.model.catalog.ProductOffer;
import cl.pokemart.pokemart_backend.model.catalog.ProductReview;
import cl.pokemart.pokemart_backend.model.catalog.ProductStockBase;
import cl.pokemart.pokemart_backend.model.user.Role;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.repository.catalog.CategoryRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductOfferRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductReviewRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductStockBaseRepository;
import cl.pokemart.pokemart_backend.service.order.OrderService;
import cl.pokemart.pokemart_backend.service.user.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductOfferRepository productOfferRepository;
    private final ProductStockBaseRepository productStockBaseRepository;
    private final ProductReviewRepository productReviewRepository;
    private final OrderService orderService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataInitializer(UserService userService,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           ProductOfferRepository productOfferRepository,
                           ProductStockBaseRepository productStockBaseRepository,
                           ProductReviewRepository productReviewRepository,
                           OrderService orderService) {
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productOfferRepository = productOfferRepository;
        this.productStockBaseRepository = productStockBaseRepository;
        this.productReviewRepository = productReviewRepository;
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) {
        seedUsersAndCatalog();
        seedOrdersDemo();
    }

    private void seedUsersAndCatalog() {
        try {
            userService.ensureUser(
                    "admin@pokemart.cl",
                    "admin",
                    "admin123",
                    "Admin",
                    "Pokemart",
                    "9.876.543-2",
                    "Oficina Central Pokemart",
                    "Kanto",
                    "Ciudad Central",
                    LocalDate.of(1985, 1, 1),
                    "+56911111111",
                    Role.ADMIN
            );
            User vendor = userService.ensureUser(
                    "vendedor@pokemart.cl",
                    "vendedor",
                    "vendedor123",
                    "Vendedor",
                    "Pokemart",
                    "22.222.222-2",
                    "Av. Vendedor 456",
                    "Kanto",
                    "Ciudad Central",
                    LocalDate.of(1990, 2, 2),
                    "+56922222222",
                    Role.VENDEDOR
            );

            seedUsersFromJson(relPath("../Poke-Mart.Fullstack2_React/src/data/users.json"));
            seedCatalogFromJson(
                    relPath("../Poke-Mart.Fullstack2_React/src/data/productos.json"),
                    relPath("../Poke-Mart.Fullstack2_React/src/data/ofertas.json"),
                    vendor
            );
            seedReviewsFromJson(relPath("../Poke-Mart.Fullstack2_React/src/data/reviews.json"));
        } catch (Exception e) {
            log.warn("Seed general falló: {}", e.getMessage());
        }
    }

    private void seedOrdersDemo() {
        try {
            List<Product> products = productRepository.findAllActive();
            if (products.isEmpty()) {
                log.warn("No se sembraron ordenes demo porque no hay productos activos cargados");
                return;
            }

            List<User> allUsers = userService.findAll();
            Optional<User> anyClient = allUsers.stream().filter(u -> u.getRole() == Role.CLIENTE).findFirst();
            User customer = anyClient.orElse(allUsers.stream().filter(u -> u.getRole() == Role.VENDEDOR || u.getRole() == Role.ADMIN).findFirst().orElse(null));

            int existing = orderService.listForAdmin().size();
            if (existing == 0) {
                log.info("Sembrando ordenes demo (productos: {}, usuarios: {})", products.size(), allUsers.size());
                createOrderDemo(customer, products, "Cliente", "Demo", "cliente@pokemart.cl", "+56900000000", "Kanto", "Ciudad Central", "Calle Principal 123", "Depto 101", 1, 2);
                createOrderDemo(customer, products, "Ash", "Ketchum", "ash@pokemart.cl", "+56912345678", "Kanto", "Pueblo Paleta", "Camino 1", "Casa 2", 2, 1);
                createOrderDemo(customer, products, "Misty", "Waterflower", "misty@pokemart.cl", "+56987654321", "Kanto", "Ciudad Celeste", "Av. Lago 89", "Depto 901", 1, 1);
            } else {
                log.info("No se sembraron ordenes demo (ya existen {} ordenes)", existing);
            }
        } catch (Exception e) {
            log.warn("No se pudieron sembrar ordenes demo", e);
        }
    }

    private void createOrderDemo(User customer, List<Product> products, String nombre, String apellido, String correo, String telefono, String region, String comuna, String calle, String departamento, int... cantidades) {
        if (products.isEmpty()) return;
        List<OrderItemRequest> items = new java.util.ArrayList<>();
        for (int i = 0; i < products.size() && i < cantidades.length; i++) {
            int qty = Math.max(1, cantidades[i]);
            items.add(new OrderItemRequest(products.get(i).getId(), qty));
        }
        if (items.isEmpty()) {
            items.add(new OrderItemRequest(products.get(0).getId(), 1));
        }

        OrderRequest orderRequest = new OrderRequest(
                nombre,
                apellido,
                correo,
                telefono,
                region,
                comuna,
                calle,
                departamento,
                "Orden demo generada por DataInitializer",
                "credit",
                items
        );
        orderService.createOrder(orderRequest, customer);
        log.info("Orden demo creada para {} {} con {} items", nombre, apellido, items.size());
    }

    private void seedUsersFromJson(Path jsonPath) {
        try {
            File file = jsonPath.toFile();
            if (!file.exists()) {
                log.warn("No se encontró users.json en {}", jsonPath);
                return;
            }
            JsonNode root = objectMapper.readTree(file);
            JsonNode usersNode = root.has("users") ? root.get("users") : root;
            if (usersNode != null && usersNode.isArray()) {
                for (JsonNode u : usersNode) {
                    String roleStr = u.path("role").asText("cliente").toUpperCase();
                    Role role = "ADMIN".equals(roleStr) ? Role.ADMIN : "VENDEDOR".equals(roleStr) ? Role.VENDEDOR : Role.CLIENTE;
                    if (role != Role.CLIENTE) continue;

                    String email = u.path("email").asText();
                    String username = u.path("username").asText();
                    String password = u.path("password").asText("cliente123");
                    String nombre = u.path("nombre").asText(null);
                    String apellido = u.path("apellido").asText(null);
                    String rut = u.path("run").asText(null);
                    String direccion = u.path("direccion").asText(null);
                    String region = u.path("region").asText(null);
                    String comuna = u.path("comuna").asText(null);
                    LocalDate fechaNacimiento = null;
                    try {
                        String fn = u.path("fechaNacimiento").asText(null);
                        if (fn != null) fechaNacimiento = LocalDate.parse(fn);
                    } catch (Exception ignored) {
                    }
                    try {
                        userService.ensureUser(
                                email,
                                username,
                                password,
                                nombre,
                                apellido,
                                rut,
                                direccion,
                                region,
                                comuna,
                                fechaNacimiento,
                                null,
                                role
                        );
                    } catch (Exception ex) {
                        log.warn("No se pudo crear usuario {}: {}", username, ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error leyendo users.json: {}", e.getMessage());
        }
    }

    private void seedCatalogFromJson(Path productsPath, Path offersPath, User seller) {
        try {
            File file = productsPath.toFile();
            if (!file.exists()) {
                log.warn("No se encontró productos.json en {}", productsPath);
                return;
            }
            List<Map<String, Object>> products = objectMapper.readValue(file, new TypeReference<>() {});
            Map<String, Category> categories = new HashMap<>();

            for (Map<String, Object> p : products) {
                String categoria = (String) p.getOrDefault("categoria", "general");
                String slug = slugify(categoria);
                Category cat = categories.computeIfAbsent(slug, s -> ensureCategory(s, categoria));

                String name = (String) p.get("nombre");
                String desc = (String) p.getOrDefault("descripcion", "");
                String image = (String) p.getOrDefault("imagen", "");
                BigDecimal price = BigDecimal.valueOf(((Number) p.getOrDefault("precio", 0)).doubleValue());
                int stock = ((Number) p.getOrDefault("stock", 0)).intValue();

                ensureProduct(name, desc, cat, seller, image, price, stock);
            }

            File offersFile = offersPath.toFile();
            if (!offersFile.exists()) {
                log.warn("No se encontró ofertas.json en {}", offersPath);
                return;
            }
            List<Map<String, Object>> offers = objectMapper.readValue(offersFile, new TypeReference<>() {});
            for (Map<String, Object> o : offers) {
                Number productId = (Number) o.get("id");
                if (productId == null) continue;
                Product product = productRepository.findById(productId.longValue()).orElse(null);
                if (product == null) continue;
                int pct = ((Number) o.getOrDefault("discountPct", 0)).intValue();
                LocalDateTime endsAt = null;
                try {
                    String ends = (String) o.get("endsAt");
                    if (ends != null) endsAt = OffsetDateTime.parse(ends).toLocalDateTime();
                } catch (Exception ignored) {
                }
                ensureOffer(product, pct, endsAt);
            }
        } catch (Exception e) {
            log.warn("Error cargando catalogo desde json: {}", e.getMessage());
        }
    }

    private Category ensureCategory(String slug, String name) {
        return categoryRepository.findBySlugIgnoreCase(slug)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .slug(slug)
                        .name(name)
                        .build()));
    }

    private Product ensureProduct(String name, String description, Category category, User seller, String imageUrl, BigDecimal price, int stock) {
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> productRepository.save(Product.builder()
                        .name(name)
                        .description(description)
                        .category(category)
                        .seller(seller)
                        .imageUrl(imageUrl)
                        .price(price)
                        .stock(stock)
                        .active(true)
                        .build()));
        productStockBaseRepository.findByProduct(product)
                .orElseGet(() -> productStockBaseRepository.save(ProductStockBase.builder()
                        .product(product)
                        .stockBase(stock)
                        .build()));
        return product;
    }

    private void ensureOffer(Product product, int discountPct, LocalDateTime endsAt) {
        boolean exists = productOfferRepository.findActive(LocalDateTime.now()).stream()
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

    private void seedReviewsFromJson(Path reviewsPath) {
        try {
            File file = reviewsPath.toFile();
            if (!file.exists()) {
                log.warn("No se encontró reviews.json en {}", reviewsPath);
                return;
            }
            Map<String, List<Map<String, Object>>> data = objectMapper.readValue(file, new TypeReference<>() {});
            if (data == null || data.isEmpty()) {
                log.info("reviews.json vacio, no se sembraron reseñas");
                return;
            }
            List<User> clients = userService.findAll().stream()
                    .filter(u -> u.getRole() == Role.CLIENTE)
                    .toList();
            if (clients.isEmpty()) {
                log.warn("No hay clientes para asociar reseñas; se omite seed de reviews");
                return;
            }
            int clientIndex = 0;
            for (Map.Entry<String, List<Map<String, Object>>> entry : data.entrySet()) {
                Long productId = null;
                try {
                    productId = Long.parseLong(entry.getKey());
                } catch (NumberFormatException ignored) {
                }
                if (productId == null) continue;
                if (productReviewRepository.countByProductId(productId) > 0) continue;
                Product product = productRepository.findById(productId).orElse(null);
                if (product == null) continue;

                for (Map<String, Object> r : entry.getValue()) {
                    User author = clients.get(clientIndex % clients.size());
                    clientIndex++;
                    Integer rating = ((Number) r.getOrDefault("rating", 5)).intValue();
                    String comment = (String) r.getOrDefault("texto", "Sin comentario");
                    LocalDate date = null;
                    try {
                        String fecha = (String) r.get("fecha");
                        if (fecha != null) date = LocalDate.parse(fecha);
                    } catch (Exception ignored) {}
                    ProductReview review = ProductReview.builder()
                            .product(product)
                            .user(author)
                            .authorName(author.getDisplayName())
                            .rating(rating)
                            .comment(comment)
                            .createdAt(date != null ? date.atStartOfDay() : LocalDateTime.now())
                            .build();
                    productReviewRepository.save(review);
                }
            }
            log.info("Seeded reviews desde reviews.json");
        } catch (Exception e) {
            log.warn("Error sembrando reseñas: {}", e.getMessage());
        }
    }

    private Path relPath(String relative) {
        return Path.of(relative).normalize();
    }

    private String slugify(String input) {
        if (input == null) return "general";
        String noAccent = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noAccent.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
    }
}
