package module.bussiness.product;

import java.util.List;

public class FilterResult {
    private List<ProductCardView> products;
    private int total;
    private int page;
    private int pageSize;

    public FilterResult() {}

    public FilterResult(List<ProductCardView> products, int total, int page, int pageSize) {
        this.products = products;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<ProductCardView> getProducts() { return products; }
    public void setProducts(List<ProductCardView> products) { this.products = products; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public int getTotalPages() {
        return pageSize == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
    }
}
