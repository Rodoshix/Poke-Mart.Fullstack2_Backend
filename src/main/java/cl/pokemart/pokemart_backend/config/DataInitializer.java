package cl.pokemart.pokemart_backend.config;

import cl.pokemart.pokemart_backend.dto.blog.BlogRequest;
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
import cl.pokemart.pokemart_backend.service.blog.BlogService;
import cl.pokemart.pokemart_backend.service.order.OrderService;
import cl.pokemart.pokemart_backend.service.user.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    private final BlogService blogService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    public DataInitializer(UserService userService,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           ProductOfferRepository productOfferRepository,
                           ProductStockBaseRepository productStockBaseRepository,
                           ProductReviewRepository productReviewRepository,
                           OrderService orderService,
                           BlogService blogService) {
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productOfferRepository = productOfferRepository;
        this.productStockBaseRepository = productStockBaseRepository;
        this.productReviewRepository = productReviewRepository;
        this.orderService = orderService;
        this.blogService = blogService;
    }

    @Override
    public void run(String... args) {
        seedUsersAndCatalog();
        seedOrdersDemo();
    }

    private void seedUsersAndCatalog() {
        try {
            userService.ensureUser(
                    "admin@gmail.com",
                    "admin",
                    "admin123",
                    "Admin",
                    "Pokemart",
                    "88.888.888-8",
                    "Oficina Central Pokemart",
                    "Kanto",
                    "Ciudad Central",
                    LocalDate.of(1985, 1, 1),
                    "+56911111111",
                    Role.ADMIN
            );
            userService.ensureUser(
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

            seedUsersFromJson("users.json");
            seedCatalogFromJson("productos.json", "ofertas.json");
            seedReviewsFromJsonFlexible("reviews.json");
            seedBlogs();
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
                BigDecimal.ZERO,
                items
        );
        orderService.createOrder(orderRequest, customer);
        log.info("Orden demo creada para {} {} con {} items", nombre, apellido, items.size());
    }

    private void seedBlogs() {
        try {
            if (!blogService.listAdmin(null, null, null).isEmpty()) {
                log.info("No se sembraron blogs (ya existen entradas)");
                return;
            }

            log.info("Sembrando blogs demo");
            BlogRequest b1 = buildBlog("Guia rapida de Poke Balls para principiantes",
                    "No sabes cuando usar una Super Ball o una Ultra Ball? Repasamos los tipos de Poke Balls, sus ventajas y consejos para mejorar tu tasa de captura.",
                    "Las Poke Balls basicas funcionan bien al inicio, pero conviene llevar siempre algunas Super Balls para encuentros sorpresivos. Recuerda usar bayas antes de lanzar y aprovechar los momentos en los que el Pokemon esta menos agresivo.",
                    "Guias",
                    "https://images.unsplash.com/photo-1613771404721-f93648b600b0?auto=format&fit=crop&w=800&q=80",
                    List.of("pokeballs", "consejos", "principiantes"),
                    "PUBLISHED");

            BlogRequest b2 = buildBlog("Kit de expedicion esencial para rutas largas",
                    "Checklist practica para que no te falte nada en tu proxima aventura por las rutas.",
                    "Incluye carpa liviana, repelentes, sacos de dormir y baterias de respaldo. Ajusta el kit al clima de la region y considera peso/espacio en tu mochila.",
                    "Expedicion",
                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=800&q=80",
                    List.of("expedicion", "checklist"),
                    "PUBLISHED");

            BlogRequest b3 = buildBlog("Tecnologia Rotom: ventajas del Rotom Phone",
                    "El Rotom Phone es mas que un celular: mapa dinamico, Pokedex integrada y utilidades unicas.",
                    "Ideal para entrenadores que viajan. Aprovecha la Pokedex integrada, mapas sin conexion y la compatibilidad con accesorios.",
                    "Tecnologia",
                    "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=800&q=80",
                    List.of("tecnologia", "rotom", "pokedex"),
                    "PUBLISHED");

            BlogRequest b4 = buildBlog("Como cuidar tus zapatillas de entrenador",
                    "Trucos sencillos para alargar la vida de tus zapatillas en climas humedos o con mucho polvo.",
                    "Lavalas con suavidad, secalas a la sombra y rota pares para extender su vida util. Usa sprays repelentes si viajas a zonas lluviosas.",
                    "Ropa",
                    "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=800&q=80",
                    List.of("ropa", "cuidado", "consejos"),
                    "PUBLISHED");

            blogService.create(b1);
            blogService.create(b2);
            blogService.create(b3);
            blogService.create(b4);
        } catch (Exception e) {
            log.warn("No se pudieron sembrar blogs demo: {}", e.getMessage());
        }
    }

    private BlogRequest buildBlog(String titulo, String descripcion, String contenido, String categoria, String imagen, List<String> etiquetas, String estado) {
        BlogRequest r = new BlogRequest();
        r.setTitulo(titulo);
        r.setDescripcion(descripcion);
        r.setContenido(contenido);
        r.setCategoria(categoria);
        r.setImagen(imagen);
        r.setEtiquetas(etiquetas);
        r.setEstado(estado);
        return r;
    }

    private void seedUsersFromJson(String resourcePath) {
        try (InputStream is = readResource(resourcePath)) {
            if (is == null) {
                log.warn("No se encontró users.json en {}", resourcePath);
                return;
            }
            JsonNode root = objectMapper.readTree(is);
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

    private void seedCatalogFromJson(String productsPath, String offersPath) {
        try (InputStream productsIs = readResource(productsPath)) {
            if (productsIs == null) {
                log.warn("No se encontró productos.json en {}", productsPath);
                return;
            }
            List<Map<String, Object>> products = objectMapper.readValue(productsIs, new TypeReference<>() {});
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

                ensureProduct(name, desc, cat, image, price, stock);
            }

            try (InputStream offersIs = readResource(offersPath)) {
                if (offersIs == null) {
                    log.warn("No se encontró ofertas.json en {}", offersPath);
                    return;
                }
                List<Map<String, Object>> offers = objectMapper.readValue(offersIs, new TypeReference<>() {});
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

    private Product ensureProduct(String name, String description, Category category, String imageUrl, BigDecimal price, int stock) {
        String normalizedImage = persistImageSeed(imageUrl, name);
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> productRepository.save(Product.builder()
                        .name(name)
                        .description(description)
                        .category(category)
                        .imageUrl(normalizedImage)
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
            int safeDiscount = Math.min(99, Math.max(1, discountPct));
            productOfferRepository.save(ProductOffer.builder()
                    .product(product)
                    .discountPct(safeDiscount)
                    .endsAt(endsAt)
                    .active(true)
                    .build());
        }
    }

    private void seedReviewsFromJson(String reviewsPath) {
        try (InputStream is = readResource(reviewsPath)) {
            if (is == null) {
                log.warn("No se encontró reviews.json en {}", reviewsPath);
                return;
            }
            Map<String, List<Map<String, Object>>> data = objectMapper.readValue(is, new TypeReference<>() {});
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


    /**
     * Version flexible: asigna rese?as en orden del JSON a los productos activos ordenados por id,
     * evitando depender de IDs fijos cuando las secuencias cambian tras truncar tablas.
     */
    private void seedReviewsFromJsonFlexible(String reviewsPath) {
        try (InputStream is = readResource(reviewsPath)) {
            if (is == null) {
                log.warn("No se encontro reviews.json en {}", reviewsPath);
                return;
            }
            Map<String, List<Map<String, Object>>> data = objectMapper.readValue(is, new TypeReference<>() {});
            if (data == null || data.isEmpty()) {
                log.info("reviews.json vacio, no se sembraron resenas");
                return;
            }
            List<User> clients = userService.findAll().stream()
                    .filter(u -> u.getRole() == Role.CLIENTE)
                    .toList();
            if (clients.isEmpty()) {
                log.warn("No hay clientes para asociar resenas; se omite seed de reviews");
                return;
            }
            List<Product> products = productRepository.findAllActive().stream()
                    .sorted(java.util.Comparator.comparing(Product::getId))
                    .toList();
            if (products.isEmpty()) {
                log.warn("No hay productos activos para seed de reviews");
                return;
            }

            var reviewEntries = data.entrySet().stream().toList();
            int clientIndex = 0;
            for (int i = 0; i < reviewEntries.size() && i < products.size(); i++) {
                Product product = products.get(i);
                if (product == null) continue;
                if (productReviewRepository.countByProductId(product.getId()) > 0) continue;
                List<Map<String, Object>> reviews = reviewEntries.get(i).getValue();
                if (reviews == null || reviews.isEmpty()) continue;

                for (Map<String, Object> r : reviews) {
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
            log.info("Seeded reviews desde reviews.json (flexible)");
        } catch (Exception e) {
            log.warn("Error sembrando resenas: {}", e.getMessage());
        }
    }

    private InputStream readResource(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return resource.exists() ? resource.getInputStream() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String persistImageSeed(String rawImage, String nameHint) {
        if (rawImage == null || rawImage.isBlank()) return null;
        String value = rawImage.trim();
        // Si ya es URL pública o /uploads, devolver tal cual
        if (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("/uploads/")) {
            return value;
        }
        try {
            Path baseDir = Paths.get(uploadsDir).toAbsolutePath().normalize().resolve("products");
            Files.createDirectories(baseDir);

            String ext = "png";
            String data = value;
            if (value.startsWith("data:")) {
                int comma = value.indexOf(',');
                if (comma > 0) {
                    String meta = value.substring(5, comma);
                    if (meta.contains("jpeg") || meta.contains("jpg")) ext = "jpg";
                    else if (meta.contains("webp")) ext = "webp";
                    data = value.substring(comma + 1);
                }
            } else if (value.contains("base64,")) {
                data = value.substring(value.indexOf("base64,") + 7);
            }
            byte[] bytes = java.util.Base64.getDecoder().decode(data);
            String safeName = slugify(nameHint != null ? nameHint : UUID.randomUUID().toString());
            String filename = safeName + "-" + UUID.randomUUID() + "." + ext;
            Path target = baseDir.resolve(filename);
            Files.write(target, bytes);
            return "/uploads/products/" + filename;
        } catch (Exception e) {
            log.warn("No se pudo persistir imagen seed, se deja valor original: {}", e.getMessage());
            return value;
        }
    }

    private String slugify(String input) {
        if (input == null) return "general";
        String noAccent = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noAccent.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
    }
}
