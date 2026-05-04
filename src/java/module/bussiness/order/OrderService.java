package module.bussiness.order;

import entity.OrderEntity;
import java.util.List;
import module.bussiness.order.repository.impl.OrderCartRepository;
import module.bussiness.order.repository.impl.OrderRepository;

public class OrderService {
    public static final String STATUS_CHO_VAO_GIO = "CHO_VAO_GIO";
    public static final String STATUS_DA_DAT_HANG = "DA_DAT_HANG";
    public static final String STATUS_DA_THANH_TOAN_THANH_CONG = "DA_THANH_TOAN_THANH_CONG";

    private final OrderRepository orderRepository;
    private final OrderCartRepository orderCartRepository;

    public OrderService() {
        this.orderRepository = new OrderRepository();
        this.orderCartRepository = new OrderCartRepository();
    }

    public OrderRepository getOrderRepository() {
        return orderRepository;
    }

    public OrderCartRepository getOrderCartRepository() {
        return orderCartRepository;
    }

    public void saveCartOrder(String userId, String productId, String variantId, int quantity) {
        if (isBlank(userId) || isBlank(productId)) {
            return;
        }
        orderRepository.upsertCartOrder(userId.trim(), productId.trim(), trimToEmpty(variantId), quantity, STATUS_CHO_VAO_GIO);
    }

    public void markPlaced(String userId, String productId, String variantId) {
        if (isBlank(userId) || isBlank(productId)) {
            return;
        }
        orderRepository.updateStatusByUserAndItem(userId.trim(), productId.trim(), trimToEmpty(variantId), STATUS_CHO_VAO_GIO, STATUS_DA_DAT_HANG);
    }

    public void saveDeliveryInfoForCartOrder(String userId, String productId, String variantId, String phone, String address) {
        if (isBlank(userId) || isBlank(productId)) {
            return;
        }
        orderRepository.updateDeliveryInfo(userId.trim(), productId.trim(), trimToEmpty(variantId), STATUS_CHO_VAO_GIO, trimToEmpty(phone), trimToEmpty(address));
    }

    public void markPaidSuccess(String userId, String productId, String variantId) {
        if (isBlank(userId) || isBlank(productId)) {
            return;
        }
        orderRepository.updateStatusByUserAndItem(userId.trim(), productId.trim(), trimToEmpty(variantId), STATUS_DA_DAT_HANG, STATUS_DA_THANH_TOAN_THANH_CONG);
    }

    public List<OrderEntity> getCartOrders(String userId) {
        if (isBlank(userId)) {
            return java.util.Collections.emptyList();
        }
        return orderRepository.findByUserIdAndStatus(userId.trim(), STATUS_CHO_VAO_GIO);
    }

    public void updateCartQuantity(String userId, String productId, String variantId, int quantity) {
        if (isBlank(userId) || isBlank(productId)) {
            return;
        }
        orderRepository.updateCartQuantity(userId.trim(), productId.trim(), trimToEmpty(variantId), quantity, STATUS_CHO_VAO_GIO);
    }

    public void removeCartOrder(String userId, String productId, String variantId) {
        if (isBlank(userId) || isBlank(productId)) {
            return;
        }
        orderRepository.removeCartOrder(userId.trim(), productId.trim(), trimToEmpty(variantId), STATUS_CHO_VAO_GIO);
    }

    public void clearCartOrders(String userId) {
        if (isBlank(userId)) {
            return;
        }
        orderRepository.clearCartOrders(userId.trim(), STATUS_CHO_VAO_GIO);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
