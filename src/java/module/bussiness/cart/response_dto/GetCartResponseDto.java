package module.bussiness.cart.response_dto;

import java.math.BigDecimal;
import java.util.List;
import module.bussiness.cart.CartItemView;
import module.core.common.BaseResponse;

public class GetCartResponseDto extends BaseResponse {
    private String cartId;
    private List<CartItemView> items;
    private BigDecimal total;
    private int page;
    private int pageSize;
    private int totalPages;
    private int totalItems;

    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }
    public List<CartItemView> getItems() { return items; }
    public void setItems(List<CartItemView> items) { this.items = items; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
}
