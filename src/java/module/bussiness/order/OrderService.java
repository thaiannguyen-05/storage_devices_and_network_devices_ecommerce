package module.bussiness.order;

import module.core.sql.repository.OrderCartRepository;
import module.core.sql.repository.OrderRepository;

public class OrderService {
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
}
