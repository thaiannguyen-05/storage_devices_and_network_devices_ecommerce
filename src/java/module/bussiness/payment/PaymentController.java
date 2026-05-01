package module.bussiness.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.controller.BaseController;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import module.bussiness.cart.dto.CartItemView;
import module.core.config.ConfigService;

@WebServlet(name = "payment", urlPatterns = {"/payment/*"})
public class PaymentController extends BaseController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final PaymentService paymentService = new PaymentService();

    @Override
    protected void registerRoutes() {
        registerGet("/", this::showPaymentPage);
        registerGet("/success", this::handlePaymentSuccess);
        registerGet("/error", this::handlePaymentError);
        registerGet("/cancel", this::handlePaymentCancel);
        registerPost("/", this::handlePaymentPost);
        registerPost("/sepay/query", this::handleSePayQuery);
        registerPost("/sepay/cancel", this::handleSePayCancel);
        registerPost("/sepay/void", this::handleSePayVoid);
        registerPost("/sepay/webhook", this::handleSePayWebhook);
    }

    private void showPaymentPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        List<CartItemView> checkoutItems = resolveCheckoutItems(request, session);

        request.setAttribute("checkoutItems", checkoutItems);
        request.setAttribute("checkoutCount", count(checkoutItems));
        request.setAttribute("source", source(request));

        long subtotal = total(checkoutItems);
        long discount = 0;
        request.setAttribute("subtotalText", formatVnd(subtotal));
        request.setAttribute("discountText", "-" + formatVnd(discount));
        request.setAttribute("totalPriceText", formatVnd(Math.max(0, subtotal - discount)));

        try {
            request.getRequestDispatcher("/views/payment/index.jsp").forward(request, response);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void handlePaymentPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String actionType = request.getParameter("actionType");
        if (actionType == null) {
            actionType = "placeOrder";
        }

        if ("applyVoucher".equalsIgnoreCase(actionType)) {
            String voucherCode = request.getParameter("voucherCode");
            request.setAttribute("voucherCode", voucherCode == null ? "" : voucherCode.trim());
            if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                request.setAttribute("voucherSummary", "0 VND (demo)");
            }
            showPaymentPage(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        List<CartItemView> checkoutItems = resolveCheckoutItems(request, session);
        long orderAmount = total(checkoutItems);

        String paymentMethod = request.getParameter("paymentMethod");

        Map<String, Object> payload = new HashMap<>();
        payload.put("order_amount", String.valueOf(Math.max(0, orderAmount)));
        payload.put("merchant", ConfigService.getOrDefault("SEPAY_MERCHANT", ""));
        payload.put("currency", "VND");
        payload.put("operation", "PURCHASE");
        payload.put("order_description", "Thanh toán đơn hàng tại StoreIT");
        payload.put("order_invoice_number", "INV_" + System.currentTimeMillis());
        payload.put("customer_id", session.getId());
        String publicBaseUrl = resolvePublicBaseUrl(request);
        payload.put("success_url", request.getParameter("success_url") == null ? publicBaseUrl + "/payment/success" : request.getParameter("success_url"));
        payload.put("error_url", request.getParameter("error_url") == null ? publicBaseUrl + "/payment/error" : request.getParameter("error_url"));
        payload.put("cancel_url", request.getParameter("cancel_url") == null ? publicBaseUrl + "/payment/cancel" : request.getParameter("cancel_url"));
        payload.put("ipn_url", ConfigService.getOrDefault("SEPAY_IPN_URL", publicBaseUrl + "/payment/sepay/webhook"));
        payload.put("source", source(request));
        payload.put("actionType", actionType);
        payload.put("orderId", request.getParameter("orderId"));
        payload.put("userId", request.getParameter("userId"));

        Map<String, Object> paymentResult = paymentService.processPayment(paymentMethod, payload);

        String method = String.valueOf(paymentResult.getOrDefault("method", ""));
        String redirectUrl = String.valueOf(paymentResult.getOrDefault("redirect_url", ""));
        if (Boolean.TRUE.equals(paymentResult.get("success")) && Constant.PAYMENT_METHOD_SEPAY.equals(method) && !redirectUrl.isBlank()) {
            response.sendRedirect(redirectUrl);
            return;
        }

        if (Boolean.TRUE.equals(paymentResult.get("success"))) {
            request.setAttribute("paymentSuccess", String.valueOf(paymentResult.get("message")));
        } else {
            request.setAttribute("paymentError", String.valueOf(paymentResult.get("message")));
        }

        request.setAttribute("selectedPaymentMethod", method);
        showPaymentPage(request, response);
    }

    private void handleSePayQuery(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> payload = new HashMap<>();
        payload.put("merchant", request.getParameter("merchant"));
        payload.put("order_invoice_number", request.getParameter("order_invoice_number"));
        payload.put("transaction_id", request.getParameter("transaction_id"));
        payload.put("orderId", request.getParameter("orderId"));

        Map<String, Object> result = paymentService.querySePayTransaction(payload);
        response.setStatus(Boolean.TRUE.equals(result.get("success")) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_GATEWAY);
        OBJECT_MAPPER.writeValue(response.getWriter(), result);
    }

    private void handleSePayCancel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> payload = new HashMap<>();
        payload.put("merchant", request.getParameter("merchant"));
        payload.put("order_invoice_number", request.getParameter("order_invoice_number"));
        payload.put("orderId", request.getParameter("orderId"));

        if (request.getParameter("order_invoice_number") == null || request.getParameter("order_invoice_number").isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            OBJECT_MAPPER.writeValue(response.getWriter(), Map.of("success", false, "message", "Thiếu order_invoice_number"));
            return;
        }

        Map<String, Object> result = paymentService.cancelSePayOrder(payload);
        response.setStatus(Boolean.TRUE.equals(result.get("success")) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_GATEWAY);
        OBJECT_MAPPER.writeValue(response.getWriter(), result);
    }

    private void handleSePayVoid(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> payload = new HashMap<>();
        payload.put("merchant", request.getParameter("merchant"));
        payload.put("order_invoice_number", request.getParameter("order_invoice_number"));
        payload.put("transaction_id", request.getParameter("transaction_id"));
        payload.put("orderId", request.getParameter("orderId"));

        if ((request.getParameter("order_invoice_number") == null || request.getParameter("order_invoice_number").isBlank())
                && (request.getParameter("transaction_id") == null || request.getParameter("transaction_id").isBlank())) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            OBJECT_MAPPER.writeValue(response.getWriter(), Map.of("success", false, "message", "Thiếu order_invoice_number hoặc transaction_id"));
            return;
        }

        Map<String, Object> result = paymentService.voidSePayTransaction(payload);
        response.setStatus(Boolean.TRUE.equals(result.get("success")) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_GATEWAY);
        OBJECT_MAPPER.writeValue(response.getWriter(), result);
    }

    private void handlePaymentSuccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setAttribute("paymentSuccess", "Thanh toán thành công.");
        showPaymentPage(request, response);
    }

    private void handlePaymentError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setAttribute("paymentError", "Thanh toán thất bại.");
        showPaymentPage(request, response);
    }

    private void handlePaymentCancel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setAttribute("paymentError", "Thanh toán đã bị hủy.");
        showPaymentPage(request, response);
    }

    private List<CartItemView> resolveCheckoutItems(HttpServletRequest request, HttpSession session) {
        String source = source(request);
        if ("buyNow".equalsIgnoreCase(source)) {
            List<CartItemView> items = new ArrayList<>();
            CartItemView item = new CartItemView();
            item.setProductId(value(request, "productId"));
            item.setName(value(request, "name"));
            item.setCategory(value(request, "category"));
            item.setBrandId(value(request, "brandId"));
            item.setImageUrl(value(request, "imageUrl"));
            item.setUnitPrice(parseLong(request.getParameter("priceValue"), 0));
            item.setStock((int) parseLong(request.getParameter("stock"), 0));
            item.setQuantity((int) parseLong(request.getParameter("quantity"), 1));
            items.add(item);
            return items;
        }

        Object raw = session.getAttribute("cartItems");
        if (!(raw instanceof Map)) {
            return new ArrayList<>();
        }

        @SuppressWarnings("unchecked")
        Map<String, CartItemView> cart = (Map<String, CartItemView>) raw;
        return new ArrayList<>(cart.values());
    }

    private String source(HttpServletRequest request) {
        String source = request.getParameter("source");
        return source == null || source.isBlank() ? "cart" : source.trim();
    }

    private String value(HttpServletRequest request, String key) {
        String value = request.getParameter(key);
        return value == null ? "" : value.trim();
    }

    private int count(List<CartItemView> items) {
        int count = 0;
        for (CartItemView item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    private long total(List<CartItemView> items) {
        long total = 0;
        for (CartItemView item : items) {
            total += item.getUnitPrice() * item.getQuantity();
        }
        return total;
    }

    private String formatVnd(long amount) {
        return new DecimalFormat("#,##0").format(amount) + " VND";
    }

    private long parseLong(String raw, long fallback) {
        try {
            return Long.parseLong(raw);
        } catch (Exception e) {
            return fallback;
        }
    }

    private String resolvePublicBaseUrl(HttpServletRequest request) {
        String configured = ConfigService.getOrDefault("PAYMENT_PUBLIC_BASE_URL", "");
        if (!configured.isBlank()) {
            return trimTrailingSlash(configured);
        }

        configured = ConfigService.getOrDefault("SEPAY_PUBLIC_BASE_URL", "");
        if (!configured.isBlank()) {
            return trimTrailingSlash(configured);
        }

        return buildBaseUrl(request);
    }

    private String buildBaseUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder();
        url.append(request.getScheme()).append("://").append(request.getServerName());
        int port = request.getServerPort();
        if (port != 80 && port != 443) {
            url.append(":").append(port);
        }
        return url.toString();
    }

    private String trimTrailingSlash(String value) {
        String text = value.trim();
        if (text.endsWith("/")) {
            return text.substring(0, text.length() - 1);
        }
        return text;
    }

    private void handleSePayWebhook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> payload;
        try {
            payload = OBJECT_MAPPER.readValue(
                    request.getInputStream(),
                    new TypeReference<Map<String, Object>>() {
                    }
            );
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            OBJECT_MAPPER.writeValue(response.getWriter(), Map.of("success", false, "message", "Invalid webhook payload"));
            return;
        }

        Map<String, Object> result = paymentService.handleSePayWebhook(payload);
        int statusCode = 200;
        if (!Boolean.TRUE.equals(result.get("success"))) {
            Object code = result.get("code");
            if (code instanceof Number) {
                statusCode = ((Number) code).intValue();
            } else {
                statusCode = HttpServletResponse.SC_BAD_REQUEST;
            }
        }
        response.setStatus(statusCode);
        OBJECT_MAPPER.writeValue(response.getWriter(), result);
    }
}
