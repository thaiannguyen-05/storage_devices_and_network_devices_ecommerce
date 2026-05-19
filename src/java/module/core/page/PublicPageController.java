package module.core.page;

import common.annotation.Public;
import common.controller.BaseController;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Public
@WebServlet(name = "PublicPages", urlPatterns = {"/about"})
public class PublicPageController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        forwardToJsp(req, res, "/pages" + req.getServletPath() + ".jsp");
    }
}
