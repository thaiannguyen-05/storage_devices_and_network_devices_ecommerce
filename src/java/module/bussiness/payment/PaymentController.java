package module.bussiness.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.annotation.Public;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import entity.PaymentEntity;
import entity.ProductEntity;
import entity.ProductVariantEntity;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import module.bussiness.cart.dto.CartItemView;
import module.bussiness.order.OrderService;
import module.bussiness.product.repository.impl.ProductRepository;
import module.bussiness.product.repository.impl.ProductVariantRepository;
import module.bussiness.payment.repository.impl.PaymentRepository;
import module.core.user.repository.impl.UserRepository;
import entity.UserEntity;

@WebServlet(name = "PaymentController", urlPatterns = {"/payment", "/payment/*"})
@Public
public class PaymentController extends HttpServlet {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String UTF_8 = "UTF-8";

    private final PaymentService paymentService = new PaymentService();
    private final PaymentRepository paymentRepository = new PaymentRepository();
    private final OrderService orderService = new OrderService();
    private final ProductRepository productRepository = new ProductRepository();
    private final ProductVariantRepository productVariantRepository = new ProductVariantRepository();
    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(UTF_8);
        String pathInfo = normalizePath(request.getPathInfo());

        if ("/done".equals(pathInfo)) {
            showPaymentDonePage(request, response);
        } else if ("/checkout".equals(pathInfo)) {
            showSePayCheckout(request, response);
        } else {
            showPaymentPage(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(UTF_8);
        response.setCharacterEncoding(UTF_8);
        String pathInfo = normalizePath(request.getPathInfo());

        if ("/webhook".equals(pathInfo)) {
            handleWebhook(request, response);
            return;
        }

        String actionType = request.getParameter("actionType");
        if (actionType == null) actionType = "placeOrder";

        if ("applyVoucher".equalsIgnoreCase(actionType)) {
            applyVoucher(request, response);
            return;
        }

        if ("placeOrder".equalsIgnoreCase(actionType)) {
            placeOrder(request, response);
            return;
        }

        if ("sepayInit".equalsIgnoreCase(actionType)) {
            initSePayPayment(request, response);
            return;
        }

        // CRUD actions for admin
        if ("createPayment".equalsIgnoreCase(actionType)) {
            createPayment(request, response);
        } else if ("updatePayment".equalsIgnoreCase(actionType)) {
            updatePayment(request, response);
        } else if ("deletePayment".equalsIgnoreCase(actionType)) {
            deletePayment(request, response);
        } else if ("queryPayment".equalsIgnoreCase(actionType)) {
            queryPayment(request, response);
        } else if ("listPayments".equalsIgnoreCase(actionType)) {
            listPayments(request, response);
        } else {
            placeOrder(request, response);
        }
    }

    private void showPaymentPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        String authUserId = session.getAttribute("authUserId") == null ? "" : String.valueOf(session.getAttribute("authUserId")).trim();

        List<CartItemView> checkoutItems = resolveCheckoutItems(request, session);

        if (!authUserId.isBlank()) {
            UserEntity authUser = userRepository.findById(authUserId);
            if (authUser != null) {
                request.setAttribute("fullName", value(authUser.getName()));
                request.setAttribute("email", value(authUser.getEmail()));
            }
        }

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

    private void showSePayCheckout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            request.getRequestDispatcher("/views/payment/sepay-checkout.jsp").forward(request, response);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void showPaymentDonePage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            request.getRequestDispatcher("/views/payment/done.jsp").forward(request, response);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void placeOrder(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        String authUserId = session.getAttribute("authUserId") == null ? "" : String.valueOf(session.getAttribute("authUserId")).trim();
        String currentSource = source(request);
        List<CartItemView> checkoutItems = resolveCheckoutItems(request, session);

        try {
            if (!authUserId.isBlank()) {
                String phone = value(request, "phone");
                String address = value(request, "address");
                String city = value(request, "city");
                String mergedAddress = address;
                if (!city.isBlank()) {
                    mergedAddress = mergedAddress.isBlank() ? city : (mergedAddress + " - " + city);
                }

                String paymentMethod = value(request, "paymentMethod");

                if ("SEPAY".equalsIgnoreCase(paymentMethod)) {
                    initSePayAndPlaceOrder(request, response, session, authUserId, checkoutItems, phone, mergedAddress);
                    return;
                }

                if ("buyNow".equalsIgnoreCase(currentSource)) {
                    for (CartItemView item : checkoutItems) {
                        orderService.saveCartOrder(authUserId, item.getProductId(), item.getVariantId(), item.getQuantity());
                        orderService.saveDeliveryInfoForCartOrder(authUserId, item.getProductId(), item.getVariantId(), phone, mergedAddress);
                        orderService.markPlaced(authUserId, item.getProductId(), item.getVariantId());
                    }
                } else {
                    for (CartItemView item : checkoutItems) {
                        orderService.saveDeliveryInfoForCartOrder(authUserId, item.getProductId(), item.getVariantId(), phone, mergedAddress);
                        orderService.markPlaced(authUserId, item.getProductId(), item.getVariantId());
                    }
                }

                if (!"COD".equalsIgnoreCase(paymentMethod)) {
                    for (CartItemView item : checkoutItems) {
                        orderService.markPaidSuccess(authUserId, item.getProductId(), item.getVariantId());
                    }
                }
            }

            if ("cart".equalsIgnoreCase(currentSource)) {
                removeCheckedOutItemsFromSessionCart(session, checkoutItems);
            }

            response.sendRedirect("/payment/done");
            return;
        } catch (Exception e) {
            request.setAttribute("paymentError", "Đặt hàng thất bại: " + e.getMessage());
        }

        showPaymentPage(request, response);
    }

    private void initSePayAndPlaceOrder(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String authUserId, List<CartItemView> checkoutItems,
            String phone, String address) throws IOException {

        try {
            String orderId = UUID.randomUUID().toString();

            // Save order first
            for (CartItemView item : checkoutItems) {
                if ("buyNow".equalsIgnoreCase(source(request))) {
                    orderService.saveCartOrder(authUserId, item.getProductId(), item.getVariantId(), item.getQuantity());
                }
                orderService.saveDeliveryInfoForCartOrder(authUserId, item.getProductId(), item.getVariantId(), phone, address);
                orderService.markPlaced(authUserId, item.getProductId(), item.getVariantId());
            }

            long totalAmount = total(checkoutItems);
            Map<String, Object> payload = new java.util.LinkedHashMap<>();
            payload.put("orderId", orderId);
            payload.put("userId", authUserId);
            payload.put("order_amount", String.valueOf(totalAmount));
            payload.put("order_description", "Thanh toán đơn hàng " + orderId);
            payload.put("success_url", resolveBaseUrl(request) + "/payment/checkout?status=success&orderId=" + orderId);
            payload.put("error_url", resolveBaseUrl(request) + "/payment/checkout?status=failed&orderId=" + orderId);
            payload.put("cancel_url", resolveBaseUrl(request) + "/payment/checkout?status=cancelled&orderId=" + orderId);
            payload.put("ipn_url", resolveBaseUrl(request) + "/payment/webhook");

            Map<String, Object> result = paymentService.initSePayCheckout(payload, "VISA");

            if (Boolean.TRUE.equals(result.get("success"))) {
                String redirectUrl = String.valueOf(result.get("redirect_url"));
                response.sendRedirect(redirectUrl);
            } else {
                request.setAttribute("paymentError", String.valueOf(result.get("message")));
                showPaymentPage(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("paymentError", "Lỗi thanh toán SePay: " + e.getMessage());
            showPaymentPage(request, response);
        }
    }

    private void initSePayPayment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        String authUserId = session.getAttribute("authUserId") == null ? "" : String.valueOf(session.getAttribute("authUserId")).trim();

        try {
            long amount = parseLong(request.getParameter("amount"), 0);
            String orderId = UUID.randomUUID().toString();

            Map<String, Object> payload = new java.util.LinkedHashMap<>();
            payload.put("orderId", orderId);
            payload.put("userId", authUserId);
            payload.put("order_amount", String.valueOf(amount));
            payload.put("order_description", "Thanh toán " + formatVnd(amount));
            payload.put("success_url", resolveBaseUrl(request) + "/payment/checkout?status=success&orderId=" + orderId);
            payload.put("error_url", resolveBaseUrl(request) + "/payment/checkout?status=failed&orderId=" + orderId);
            payload.put("cancel_url", resolveBaseUrl(request) + "/payment/checkout?status=cancelled&orderId=" + orderId);
            payload.put("ipn_url", resolveBaseUrl(request) + "/payment/webhook");

            Map<String, Object> result = paymentService.initSePayCheckout(payload, "VISA");
            writeJson(response, result);
        } catch (Exception e) {
            writeJson(response, Map.of("success", false, "message", e.getMessage()));
        }
    }

    private void handleWebhook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> payload;
        try {
            payload = OBJECT_MAPPER.readValue(request.getInputStream(), new TypeReference<Map<String, Object>>() {});
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

    // === CRUD Payment ===

    private void createPayment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String orderId = value(request, "orderId");
        String userId = value(request, "userId");
        BigDecimal amount = parseBigDecimal(request.getParameter("amount"), BigDecimal.ZERO);

        String id = UUID.randomUUID().toString();
        String signature = generateSignature(id, orderId, amount);

        PaymentEntity entity = new PaymentEntity();
        entity.setId(id);
        entity.setOrderId(orderId);
        entity.setUserId(userId);
        entity.setAmount(amount);
        entity.setAccessKey("SEPAY");
        entity.setPartnerCode("SEPAY");
        entity.setRedirectUrl("");
        entity.setIpnUrl("");
        entity.setExtraData(orderId);
        entity.setRequestType("PURCHASE");
        entity.setSignature(signature);
        entity.setStatus(Constant.PAYMENT_STATUS_PENDING);

        paymentRepository.saveInitPayment(orderId, userId, orderId, amount, "", signature, Constant.PAYMENT_STATUS_PENDING);

        writeJson(response, Map.of("success", true, "paymentId", id));
    }

    private void updatePayment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = value(request, "id");
        String orderId = value(request, "orderId");
        BigDecimal amount = parseBigDecimal(request.getParameter("amount"), null);
        String status = value(request, "status");
        String redirectUrl = value(request, "redirectUrl");
        String signature = value(request, "signature");

        boolean ok = paymentRepository.updatePayment(id, orderId, amount, status, redirectUrl, signature);
        writeJson(response, Map.of("success", ok, "message", ok ? "Updated" : "Not found"));
    }

    private void deletePayment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = value(request, "id");
        boolean ok = paymentRepository.deleteById(id);
        writeJson(response, Map.of("success", ok, "message", ok ? "Deleted" : "Not found"));
    }

    private void queryPayment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = value(request, "id");
        String orderId = value(request, "orderId");

        PaymentEntity entity = null;
        if (!id.isBlank()) {
            entity = paymentRepository.findById(id);
        } else if (!orderId.isBlank()) {
            // Find by orderId - need to add this method or use findAll + filter
            for (PaymentEntity p : paymentRepository.findAll()) {
                if (orderId.equals(p.getOrderId())) {
                    entity = p;
                    break;
                }
            }
        }

        if (entity != null) {
            Map<String, Object> data = Map.of(
                    "id", entity.getId(),
                    "orderId", entity.getOrderId(),
                    "userId", entity.getUserId(),
                    "amount", entity.getAmount(),
                    "status", entity.getStatus(),
                    "partnerCode", entity.getPartnerCode()
            );
            writeJson(response, Map.of("success", true, "data", data));
        } else {
            writeJson(response, Map.of("success", false, "message", "Payment not found"));
        }
    }

    private void listPayments(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userId = value(request, "userId");
        List<PaymentEntity> payments;

        if (!userId.isBlank()) {
            payments = paymentRepository.findByUserId(userId);
        } else {
            payments = paymentRepository.findAll();
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (PaymentEntity p : payments) {
            items.add(Map.of(
                    "id", p.getId(),
                    "orderId", p.getOrderId(),
                    "userId", p.getUserId(),
                    "amount", p.getAmount(),
                    "status", p.getStatus(),
                    "partnerCode", p.getPartnerCode(),
                    "createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : ""
            ));
        }

        writeJson(response, Map.of("success", true, "data", items, "count", items.size()));
    }

    // === Helpers ===

    private String generateSignature(String id, String orderId, BigDecimal amount) {
        String secret = module.core.config.ConfigService.getOrDefault("SEPAY_SECRET_KEY", "default");
        String rawData = "id=" + id + "&orderId=" + orderId + "&amount=" + amount;
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] bytes = mac.doFinal(rawData.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String resolveBaseUrl(HttpServletRequest request) {
        String configured = module.core.config.ConfigService.getOrDefault("PAYMENT_PUBLIC_BASE_URL", "");
        if (!configured.isBlank()) return configured;

        StringBuilder url = new StringBuilder();
        url.append(request.getScheme()).append("://").append(request.getServerName());
        int port = request.getServerPort();
        if (port != 80 && port != 443) url.append(":").append(port);
        return url.toString();
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) return "/";
        String normalized = path.startsWith("/") ? path : "/" + path;
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private List<CartItemView> resolveCheckoutItems(HttpServletRequest request, HttpSession session) {
        String source = source(request);
        if ("buyNow".equalsIgnoreCase(source)) {
            List<CartItemView> items = new ArrayList<>();
            String variantId = value(request, "variantId");
            String productId = value(request, "productId");

            if (!variantId.isBlank()) {
                ProductVariantEntity variant = productVariantRepository.findById(variantId);
                if (variant != null) {
                    productId = variant.getProductId() == null ? "" : variant.getProductId().trim();
                    ProductEntity product = productRepository.findById(productId);
                    if (product != null) {
                        CartItemView item = new CartItemView();
                        item.setProductId(productId);
                        item.setVariantId(variantId);
                        item.setSku(value(variant.getSku()));
                        item.setName(value(product.getName()));
                        item.setCategory(value(product.getCategory()));
                        item.setBrandId(value(product.getBrandId()));
                        item.setImageUrl(value(variant.getImageUrl()));
                        item.setUnitPrice(variant.getPrice() == null ? 0 : variant.getPrice().longValue());
                        item.setStock(variant.getQuantity());
                        int qty = (int) parseLong(request.getParameter("quantity"), 1);
                        if (item.getStock() > 0) qty = Math.min(qty, item.getStock());
                        item.setQuantity(Math.max(1, qty));
                        items.add(item);
                        return items;
                    }
                }
            }
            return items;
        }

        Object raw = session.getAttribute("cartItems");
        if (!(raw instanceof Map)) return new ArrayList<>();

        @SuppressWarnings("unchecked")
        Map<String, CartItemView> cart = (Map<String, CartItemView>) raw;
        return new ArrayList<>(cart.values());
    }

    private void removeCheckedOutItemsFromSessionCart(HttpSession session, List<CartItemView> checkoutItems) {
        Object raw = session.getAttribute("cartItems");
        if (!(raw instanceof Map)) return;
        @SuppressWarnings("unchecked")
        Map<String, CartItemView> cart = (Map<String, CartItemView>) raw;

        Iterator<Map.Entry<String, CartItemView>> iterator = cart.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CartItemView> entry = iterator.next();
            CartItemView current = entry.getValue();
            for (CartItemView checked : checkoutItems) {
                if (sameItem(current, checked)) {
                    iterator.remove();
                    break;
                }
            }
        }
        session.setAttribute("cartItems", cart);
    }

    private boolean sameItem(CartItemView left, CartItemView right) {
        if (left == null || right == null) return false;
        String lp = left.getProductId() == null ? "" : left.getProductId().trim();
        String rp = right.getProductId() == null ? "" : right.getProductId().trim();
        String lv = left.getVariantId() == null ? "" : left.getVariantId().trim();
        String rv = right.getVariantId() == null ? "" : right.getVariantId().trim();
        return lp.equals(rp) && lv.equals(rv);
    }

    private String source(HttpServletRequest request) {
        String s = request.getParameter("source");
        return s == null || s.isBlank() ? "cart" : s.trim();
    }

    private String value(HttpServletRequest request, String key) {
        String v = request.getParameter(key);
        return v == null ? "" : v.trim();
    }

    private String value(String raw) {
        return raw == null ? "" : raw.trim();
    }

    private int count(List<CartItemView> items) {
        int c = 0;
        for (CartItemView item : items) c += item.getQuantity();
        return c;
    }

    private long total(List<CartItemView> items) {
        long t = 0;
        for (CartItemView item : items) t += item.getUnitPrice() * item.getQuantity();
        return t;
    }

    private String formatVnd(long amount) {
        return new DecimalFormat("#,##0").format(amount) + " VND";
    }

    private long parseLong(String raw, long fallback) {
        try { return Long.parseLong(raw); } catch (Exception e) { return fallback; }
    }

    private BigDecimal parseBigDecimal(String raw, BigDecimal fallback) {
        try { return raw == null ? fallback : new BigDecimal(raw); } catch (Exception e) { return fallback; }
    }

    private void writeJson(HttpServletResponse response, Map<String, Object> data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        OBJECT_MAPPER.writeValue(response.getWriter(), data);
    }

    private void applyVoucher(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String voucherCode = value(request, "voucherCode");
        request.setAttribute("voucherCode", voucherCode);
        if (!voucherCode.isEmpty()) {
            request.setAttribute("voucherSummary", "0 VND (demo)");
        }
        showPaymentPage(request, response);
    }
}
