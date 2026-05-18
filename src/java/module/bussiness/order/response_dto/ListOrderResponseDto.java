package module.bussiness.order.response_dto;

import entity.OrderEntity;
import java.util.List;
import module.core.common.BaseResponse;

public class ListOrderResponseDto extends BaseResponse {
    private List<OrderEntity> orders;
    private int total;

    public List<OrderEntity> getOrders() { return orders; }
    public void setOrders(List<OrderEntity> orders) { this.orders = orders; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
