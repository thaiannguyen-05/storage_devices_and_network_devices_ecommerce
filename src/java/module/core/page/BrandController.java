package module.core.page;

import common.annotation.RequiresRole;
import common.controller.BaseController;
import common.type.UserPayload;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import module.bussiness.brand.BrandService;
import module.core.common.BaseResponse;

@RequiresRole("ADMIN")
@WebServlet(name = "Brand", urlPatterns = {"/admin/brands"})
public class BrandController extends BaseController {
    private final BrandService brandService = new BrandService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = action(req, "list");
        if ("create".equals(action) || "edit".equals(action)) {
            if ("edit".equals(action)) {
                req.setAttribute("brand", brandService.getBrandById(req.getParameter("id")));
                forwardToJsp(req, res, "/views/admin/brands/edit.jsp");
            } else {
                forwardToJsp(req, res, "/views/admin/brands/create.jsp");
            }
            return;
        }
        req.setAttribute("brands", brandService.getAllBrands());
        forwardToJsp(req, res, "/views/admin/brands/list.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = action(req, "list");
        BaseResponse result;
        if ("create".equals(action)) {
            UserPayload currentUser = getUserFromSession(req);
            result = brandService.createBrand(req.getParameter("name"), req.getParameter("description"),
                    req.getParameter("status"), currentUser == null ? null : currentUser.getUserId());
        } else if ("edit".equals(action)) {
            result = brandService.updateBrand(req.getParameter("id"), req.getParameter("name"),
                    req.getParameter("description"), req.getParameter("status"));
        } else if ("delete".equals(action)) {
            result = brandService.deleteBrand(req.getParameter("id"));
        } else if ("toggle-status".equals(action)) {
            result = brandService.changeStatus(req.getParameter("id"), req.getParameter("status"));
        } else {
            result = new BaseResponse();
            result.setSuccess(true);
        }
        req.getSession().setAttribute(result.isSuccess() ? "flashSuccess" : "flashError",
                result.isSuccess() ? result.getSuccessMessage() : result.getErrorMessage());
        redirect(req, res, "/admin/brands?action=list");
    }

    private String action(HttpServletRequest req, String fallback) {
        String action = req.getParameter("action");
        return action == null || action.trim().isEmpty() ? fallback : action;
    }
}
