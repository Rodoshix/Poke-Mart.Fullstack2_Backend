package cl.pokemart.pokemart_backend.service.catalog;

import cl.pokemart.pokemart_backend.dto.catalog.ProductRequest;
import cl.pokemart.pokemart_backend.dto.catalog.ProductResponse;
import cl.pokemart.pokemart_backend.model.catalog.Category;
import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.model.catalog.ProductOffer;
import cl.pokemart.pokemart_backend.model.catalog.ProductStockBase;
import cl.pokemart.pokemart_backend.model.user.Role;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.repository.catalog.CategoryRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductOfferRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductStockBaseRepository;
import cl.pokemart.pokemart_backend.repository.order.OrderItemRepository;
import cl.pokemart.pokemart_backend.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductOfferRepository productOfferRepository;
    private final UserRepository userRepository;
    private final ProductStockBaseRepository productStockBaseRepository;
    private final OrderItemRepository orderItemRepository;

    public CatalogService(CategoryRepository categoryRepository,
                          ProductRepository productRepository,
                          ProductOfferRepository productOfferRepository,
                          UserRepository userRepository,
                          ProductStockBaseRepository productStockBaseRepository,
                          OrderItemRepository orderItemRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productOfferRepository = productOfferRepository;
        this.userRepository = userRepository;
        this.productStockBaseRepository = productStockBaseRepository;
        this.orderItemRepository = orderItemRepository;
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
    public List<ProductResponse> listProductsForManagement(boolean includeInactive, User current) {
        ensureManager(current);
        List<Product> products;
        if (current.getRole() == Role.VENDEDOR) {
            products = productRepository.findBySeller(current);
            if (!includeInactive) {
                products = products.stream()
                        .filter(p -> Boolean.TRUE.equals(p.getActive()))
                        .toList();
            }
        } else {
            products = includeInactive ? productRepository.findAll() : productRepository.findAllActive();
        }
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
        enforceOwnership(product, current);
        var offers = productOfferRepository.findActive(LocalDateTime.now());
        return mapToResponse(product, findOfferForProduct(product, offers));
    }

    // Admin/Vendedor
    public ProductResponse createProduct(ProductRequest request, User current) {
        Category category = resolveCategory(request.getCategoriaSlug(), request.getCategoriaSlug());
        User seller = resolveSellerForCreation(current);

        Product product = Product.builder()
                .name(request.getNombre())
                .description(request.getDescripcion())
                .price(request.getPrecio() != null ? request.getPrecio() : BigDecimal.ZERO)
                .stock(request.getStock() != null ? request.getStock() : 0)
                .imageUrl(request.getImagenUrl())
                .category(category)
                .seller(seller)
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        ensureStockBase(saved, request.getStockBase());
        return mapToResponse(saved, Optional.empty());
    }

    public ProductResponse updateProduct(Long id, ProductRequest request, User current) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        enforceOwnership(product, current);
        Category category = resolveCategory(request.getCategoriaSlug(), request.getCategoriaSlug());

        product.setName(request.getNombre());
        product.setDescription(request.getDescripcion());
        product.setPrice(request.getPrecio());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImagenUrl());
        product.setCategory(category);
        if (request.getStockBase() != null) {
            ensureStockBase(product, request.getStockBase());
        }

        return mapToResponse(product, findOfferForProduct(product, productOfferRepository.findActive(LocalDateTime.now())));
    }

    public ProductResponse setProductActive(Long id, boolean active, User current) {
        ensureManager(current);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        enforceOwnership(product, current);
        product.setActive(active);
        return mapToResponse(product, findOfferForProduct(product, productOfferRepository.findActive(LocalDateTime.now())));
    }

    public void deleteProduct(Long id, User current, boolean hardDelete) {
        ensureManager(current);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        enforceOwnership(product, current);
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
    }

    public ProductResponse addOffer(Long productId, Integer discountPct, LocalDateTime endsAt, User current) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        enforceOwnership(product, current);
        if (discountPct == null || discountPct <= 0 || discountPct >= 100) {
            throw new IllegalArgumentException("Descuento invalido");
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

    // Helpers
    private void enforceOwnership(Product product, User current) {
        if (current == null) {
            throw new SecurityException("No autenticado");
        }
        if (current.getRole() == Role.ADMIN) return;
        if (product.getSeller() == null || !product.getSeller().getId().equals(current.getId())) {
            throw new SecurityException("No autorizado");
        }
    }

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

    private User resolveSellerForCreation(User current) {
        if (current == null) throw new SecurityException("No autenticado");
        if (current.getRole() == Role.ADMIN || current.getRole() == Role.VENDEDOR) {
            return userRepository.findById(current.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        }
        throw new SecurityException("No autorizado");
    }

    private void ensureManager(User current) {
        if (current == null || (current.getRole() != Role.ADMIN && current.getRole() != Role.VENDEDOR)) {
            throw new SecurityException("No autorizado");
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
                .vendedor(product.getSeller() != null ? product.getSeller().getUsername() : null)
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
