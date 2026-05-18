package module.bussiness.cart;

import common.controller.BaseController;
import common.type.UserPayload;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import module.bussiness.cart.response_dto.GetCartResponseDto;
import module.core.config.AppConfig;

@WebServlet(name = "Cart", urlPatterns = {"/cart"})
public class CartController extends BaseController {
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        UserPayload user = getUserFromSession(req);
        int page = parseInt(req.getParameter("page"), 1);
        GetCartResponseDto cartResult = cartService.getCart(user.getUserId(), page, AppConfig.PAGE_SIZE);
        req.setAttribute("cartResult", cartResult);
        forwardToJsp(req, res, "/pages/cart.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        UserPayload user = getUserFromSession(req);
        String action = req.getParameter("action");
        if ("add".equals(action)) {
            cartService.addToCart(user.getUserId(), req.getParameter("productId"), req.getParameter("variantId"), parseInt(req.getParameter("quantity"), 1));
        } else if ("update".equals(action)) {
            cartService.updateQuantity(user.getUserId(), req.getParameter("itemId"), parseInt(req.getParameter("quantity"), 1));
        } else if ("remove".equals(action)) {
            cartService.removeItem(user.getUserId(), req.getParameter("itemId"));
        } else if ("clear".equals(action)) {
            cartService.clearCart(user.getUserId());
        }
        redirect(req, res, "/cart");
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
