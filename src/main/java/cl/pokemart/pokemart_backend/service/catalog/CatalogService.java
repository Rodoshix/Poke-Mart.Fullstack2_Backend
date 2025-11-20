package cl.pokemart.pokemart_backend.service.catalog;

import cl.pokemart.pokemart_backend.dto.catalog.ProductRequest;
import cl.pokemart.pokemart_backend.dto.catalog.ProductResponse;
import cl.pokemart.pokemart_backend.model.catalog.Category;
import cl.pokemart.pokemart_backend.model.catalog.Product;
import cl.pokemart.pokemart_backend.model.catalog.ProductOffer;
import cl.pokemart.pokemart_backend.model.user.Role;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.repository.catalog.CategoryRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductOfferRepository;
import cl.pokemart.pokemart_backend.repository.catalog.ProductRepository;
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

    public CatalogService(CategoryRepository categoryRepository,
                          ProductRepository productRepository,
                          ProductOfferRepository productOfferRepository,
                          UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productOfferRepository = productOfferRepository;
        this.userRepository = userRepository;
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

        return mapToResponse(product, findOfferForProduct(product, productOfferRepository.findActive(LocalDateTime.now())));
    }

    public void deleteProduct(Long id, User current) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        enforceOwnership(product, current);
        product.setActive(false);
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
        return ProductResponse.builder()
                .id(product.getId())
                .nombre(product.getName())
                .descripcion(product.getDescription())
                .precio(product.getPrice())
                .stock(product.getStock())
                .imagenUrl(product.getImageUrl())
                .categoria(product.getCategory() != null ? product.getCategory().getName() : null)
                .offer(offerInfo)
                .vendedor(product.getSeller() != null ? product.getSeller().getUsername() : null)
                .build();
    }
}
