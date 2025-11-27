package cl.pokemart.pokemart_backend.service.catalog;

import cl.pokemart.pokemart_backend.dto.catalog.ProductRequest;
import cl.pokemart.pokemart_backend.dto.catalog.ProductResponse;
import cl.pokemart.pokemart_backend.dto.catalog.AdminOfferRequest;
import cl.pokemart.pokemart_backend.dto.catalog.AdminOfferResponse;
import cl.pokemart.pokemart_backend.dto.catalog.ReviewRequest;
import cl.pokemart.pokemart_backend.dto.catalog.ReviewResponse;
import cl.pokemart.pokemart_backend.dto.catalog.AdminReviewResponse;
import cl.pokemart.pokemart_backend.model.catalog.Category;
import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.model.catalog.ProductOffer;
import cl.pokemart.pokemart_backend.model.catalog.ProductStockBase;
import cl.pokemart.pokemart_backend.model.catalog.ProductReview;
import cl.pokemart.pokemart_backend.model.user.Role;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.repository.catalog.CategoryRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductOfferRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductStockBaseRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductReviewRepository;
import cl.pokemart.pokemart_backend.repository.order.OrderItemRepository;
import cl.pokemart.pokemart_backend.service.common.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductOfferRepository productOfferRepository;
    private final ProductStockBaseRepository productStockBaseRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductReviewRepository productReviewRepository;
    private final FileStorageService fileStorageService;
    private final java.util.concurrent.ConcurrentHashMap<String, CacheEntry<List<ProductResponse>>> productsCache = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 60_000; // 60s

    public CatalogService(CategoryRepository categoryRepository,
                          ProductRepository productRepository,
                          ProductOfferRepository productOfferRepository,
                          ProductStockBaseRepository productStockBaseRepository,
                          OrderItemRepository orderItemRepository,
                          ProductReviewRepository productReviewRepository,
                          FileStorageService fileStorageService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productOfferRepository = productOfferRepository;
        this.productStockBaseRepository = productStockBaseRepository;
        this.orderItemRepository = orderItemRepository;
        this.productReviewRepository = productReviewRepository;
        this.fileStorageService = fileStorageService;
    }

    // Public
    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts(String categorySlug) {
        String cacheKey = StringUtils.hasText(categorySlug) ? "cat:" + categorySlug.toLowerCase() : "all";
        List<ProductResponse> cached = getCache(cacheKey);
        if (cached != null) return cached;

        List<Product> products = StringUtils.hasText(categorySlug)
                ? productRepository.findActiveByCategory(categorySlug)
                : productRepository.findAllActive();
        var offers = productOfferRepository.findActive(LocalDateTime.now());
        List<ProductResponse> response = products.stream()
                .map(p -> mapToResponse(p, findOfferForProduct(p, offers), null))
                .toList();
        putCache(cacheKey, response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<AdminOfferResponse> listOffersForManagement(boolean includeInactive, User current) {
        ensureManager(current);
        List<ProductOffer> offers = includeInactive ? productOfferRepository.findAll() : productOfferRepository.findActive(LocalDateTime.now());
        return offers.stream()
                .filter(o -> isOfferVisibleForUser(o, current))
                .map(AdminOfferResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminOfferResponse getOfferForManagement(Long id, User current) {
        ensureManager(current);
        ProductOffer offer = productOfferRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Oferta no encontrada"));
        return AdminOfferResponse.from(offer);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listActiveOffers() {
        var offers = productOfferRepository.findActive(LocalDateTime.now());
        return offers.stream()
                .filter(o -> o.getProduct() != null && Boolean.TRUE.equals(o.getProduct().getActive()))
                .map(o -> mapToResponse(o.getProduct(), Optional.of(o), null))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findActiveById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        var offers = productOfferRepository.findActive(LocalDateTime.now());
        return mapToResponse(product, findOfferForProduct(product, offers), null);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listReviews(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        return productReviewRepository.findByProductId(product.getId()).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminReviewResponse> listReviewsAdmin() {
        return productReviewRepository.findAllWithProduct().stream()
                .map(AdminReviewResponse::from)
                .toList();
    }

    public ReviewResponse addReview(Long productId, ReviewRequest request, User current) {
        if (current == null) throw new SecurityException("No autenticado");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        ProductReview review = ProductReview.builder()
                .product(product)
                .user(current)
                .rating(request.getRating())
                .comment(request.getComment())
                .authorName(current.getDisplayName())
                .build();
        ProductReview saved = productReviewRepository.save(review);
        updateReviewStats(product.getId());
        invalidateProductsCache();
        return ReviewResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProductsForManagement(boolean includeInactive, User current) {
        ensureManager(current);
        List<Product> products = includeInactive ? productRepository.findAll() : productRepository.findAllActive();
        var offers = productOfferRepository.findActive(LocalDateTime.now());
        return products.stream()
                .map(p -> mapToResponse(p, findOfferForProduct(p, offers), null))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductForManagement(Long id, User current) {
        ensureManager(current);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        var offers = productOfferRepository.findActive(LocalDateTime.now());
        return mapToResponse(product, findOfferForProduct(product, offers), null);
    }

    // Admin/Vendedor
    public ProductResponse createProduct(ProductRequest request, User current) {
        ensureAdmin(current);
        Category category = resolveCategory(request.getCategoriaSlug(), request.getCategoriaSlug());
        String imageValue = normalizeImage(request.getImagenUrl());

        Product product = Product.builder()
                .name(request.getNombre())
                .description(request.getDescripcion())
                .price(request.getPrecio() != null ? request.getPrecio() : BigDecimal.ZERO)
                .stock(request.getStock() != null ? request.getStock() : 0)
                .imageUrl(imageValue)
                .reviewCount(0L)
                .reviewAvg(0.0)
                .category(category)
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        ensureStockBase(saved, request.getStockBase());
        invalidateProductsCache();
        return mapToResponse(saved, Optional.empty(), null);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request, User current) {
        ensureAdmin(current);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        Category category = resolveCategory(request.getCategoriaSlug(), request.getCategoriaSlug());

        String previousImageUrl = product.getImageUrl();
        String imageValue = normalizeImage(request.getImagenUrl());
        product.setName(request.getNombre());
        product.setDescription(request.getDescripcion());
        product.setPrice(request.getPrecio());
        product.setStock(request.getStock());
        product.setImageUrl(imageValue);
        product.setCategory(category);
        if (request.getStockBase() != null) {
            ensureStockBase(product, request.getStockBase());
        }
        // Si la imagen cambió, intenta borrar la anterior para no dejar archivos huérfanos (solo si era un archivo/URL)
        if (StringUtils.hasText(previousImageUrl)
                && imageValue != null
                && !previousImageUrl.equals(imageValue)
                && !isDataUrl(previousImageUrl)) {
            fileStorageService.deleteByUrl(previousImageUrl);
        }

        invalidateProductsCache();
        return mapToResponse(product, findOfferForProduct(product, productOfferRepository.findActive(LocalDateTime.now())), null);
    }

    public ProductResponse setProductActive(Long id, boolean active, User current) {
        ensureManager(current);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        product.setActive(active);
        invalidateProductsCache();
        return mapToResponse(product, findOfferForProduct(product, productOfferRepository.findActive(LocalDateTime.now())), null);
    }

    public void deleteProduct(Long id, User current, boolean hardDelete) {
        ensureAdmin(current);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        String imageUrl = product.getImageUrl();
        if (!hardDelete) {
            product.setActive(false);
            return;
        }
        var offers = productOfferRepository.findByProduct(product);
        if (offers != null && !offers.isEmpty()) {
            productOfferRepository.deleteAll(offers);
        }
        productStockBaseRepository.deleteByProduct(product);
        var orderItems = orderItemRepository.findByProducto(product);
        if (orderItems != null && !orderItems.isEmpty()) {
            orderItems.forEach(item -> item.setProducto(null));
            orderItemRepository.saveAll(orderItems);
        }
        productRepository.delete(product);
        fileStorageService.deleteByUrl(imageUrl);
        invalidateProductsCache();
    }

    public ProductResponse addOffer(Long productId, Integer discountPct, LocalDateTime endsAt, User current) {
        ensureAdmin(current);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        if (discountPct == null || discountPct <= 0 || discountPct > 99) {
            throw new IllegalArgumentException("Descuento invalido (1-99%)");
        }
        ProductOffer offer = ProductOffer.builder()
                .product(product)
                .discountPct(discountPct)
                .endsAt(endsAt)
                .active(true)
                .build();
        productOfferRepository.save(offer);
        invalidateProductsCache();
        return mapToResponse(product, Optional.of(offer), null);
    }

    public AdminOfferResponse createOffer(AdminOfferRequest request, User current) {
        ensureAdmin(current);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        validateDiscount(request.getDiscountPct());
        ProductOffer offer = ProductOffer.builder()
                .product(product)
                .discountPct(request.getDiscountPct())
                .endsAt(parseDateTime(request.getEndsAt()))
                .active(request.getActive() == null ? true : request.getActive())
                .build();
        ProductOffer saved = productOfferRepository.save(offer);
        return AdminOfferResponse.from(saved);
    }

    public AdminOfferResponse updateOffer(Long id, AdminOfferRequest request, User current) {
        ensureAdmin(current);
        ProductOffer offer = productOfferRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Oferta no encontrada"));
        if (request.getProductId() != null && !request.getProductId().equals(offer.getProduct().getId())) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
            offer.setProduct(product);
        }
        validateDiscount(request.getDiscountPct());
        offer.setDiscountPct(request.getDiscountPct());
        offer.setEndsAt(parseDateTime(request.getEndsAt()));
        if (request.getActive() != null) {
            offer.setActive(request.getActive());
        }
        invalidateProductsCache();
        return AdminOfferResponse.from(offer);
    }

    public AdminOfferResponse setOfferActive(Long id, boolean active, User current) {
        ensureAdmin(current);
        ProductOffer offer = productOfferRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Oferta no encontrada"));
        offer.setActive(active);
        return AdminOfferResponse.from(offer);
    }

    public void deleteOffer(Long id, boolean hard, User current) {
        ensureAdmin(current);
        ProductOffer offer = productOfferRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Oferta no encontrada"));
        if (hard) {
            productOfferRepository.delete(offer);
        } else {
            offer.setActive(false);
        }
        invalidateProductsCache();
    }

    public void deleteReview(Long id) {
        var review = productReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review no encontrada"));
        Long productId = review.getProduct() != null ? review.getProduct().getId() : null;
        productReviewRepository.delete(review);
        if (productId != null) {
            updateReviewStats(productId);
            invalidateProductsCache();
        }
    }

    // Helpers
    private Category resolveCategory(String slug, String nameFallback) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Categoria requerida");
        }
        return categoryRepository.findBySlugIgnoreCase(slug)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .slug(slug.toLowerCase().replace(" ", "-"))
                        .name(nameFallback != null ? nameFallback : slug)
                        .build()));
    }

    private void ensureManager(User current) {
        if (current == null || (current.getRole() != Role.ADMIN && current.getRole() != Role.VENDEDOR)) {
            throw new SecurityException("No autorizado");
        }
    }

    private void ensureAdmin(User current) {
        if (current == null || current.getRole() != Role.ADMIN) {
            throw new SecurityException("No autorizado");
        }
    }

    private boolean isOfferVisibleForUser(ProductOffer offer, User current) {
        if (current == null) return false;
        return current.getRole() == Role.ADMIN || current.getRole() == Role.VENDEDOR;
    }

    private void validateDiscount(Integer discount) {
        if (discount == null || discount <= 0 || discount > 99) {
            throw new IllegalArgumentException("Descuento invalido (1-99%)");
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Formato de fecha invalido (usar ISO-8601)");
        }
    }

    private Optional<ProductOffer> findOfferForProduct(Product product, List<ProductOffer> offers) {
        if (offers == null) return Optional.empty();
        return offers.stream().filter(o -> o.getProduct().getId().equals(product.getId()) && !o.isExpired()).findFirst();
    }

    private Map<Long, ReviewStats> aggregateReviewStats(List<Product> products) {
        if (products == null || products.isEmpty()) return Map.of();
        List<Long> ids = products.stream().map(Product::getId).toList();
        return productReviewRepository.aggregateByProductIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> new ReviewStats(((Number) row[1]).longValue(), row[2] != null ? ((Number) row[2]).doubleValue() : 0.0)
                ));
    }

    private record ReviewStats(long count, double avg) {}

    private ProductResponse mapToResponse(Product product, Optional<ProductOffer> offerOpt, Map<Long, ReviewStats> statsMap) {
        ProductResponse.OfferInfo offerInfo = null;
        if (offerOpt.isPresent() && !offerOpt.get().isExpired()) {
            var o = offerOpt.get();
            offerInfo = ProductResponse.OfferInfo.builder()
                    .discountPct(o.getDiscountPct())
                    .endsAt(o.getEndsAt() != null ? o.getEndsAt().toString() : null)
                    .build();
        }
        Integer stockBase = productStockBaseRepository.findByProductId(product.getId())
                .map(ProductStockBase::getStockBase)
                .orElse(null);
        long reviewCount = product.getReviewCount() != null ? product.getReviewCount() : 0L;
        double reviewAvg = product.getReviewAvg() != null ? product.getReviewAvg() : 0.0;
        if (statsMap != null && statsMap.containsKey(product.getId())) {
            var stats = statsMap.get(product.getId());
            reviewCount = stats.count();
            reviewAvg = stats.avg();
        }
        return ProductResponse.builder()
                .id(product.getId())
                .nombre(product.getName())
                .descripcion(product.getDescription())
                .precio(product.getPrice())
                .stock(product.getStock())
                .stockBase(stockBase)
                .imagenUrl(product.getImageUrl())
                .categoria(product.getCategory() != null ? product.getCategory().getName() : null)
                .offer(offerInfo)
                .vendedor(null)
                .reviewCount((int) reviewCount)
                .reviewAvg(reviewAvg)
                .active(product.getActive())
                .build();
    }

    private String normalizeImage(String raw) {
        if (!StringUtils.hasText(raw)) return null;
        String value = raw.trim();
        return value;
    }

    private boolean isDataUrl(String value) {
        if (!StringUtils.hasText(value)) return false;
        String v = value.trim().toLowerCase();
        return v.startsWith("data:");
    }

    private void ensureStockBase(Product product, Integer stockBaseValue) {
        int base = stockBaseValue != null ? stockBaseValue : (product.getStock() != null ? product.getStock() : 0);
        ProductStockBase stockBase = productStockBaseRepository.findByProduct(product)
                .orElseGet(() -> ProductStockBase.builder().product(product).build());
        stockBase.setStockBase(base);
        productStockBaseRepository.save(stockBase);
    }

    private void updateReviewStats(Long productId) {
        long count = productReviewRepository.countByProductId(productId);
        double avg = productReviewRepository.averageRating(productId).orElse(0.0);
        productRepository.findById(productId).ifPresent(p -> {
            p.setReviewCount(count);
            p.setReviewAvg(avg);
            productRepository.save(p);
        });
    }

    private void invalidateProductsCache() {
        productsCache.clear();
    }

    private List<ProductResponse> getCache(String key) {
        CacheEntry<List<ProductResponse>> entry = productsCache.get(key);
        if (entry == null) return null;
        if (entry.expiresAt < System.currentTimeMillis()) {
            productsCache.remove(key);
            return null;
        }
        return entry.value;
    }

    private void putCache(String key, List<ProductResponse> value) {
        productsCache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + CACHE_TTL_MS));
    }

    private record CacheEntry<T>(T value, long expiresAt) {}

    private record ReviewStats(long count, double avg) {}
}


