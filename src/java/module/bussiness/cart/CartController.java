package module.bussiness.cart;

import common.controller.BaseController;
import common.type.UserPayload;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import module.bussiness.cart.response_dto.AddToCartResponseDto;
import module.bussiness.cart.response_dto.GetCartResponseDto;
import module.bussiness.cart.response_dto.RemoveFromCartResponseDto;
import module.bussiness.cart.response_dto.UpdateCartItemResponseDto;
import module.core.common.BaseResponse;
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
            AddToCartResponseDto result = cartService.addToCart(user.getUserId(), req.getParameter("productId"), req.getParameter("variantId"), parseInt(req.getParameter("quantity"), 1));
            flash(req, result);
            redirect(req, res, result.isSuccess() ? "/cart" : "/product?id=" + safe(req.getParameter("productId")));
            return;
        } else if ("update".equals(action)) {
            UpdateCartItemResponseDto result = cartService.updateQuantity(user.getUserId(), req.getParameter("itemId"), parseInt(req.getParameter("quantity"), 1));
            flash(req, result);
        } else if ("remove".equals(action)) {
            RemoveFromCartResponseDto result = cartService.removeItem(user.getUserId(), req.getParameter("itemId"));
            flash(req, result);
        } else if ("clear".equals(action)) {
            RemoveFromCartResponseDto result = cartService.clearCart(user.getUserId());
            flash(req, result);
        }
        redirect(req, res, "/cart");
    }

    private void flash(HttpServletRequest req, BaseResponse result) {
        if (result == null) {
            return;
        }
        HttpSession session = req.getSession();
        if (result.isSuccess() && result.getSuccessMessage() != null) {
            session.setAttribute("flashSuccess", result.getSuccessMessage());
        } else if (!result.isSuccess() && result.getErrorMessage() != null) {
            session.setAttribute("flashError", result.getErrorMessage());
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
