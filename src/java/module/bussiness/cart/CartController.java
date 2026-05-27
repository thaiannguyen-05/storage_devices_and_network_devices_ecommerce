package module.bussiness.cart;

import common.controller.BaseController;
import common.logger.AppLogger;
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
    private static final AppLogger LOGGER = AppLogger.of(CartController.class);
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            LOGGER.info("Cart GET: fetching cart for current user");
            UserPayload user = getUserFromSession(req);
            if (user == null) {
                LOGGER.info("Cart GET: user not logged in, redirecting to login");
                redirect(req, res, "/auth?action=login");
                return;
            }
            LOGGER.info("Cart GET: user=" + user.getUserId());
            int page = parseInt(req.getParameter("page"), 1);
            LOGGER.info("Cart GET: calling cartService.getCart page=" + page);
            GetCartResponseDto cartResult = cartService.getCart(user.getUserId(), page, AppConfig.PAGE_SIZE);
            LOGGER.info("Cart GET: result items=" + (cartResult != null ? cartResult.getItems().size() : "null"));
            req.setAttribute("cartResult", cartResult);
            forwardToJsp(req, res, "/pages/cart.jsp");
        } catch (Exception ex) {
            LOGGER.error("Cart GET failed", ex);
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        UserPayload user = getUserFromSession(req);
        if (user == null) {
            redirect(req, res, "/auth?action=login");
            return;
        }
        String action = req.getParameter("action");
        try {
            if ("add".equals(action)) {
                String productId = req.getParameter("productId");
                String variantId = req.getParameter("variantId");
                int quantity = parseInt(req.getParameter("quantity"), 1);
                LOGGER.info("Cart add attempt: user=" + user.getUserId() + ", product=" + productId + ", variant=" + variantId + ", qty=" + quantity);
                AddToCartResponseDto result = cartService.addToCart(user.getUserId(), productId, variantId, quantity);
                LOGGER.info("Cart add result: success=" + result.isSuccess() + ", error=" + result.getErrorMessage());
                flash(req, result);
                redirect(req, res, result.isSuccess() ? "/cart" : "/product?id=" + safe(productId));
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
        } catch (Exception ex) {
            LOGGER.error("Cart action failed: action=" + action, ex);
            HttpSession session = req.getSession();
            session.setAttribute("flashError", "Có lỗi xảy ra: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
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
