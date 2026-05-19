package module.core.page;

import common.annotation.RequiresRole;
import common.controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import module.core.admin.AdminAnalyticsService;
import module.core.admin.response_dto.DashboardResponseDto;

@RequiresRole("ADMIN")
@WebServlet(name = "AdminDashboard", urlPatterns = {"/admin/dashboard", "/admin/dashboard/api/stats"})
public class AdminDashboardController extends BaseController {
    private final AdminAnalyticsService analyticsService = new AdminAnalyticsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        DashboardResponseDto data = analyticsService.getDashboardData();
        if (req.getServletPath().endsWith("/api/stats")) {
            sendJson(res, data);
            return;
        }
        req.setAttribute("dashboardStats", data.getDashboardStats());
        req.setAttribute("recentOrders", data.getRecentOrders());
        req.setAttribute("topProducts", data.getTopProducts());
        req.setAttribute("revenueTrend", data.getRevenueTrend());
        req.setAttribute("orderStatusCounts", data.getOrderStatusCounts());
        forwardToJsp(req, res, "/views/admin/dashboard.jsp");
    }
}
