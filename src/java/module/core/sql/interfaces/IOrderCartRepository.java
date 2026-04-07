package module.core.sql.interfaces;

import entity.OrderCartEntity;
import module.bussiness.cart.dto.CreateCartDto;

public interface IOrderCartRepository {
    OrderCartEntity registerCart(CreateCartDto dto);
}
