package module.bussiness.order.repository.interfaces;

import entity.OrderEntity;
import java.util.List;

public interface IOrderRepository {
    void upsertCartOrder(String userId, String productId, String variantId, int quantity, String status);
    void updateStatusByUserAndItem(String userId, String productId, String variantId, String fromStatus, String toStatus);
    void updateDeliveryInfo(String userId, String productId, String variantId, String status, String phone, String address);
    void updateCartQuantity(String userId, String productId, String variantId, int quantity, String status);
    void removeCartOrder(String userId, String productId, String variantId, String status);
    void clearCartOrders(String userId, String status);
    List<OrderEntity> findByUserIdAndStatus(String userId, String status);
}
