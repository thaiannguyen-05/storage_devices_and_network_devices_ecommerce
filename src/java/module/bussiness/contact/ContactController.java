package module.bussiness.contact;

import common.annotation.Public;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Public
@WebServlet(name = "contact", urlPatterns = {"/contact"})
public class ContactController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/contact/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String fullName = value(request.getParameter("fullName"));
        String email = value(request.getParameter("email"));
        String subject = value(request.getParameter("subject"));
        String message = value(request.getParameter("message"));

        if (fullName.isBlank() || email.isBlank() || subject.isBlank() || message.isBlank()) {
            request.setAttribute("error", "Vui lòng điền đầy đủ thông tin.");
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);
            request.setAttribute("subject", subject);
            request.setAttribute("message", message);
            request.getRequestDispatcher("/views/contact/index.jsp").forward(request, response);
            return;
        }

        if (!email.matches("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")) {
            request.setAttribute("error", "Email không đúng định dạng.");
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);
            request.setAttribute("subject", subject);
            request.setAttribute("message", message);
            request.getRequestDispatcher("/views/contact/index.jsp").forward(request, response);
            return;
        }

        // TODO: Save to database when repository is ready
        // For now, just show success message
        request.setAttribute("success", "Cảm ơn bạn đã liên hệ. Chúng tôi sẽ phản hồi sớm nhất.");
        request.getRequestDispatcher("/views/contact/index.jsp").forward(request, response);
    }

    private String value(String s) {
        return s == null ? "" : s.trim();
    }
}
