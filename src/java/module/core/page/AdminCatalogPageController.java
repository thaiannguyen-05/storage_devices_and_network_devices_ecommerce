package module.core.page;

import common.annotation.RequiresRole;
import common.controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiresRole("ADMIN")
@WebServlet(name = "AdminCatalogPages", urlPatterns = {"/admin/brands", "/admin/vouchers"})
public class AdminCatalogPageController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String section = req.getServletPath().substring("/admin/".length());
        String action = req.getParameter("action");
        if ("create".equals(action) || "edit".equals(action)) {
            forwardToJsp(req, res, "/admin/" + singular(section) + "-form.jsp");
        } else {
            forwardToJsp(req, res, "/admin/" + singular(section) + "-list.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String section = req.getServletPath().substring("/admin/".length());
        redirect(req, res, "/admin/" + section + "?action=list");
    }

    private String singular(String section) {
        if ("brands".equals(section)) {
            return "brand";
        }
        if ("vouchers".equals(section)) {
            return "voucher";
        }
        return section;
    }
}
