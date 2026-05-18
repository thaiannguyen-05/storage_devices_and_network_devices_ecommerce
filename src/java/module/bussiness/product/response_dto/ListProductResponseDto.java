package module.bussiness.product.response_dto;

import entity.ProductEntity;
import java.util.List;
import module.core.common.BaseResponse;

public class ListProductResponseDto extends BaseResponse {
    private List<ProductEntity> products;
    private int total;

    public List<ProductEntity> getProducts() { return products; }
    public void setProducts(List<ProductEntity> products) { this.products = products; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
