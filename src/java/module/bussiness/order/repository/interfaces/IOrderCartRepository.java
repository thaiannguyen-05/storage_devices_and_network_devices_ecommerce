package module.bussiness.order.repository.interfaces;

import entity.OrderCartEntity;
import module.bussiness.cart.dto.CreateCartDto;

public interface IOrderCartRepository {
    OrderCartEntity registerCart(CreateCartDto dto);
}
