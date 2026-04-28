package module.bussiness.order;

import module.bussiness.order.repository.impl.OrderCartRepository;
import module.bussiness.order.repository.impl.OrderRepository;

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
