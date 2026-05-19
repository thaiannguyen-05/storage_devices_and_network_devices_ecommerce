package module.bussiness.order.repository.interfaces;

import entity.OrderEntity;
import java.util.List;
import java.util.Map;
import module.core.admin.dto.AdminOrderDto;

public interface IOrderRepository {
    void insert(OrderEntity order);
    OrderEntity findById(String id);
    List<OrderEntity> findByUserId(String userId, int offset, int limit);
    List<OrderEntity> findAll(int offset, int limit);
    int countAll();
    List<AdminOrderDto> findAllWithUser(int offset, int limit, String status, String keyword);
    int countAllWithUser(String status, String keyword);
    AdminOrderDto findByIdWithDetails(String id);
    Map<String, Integer> countByStatus();
    void updateStatus(String id, String status);
}
