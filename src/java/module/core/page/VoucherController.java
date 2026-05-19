package module.core.page;

import common.annotation.RequiresRole;
import common.controller.BaseController;
import common.type.UserPayload;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import module.bussiness.voucher.VoucherService;
import module.core.common.BaseResponse;

@RequiresRole("ADMIN")
@WebServlet(name = "Voucher", urlPatterns = {"/admin/vouchers"})
public class VoucherController extends BaseController {
    private final VoucherService voucherService = new VoucherService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = action(req, "list");
        if ("create".equals(action) || "edit".equals(action)) {
            if ("edit".equals(action)) {
                req.setAttribute("voucher", voucherService.getVoucherById(req.getParameter("id")));
                forwardToJsp(req, res, "/views/admin/vouchers/edit.jsp");
            } else {
                UserPayload currentUser = getUserFromSession(req);
                req.setAttribute("defaultUserId", currentUser == null ? "" : currentUser.getUserId());
                forwardToJsp(req, res, "/views/admin/vouchers/create.jsp");
            }
            return;
        }
        req.setAttribute("vouchers", voucherService.getAllVouchers());
        forwardToJsp(req, res, "/views/admin/vouchers/list.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = action(req, "list");
        BaseResponse result;
        if ("create".equals(action)) {
            result = voucherService.createVoucher(req.getParameter("id"), req.getParameter("percent"),
                    req.getParameter("userId"), req.getParameter("expTime"), req.getParameter("quantity"));
        } else if ("edit".equals(action)) {
            result = voucherService.updateVoucher(req.getParameter("id"), req.getParameter("percent"),
                    req.getParameter("userId"), req.getParameter("expTime"), req.getParameter("quantity"));
        } else if ("delete".equals(action)) {
            result = voucherService.deleteVoucher(req.getParameter("id"));
        } else {
            result = new BaseResponse();
            result.setSuccess(true);
        }
        req.getSession().setAttribute(result.isSuccess() ? "flashSuccess" : "flashError",
                result.isSuccess() ? result.getSuccessMessage() : result.getErrorMessage());
        redirect(req, res, "/admin/vouchers?action=list");
    }

    private String action(HttpServletRequest req, String fallback) {
        String action = req.getParameter("action");
        return action == null || action.trim().isEmpty() ? fallback : action;
    }
}
