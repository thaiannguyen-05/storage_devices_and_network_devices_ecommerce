package module.bussiness.product;

import entity.ProductEntity;
import entity.ProductReviewEntity;
import entity.ProductVariantEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import module.core.sql.ConnecDb;

public class ProductService {
    private static final Logger LOGGER = Logger.getLogger(ProductService.class.getName());
    private final IProductRepository productRepository;
    private final IProductVariantRepository productVariantRepository;
    private final IBrandRepository brandRepository;

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

    private String normalizeProductFamilyKey(ProductEntity product, ProductVariantEntity variant) {
        if (variant != null && variant.getSku() != null && !variant.getSku().isBlank()) {
            String normalizedSku = variant.getSku().trim().toUpperCase(Locale.ROOT).replace('_', '-');

            Pattern fullSizePattern = Pattern.compile("-(\\d+)(TB|GB)(-[A-Z0-9]+)?$");
            Matcher fullSize = fullSizePattern.matcher(normalizedSku);
            if (fullSize.find()) {
                return normalizedSku.substring(0, fullSize.start());
            }

            Pattern numericBandPattern = Pattern.compile("-(\\d+)-[A-Z0-9]+$");
            Matcher numericBand = numericBandPattern.matcher(normalizedSku);
            if (numericBand.find()) {
                return normalizedSku.substring(0, numericBand.start());
            }

            String[] skuParts = normalizedSku.split("-");
            if (skuParts.length >= 2) {
                StringBuilder baseSku = new StringBuilder();
                for (int i = 0; i < skuParts.length - 1; i++) {
                    if (i > 0) {
                        baseSku.append("-");
                    }
                    baseSku.append(skuParts[i]);
                }
                return baseSku.toString();
            }
        }
        return product.getName() == null ? "" : product.getName().trim().toUpperCase(Locale.ROOT);
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

        Set<String> seenFamilies = new HashSet<>();
        Map<String, entity.BrandEntity> brandCache = new HashMap<>();
        Map<String, ReviewStats> reviewStatsMap = getReviewStatsMap();
        for (ProductEntity p : products) {
            String productCategory = p.getCategory() == null ? "" : p.getCategory().trim().toUpperCase(Locale.ROOT);
            if (!normalizedFilter.isEmpty() && !normalizedFilter.equals(productCategory)) {
                continue;
            }

            List<ProductVariantEntity> variants = productVariantRepository.findByProductId(p.getId());
            ProductVariantEntity variant = variants.isEmpty() ? null : variants.get(0);

            String familyKey = normalizeProductFamilyKey(p, variant);

            if (seenFamilies.contains(familyKey)) {
                continue;
            }
            seenFamilies.add(familyKey);

            ProductCardView card = new ProductCardView();
            card.setId(p.getId());
            card.setName(p.getName());
            card.setCategory(p.getCategory());
            card.setStatus(p.getStatus());
            card.setBrandId(p.getBrandId());
            entity.BrandEntity brand = brandCache.get(p.getBrandId());
            if (brand == null && p.getBrandId() != null && !p.getBrandId().isBlank()) {
                brand = brandRepository.findById(p.getBrandId());
                brandCache.put(p.getBrandId(), brand);
            }
            card.setBrandName(brand == null ? p.getBrandId() : brand.getName());

            int totalQty = 0;
            for (ProductVariantEntity v : variants) {
                totalQty += v.getQuantity();
            }
            card.setTotalQuantity(totalQty);

            ReviewStats reviewStats = reviewStatsMap.getOrDefault(p.getId(), new ReviewStats(0, 0));
            double avg = reviewStats.count == 0 ? 0 : (double) reviewStats.sum / reviewStats.count;
            card.setRating(avg);
            card.setReviewCount(reviewStats.count);

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

            StringBuilder variantsJson = new StringBuilder("[");
            for (int i = 0; i < variants.size(); i++) {
                ProductVariantEntity v = variants.get(i);
                if (i > 0) {
                    variantsJson.append(",");
                }
                String imageUrl = v.getImageUrl() == null ? "" : v.getImageUrl();
                String sku = v.getSku() == null ? "" : v.getSku();
                long priceVal = v.getPrice() == null ? 0 : v.getPrice().longValue();
                String priceDisplay = v.getPrice() == null ? "0 VND" : format.format(v.getPrice()) + " VND";
                variantsJson.append("{\"productId\":\"")
                        .append(escapeJson(p.getId()))
                        .append("\",\"variantId\":\"")
                        .append(escapeJson(v.getId()))
                        .append("\",\"sku\":\"")
                        .append(escapeJson(sku))
                        .append("\",\"priceText\":\"")
                        .append(escapeJson(priceDisplay))
                        .append("\",\"priceValue\":")
                        .append(priceVal)
                        .append(",\"stock\":")
                        .append(v.getQuantity())
                        .append(",\"imageUrl\":\"")
                        .append(escapeJson(imageUrl))
                        .append("\"}");
            }
            variantsJson.append("]");
            card.setVariantsJson(variantsJson.toString());
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
        entity.BrandEntity brand = brandRepository.findById(product.getBrandId());
        detail.setBrandName(brand == null ? product.getBrandId() : brand.getName());

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

    public List<ProductVariantEntity> getProductVariants(String productId) {
        return productVariantRepository.findByProductId(productId);
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

        Collections.shuffle(other);

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
        return getFeaturedProducts(getProductCards(), limit);
    }

    public List<ProductCardView> getFeaturedProducts(List<ProductCardView> cards, int limit) {
        List<ProductCardView> sortedCards = new ArrayList<>(cards);
        sortedCards.sort(Comparator
                .comparing((ProductCardView card) -> !"ACTIVE".equalsIgnoreCase(card.getStatus()))
                .thenComparing((ProductCardView card) -> card.getQuantity() <= 0)
                .thenComparing(ProductCardView::getQuantity, Comparator.reverseOrder()));

        if (limit <= 0 || limit >= sortedCards.size()) {
            return sortedCards;
        }
        return new ArrayList<>(sortedCards.subList(0, limit));
    }

    public List<ProductReviewEntity> getProductReviews(String productId) {
        String sql = "SELECT id, productId, reviewerName, rating, comment, reviewedAt FROM ProductReview WHERE productId = ? ORDER BY reviewedAt DESC";
        List<ProductReviewEntity> result = new ArrayList<>();
        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductReviewEntity item = new ProductReviewEntity();
                    item.setId(String.valueOf(rs.getLong("id")));
                    item.setProductId(rs.getString("productId"));
                    item.setReviewerName(rs.getString("reviewerName"));
                    item.setRating(rs.getInt("rating"));
                    item.setComment(rs.getString("comment"));
                    Timestamp reviewedAt = rs.getTimestamp("reviewedAt");
                    item.setCreatedAt(reviewedAt == null ? null : reviewedAt.toLocalDateTime());
                    result.add(item);
                }
            }
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Skipping product reviews for productId=" + productId + " due to query/schema issue", e);
            return Collections.emptyList();
        }
    }

    public boolean createReview(String productId, String reviewerName, int rating, String comment) {
        String sql = "INSERT INTO ProductReview (productId, reviewerName, rating, comment, reviewedAt) VALUES (?, ?, ?, ?, NOW())";
        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, productId);
            ps.setString(2, reviewerName);
            ps.setInt(3, Math.max(1, Math.min(5, rating)));
            ps.setString(4, comment);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create product review", e);
        }
    }

    private Map<String, ReviewStats> getReviewStatsMap() {
        String sql = "SELECT productId, COUNT(*) AS reviewCount, COALESCE(SUM(rating), 0) AS ratingSum FROM ProductReview GROUP BY productId";
        Map<String, ReviewStats> result = new HashMap<>();
        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String productId = rs.getString("productId");
                int reviewCount = rs.getInt("reviewCount");
                int ratingSum = rs.getInt("ratingSum");
                result.put(productId, new ReviewStats(reviewCount, ratingSum));
            }
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Skipping product review statistics due to query/schema issue", e);
            return Collections.emptyMap();
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static class ReviewStats {
        private final int count;
        private final int sum;

        private ReviewStats(int count, int sum) {
            this.count = count;
            this.sum = sum;
        }
    }
}
