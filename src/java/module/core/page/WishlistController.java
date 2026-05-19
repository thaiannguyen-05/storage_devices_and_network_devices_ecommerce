package module.core.page;

import common.controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import module.bussiness.wishlist.WishlistService;
import module.core.common.BaseResponse;

@WebServlet(name = "Wishlist", urlPatterns = {"/wishlist"})
public class WishlistController extends BaseController {
    private final WishlistService wishlistService = new WishlistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String userId = getCurrentUserId(req);
        if (userId == null) {
            redirect(req, res, "/auth?action=login");
            return;
        }
        req.setAttribute("wishlistProducts", wishlistService.getWishlistByUserId(userId));
        forwardToJsp(req, res, "/pages/wishlist.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String userId = getCurrentUserId(req);
        if (userId == null) {
            redirect(req, res, "/auth?action=login");
            return;
        }
        String action = req.getParameter("action");
        BaseResponse result;
        if ("add".equals(action)) {
            result = wishlistService.addToWishlist(userId, req.getParameter("productId"));
        } else if ("clear".equals(action)) {
            result = wishlistService.clearWishlist(userId);
        } else {
            result = wishlistService.removeFromWishlist(userId, req.getParameter("productId"));
        }
        req.getSession().setAttribute(result.isSuccess() ? "flashSuccess" : "flashError",
                result.isSuccess() ? result.getSuccessMessage() : result.getErrorMessage());
        redirect(req, res, "/wishlist");
    }
}
