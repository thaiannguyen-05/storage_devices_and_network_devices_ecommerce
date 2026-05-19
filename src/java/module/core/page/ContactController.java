package module.core.page;

import common.annotation.Public;
import common.controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import module.bussiness.contact.ContactService;
import module.core.common.BaseResponse;

@Public
@WebServlet(name = "Contact", urlPatterns = {"/contact"})
public class ContactController extends BaseController {
    private final ContactService contactService = new ContactService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        forwardToJsp(req, res, "/pages/contact.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        BaseResponse result = contactService.submitContact(req.getParameter("name"), req.getParameter("email"),
                req.getParameter("message"));
        if (result.isSuccess()) {
            req.setAttribute("success", result.getSuccessMessage());
        } else {
            req.setAttribute("error", result.getErrorMessage());
            req.setAttribute("submittedName", req.getParameter("name"));
            req.setAttribute("submittedEmail", req.getParameter("email"));
            req.setAttribute("submittedMessage", req.getParameter("message"));
        }
        forwardToJsp(req, res, "/pages/contact.jsp");
    }
}
