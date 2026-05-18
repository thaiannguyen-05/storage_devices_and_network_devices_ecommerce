package module.bussiness.order.repository.interfaces;

import entity.OrderEntity;
import java.util.List;

public interface IOrderRepository {
    void insert(OrderEntity order);
    OrderEntity findById(String id);
    List<OrderEntity> findByUserId(String userId, int offset, int limit);
    List<OrderEntity> findAll(int offset, int limit);
    int countAll();
    void updateStatus(String id, String status);
}
