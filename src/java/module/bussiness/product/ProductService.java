package module.bussiness.product;

import entity.ProductEntity;
import entity.ProductVariantEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

public class ProductService {
    private final ProductRepository productRepository = new ProductRepository();
    private final VariantRepository variantRepository = new VariantRepository();
    private final BrandRepository brandRepository = new BrandRepository();

    public Map<String, Object> getHomePage() {
        Map<String, Object> data = new HashMap<>();
        data.put("recent", toCards(productRepository.findActive(0, 8)));
        data.put("bestSelling", toCards(productRepository.findBestSelling(8)));
        data.put("discount", toCards(productRepository.findActive(0, 8)));
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
        response.setTotal(response.getProducts().size());
        response.setSuccess(true);
        return response;
    }

    public List<ProductCardView> autocomplete(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return toCards(productRepository.search(keyword, 0, 5));
    }

    public ListProductResponseDto findByCategory(String category, int page) {
        int offset = Math.max(0, page - 1) * AppConfig.PAGE_SIZE;
        ListProductResponseDto response = new ListProductResponseDto();
        response.setProducts(productRepository.findByCategory(category, offset, AppConfig.PAGE_SIZE));
        response.setTotal(response.getProducts().size());
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

    public List<ProductCardView> filterProducts(String[] categories, String[] brands, String priceRange, String status, String sort) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.id, p.name, b.name AS brandName, p.category, p.status, p.createdAt " +
            "FROM Product p " +
            "LEFT JOIN Brand b ON p.brandId = b.id " +
            "WHERE 1=1 "
        );
        java.util.List<Object> params = new java.util.ArrayList<>();
        
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

        if (priceRange != null && !priceRange.trim().isEmpty()) {
            String[] range = priceRange.split("-");
            if (range.length > 0 && !range[0].trim().isEmpty()) {
                sql.append("AND EXISTS (SELECT 1 FROM ProductVariant pv WHERE pv.productId = p.id AND pv.price >= ?) ");
                params.add(new java.math.BigDecimal(range[0]));
            }
            if (range.length > 1 && !range[1].trim().isEmpty()) {
                sql.append("AND EXISTS (SELECT 1 FROM ProductVariant pv WHERE pv.productId = p.id AND pv.price <= ?) ");
                params.add(new java.math.BigDecimal(range[1]));
            }
        }

        if ("priceAsc".equals(sort)) {
            sql.append("ORDER BY (SELECT MIN(pv.price) FROM ProductVariant pv WHERE pv.productId = p.id) ASC ");
        } else if ("priceDesc".equals(sort)) {
            sql.append("ORDER BY (SELECT MAX(pv.price) FROM ProductVariant pv WHERE pv.productId = p.id) DESC ");
        } else if ("bestSeller".equals(sort)) {
            sql.append("ORDER BY (SELECT COUNT(*) FROM `Order` o WHERE o.productId = p.id) DESC ");
        } else {
            sql.append("ORDER BY p.createdAt DESC ");
        }

        try {
            List<ProductEntity> products = module.core.sql.JdbcHelper.executeQuery(sql.toString(), rs -> {
                return new ProductEntity(rs.getString("id"), rs.getString("name"), "",
                        "", rs.getString("status"), "",
                        rs.getTimestamp("createdAt").toLocalDateTime(), rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getString("category"));
            }, params.toArray());
            return toCards(products);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    private void fail(BaseResponse response, String message) {
        response.setSuccess(false);
        response.setErrorMessage(message);
    }

    private List<ProductCardView> toCards(List<ProductEntity> products) {
        List<ProductCardView> cards = new ArrayList<>();
        for (ProductEntity product : products) {
            List<ProductVariantEntity> variants = variantRepository.findByProductId(product.getId());
            ProductVariantEntity variant = variants.isEmpty() ? null : variants.get(0);
            cards.add(new ProductCardView(product.getId(), product.getName(),
                    variant == null ? "" : variant.getImageUrl(),
                    variant == null ? java.math.BigDecimal.ZERO : variant.getPrice()));
        }
        return cards;
    }
}
