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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import entity.ProductEntity;
import entity.ProductVariantEntity;
import module.bussiness.cart.dto.CartItemView;
import module.bussiness.order.OrderService;
import module.bussiness.product.repository.impl.ProductRepository;
import module.bussiness.product.repository.impl.ProductVariantRepository;
import module.core.user.repository.impl.UserRepository;
import entity.UserEntity;

@WebServlet(name = "payment", urlPatterns = {"/payment/*"})
public class PaymentController extends BaseController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final PaymentService paymentService = new PaymentService();
    private final OrderService orderService = new OrderService();
    private final ProductRepository productRepository = new ProductRepository();
    private final ProductVariantRepository productVariantRepository = new ProductVariantRepository();
    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void registerRoutes() {
        registerGet("/", this::showPaymentPage);
        registerGet("/done", this::showPaymentDonePage);
        registerPost("/", this::handlePaymentPost);
        registerPost("/sepay/webhook", this::handleSePayWebhook);
    }

    private void showPaymentPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        String authUserId = session.getAttribute("authUserId") == null ? "" : String.valueOf(session.getAttribute("authUserId")).trim();
        if (authUserId.isBlank()) {
            String next = java.net.URLEncoder.encode(request.getRequestURL().toString() + (request.getQueryString() == null ? "" : "?" + request.getQueryString()), java.nio.charset.StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/auth?action=signin&next=" + next);
            return;
        }

        List<CartItemView> checkoutItems = resolveCheckoutItems(request, session);
        String currentSource = source(request);

        UserEntity authUser = userRepository.findById(authUserId);
        if (authUser != null) {
            request.setAttribute("fullName", value(authUser.getName()));
            request.setAttribute("email", value(authUser.getEmail()));
        }

        request.setAttribute("checkoutItems", checkoutItems);
        request.setAttribute("checkoutCount", count(checkoutItems));
        request.setAttribute("source", source(request));
        if ("1".equals(value(request, "done"))) {
            request.setAttribute("paymentDoneFlash", "1");
        }

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

    private void showPaymentDonePage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            request.getRequestDispatcher("/views/payment/done.jsp").forward(request, response);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void handlePaymentPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
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

                String paymentMethod = value(request, "paymentMethod");
                if (!"COD".equalsIgnoreCase(paymentMethod)) {
                    for (CartItemView item : checkoutItems) {
                        orderService.markPaidSuccess(authUserId, item.getProductId(), item.getVariantId());
                    }
                }
            }

            if ("cart".equalsIgnoreCase(currentSource)) {
                removeCheckedOutItemsFromSessionCart(session, checkoutItems);
            }

            response.sendRedirect(request.getContextPath() + "/payment?done=1");
            return;
        } catch (Exception e) {
            request.setAttribute("paymentError", "Đặt hàng thất bại: " + e.getMessage());
        }

        showPaymentPage(request, response);
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
                        if (item.getStock() > 0) {
                            qty = Math.min(qty, item.getStock());
                        }
                        item.setQuantity(Math.max(1, qty));
                        items.add(item);
                        return items;
                    }
                }
            }

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

    private void removeCheckedOutItemsFromSessionCart(HttpSession session, List<CartItemView> checkoutItems) {
        Object raw = session.getAttribute("cartItems");
        if (!(raw instanceof Map)) {
            return;
        }
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
        if (left == null || right == null) {
            return false;
        }
        String lp = left.getProductId() == null ? "" : left.getProductId().trim();
        String rp = right.getProductId() == null ? "" : right.getProductId().trim();
        String lv = left.getVariantId() == null ? "" : left.getVariantId().trim();
        String rv = right.getVariantId() == null ? "" : right.getVariantId().trim();
        return lp.equals(rp) && lv.equals(rv);
    }

    private String source(HttpServletRequest request) {
        String source = request.getParameter("source");
        return source == null || source.isBlank() ? "cart" : source.trim();
    }

    private String value(HttpServletRequest request, String key) {
        String value = request.getParameter(key);
        return value == null ? "" : value.trim();
    }

    private String value(String raw) {
        return raw == null ? "" : raw.trim();
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
