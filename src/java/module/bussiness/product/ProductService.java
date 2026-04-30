package module.bussiness.product;

import entity.ProductEntity;
import entity.ProductReviewEntity;
import entity.ProductVariantEntity;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import module.bussiness.product.dto.CreateProduct;
import module.bussiness.product.dto.ProductCardView;
import module.bussiness.product.dto.ProductDetailView;
import module.bussiness.product.dto.UpdateProduct;
import module.bussiness.product.repository.impl.BrandRepository;
import module.bussiness.product.repository.impl.ProductRepository;
import module.bussiness.product.repository.impl.ProductVariantRepository;
import module.bussiness.product.repository.interfaces.IBrandRepository;
import module.bussiness.product.repository.interfaces.IProductRepository;
import module.bussiness.product.repository.interfaces.IProductVariantRepository;

public class ProductService {
    private final IProductRepository productRepository;
    private final IProductVariantRepository productVariantRepository;
    private final IBrandRepository brandRepository;
    private final List<ProductReviewEntity> inMemoryReviews = new ArrayList<>();

    public ProductService(IProductRepository productRepository, IProductVariantRepository productVariantRepository, IBrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.brandRepository = brandRepository;
    }

    public ProductService() {
        this(new ProductRepository(), new ProductVariantRepository(), new BrandRepository());
    }

    public List<ProductEntity> getAllProducts() {
        return productRepository.findAll();
    }

    public ProductEntity getProductById(String id) {
        return productRepository.findById(id);
    }

    public boolean createProduct(CreateProduct dto) {
        return productRepository.create(dto);
    }

    public boolean updateProduct(String id, UpdateProduct dto) {
        return productRepository.update(id, dto);
    }

    public boolean deleteProduct(String id) {
        return productRepository.delete(id);
    }

    public List<ProductCardView> getProductCards() {
        return getProductCards(null);
    }

    public List<ProductCardView> getProductCards(String categoryFilter) {
        List<ProductEntity> products = productRepository.findAll();
        List<ProductCardView> cards = new ArrayList<>();
        DecimalFormat format = new DecimalFormat("#,##0");
        String normalizedFilter = categoryFilter == null ? "" : categoryFilter.trim().toUpperCase(Locale.ROOT);

        for (ProductEntity p : products) {
            String productCategory = p.getCategory() == null ? "" : p.getCategory().trim().toUpperCase(Locale.ROOT);
            if (!normalizedFilter.isEmpty() && !normalizedFilter.equals(productCategory)) {
                continue;
            }

            ProductVariantEntity variant = productVariantRepository.findFirstByProductId(p.getId());

            ProductCardView card = new ProductCardView();
            card.setId(p.getId());
            card.setName(p.getName());
            card.setCategory(p.getCategory());
            card.setStatus(p.getStatus());
            card.setBrandId(p.getBrandId());

            if (variant != null) {
                card.setImageUrl(variant.getImageUrl());
                card.setPriceText(format.format(variant.getPrice()) + " VND");
                card.setPriceValue(variant.getPrice().longValue());
                card.setQuantity(variant.getQuantity());
            } else {
                card.setImageUrl("https://images.unsplash.com/photo-1591488320449-011701bb6704?w=800");
                card.setPriceText("No variant price");
                card.setPriceValue(0);
                card.setQuantity(0);
            }
            cards.add(card);
        }
        return cards;
    }

    public ProductDetailView getProductDetail(String id) {
        ProductEntity product = productRepository.findById(id);
        if (product == null) {
            return null;
        }

        ProductVariantEntity variant = productVariantRepository.findFirstByProductId(product.getId());
        DecimalFormat format = new DecimalFormat("#,##0");

        ProductDetailView detail = new ProductDetailView();
        detail.setId(product.getId());
        detail.setName(product.getName());
        detail.setDescription(product.getDescription());
        detail.setCategory(product.getCategory());
        detail.setStatus(product.getStatus());
        detail.setBrandId(product.getBrandId());

        if (variant != null) {
            detail.setImageUrl(variant.getImageUrl());
            detail.setPriceValue(variant.getPrice().longValue());
            detail.setPriceText(format.format(variant.getPrice()) + " VND");
            detail.setQuantity(variant.getQuantity());
        } else {
            detail.setImageUrl("https://images.unsplash.com/photo-1591488320449-011701bb6704?w=1000");
            detail.setPriceValue(0);
            detail.setPriceText("0 VND");
            detail.setQuantity(0);
        }

        List<ProductReviewEntity> reviews = getProductReviews(id);
        double avg = 0;
        if (!reviews.isEmpty()) {
            int sum = 0;
            for (ProductReviewEntity r : reviews) {
                sum += r.getRating();
            }
            avg = (double) sum / reviews.size();
        }
        detail.setRating(avg);
        detail.setReviewCount(reviews.size());
        return detail;
    }

    public List<String> getGalleryImages(String productId) {
        List<ProductVariantEntity> variants = productVariantRepository.findByProductId(productId);
        List<String> images = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (ProductVariantEntity v : variants) {
            String url = v.getImageUrl();
            if (url != null && !url.isBlank() && !seen.contains(url)) {
                images.add(url);
                seen.add(url);
            }
            if (images.size() >= 4) {
                break;
            }
        }

        while (images.size() < 4) {
            if (images.isEmpty()) {
                images.add("https://images.unsplash.com/photo-1591488320449-011701bb6704?w=1000");
            } else {
                images.add(images.get(images.size() - 1));
            }
        }
        return images;
    }

    public List<ProductCardView> getRelatedProducts(String currentProductId, String category, int limit) {
        List<ProductCardView> all = getProductCards();
        List<ProductCardView> sameCategory = new ArrayList<>();
        List<ProductCardView> other = new ArrayList<>();

        String normalized = category == null ? "" : category.trim().toUpperCase(Locale.ROOT);
        for (ProductCardView item : all) {
            if (item.getId().equals(currentProductId)) {
                continue;
            }
            String c = item.getCategory() == null ? "" : item.getCategory().trim().toUpperCase(Locale.ROOT);
            if (!normalized.isEmpty() && normalized.equals(c)) {
                sameCategory.add(item);
            } else {
                other.add(item);
            }
        }

        List<ProductCardView> result = new ArrayList<>();
        for (ProductCardView s : sameCategory) {
            if (result.size() >= limit) {
                break;
            }
            result.add(s);
        }
        for (ProductCardView o : other) {
            if (result.size() >= limit) {
                break;
            }
            result.add(o);
        }
        return result;
    }

    public List<ProductCardView> getFeaturedProducts(int limit) {
        List<ProductCardView> cards = getProductCards();
        List<ProductCardView> featured = new ArrayList<>();

        for (ProductCardView card : cards) {
            if (!"ACTIVE".equalsIgnoreCase(card.getStatus())) {
                continue;
            }
            if (card.getQuantity() <= 0) {
                continue;
            }
            featured.add(card);
            if (featured.size() >= limit) {
                return featured;
            }
        }

        for (ProductCardView card : cards) {
            if (featured.size() >= limit) {
                break;
            }
            if (!featured.contains(card)) {
                featured.add(card);
            }
        }
        return featured;
    }

    public List<ProductReviewEntity> getProductReviews(String productId) {
        List<ProductReviewEntity> result = new ArrayList<>();
        for (ProductReviewEntity review : inMemoryReviews) {
            if (productId.equals(review.getProductId())) {
                result.add(review);
            }
        }
        return result;
    }

    public boolean createReview(String productId, String reviewerName, int rating, String comment) {
        ProductReviewEntity item = new ProductReviewEntity();
        item.setId(UUID.randomUUID().toString());
        item.setProductId(productId);
        item.setReviewerName(reviewerName);
        item.setRating(Math.max(1, Math.min(5, rating)));
        item.setComment(comment);
        item.setCreatedAt(LocalDateTime.now());
        inMemoryReviews.add(item);
        return true;
    }
}
