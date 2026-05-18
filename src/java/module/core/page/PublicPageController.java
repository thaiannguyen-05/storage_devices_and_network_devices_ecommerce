package module.core.page;

import common.annotation.Public;
import common.controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Public
@WebServlet(name = "PublicPages", urlPatterns = {"/contact", "/about"})
public class PublicPageController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        forwardToJsp(req, res, "/pages" + req.getServletPath() + ".jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        req.setAttribute("success", "Da ghi nhan thong tin");
        forwardToJsp(req, res, "/pages" + req.getServletPath() + ".jsp");
    }
}
