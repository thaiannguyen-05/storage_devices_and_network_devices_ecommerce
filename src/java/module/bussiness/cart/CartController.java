package module.bussiness.cart;

import common.annotation.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import entity.ItemCartEntity;
import entity.OrderCartEntity;
import entity.ProductEntity;
import entity.ProductVariantEntity;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import module.bussiness.cart.dto.CartItemView;
import module.bussiness.cart.dto.CreateCartDto;
import module.bussiness.cart.repository.impl.ItemCartRepository;
import module.bussiness.product.repository.impl.ProductRepository;
import module.bussiness.product.repository.impl.ProductVariantRepository;

@Role({"USER", "ADMIN"})
@WebServlet(name = "CartController", urlPatterns = {"/cart", "/CartController"})
public class CartController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CartController.class.getName());

    private final CartService cartService = new CartService();
    private final ItemCartRepository itemCartRepository = new ItemCartRepository();
    private final ProductRepository productRepository = new ProductRepository();
    private final ProductVariantRepository productVariantRepository = new ProductVariantRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        String authUserId = resolveAuthUserId(session);
        if (!authUserId.isBlank()) {
            syncCartFromDb(session, authUserId);
        }
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

        String authUserId = resolveAuthUserId(session);
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        if ("add".equals(action) && authUserId.isBlank()) {
            LOGGER.info("[CART_ADD_AUTH_REQUIRED] traceId=" + traceId + " action=" + action + " productId=" + safe(request.getParameter("productId")) + " variantId=" + safe(request.getParameter("variantId")) + " quantity=" + safe(request.getParameter("quantity")));
            String next = encodeUrl(resolveCurrentUrl(request));
            String loginUrl = "/auth?action=signin&next=" + next;
            if (isAjax) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.write("{\"success\":false,\"needLogin\":true,\"loginUrl\":\"" + loginUrl + "\"}");
                }
                return;
            }
            response.sendRedirect(loginUrl);
            return;
        }

        try {
            switch (action) {
                case "add":
                    LOGGER.info("[CART_ADD_START] traceId=" + traceId + " userId=" + authUserId + " productId=" + safe(request.getParameter("productId")) + " variantId=" + safe(request.getParameter("variantId")) + " quantity=" + safe(request.getParameter("quantity")) + " ajax=" + isAjax);
                    addItem(request, session, cart);
                    LOGGER.info("[CART_ADD_DONE] traceId=" + traceId + " cartCount=" + count(cart));
                    break;
                case "update":
                    updateItem(request, session, cart);
                    break;
                case "remove":
                    removeItem(request, session, cart);
                    break;
                case "clear":
                    clearCart(session, cart);
                    break;
                default:
                    break;
            }
            session.setAttribute("cartItems", cart);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[CART_ACTION_ERROR] traceId=" + traceId + " action=" + action + " userId=" + authUserId + " productId=" + safe(request.getParameter("productId")) + " variantId=" + safe(request.getParameter("variantId")) + " quantity=" + safe(request.getParameter("quantity")), e);
            if ("add".equals(action) && isAjax) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.write("{\"success\":false,\"message\":\"Không thể lưu giỏ hàng vào hệ thống: " + safe(e.getMessage()) + "\"}");
                }
                return;
            }
            throw e;
        }

        if ("add".equals(action) && isAjax) {
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"success\":true,\"cartCount\":" + count(cart) + ",\"message\":\"Thêm vào giỏ hàng thành công\"}");
            }
            return;
        }

        if ("add".equals(action)) {
            String referer = request.getHeader("Referer");
            String contextPath = request.getContextPath();
            if (referer != null && !referer.isBlank()) {
                String redirectUrl = referer + (referer.contains("?") ? "&" : "?") + "cartAdded=1";
                response.sendRedirect(redirectUrl);
                return;
            }
            response.sendRedirect("/product?cartAdded=1");
            return;
        }

        response.sendRedirect("/cart");
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

    private void addItem(HttpServletRequest request, HttpSession session, Map<String, CartItemView> cart) {
        String productId = safe(request.getParameter("productId"));
        String variantId = safe(request.getParameter("variantId"));

        if (productId.isBlank() && !variantId.isBlank()) {
            ProductVariantEntity selectedVariant = productVariantRepository.findById(variantId);
            if (selectedVariant != null) {
                productId = safe(selectedVariant.getProductId());
            }
        }

        if (variantId.isBlank() && !productId.isBlank()) {
            ProductVariantEntity fallbackVariant = productVariantRepository.findFirstByProductId(productId);
            if (fallbackVariant != null) {
                variantId = safe(fallbackVariant.getId());
            }
        }

        String itemKey = makeItemKey(productId, variantId);
        if (itemKey.isBlank()) {
            throw new IllegalArgumentException("Thiếu productId hoặc variantId không hợp lệ");
        }

        CartItemView item = cart.get(itemKey);
        if (item == null) {
            item = new CartItemView();
            item.setProductId(productId);
            item.setVariantId(variantId);
            item.setSku(safe(request.getParameter("sku")));
            item.setName(safe(request.getParameter("name")));
            item.setCategory(safe(request.getParameter("category")));
            item.setBrandId(safe(request.getParameter("brandId")));
            item.setImageUrl(safe(request.getParameter("imageUrl")));
            item.setUnitPrice(parseLong(request.getParameter("priceValue"), 0));
            item.setStock((int) parseLong(request.getParameter("stock"), 0));
            item.setQuantity(Math.max(1, (int) parseLong(request.getParameter("quantity"), 1)));

            if (item.getName().isBlank() || item.getImageUrl().isBlank() || item.getUnitPrice() <= 0 || item.getStock() <= 0 || item.getSku().isBlank()) {
                ProductEntity product = productRepository.findById(productId);
                ProductVariantEntity variant = productVariantRepository.findById(variantId);
                if (product != null) {
                    if (item.getName().isBlank()) item.setName(safe(product.getName()));
                    if (item.getCategory().isBlank()) item.setCategory(safe(product.getCategory()));
                    if (item.getBrandId().isBlank()) item.setBrandId(safe(product.getBrandId()));
                }
                if (variant != null) {
                    if (item.getSku().isBlank()) item.setSku(safe(variant.getSku()));
                    if (item.getImageUrl().isBlank()) item.setImageUrl(safe(variant.getImageUrl()));
                    if (item.getUnitPrice() <= 0 && variant.getPrice() != null) item.setUnitPrice(variant.getPrice().longValue());
                    if (item.getStock() <= 0) item.setStock(variant.getQuantity());
                }
            }
            cart.put(itemKey, item);
        } else {
            int next = item.getQuantity() + Math.max(1, (int) parseLong(request.getParameter("quantity"), 1));
            if (item.getStock() > 0) {
                next = Math.min(next, item.getStock());
            }
            item.setQuantity(Math.max(1, next));
        }

        String authUserId = resolveAuthUserId(session);
        if (!authUserId.isBlank()) {
            OrderCartEntity userCart = requireUserCart(authUserId);
            ItemCartEntity savedItem = itemCartRepository.findByCartIdAndProductAndVariant(userCart.getId(), item.getProductId(), item.getVariantId());
            if (savedItem == null) {
                itemCartRepository.create(userCart.getId(), item.getProductId(), item.getVariantId(), item.getQuantity());
            } else {
                itemCartRepository.updateQuantity(savedItem.getId(), item.getQuantity());
            }
        }
    }

    private void updateItem(HttpServletRequest request, HttpSession session, Map<String, CartItemView> cart) {
        String productId = safe(request.getParameter("productId"));
        String variantId = safe(request.getParameter("variantId"));
        String itemKey = makeItemKey(productId, variantId);
        CartItemView item = cart.get(itemKey);
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

        String authUserId = resolveAuthUserId(session);
        if (!authUserId.isBlank()) {
            OrderCartEntity userCart = requireUserCart(authUserId);
            ItemCartEntity savedItem = itemCartRepository.findByCartIdAndProductAndVariant(userCart.getId(), item.getProductId(), item.getVariantId());
            if (savedItem != null) {
                itemCartRepository.updateQuantity(savedItem.getId(), item.getQuantity());
            }
        }
    }

    private void removeItem(HttpServletRequest request, HttpSession session, Map<String, CartItemView> cart) {
        String productId = safe(request.getParameter("productId"));
        String variantId = safe(request.getParameter("variantId"));
        cart.remove(makeItemKey(productId, variantId));

        String authUserId = resolveAuthUserId(session);
        if (!authUserId.isBlank()) {
            OrderCartEntity userCart = requireUserCart(authUserId);
            itemCartRepository.deleteByCartIdAndProductAndVariant(userCart.getId(), productId, variantId);
        }
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

    private String makeItemKey(String productId, String variantId) {
        String p = safe(productId);
        if (p.isBlank()) {
            return "";
        }
        String v = safe(variantId);
        return v.isBlank() ? p : p + "::" + v;
    }

    private String resolveAuthUserId(HttpSession session) {
        Object authUserId = session.getAttribute("authUserId");
        return authUserId == null ? "" : String.valueOf(authUserId).trim();
    }

    private void clearCart(HttpSession session, Map<String, CartItemView> cart) {
        cart.clear();
        String authUserId = resolveAuthUserId(session);
        if (!authUserId.isBlank()) {
            OrderCartEntity userCart = requireUserCart(authUserId);
            itemCartRepository.clearByCartId(userCart.getId());
        }
    }

    private String resolveCurrentUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return referer;
        }
        return "/product";
    }

    private String encodeUrl(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void syncCartFromDb(HttpSession session, String authUserId) {
        OrderCartEntity userCart = requireUserCart(authUserId);
        List<ItemCartEntity> cartItems = itemCartRepository.findByCartId(userCart.getId());
        Map<String, CartItemView> dbCart = new LinkedHashMap<>();
        for (ItemCartEntity cartItem : cartItems) {
            String productId = safe(cartItem.getProductId());
            String variantId = safe(cartItem.getVariantId());
            if (productId.isBlank()) {
                continue;
            }

            ProductEntity product = productRepository.findById(productId);
            if (product == null) {
                continue;
            }

            ProductVariantEntity variant = null;
            if (!variantId.isBlank()) {
                variant = productVariantRepository.findById(variantId);
            }
            if (variant == null) {
                variant = productVariantRepository.findFirstByProductId(productId);
            }

            CartItemView item = new CartItemView();
            item.setProductId(productId);
            item.setVariantId(variantId);
            item.setName(product.getName());
            item.setCategory(product.getCategory());
            item.setBrandId(product.getBrandId());
            item.setQuantity(Math.max(1, cartItem.getQuantity()));

            if (variant != null) {
                item.setSku(safe(variant.getSku()));
                item.setImageUrl(safe(variant.getImageUrl()));
                item.setUnitPrice(variant.getPrice() == null ? 0 : variant.getPrice().longValue());
                item.setStock(variant.getQuantity());
            } else {
                item.setSku("");
                item.setImageUrl("https://images.unsplash.com/photo-1591488320449-011701bb6704?w=800");
                item.setUnitPrice(0);
                item.setStock(0);
            }

            String key = makeItemKey(productId, variantId);
            dbCart.put(key, item);
        }
        session.setAttribute("cartItems", dbCart);
    }

    private OrderCartEntity requireUserCart(String authUserId) {
        OrderCartEntity userCart = cartService.getCartByUserId(authUserId);
        if (userCart != null) {
            return userCart;
        }
        return cartService.createCart(new CreateCartDto(authUserId));
    }
}
