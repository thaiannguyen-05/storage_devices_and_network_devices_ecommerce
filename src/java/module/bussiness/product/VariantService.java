package module.bussiness.product;

import entity.ProductVariantEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import module.bussiness.product.dto.CreateVariantDto;
import module.bussiness.product.dto.UpdateVariantDto;
import module.bussiness.product.repository.impl.VariantRepository;
import module.bussiness.product.response_dto.CreateProductResponseDto;
import module.bussiness.product.response_dto.DeleteProductResponseDto;
import module.bussiness.product.response_dto.UpdateProductResponseDto;
import module.core.common.BaseResponse;

public class VariantService {
    private final VariantRepository variantRepository = new VariantRepository();

    public CreateProductResponseDto createVariant(CreateVariantDto dto) {
        CreateProductResponseDto response = new CreateProductResponseDto();
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0 || isBlank(dto.getSku())) {
            fail(response, "Valid price and SKU are required");
            return response;
        }
        if (variantRepository.findBySku(dto.getSku()) != null) {
            fail(response, "SKU already exists");
            return response;
        }
        String id = UUID.randomUUID().toString();
        variantRepository.insert(new ProductVariantEntity(id, dto.getProductId(), dto.getPrice(),
                defaultValue(dto.getImageUrl(), ""), defaultValue(dto.getStatus(), "ACTIVE"),
                LocalDateTime.now(), LocalDateTime.now(), dto.getSku(), dto.getQuantity() == null ? 0 : dto.getQuantity()));
        response.setSuccess(true);
        response.setSuccessMessage("Variant created");
        response.setProductId(id);
        return response;
    }

    public UpdateProductResponseDto updateVariant(UpdateVariantDto dto) {
        UpdateProductResponseDto response = new UpdateProductResponseDto();
        ProductVariantEntity variant = variantRepository.findById(dto.getId());
        if (variant == null) {
            fail(response, "Variant not found");
            return response;
        }
        variant.setPrice(dto.getPrice() == null ? variant.getPrice() : dto.getPrice());
        variant.setImageUrl(defaultValue(dto.getImageUrl(), variant.getImageUrl()));
        variant.setStatus(defaultValue(dto.getStatus(), variant.getStatus()));
        variant.setSku(defaultValue(dto.getSku(), variant.getSku()));
        variant.setQuantity(dto.getQuantity() == null ? variant.getQuantity() : dto.getQuantity());
        variantRepository.update(variant);
        response.setSuccess(true);
        response.setSuccessMessage("Variant updated");
        return response;
    }

    public DeleteProductResponseDto deleteVariant(String id) {
        DeleteProductResponseDto response = new DeleteProductResponseDto();
        variantRepository.delete(id);
        response.setSuccess(true);
        response.setSuccessMessage("Variant deleted");
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
}
