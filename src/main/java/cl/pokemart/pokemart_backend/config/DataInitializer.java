package cl.pokemart.pokemart_backend.config;

import cl.pokemart.pokemart_backend.model.catalog.Category;
import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.model.catalog.ProductOffer;
import cl.pokemart.pokemart_backend.model.user.Role;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.service.order.OrderService;
import cl.pokemart.pokemart_backend.repository.catalog.CategoryRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductOfferRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductRepository;
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
    private final OrderService orderService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataInitializer(UserService userService,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           ProductOfferRepository productOfferRepository,
                           OrderService orderService) {
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productOfferRepository = productOfferRepository;
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) {
        seedUsersAndCatalog();
        seedOrdersDemo();
    }

    private void seedUsersAndCatalog() {
        try {
            // Admin y vendedor con credenciales pedidas
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
        } catch (Exception e) {
            log.warn("Seed general falló: {}", e.getMessage());
        }
    }

    private void seedOrdersDemo() {
        try {
            List<Product> products = productRepository.findAllActive();
            if (products.isEmpty()) return;

            // pick first cliente
            Optional<User> anyClient = userService.findAll().stream().filter(u -> u.getRole() == Role.CLIENTE).findFirst();
            User customer = anyClient.orElse(null);

            List<Map<String, Object>> items = new java.util.ArrayList<>();
            items.add(new java.util.HashMap<>(Map.of("productoId", products.get(0).getId(), "cantidad", 1)));
            if (products.size() > 1) {
                items.add(new java.util.HashMap<>(Map.of("productoId", products.get(1).getId(), "cantidad", 2)));
            }

            Map<String, Object> request = new java.util.HashMap<>();
            request.put("nombre", customer != null && customer.getProfile() != null ? customer.getProfile().getNombre() : "Cliente");
            request.put("apellido", customer != null && customer.getProfile() != null ? customer.getProfile().getApellido() : "Pokemart");
            request.put("correo", customer != null ? customer.getEmail() : "cliente@pokemart.cl");
            request.put("telefono", customer != null && customer.getProfile() != null ? customer.getProfile().getTelefono() : "+56900000000");
            request.put("region", "Kanto");
            request.put("comuna", "Ciudad Central");
            request.put("calle", "Calle Principal 123");
            request.put("departamento", "Depto 101");
            request.put("notas", "Entrega en horario laboral");
            request.put("metodoPago", "credit");
            request.put("items", items);

            // simple idempotency: only create if empty
            if (orderService.listForAdmin().isEmpty()) {
                var orderRequest = objectMapper.convertValue(request, cl.pokemart.pokemart_backend.dto.order.OrderRequest.class);
                orderService.createOrder(orderRequest, customer);
            }
        } catch (Exception e) {
            log.warn("No se pudieron sembrar ordenes demo: {}", e.getMessage());
        }
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
                    if (role != Role.CLIENTE) continue; // admin/vendedor ya seedeados

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
        return productRepository.findAll().stream()
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
