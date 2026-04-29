/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package module.bussiness.cart;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
@WebServlet(name = "CartController", urlPatterns = {"/cart"})
public class CartController extends HttpServlet {
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, CartItemView> cart = getCart(request);
        List<CartItemView> items = new ArrayList<>(cart.values());
        long total = 0;
        int cartCount = 0;
        for (CartItemView item : items) {
            total += item.getUnitPrice() * item.getQuantity();
            cartCount += item.getQuantity();
        }

        request.setAttribute("cartItems", items);
        request.setAttribute("cartCount", cartCount);
        request.setAttribute("totalPriceValue", total);
        request.setAttribute("totalPriceText", new DecimalFormat("#,##0").format(total) + " VND");
        request.getRequestDispatcher("/views/cart/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        Map<String, CartItemView> cart = getCart(request);

        if ("add".equalsIgnoreCase(action)) {
            String productId = request.getParameter("productId");
            if (productId != null && !productId.isBlank()) {
                CartItemView item = cart.get(productId);
                if (item == null) {
                    item = new CartItemView();
                    item.setProductId(productId);
                    item.setName(request.getParameter("name"));
                    item.setCategory(request.getParameter("category"));
                    item.setBrandId(request.getParameter("brandId"));
                    item.setImageUrl(request.getParameter("imageUrl"));
                    item.setUnitPrice(parseLongOrDefault(request.getParameter("priceValue"), 0));
                    item.setStock(parseIntOrDefault(request.getParameter("stock"), 0));
                    item.setQuantity(1);
                    cart.put(productId, item);
                } else {
                    int maxQty = Math.max(item.getStock(), 1);
                    item.setQuantity(Math.min(item.getQuantity() + 1, maxQty));
                }
            }
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.isBlank()) {
                response.sendRedirect(referer);
            } else {
                response.sendRedirect(request.getContextPath() + "/product");
            }
            return;
        }

        if ("remove".equalsIgnoreCase(action)) {
            String productId = request.getParameter("productId");
            if (productId != null) {
                cart.remove(productId);
            }
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        if ("clear".equalsIgnoreCase(action)) {
            cart.clear();
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        if ("update".equalsIgnoreCase(action)) {
            String productId = request.getParameter("productId");
            CartItemView item = cart.get(productId);
            if (item != null) {
                String op = request.getParameter("op");
                int requestedQty;
                if ("inc".equalsIgnoreCase(op)) {
                    requestedQty = item.getQuantity() + 1;
                } else if ("dec".equalsIgnoreCase(op)) {
                    requestedQty = item.getQuantity() - 1;
                } else {
                    requestedQty = parseIntOrDefault(request.getParameter("quantity"), item.getQuantity());
                }
                int boundedQty = Math.max(1, Math.min(requestedQty, Math.max(item.getStock(), 1)));
                item.setQuantity(boundedQty);
            }
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/cart");
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
        return "Cart controller";
    }

}
