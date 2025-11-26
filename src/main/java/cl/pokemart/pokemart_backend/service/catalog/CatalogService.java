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
        List<Product> products = StringUtils.hasText(categorySlug)
                ? productRepository.findActiveByCategory(categorySlug)
                : productRepository.findAllActive();
        var offers = productOfferRepository.findActive(LocalDateTime.now());
        return products.stream()
                .map(p -> mapToResponse(p, findOfferForProduct(p, offers)))
                .toList();
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
                .map(o -> mapToResponse(o.getProduct(), Optional.of(o)))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findActiveById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        var offers = productOfferRepository.findActive(LocalDateTime.now());
        return mapToResponse(product, findOfferForProduct(product, offers));
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
        return ReviewResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProductsForManagement(boolean includeInactive, User current) {
        ensureManager(current);
        List<Product> products = includeInactive ? productRepository.findAll() : productRepository.findAllActive();
        var offers = productOfferRepository.findActive(LocalDateTime.now());
        return products.stream()
                .map(p -> mapToResponse(p, findOfferForProduct(p, offers)))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductForManagement(Long id, User current) {
        ensureManager(current);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        var offers = productOfferRepository.findActive(LocalDateTime.now());
        return mapToResponse(product, findOfferForProduct(product, offers));
    }

    // Admin/Vendedor
    public ProductResponse createProduct(ProductRequest request, User current) {
        ensureAdmin(current);
        Category category = resolveCategory(request.getCategoriaSlug(), request.getCategoriaSlug());

        Product product = Product.builder()
                .name(request.getNombre())
                .description(request.getDescripcion())
                .price(request.getPrecio() != null ? request.getPrecio() : BigDecimal.ZERO)
                .stock(request.getStock() != null ? request.getStock() : 0)
                .imageUrl(request.getImagenUrl())
                .category(category)
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        ensureStockBase(saved, request.getStockBase());
        return mapToResponse(saved, Optional.empty());
    }

    public ProductResponse updateProduct(Long id, ProductRequest request, User current) {
        ensureAdmin(current);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        Category category = resolveCategory(request.getCategoriaSlug(), request.getCategoriaSlug());

        String previousImageUrl = product.getImageUrl();
        product.setName(request.getNombre());
        product.setDescription(request.getDescripcion());
        product.setPrice(request.getPrecio());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImagenUrl());
        product.setCategory(category);
        if (request.getStockBase() != null) {
            ensureStockBase(product, request.getStockBase());
        }
        // Si la imagen cambió, intenta borrar la anterior para no dejar archivos huérfanos
        if (StringUtils.hasText(previousImageUrl)
                && request.getImagenUrl() != null
                && !previousImageUrl.equals(request.getImagenUrl())) {
            fileStorageService.deleteByUrl(previousImageUrl);
        }

        return mapToResponse(product, findOfferForProduct(product, productOfferRepository.findActive(LocalDateTime.now())));
    }

    public ProductResponse setProductActive(Long id, boolean active, User current) {
        ensureManager(current);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        product.setActive(active);
        return mapToResponse(product, findOfferForProduct(product, productOfferRepository.findActive(LocalDateTime.now())));
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
        return mapToResponse(product, Optional.of(offer));
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
    }

    public void deleteReview(Long id) {
        var review = productReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review no encontrada"));
        productReviewRepository.delete(review);
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

    private ProductResponse mapToResponse(Product product, Optional<ProductOffer> offerOpt) {
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
        long reviewCount = productReviewRepository.countByProductId(product.getId());
        double reviewAvg = productReviewRepository.averageRating(product.getId()).orElse(0.0);
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

    private void ensureStockBase(Product product, Integer stockBaseValue) {
        int base = stockBaseValue != null ? stockBaseValue : (product.getStock() != null ? product.getStock() : 0);
        ProductStockBase stockBase = productStockBaseRepository.findByProduct(product)
                .orElseGet(() -> ProductStockBase.builder().product(product).build());
        stockBase.setStockBase(base);
        productStockBaseRepository.save(stockBase);
    }
}
