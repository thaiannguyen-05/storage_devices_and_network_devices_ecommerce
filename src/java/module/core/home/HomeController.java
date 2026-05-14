package module.core.home;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;

import java.io.IOException;

@WebServlet(name = "home", urlPatterns = {"/"})
public class HomeController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (isRootRequest(request)) {
            response.sendRedirect(response.encodeRedirectURL(
                request.getContextPath() + "/product"
            ));
            return;
        }

        RequestDispatcher defaultDispatcher = request.getServletContext().getNamedDispatcher("default");
        if (defaultDispatcher != null) {
            defaultDispatcher.forward(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private boolean isRootRequest(HttpServletRequest request) {
        String requestUri = value(request.getRequestURI());
        String contextPath = value(request.getContextPath());

        String path = requestUri;
        if (!contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            path = requestUri.substring(contextPath.length());
        }

        return path.isBlank() || "/".equals(path);
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }
}
