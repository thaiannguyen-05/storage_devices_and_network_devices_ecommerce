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
import java.util.List;
import java.util.Map;
import module.bussiness.cart.dto.CartItemView;

@WebServlet(name = "payment", urlPatterns = {"/payment/*"})
public class PaymentController extends BaseController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final PaymentService paymentService = new PaymentService();

    @Override
    protected void registerRoutes() {
        registerGet("/", this::showPaymentPage);
        registerPost("/", this::handlePaymentPost);
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

        request.setAttribute("paymentSuccess", "Đặt hàng thành công (demo).");
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
        response.setStatus(HttpServletResponse.SC_OK);
        OBJECT_MAPPER.writeValue(response.getWriter(), result);
    }
}
