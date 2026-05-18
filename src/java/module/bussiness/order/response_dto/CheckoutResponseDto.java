package module.bussiness.order.response_dto;

import java.util.ArrayList;
import java.util.List;
import module.core.common.BaseResponse;

public class CheckoutResponseDto extends BaseResponse {
    private List<String> orderIds = new ArrayList<>();

    public List<String> getOrderIds() { return orderIds; }
    public void setOrderIds(List<String> orderIds) { this.orderIds = orderIds; }
}
