package module.bussiness.product.response_dto;

import entity.ProductEntity;
import entity.ProductVariantEntity;
import java.util.List;
import module.core.common.BaseResponse;

public class GetProductResponseDto extends BaseResponse {
    private ProductEntity product;
    private List<ProductVariantEntity> variants;

    public ProductEntity getProduct() { return product; }
    public void setProduct(ProductEntity product) { this.product = product; }
    public List<ProductVariantEntity> getVariants() { return variants; }
    public void setVariants(List<ProductVariantEntity> variants) { this.variants = variants; }
}
