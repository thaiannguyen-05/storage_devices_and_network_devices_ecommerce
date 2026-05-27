package module.bussiness.order;

import entity.OrderEntity;
import entity.ProductVariantEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import module.bussiness.cart.CartItemView;
import module.bussiness.cart.CartService;
import module.bussiness.order.dto.CheckoutDto;
import module.bussiness.order.dto.CheckoutItemDto;
import module.bussiness.order.repository.impl.OrderRepository;
import module.bussiness.order.response_dto.CancelOrderResponseDto;
import module.bussiness.order.response_dto.CheckoutResponseDto;
import module.bussiness.order.response_dto.GetOrderResponseDto;
import module.bussiness.order.response_dto.ListOrderResponseDto;
import module.bussiness.order.response_dto.UpdateOrderStatusResponseDto;
import module.bussiness.product.repository.impl.VariantRepository;
import module.core.common.BaseResponse;
import module.core.config.AppConfig;
import module.core.outbox.OutBoxService;
import module.core.outbox.TypeEvent;
import module.core.sql.JdbcHelper;

public class OrderService {
    private final OrderRepository orderRepository = new OrderRepository();
    private final VariantRepository variantRepository = new VariantRepository();
    private final CartService cartService = new CartService();
    private final OutBoxService outBoxService = new OutBoxService();

    public CheckoutResponseDto checkout(String userId, CheckoutDto dto) {
        CheckoutResponseDto response = new CheckoutResponseDto();
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            dto.setItems(new ArrayList<>());
            for (CartItemView cartItem : cartService.getCart(userId).getItems()) {
                CheckoutItemDto item = new CheckoutItemDto();
                item.setProductId(cartItem.getProductId());
                item.setVariantId(cartItem.getVariantId());
                item.setQuantity(cartItem.getQuantity());
                dto.getItems().add(item);
            }
        }
        if (dto.getItems().isEmpty() || isBlank(dto.getName()) || isBlank(dto.getEmail())
                || isBlank(dto.getPhone()) || isBlank(dto.getAddress()) || isBlank(dto.getPaymentMethod())) {
            fail(response, "Name, email, phone, address and payment method are required");
            return response;
        }
        if (!isValidEmail(dto.getEmail())) {
            fail(response, "Email định dạng không đúng");
            return response;
        }
        if (!isValidPhone(dto.getPhone())) {
            fail(response, "Số điện thoại cần 9-11 chữ số, bắt đầu bằng 0");
            return response;
        }
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        for (CheckoutItemDto item : dto.getItems()) {
            ProductVariantEntity variant = variantRepository.findById(item.getVariantId());
            if (variant == null || item.getQuantity() <= 0 || variant.getQuantity() < item.getQuantity()) {
                fail(response, "Product variant stock is invalid");
                return response;
            }
            totalAmount = totalAmount.add(
                    variant.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
        }

        // Process voucher discount
        double discountPercent = 0;
        String appliedVoucherId = null;
        if (!isBlank(dto.getVoucherId())) {
            java.util.List<entity.VoucherEntity> vouchers = module.core.sql.JdbcHelper.executeQuery(
                "SELECT * FROM `Voucher` WHERE id = ? AND userId = ? AND expTime >= CURRENT_DATE AND quantity > 0",
                rs -> {
                    entity.VoucherEntity v = new entity.VoucherEntity();
                    v.setId(rs.getString("id"));
                    v.setPercent(rs.getDouble("percent"));
                    v.setUserId(rs.getString("userId"));
                    java.sql.Date expDate = rs.getDate("expTime");
                    v.setExpTime(expDate == null ? null : expDate.toLocalDate());
                    v.setQuantity(rs.getInt("quantity"));
                    return v;
                }, dto.getVoucherId(), userId
            );
            if (vouchers.isEmpty()) {
                fail(response, "Voucher không hợp lệ hoặc đã hết hạn");
                return response;
            }
            entity.VoucherEntity voucher = vouchers.get(0);
            discountPercent = voucher.getPercent();
            appliedVoucherId = voucher.getId();
            java.math.BigDecimal discount = totalAmount.multiply(
                    java.math.BigDecimal.valueOf(discountPercent / 100.0));
            totalAmount = totalAmount.subtract(discount).max(java.math.BigDecimal.ZERO);
            // Deduct voucher quantity
            JdbcHelper.executeUpdate("UPDATE `Voucher` SET quantity = quantity - 1 WHERE id = ?", appliedVoucherId);
        }

        String cartId = cartService.getCart(userId).getCartId();
        for (CheckoutItemDto item : dto.getItems()) {
            String orderId = UUID.randomUUID().toString();
            OrderEntity order = new OrderEntity(orderId, userId, item.getProductId(), item.getVariantId(), item.getQuantity(),
                    LocalDateTime.now(), LocalDateTime.now(), "PENDING");
            order.setPhone(dto.getPhone());
            order.setAddress(dto.getAddress());
            order.setCustomerName(dto.getName());
            order.setEmail(dto.getEmail());
            order.setNote(dto.getNote());
            order.setPaymentMethod(dto.getPaymentMethod());
            order.setVoucherId(appliedVoucherId);
            order.setTotalAmount(totalAmount);
            orderRepository.insert(order);
            JdbcHelper.executeUpdate("UPDATE ProductVariant SET quantity = quantity - ? WHERE id = ?", item.getQuantity(), item.getVariantId());
            JdbcHelper.executeUpdate("DELETE FROM ItemCart WHERE cartId = ? AND variantId = ?", cartId, item.getVariantId());
            outBoxService.publishEvent(orderId, TypeEvent.ORDER_CREATED, userId);
            response.getOrderIds().add(orderId);
        }
        response.setSuccess(true);
        response.setSuccessMessage("Checkout successful");
        return response;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.trim().matches("^0\\d{9,10}$");
    }

    public GetOrderResponseDto getOrderDetail(String orderId, String userId) {
        GetOrderResponseDto response = new GetOrderResponseDto();
        OrderEntity order = orderRepository.findById(orderId);
        if (order == null || (userId != null && !userId.equals(order.getUserId()))) {
            fail(response, "Order not found");
            return response;
        }
        response.setOrder(order);
        response.setSuccess(true);
        return response;
    }

    public ListOrderResponseDto getOrderHistory(String userId, int page) {
        int offset = Math.max(0, page - 1) * AppConfig.PAGE_SIZE;
        ListOrderResponseDto response = new ListOrderResponseDto();
        response.setOrders(orderRepository.findByUserId(userId, offset, AppConfig.PAGE_SIZE));
        response.setSuccess(true);
        return response;
    }

    public ListOrderResponseDto listAllOrders(int page) {
        int offset = Math.max(0, page - 1) * AppConfig.PAGE_SIZE;
        ListOrderResponseDto response = new ListOrderResponseDto();
        response.setOrders(orderRepository.findAll(offset, AppConfig.PAGE_SIZE));
        response.setTotal(orderRepository.countAll());
        response.setSuccess(true);
        return response;
    }

    public UpdateOrderStatusResponseDto updateStatus(String orderId, String status) {
        UpdateOrderStatusResponseDto response = new UpdateOrderStatusResponseDto();
        orderRepository.updateStatus(orderId, status);
        response.setSuccess(true);
        response.setSuccessMessage("Order status updated");
        return response;
    }

    public CancelOrderResponseDto cancelOrderByAdmin(String orderId) {
        CancelOrderResponseDto response = new CancelOrderResponseDto();
        OrderEntity order = orderRepository.findById(orderId);
        if (order == null) {
            fail(response, "Order not found");
            return response;
        }
        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            response.setSuccess(true);
            response.setSuccessMessage("Order already cancelled");
            return response;
        }
        if (!"COMPLETED".equalsIgnoreCase(order.getStatus())) {
            JdbcHelper.executeUpdate("UPDATE ProductVariant SET quantity = quantity + ? WHERE id = ?", order.getQuantity(), order.getVariantId());
        }
        orderRepository.updateStatus(orderId, "CANCELLED");
        response.setSuccess(true);
        response.setSuccessMessage("Order cancelled");
        return response;
    }

    public CancelOrderResponseDto cancelOrder(String orderId, String userId) {
        CancelOrderResponseDto response = new CancelOrderResponseDto();
        OrderEntity order = orderRepository.findById(orderId);
        if (order == null || !userId.equals(order.getUserId()) || !"PENDING".equals(order.getStatus())) {
            fail(response, "Only pending owned orders can be cancelled");
            return response;
        }
        orderRepository.updateStatus(orderId, "CANCELLED");
        JdbcHelper.executeUpdate("UPDATE ProductVariant SET quantity = quantity + ? WHERE id = ?", order.getQuantity(), order.getVariantId());
        response.setSuccess(true);
        response.setSuccessMessage("Order cancelled");
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void fail(BaseResponse response, String message) {
        response.setSuccess(false);
        response.setErrorMessage(message);
    }
}
