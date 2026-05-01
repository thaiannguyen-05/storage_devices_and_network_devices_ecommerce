package module.bussiness.order.repository.interfaces;

public interface IOrderRepository {

    boolean updateStatus(String orderId, String status);
}
