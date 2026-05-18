package module.bussiness.order.response_dto;

import entity.OrderEntity;
import module.core.common.BaseResponse;

public class GetOrderResponseDto extends BaseResponse {
    private OrderEntity order;

    public OrderEntity getOrder() { return order; }
    public void setOrder(OrderEntity order) { this.order = order; }
}
