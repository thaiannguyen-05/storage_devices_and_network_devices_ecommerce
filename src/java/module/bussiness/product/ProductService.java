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
