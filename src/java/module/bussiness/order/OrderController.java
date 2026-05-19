package module.bussiness.order;

import common.controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import module.bussiness.order.dto.CheckoutDto;
import module.bussiness.order.dto.CheckoutItemDto;
import module.bussiness.voucher.VoucherService;

@WebServlet(name = "Order", urlPatterns = {"/checkout", "/order/detail", "/orders"})
public class OrderController extends BaseController {
    private final OrderService orderService = new OrderService();
    private final VoucherService voucherService = new VoucherService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String userId = getCurrentUserId(req);
        if (userId == null) {
            redirect(req, res, "/auth?action=login");
            return;
        }
        String action = action(req, "history");
        if ("/checkout".equals(req.getServletPath())) {
            String payAction = req.getParameter("action");
            if ("check".equals(payAction)) {
                String orderIdsStr = req.getParameter("orderIds");
                boolean allPaid = true;
                if (orderIdsStr != null && !orderIdsStr.trim().isEmpty()) {
                    String[] orderIds = orderIdsStr.split(",");
                    for (String id : orderIds) {
                        module.bussiness.order.response_dto.GetOrderResponseDto details = orderService.getOrderDetail(id, null);
                        if (details.isSuccess() && details.getOrder() != null) {
                            if (!"PAID".equalsIgnoreCase(details.getOrder().getStatus()) && !"COMPLETED".equalsIgnoreCase(details.getOrder().getStatus())) {
                                allPaid = false;
                                break;
                            }
                        } else {
                            allPaid = false;
                            break;
                        }
                    }
                } else {
                    allPaid = false;
                }
                res.setContentType("application/json");
                res.setCharacterEncoding("UTF-8");
                res.getWriter().write("{\"paid\":" + allPaid + "}");
                return;
            }

            if ("pay".equals(payAction)) {
                String orderIdsStr = req.getParameter("orderIds");
                if (orderIdsStr == null || orderIdsStr.trim().isEmpty()) {
                    redirect(req, res, "/orders?action=history");
                    return;
                }
                String[] orderIds = orderIdsStr.split(",");
                java.util.List<entity.OrderEntity> orders = new java.util.ArrayList<>();
                java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
                for (String id : orderIds) {
                    module.bussiness.order.response_dto.GetOrderResponseDto details = orderService.getOrderDetail(id, userId);
                    if (details.isSuccess() && details.getOrder() != null) {
                        orders.add(details.getOrder());
                        totalAmount = totalAmount.add(details.getOrder().getTotalAmount());
                    }
                }
                req.setAttribute("paymentOrders", orders);
                req.setAttribute("paymentTotal", totalAmount);
                req.setAttribute("sepayBank", module.core.config.AppConfig.SEPAY_BANK);
                req.setAttribute("sepayAccNum", module.core.config.AppConfig.SEPAY_ACC_NUM);
                req.setAttribute("sepayAccName", module.core.config.AppConfig.SEPAY_ACC_NAME);
                
                String transCode = "DH" + orderIds[0].substring(0, Math.min(8, orderIds[0].length()));
                req.setAttribute("transCode", transCode);
                
                forwardToJsp(req, res, "/pages/pay.jsp");
                return;
            }

            module.bussiness.cart.response_dto.GetCartResponseDto cartResult = new module.bussiness.cart.CartService().getCart(userId);
            String[] selectedItemIds = req.getParameterValues("selectedItems");
            java.util.List<module.bussiness.cart.CartItemView> checkoutItems = new java.util.ArrayList<>();
            java.math.BigDecimal totalCheckoutPrice = java.math.BigDecimal.ZERO;

            if (selectedItemIds != null && selectedItemIds.length > 0) {
                java.util.Set<String> selectedSet = new java.util.HashSet<>(java.util.Arrays.asList(selectedItemIds));
                for (module.bussiness.cart.CartItemView item : cartResult.getItems()) {
                    if (selectedSet.contains(item.getId())) {
                        checkoutItems.add(item);
                        totalCheckoutPrice = totalCheckoutPrice.add(item.getLineTotal());
                    }
                }
            } else {
                checkoutItems = cartResult.getItems();
                totalCheckoutPrice = cartResult.getTotal();
            }

            req.setAttribute("checkoutItems", checkoutItems);
            req.setAttribute("totalCheckoutPrice", totalCheckoutPrice);
            req.setAttribute("vouchers", voucherService.getActiveVouchersForUser(userId));
            forwardToJsp(req, res, "/pages/checkout.jsp");
            return;
        }
        if ("/order/detail".equals(req.getServletPath())) {
            action = "detail";
        }
        if ("detail".equals(action)) {
            req.setAttribute("orderResult", orderService.getOrderDetail(req.getParameter("id"), userId));
            forwardToJsp(req, res, "/pages/order-detail.jsp");
        } else {
            req.setAttribute("ordersResult", orderService.getOrderHistory(userId, parseInt(req.getParameter("page"), 1)));
            forwardToJsp(req, res, "/pages/order-history.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String userId = getCurrentUserId(req);
        if (userId == null) {
            redirect(req, res, "/auth?action=login");
            return;
        }
        String action = action(req, "checkout");
        if ("/checkout".equals(req.getServletPath())) {
            action = "checkout";
        }
        if ("cancel".equals(action)) {
            orderService.cancelOrder(req.getParameter("id"), userId);
            redirect(req, res, "/orders?action=history");
        } else {
            CheckoutDto checkoutDto = checkoutDto(req);
            module.bussiness.order.response_dto.CheckoutResponseDto result = orderService.checkout(userId, checkoutDto);
            if (result.isSuccess()) {
                if ("SEPAY".equalsIgnoreCase(checkoutDto.getPaymentMethod())) {
                    String orderIdsParam = String.join(",", result.getOrderIds());
                    redirect(req, res, "/checkout?action=pay&orderIds=" + orderIdsParam);
                } else {
                    redirect(req, res, "/orders?action=history");
                }
            } else {
                // Reload checkout page with error
                module.bussiness.cart.response_dto.GetCartResponseDto cartResult = new module.bussiness.cart.CartService().getCart(userId);
                java.util.List<module.bussiness.cart.CartItemView> checkoutItems = cartResult.getItems();
                java.math.BigDecimal totalCheckoutPrice = cartResult.getTotal();
                req.setAttribute("checkoutItems", checkoutItems);
                req.setAttribute("totalCheckoutPrice", totalCheckoutPrice);
                req.setAttribute("vouchers", voucherService.getActiveVouchersForUser(userId));
                req.setAttribute("error", result.getErrorMessage());
                // Preserve form values
                req.setAttribute("submittedName", req.getParameter("name"));
                req.setAttribute("submittedEmail", req.getParameter("email"));
                req.setAttribute("submittedPhone", req.getParameter("phone"));
                req.setAttribute("submittedAddress", req.getParameter("address"));
                req.setAttribute("submittedNote", req.getParameter("note"));
                req.setAttribute("submittedPaymentMethod", req.getParameter("paymentMethod"));
                req.setAttribute("submittedVoucherId", req.getParameter("voucherId"));
                forwardToJsp(req, res, "/pages/checkout.jsp");
            }
        }
    }

    private CheckoutDto checkoutDto(HttpServletRequest req) {
        CheckoutDto dto = new CheckoutDto();
        dto.setName(req.getParameter("name"));
        dto.setEmail(req.getParameter("email"));
        dto.setPhone(req.getParameter("phone"));
        dto.setAddress(req.getParameter("address"));
        dto.setNote(req.getParameter("note"));
        dto.setPaymentMethod(req.getParameter("paymentMethod"));
        dto.setVoucherId(req.getParameter("voucherId"));
        String[] productIds = req.getParameterValues("productId");
        String[] variantIds = req.getParameterValues("variantId");
        String[] quantities = req.getParameterValues("quantity");
        if (productIds != null) {
            for (int i = 0; i < productIds.length; i++) {
                CheckoutItemDto item = new CheckoutItemDto();
                item.setProductId(productIds[i]);
                item.setVariantId(variantIds == null || i >= variantIds.length ? null : variantIds[i]);
                item.setQuantity(parseInt(quantities == null || i >= quantities.length ? null : quantities[i], 1));
                dto.getItems().add(item);
            }
        }
        return dto;
    }
    private String action(HttpServletRequest req, String fallback) {
        String action = req.getParameter("action");
        return action == null || action.trim().isEmpty() ? fallback : action;
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
