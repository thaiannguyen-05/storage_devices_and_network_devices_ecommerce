package module.bussiness.cart;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import module.bussiness.cart.dto.CartItemView;

@WebServlet(name = "CartController", urlPatterns = {"/cart", "/CartController"})
public class CartController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        Map<String, CartItemView> cart = getCartMap(session);

        List<CartItemView> items = new ArrayList<>(cart.values());
        request.setAttribute("cartItems", items);
        request.setAttribute("cartCount", count(cart));
        request.setAttribute("totalPriceText", formatVnd(total(cart)));
        request.getRequestDispatcher("/views/cart/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        HttpSession session = request.getSession(true);
        Map<String, CartItemView> cart = getCartMap(session);

        switch (action) {
            case "add":
                addItem(request, cart);
                break;
            case "update":
                updateItem(request, cart);
                break;
            case "remove":
                removeItem(request, cart);
                break;
            case "clear":
                cart.clear();
                break;
            default:
                break;
        }

        session.setAttribute("cartItems", cart);
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    @SuppressWarnings("unchecked")
    private Map<String, CartItemView> getCartMap(HttpSession session) {
        Object raw = session.getAttribute("cartItems");
        if (raw instanceof Map) {
            return (Map<String, CartItemView>) raw;
        }
        Map<String, CartItemView> map = new LinkedHashMap<>();
        session.setAttribute("cartItems", map);
        return map;
    }

    private void addItem(HttpServletRequest request, Map<String, CartItemView> cart) {
        String productId = safe(request.getParameter("productId"));
        if (productId.isBlank()) {
            return;
        }

        CartItemView item = cart.get(productId);
        if (item == null) {
            item = new CartItemView();
            item.setProductId(productId);
            item.setName(safe(request.getParameter("name")));
            item.setCategory(safe(request.getParameter("category")));
            item.setBrandId(safe(request.getParameter("brandId")));
            item.setImageUrl(safe(request.getParameter("imageUrl")));
            item.setUnitPrice(parseLong(request.getParameter("priceValue"), 0));
            item.setStock((int) parseLong(request.getParameter("stock"), 0));
            item.setQuantity(1);
            cart.put(productId, item);
            return;
        }

        int next = item.getQuantity() + 1;
        if (item.getStock() > 0) {
            next = Math.min(next, item.getStock());
        }
        item.setQuantity(Math.max(1, next));
    }

    private void updateItem(HttpServletRequest request, Map<String, CartItemView> cart) {
        String productId = safe(request.getParameter("productId"));
        CartItemView item = cart.get(productId);
        if (item == null) {
            return;
        }

        String op = safe(request.getParameter("op"));
        int quantity = (int) parseLong(request.getParameter("quantity"), item.getQuantity());

        if ("inc".equals(op)) {
            quantity = item.getQuantity() + 1;
        } else if ("dec".equals(op)) {
            quantity = item.getQuantity() - 1;
        }

        if (item.getStock() > 0) {
            quantity = Math.min(quantity, item.getStock());
        }
        quantity = Math.max(1, quantity);
        item.setQuantity(quantity);
    }

    private void removeItem(HttpServletRequest request, Map<String, CartItemView> cart) {
        String productId = safe(request.getParameter("productId"));
        cart.remove(productId);
    }

    private int count(Map<String, CartItemView> cart) {
        int count = 0;
        for (CartItemView item : cart.values()) {
            count += item.getQuantity();
        }
        return count;
    }

    private long total(Map<String, CartItemView> cart) {
        long total = 0;
        for (CartItemView item : cart.values()) {
            total += item.getUnitPrice() * item.getQuantity();
        }
        return total;
    }

    private String formatVnd(long amount) {
        return new DecimalFormat("#,##0").format(amount) + " VND";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private long parseLong(String raw, long fallback) {
        try {
            return Long.parseLong(raw);
        } catch (Exception e) {
            return fallback;
        }
    }
}
