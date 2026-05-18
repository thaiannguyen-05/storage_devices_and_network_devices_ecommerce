package module.bussiness.product.response_dto;

import module.core.common.BaseResponse;

public class CreateProductResponseDto extends BaseResponse {
    private String productId;
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
}
