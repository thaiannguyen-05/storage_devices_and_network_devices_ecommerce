package module.core.admin;

import common.annotation.RequiresRole;
import common.controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import module.bussiness.order.OrderService;
import module.bussiness.order.repository.impl.OrderRepository;
import module.core.config.AppConfig;

@RequiresRole("ADMIN")
@WebServlet(name = "AdminOrderController", urlPatterns = {"/admin/orders"})
public class AdminOrderController extends BaseController {
    private final OrderRepository orderRepository = new OrderRepository();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = action(req, "list");
        if ("detail".equals(action)) {
            req.setAttribute("order", orderRepository.findByIdWithDetails(req.getParameter("id")));
            forwardToJsp(req, res, "/views/admin/orders/detail.jsp");
            return;
        }

        int page = parseInt(req.getParameter("page"), 1);
        String status = trimToNull(req.getParameter("status"));
        String keyword = trimToNull(req.getParameter("keyword"));
        int offset = Math.max(0, page - 1) * AppConfig.PAGE_SIZE;
        int total = orderRepository.countAllWithUser(status, keyword);
        req.setAttribute("orders", orderRepository.findAllWithUser(offset, AppConfig.PAGE_SIZE, status, keyword));
        req.setAttribute("statusCounts", orderRepository.countByStatus());
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", Math.max(1, (int) Math.ceil(total / (double) AppConfig.PAGE_SIZE)));
        req.setAttribute("selectedStatus", status);
        req.setAttribute("keyword", keyword == null ? "" : keyword);
        forwardToJsp(req, res, "/views/admin/orders/list.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = action(req, "list");
        String orderId = req.getParameter("id");
        if ("updateStatus".equals(action)) {
            orderService.updateStatus(orderId, req.getParameter("status"));
            redirect(req, res, "/admin/orders?action=detail&id=" + orderId);
            return;
        }
        if ("cancel".equals(action)) {
            orderService.cancelOrderByAdmin(orderId);
        }
        redirect(req, res, "/admin/orders?action=list");
    }

    private String action(HttpServletRequest req, String fallback) {
        String action = req.getParameter("action");
        return action == null || action.trim().isEmpty() ? fallback : action;
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
