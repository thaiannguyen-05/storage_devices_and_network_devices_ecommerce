package module.bussiness.order.repository.interfaces;

import entity.OrderCartEntity;
import java.util.List;
import module.bussiness.cart.dto.CreateCartDto;
import module.bussiness.cart.dto.UpdateCartDto;

public interface IOrderCartRepository {
    OrderCartEntity registerCart(CreateCartDto dto);
    List<OrderCartEntity> findAll();
    OrderCartEntity findById(String id);
    OrderCartEntity findByUserId(String userId);
    boolean update(String id, UpdateCartDto dto);
    boolean delete(String id);
}
