package module.bussiness.product;

import entity.ProductEntity;
import entity.ProductVariantEntity;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import module.bussiness.product.dto.CreateProductDto;
import module.bussiness.product.dto.UpdateProductDto;
import module.bussiness.product.repository.impl.BrandRepository;
import module.bussiness.product.repository.impl.ProductRepository;
import module.bussiness.product.repository.impl.VariantRepository;
import module.bussiness.product.response_dto.CreateProductResponseDto;
import module.bussiness.product.response_dto.DeleteProductResponseDto;
import module.bussiness.product.response_dto.GetProductResponseDto;
import module.bussiness.product.response_dto.ListBrandResponseDto;
import module.bussiness.product.response_dto.ListProductResponseDto;
import module.bussiness.product.response_dto.SearchProductResponseDto;
import module.bussiness.product.response_dto.UpdateProductResponseDto;
import module.core.common.BaseResponse;
import module.core.config.AppConfig;
import module.core.sql.JdbcHelper;

public class ProductService {
    private final ProductRepository productRepository = new ProductRepository();
    private final VariantRepository variantRepository = new VariantRepository();
    private final BrandRepository brandRepository = new BrandRepository();

    public Map<String, Object> getHomePage() {
        Map<String, Object> data = new HashMap<>();
        List<ProductEntity> recentProducts = productRepository.findActive(0, 8);
        data.put("recent", toProductCards(recentProducts));
        data.put("bestSelling", toProductCards(productRepository.findBestSelling(8)));
        data.put("discount", toProductCards(recentProducts));
        return data;
    }

    public ListProductResponseDto listProducts(int page) {
        int offset = Math.max(0, page - 1) * AppConfig.PAGE_SIZE;
        ListProductResponseDto response = new ListProductResponseDto();
        response.setProducts(productRepository.findActive(offset, AppConfig.PAGE_SIZE));
        response.setTotal(productRepository.countActive());
        response.setSuccess(true);
        return response;
    }

    public GetProductResponseDto getProductDetail(String id) {
        GetProductResponseDto response = new GetProductResponseDto();
        ProductEntity product = productRepository.findById(id);
        response.setProduct(product);
        response.setVariants(product == null ? java.util.Collections.emptyList() : variantRepository.findByProductId(id));
        response.setSuccess(product != null);
        if (product == null) {
            response.setErrorMessage("Product not found");
        }
        return response;
    }

    public SearchProductResponseDto searchProducts(String keyword, int page) {
        int offset = Math.max(0, page - 1) * AppConfig.PAGE_SIZE;
        SearchProductResponseDto response = new SearchProductResponseDto();
        response.setProducts(productRepository.search(keyword, offset, AppConfig.PAGE_SIZE));
        response.setTotal(productRepository.countSearch(keyword));
        response.setSuccess(true);
        return response;
    }

    public List<ProductCardView> autocomplete(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return toProductCards(productRepository.search(keyword, 0, 5));
    }

    public ListProductResponseDto findByCategory(String category, int page) {
        int offset = Math.max(0, page - 1) * AppConfig.PAGE_SIZE;
        ListProductResponseDto response = new ListProductResponseDto();
        response.setProducts(productRepository.findByCategory(category, offset, AppConfig.PAGE_SIZE));
        response.setTotal(productRepository.countByCategory(category));
        response.setSuccess(true);
        return response;
    }

    public CreateProductResponseDto createProduct(CreateProductDto dto) {
        CreateProductResponseDto response = new CreateProductResponseDto();
        if (isBlank(dto.getName()) || isBlank(dto.getBrandId()) || isBlank(dto.getUserId()) || isBlank(dto.getCategory())) {
            fail(response, "Name, brand, user and category are required");
            return response;
        }
        String id = UUID.randomUUID().toString();
        productRepository.insert(new ProductEntity(id, dto.getName(), defaultValue(dto.getDescription(), ""),
                dto.getBrandId(), defaultValue(dto.getStatus(), "DRAFT"), dto.getUserId(),
                LocalDateTime.now(), LocalDateTime.now(), dto.getCategory()));
        response.setSuccess(true);
        response.setSuccessMessage("Product created");
        response.setProductId(id);
        return response;
    }

    public UpdateProductResponseDto updateProduct(UpdateProductDto dto) {
        UpdateProductResponseDto response = new UpdateProductResponseDto();
        ProductEntity product = productRepository.findById(dto.getId());
        if (product == null) {
            fail(response, "Product not found");
            return response;
        }
        product.setName(defaultValue(dto.getName(), product.getName()));
        product.setDescription(defaultValue(dto.getDescription(), product.getDescription()));
        product.setBrandId(defaultValue(dto.getBrandId(), product.getBrandId()));
        product.setStatus(defaultValue(dto.getStatus(), product.getStatus()));
        product.setCategory(defaultValue(dto.getCategory(), product.getCategory()));
        productRepository.update(product);
        response.setSuccess(true);
        response.setSuccessMessage("Product updated");
        return response;
    }

    public DeleteProductResponseDto deleteProduct(String id) {
        DeleteProductResponseDto response = new DeleteProductResponseDto();
        productRepository.delete(id);
        response.setSuccess(true);
        response.setSuccessMessage("Product deleted");
        return response;
    }

    public ListBrandResponseDto getAllBrands() {
        ListBrandResponseDto response = new ListBrandResponseDto();
        response.setBrands(brandRepository.findAll());
        response.setSuccess(true);
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultValue(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    public FilterResult filterProducts(String keyword, String[] categories, String[] brands, String[] priceRanges, String status, String sort, int page) {
        int offset = Math.max(0, page - 1) * AppConfig.PAGE_SIZE;
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT p.id, p.name, p.category, p.status, p.createdAt, " +
            "pv.price, pv.imageUrl " +
            "FROM Product p " +
            "LEFT JOIN Brand b ON p.brandId = b.id " +
            "LEFT JOIN ProductVariant pv ON pv.productId = p.id " +
            "WHERE 1=1 "
        );
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.name LIKE ? OR p.description LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND p.status = ? ");
            params.add(status);
        } else {
            sql.append("AND p.status = 'ACTIVE' ");
        }

        if (categories != null && categories.length > 0) {
            sql.append("AND p.category IN (");
            for (int i = 0; i < categories.length; i++) {
                sql.append("?");
                params.add(categories[i]);
                if (i < categories.length - 1) sql.append(",");
            }
            sql.append(") ");
        }

        if (brands != null && brands.length > 0) {
            sql.append("AND b.name IN (");
            for (int i = 0; i < brands.length; i++) {
                sql.append("?");
                params.add(brands[i]);
                if (i < brands.length - 1) sql.append(",");
            }
            sql.append(") ");
        }

        if (priceRanges != null && priceRanges.length > 0) {
            sql.append("AND (");
            for (int i = 0; i < priceRanges.length; i++) {
                String[] range = priceRanges[i].split("-");
                if (i > 0) sql.append(" OR ");
                sql.append("(pv.price IS NOT NULL ");
                if (range.length > 0 && !range[0].trim().isEmpty()) {
                    sql.append("AND pv.price >= ? ");
                    params.add(new java.math.BigDecimal(range[0]));
                }
                if (range.length > 1 && !range[1].trim().isEmpty()) {
                    sql.append("AND pv.price <= ? ");
                    params.add(new java.math.BigDecimal(range[1]));
                }
                sql.append(") ");
            }
            sql.append(") ");
        }

        if ("priceAsc".equals(sort)) {
            sql.append("ORDER BY pv.price ASC ");
        } else if ("priceDesc".equals(sort)) {
            sql.append("ORDER BY pv.price DESC ");
        } else if ("bestSeller".equals(sort)) {
            sql.append("ORDER BY (SELECT COUNT(*) FROM `Order` o WHERE o.productId = p.id) DESC, p.createdAt DESC ");
        } else {
            sql.append("ORDER BY p.createdAt DESC, pv.price ASC ");
        }

        // Count total before pagination
        String countSql = "SELECT COUNT(DISTINCT p.id) " +
            "FROM Product p " +
            "LEFT JOIN Brand b ON p.brandId = b.id " +
            "LEFT JOIN ProductVariant pv ON pv.productId = p.id " +
            "WHERE 1=1 ";

        // We need to extract the WHERE conditions from main SQL for count query
        // Simpler: run count with same params but without ORDER BY and LIMIT
        int total = countFilterProducts(keyword, categories, brands, priceRanges, status);

        // Add pagination to main query
        sql.append("LIMIT ? OFFSET ? ");
        params.add(AppConfig.PAGE_SIZE);
        params.add(offset);

        try {
            List<Map<String, Object>> rows = module.core.sql.JdbcHelper.executeQuery(sql.toString(), rs -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getString("id"));
                map.put("name", rs.getString("name"));
                map.put("category", rs.getString("category"));
                map.put("status", rs.getString("status"));
                map.put("createdAt", rs.getTimestamp("createdAt").toLocalDateTime());
                map.put("price", rs.getBigDecimal("price"));
                map.put("imageUrl", rs.getString("imageUrl"));
                return map;
            }, params.toArray());

            Map<String, ProductCardView> cardMap = new LinkedHashMap<>();
            for (Map<String, Object> row : rows) {
                String id = (String) row.get("id");
                if (!cardMap.containsKey(id)) {
                    java.math.BigDecimal price = (java.math.BigDecimal) row.get("price");
                    String imageUrl = (String) row.get("imageUrl");
                    cardMap.put(id, new ProductCardView(id, (String) row.get("name"),
                            imageUrl == null ? "" : imageUrl,
                            price == null ? java.math.BigDecimal.ZERO : price));
                }
            }
            return new FilterResult(new ArrayList<>(cardMap.values()), total, page, AppConfig.PAGE_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
            return new FilterResult(Collections.emptyList(), 0, page, AppConfig.PAGE_SIZE);
        }
    }

    private int countFilterProducts(String keyword, String[] categories, String[] brands, String[] priceRanges, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(DISTINCT p.id) FROM Product p " +
            "LEFT JOIN Brand b ON p.brandId = b.id " +
            "WHERE 1=1 "
        );
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (p.name LIKE ? OR p.description LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND p.status = ? ");
            params.add(status);
        } else {
            sql.append("AND p.status = 'ACTIVE' ");
        }
        if (categories != null && categories.length > 0) {
            sql.append("AND p.category IN (");
            for (int i = 0; i < categories.length; i++) {
                sql.append("?");
                params.add(categories[i]);
                if (i < categories.length - 1) sql.append(",");
            }
            sql.append(") ");
        }
        if (brands != null && brands.length > 0) {
            sql.append("AND b.name IN (");
            for (int i = 0; i < brands.length; i++) {
                sql.append("?");
                params.add(brands[i]);
                if (i < brands.length - 1) sql.append(",");
            }
            sql.append(") ");
        }
        if (priceRanges != null && priceRanges.length > 0) {
            sql.append("AND (");
            for (int i = 0; i < priceRanges.length; i++) {
                String[] range = priceRanges[i].split("-");
                if (i > 0) sql.append(" OR ");
                sql.append("EXISTS (SELECT 1 FROM ProductVariant pv WHERE pv.productId = p.id ");
                if (range.length > 0 && !range[0].trim().isEmpty()) {
                    sql.append("AND pv.price >= ? ");
                    params.add(new java.math.BigDecimal(range[0]));
                }
                if (range.length > 1 && !range[1].trim().isEmpty()) {
                    sql.append("AND pv.price <= ? ");
                    params.add(new java.math.BigDecimal(range[1]));
                }
                sql.append(") ");
            }
            sql.append(") ");
        }

        return JdbcHelper.count(sql.toString(), params.toArray());
    }

    private void fail(BaseResponse response, String message) {
        response.setSuccess(false);
        response.setErrorMessage(message);
    }

    public List<ProductCardView> toProductCards(List<ProductEntity> products) {
        Map<String, List<ProductVariantEntity>> variantsByProduct = variantRepository.findByProductIds(
            products.stream().map(ProductEntity::getId).collect(java.util.stream.Collectors.toList())
        );
        List<ProductCardView> cards = new ArrayList<>();
        for (ProductEntity product : products) {
            List<ProductVariantEntity> variants = variantsByProduct.getOrDefault(product.getId(), Collections.emptyList());
            ProductVariantEntity variant = variants.isEmpty() ? null : variants.get(0);
            cards.add(new ProductCardView(product.getId(), product.getName(),
                    variant == null ? "" : variant.getImageUrl(),
                    variant == null ? java.math.BigDecimal.ZERO : variant.getPrice()));
        }
        return cards;
    }
}
