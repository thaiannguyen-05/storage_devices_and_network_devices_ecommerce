package module.bussiness.order;

import common.controller.BaseController;
import common.guard.AuthGuard;
import common.type.UserPayload;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import module.bussiness.order.dto.CheckoutDto;
import module.bussiness.order.dto.CheckoutItemDto;

@WebServlet(name = "Order", urlPatterns = {"/checkout", "/order/detail", "/orders", "/admin/orders"})
public class OrderController extends BaseController {
    private final OrderService orderService = new OrderService();
    private final AuthGuard authGuard = new AuthGuard();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String userId = getCurrentUserId(req);
        if (userId == null) {
            redirect(req, res, "/auth?action=login");
            return;
        }
        String action = action(req, isAdminPath(req) ? "list" : "history");
        if ("/checkout".equals(req.getServletPath())) {
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

            // Seed some vouchers for the current user if they don't have any, for seamless demonstration
            int voucherCount = module.core.sql.JdbcHelper.count("SELECT COUNT(*) FROM `Voucher` WHERE userId = ?", userId);
            if (voucherCount == 0) {
                module.core.sql.JdbcHelper.executeUpdate(
                    "INSERT INTO `Voucher` (id, percent, userId, expTime, quantity) VALUES (?, ?, ?, ?, ?)",
                    "VOUCHER10", 10.0, userId, java.time.LocalDate.now().plusDays(30), 5
                );
                module.core.sql.JdbcHelper.executeUpdate(
                    "INSERT INTO `Voucher` (id, percent, userId, expTime, quantity) VALUES (?, ?, ?, ?, ?)",
                    "VOUCHER20", 20.0, userId, java.time.LocalDate.now().plusDays(30), 2
                );
                module.core.sql.JdbcHelper.executeUpdate(
                    "INSERT INTO `Voucher` (id, percent, userId, expTime, quantity) VALUES (?, ?, ?, ?, ?)",
                    "VOUCHER50", 50.0, userId, java.time.LocalDate.now().plusDays(30), 1
                );
            }

            java.util.List<entity.VoucherEntity> vouchers = module.core.sql.JdbcHelper.executeQuery(
                "SELECT * FROM `Voucher` WHERE userId = ? AND expTime >= CURRENT_DATE AND quantity > 0",
                rs -> {
                    entity.VoucherEntity v = new entity.VoucherEntity();
                    v.setId(rs.getString("id"));
                    v.setPercent(rs.getDouble("percent"));
                    v.setUserId(rs.getString("userId"));
                    java.sql.Date expDate = rs.getDate("expTime");
                    v.setExpTime(expDate == null ? null : expDate.toLocalDate());
                    java.sql.Timestamp cAt = rs.getTimestamp("createdAt");
                    v.setCreatedAt(cAt == null ? null : cAt.toLocalDateTime());
                    v.setQuantity(rs.getInt("quantity"));
                    return v;
                }, userId
            );

            req.setAttribute("checkoutItems", checkoutItems);
            req.setAttribute("totalCheckoutPrice", totalCheckoutPrice);
            req.setAttribute("vouchers", vouchers);
            forwardToJsp(req, res, "/pages/checkout.jsp");
            return;
        }
        if ("/order/detail".equals(req.getServletPath())) {
            action = "detail";
        }
        if (isAdminPath(req)) {
            if (!authGuard.checkRole(req, res, "ADMIN")) {
                return;
            }
            if ("detail".equals(action)) {
                req.setAttribute("orderResult", orderService.getOrderDetail(req.getParameter("id"), null));
                forwardToJsp(req, res, "/admin/order-detail.jsp");
                return;
            }
            req.setAttribute("ordersResult", orderService.listAllOrders(parseInt(req.getParameter("page"), 1)));
            forwardToJsp(req, res, "/admin/order-list.jsp");
        } else if ("detail".equals(action)) {
            req.setAttribute("orderResult", orderService.getOrderDetail(req.getParameter("id"), userId));
            forwardToJsp(req, res, "/pages/order-history.jsp");
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
        if ("update-status".equals(action)) {
            if (!authGuard.checkRole(req, res, "ADMIN")) {
                return;
            }
            orderService.updateStatus(req.getParameter("id"), req.getParameter("status"));
            redirect(req, res, "/admin/orders?action=list");
        } else if ("cancel".equals(action)) {
            orderService.cancelOrder(req.getParameter("id"), userId);
            redirect(req, res, "/orders?action=history");
        } else {
            module.bussiness.order.response_dto.CheckoutResponseDto result = orderService.checkout(userId, checkoutDto(req));
            if (result.isSuccess()) {
                redirect(req, res, "/orders?action=history");
            } else {
                // Reload checkout page with error
                module.bussiness.cart.response_dto.GetCartResponseDto cartResult = new module.bussiness.cart.CartService().getCart(userId);
                java.util.List<module.bussiness.cart.CartItemView> checkoutItems = cartResult.getItems();
                java.math.BigDecimal totalCheckoutPrice = cartResult.getTotal();
                java.util.List<entity.VoucherEntity> vouchers = module.core.sql.JdbcHelper.executeQuery(
                    "SELECT * FROM `Voucher` WHERE userId = ? AND expTime >= CURRENT_DATE AND quantity > 0",
                    rs -> {
                        entity.VoucherEntity v = new entity.VoucherEntity();
                        v.setId(rs.getString("id"));
                        v.setPercent(rs.getDouble("percent"));
                        v.setUserId(rs.getString("userId"));
                        java.sql.Date expDate = rs.getDate("expTime");
                        v.setExpTime(expDate == null ? null : expDate.toLocalDate());
                        java.sql.Timestamp cAt = rs.getTimestamp("createdAt");
                        v.setCreatedAt(cAt == null ? null : cAt.toLocalDateTime());
                        v.setQuantity(rs.getInt("quantity"));
                        return v;
                    }, userId
                );
                req.setAttribute("checkoutItems", checkoutItems);
                req.setAttribute("totalCheckoutPrice", totalCheckoutPrice);
                req.setAttribute("vouchers", vouchers);
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
        dto.setPhone(req.getParameter("phone"));
        dto.setAddress(req.getParameter("address"));
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

    private boolean isAdminPath(HttpServletRequest req) {
        return req.getServletPath().startsWith("/admin");
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
