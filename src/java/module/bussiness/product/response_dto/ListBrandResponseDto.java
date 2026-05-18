package module.bussiness.product.response_dto;

import entity.BrandEntity;
import java.util.List;
import module.core.common.BaseResponse;

public class ListBrandResponseDto extends BaseResponse {
    private List<BrandEntity> brands;

    public List<BrandEntity> getBrands() { return brands; }
    public void setBrands(List<BrandEntity> brands) { this.brands = brands; }
}
