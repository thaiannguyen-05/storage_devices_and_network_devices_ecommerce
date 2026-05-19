package module.bussiness.brand;

import entity.BrandEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import module.bussiness.product.repository.impl.BrandRepository;
import module.core.common.BaseResponse;

public class BrandService {
    private final BrandRepository brandRepository = new BrandRepository();

    public java.util.List<BrandEntity> getAllBrands() {
        return brandRepository.findAll();
    }

    public BrandEntity getBrandById(String id) {
        return brandRepository.findById(id);
    }

    public BaseResponse createBrand(String name, String description, String status, String userId) {
        BaseResponse response = new BaseResponse();
        if (isBlank(name) || isBlank(userId)) {
            fail(response, "Name and user are required");
            return response;
        }
        BrandEntity brand = new BrandEntity(UUID.randomUUID().toString(), name.trim(), userId,
                defaultValue(description, ""), defaultValue(status, "ACTIVE"),
                LocalDateTime.now(), LocalDateTime.now());
        brandRepository.insert(brand);
        response.setSuccess(true);
        response.setSuccessMessage("Brand created");
        return response;
    }

    public BaseResponse updateBrand(String id, String name, String description, String status) {
        BaseResponse response = new BaseResponse();
        BrandEntity brand = brandRepository.findById(id);
        if (brand == null) {
            fail(response, "Brand not found");
            return response;
        }
        brand.setName(defaultValue(name, brand.getName()));
        brand.setDescription(defaultValue(description, brand.getDescription()));
        brand.setStatus(defaultValue(status, brand.getStatus()));
        brandRepository.update(brand);
        response.setSuccess(true);
        response.setSuccessMessage("Brand updated");
        return response;
    }

    public BaseResponse deleteBrand(String id) {
        BaseResponse response = new BaseResponse();
        brandRepository.delete(id);
        response.setSuccess(true);
        response.setSuccessMessage("Brand deleted");
        return response;
    }

    public BaseResponse changeStatus(String id, String status) {
        BaseResponse response = new BaseResponse();
        brandRepository.updateStatus(id, status);
        response.setSuccess(true);
        response.setSuccessMessage("Brand status updated");
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultValue(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private void fail(BaseResponse response, String message) {
        response.setSuccess(false);
        response.setErrorMessage(message);
    }
}
