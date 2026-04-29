/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package module.bussiness.payment;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import module.core.sql.ConnecDb;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import module.bussiness.cart.dto.CartItemView;

/**
 *
 * @author An
 */
@WebServlet(name = "payment", urlPatterns = {"/payment"})
public class PaymentController extends HttpServlet {
    private static final String SESSION_CART_KEY = "cartItems";

    @SuppressWarnings("unchecked")
    private Map<String, CartItemView> getCart(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object existing = session.getAttribute(SESSION_CART_KEY);
        if (existing instanceof Map) {
            return (Map<String, CartItemView>) existing;
        }
        Map<String, CartItemView> cart = new LinkedHashMap<>();
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    private CartItemView fromRequest(HttpServletRequest request) {
        CartItemView item = new CartItemView();
        item.setProductId(request.getParameter("productId"));
        item.setName(request.getParameter("name"));
        item.setCategory(request.getParameter("category"));
        item.setBrandId(request.getParameter("brandId"));
        item.setImageUrl(request.getParameter("imageUrl"));
        item.setUnitPrice(parseLongOrDefault(request.getParameter("priceValue"), 0));
        item.setStock(parseIntOrDefault(request.getParameter("stock"), 0));
        item.setQuantity(Math.max(1, parseIntOrDefault(request.getParameter("quantity"), 1)));
        return item;
    }

    private VoucherInfo resolveVoucher(String voucherCode) {
        if (voucherCode == null) {
            return null;
        }
        String code = voucherCode.trim().toUpperCase();
        if (code.isEmpty()) {
            return null;
        }
        String sql = "SELECT discountType, discountValue, quantity, expTime FROM voucher WHERE UPPER(code) = ? LIMIT 1";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                int quantity = rs.getInt("quantity");
                java.sql.Date expDate = rs.getDate("expTime");
                if (quantity <= 0) {
                    return null;
                }
                if (expDate != null && expDate.before(new java.sql.Date(System.currentTimeMillis()))) {
                    return null;
                }
                
                VoucherInfo info = new VoucherInfo();
                info.setType(rs.getString("discountType"));
                info.setValue(rs.getDouble("discountValue"));
                return info;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static class VoucherInfo {
        private String type;
        private double value;
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
    }

    private void consumeVoucher(String voucherCode) {
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            return;
        }
        String sql = "UPDATE voucher SET quantity = CASE WHEN quantity > 0 THEN quantity - 1 ELSE 0 END "
                + "WHERE UPPER(code) = ? AND quantity > 0";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, voucherCode.trim().toUpperCase());
            ps.executeUpdate();
        } catch (Exception e) {
            // ignore voucher consume failure for now
        }
    }

    private void bindCheckoutView(HttpServletRequest request, List<CartItemView> checkoutItems, String source, String voucherCode) {
        long subtotal = 0;
        int count = 0;
        for (CartItemView item : checkoutItems) {
            subtotal += item.getUnitPrice() * item.getQuantity();
            count += item.getQuantity();
        }

        VoucherInfo voucher = resolveVoucher(voucherCode);
        long discount = 0;
        String voucherSummary = "";
        
        if (voucher != null) {
            if ("PERCENT".equalsIgnoreCase(voucher.getType())) {
                discount = (long) (subtotal * voucher.getValue() / 100);
                voucherSummary = "-" + (int)voucher.getValue() + "%";
            } else {
                discount = (long) voucher.getValue();
                voucherSummary = "-" + new DecimalFormat("#,##0").format(discount) + " VND";
            }
        }
        
        long total = Math.max(0, subtotal - discount);

        request.setAttribute("checkoutItems", checkoutItems);
        request.setAttribute("checkoutCount", count);
        request.setAttribute("subtotalText", new DecimalFormat("#,##0").format(subtotal) + " VND");
        request.setAttribute("discountText", "-" + new DecimalFormat("#,##0").format(discount) + " VND");
        request.setAttribute("totalPriceValue", total);
        request.setAttribute("totalPriceText", new DecimalFormat("#,##0").format(total) + " VND");
        request.setAttribute("voucherCode", voucherCode == null ? "" : voucherCode.trim().toUpperCase());
        request.setAttribute("voucherSummary", voucherSummary);
        request.setAttribute("source", source);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String source = request.getParameter("source");
        String voucherCode = request.getParameter("voucherCode");
        List<CartItemView> checkoutItems = new ArrayList<>();

        if ("buyNow".equalsIgnoreCase(source)) {
            checkoutItems.add(fromRequest(request));
        } else {
            checkoutItems.addAll(getCart(request).values());
            source = "cart";
        }

        bindCheckoutView(request, checkoutItems, source, voucherCode);
        request.getRequestDispatcher("/views/payment/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String source = request.getParameter("source");
        String voucherCode = request.getParameter("voucherCode");
        String actionType = request.getParameter("actionType");
        List<CartItemView> checkoutItems = new ArrayList<>();

        if ("buyNow".equalsIgnoreCase(source)) {
            checkoutItems.add(fromRequest(request));
        } else {
            checkoutItems.addAll(getCart(request).values());
            source = "cart";
        }

        if ("applyVoucher".equalsIgnoreCase(actionType)) {
            bindCheckoutView(request, checkoutItems, source, voucherCode);
            VoucherInfo info = resolveVoucher(voucherCode);
            if (info != null) {
                String valStr = "PERCENT".equalsIgnoreCase(info.getType()) ? (int)info.getValue() + "%" : new DecimalFormat("#,##0").format(info.getValue()) + " VND";
                request.setAttribute("paymentSuccess", "Áp mã thành công: " + voucherCode.toUpperCase() + " (-" + valStr + ").");
            } else if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                request.setAttribute("paymentError", "Mã voucher không hợp lệ hoặc đã hết hạn.");
            }
            request.getRequestDispatcher("/views/payment/index.jsp").forward(request, response);
            return;
        }

        if ("cart".equalsIgnoreCase(source)) {
            getCart(request).clear();
        }
        if (resolveVoucher(voucherCode) != null) {
            consumeVoucher(voucherCode);
        }

        bindCheckoutView(request, checkoutItems, source, voucherCode);
        request.setAttribute("paymentSuccess", "Đặt hàng thành công. Đơn của bạn đã được ghi nhận.");
        request.getRequestDispatcher("/views/payment/index.jsp").forward(request, response);
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private long parseLongOrDefault(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public String getServletInfo() {
        return "Payment controller";
    }

}
