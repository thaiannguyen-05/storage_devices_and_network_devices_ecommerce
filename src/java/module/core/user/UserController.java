package module.core.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.UserEntity;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import module.core.user.dto.FindAllUserDto;
import module.core.user.dto.FindUserByIdDto;

@WebServlet(name = "user", urlPatterns = {"/user"})
public class UserController extends HttpServlet {

    private final UserService userService = new UserService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("findAll".equals(action)) {
            findAllUser(request, response);
        } else if ("findById".equals(action)) {
            findById(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\": \"Action is required (findAll or findById)\"}");
        }
    }

    private void findAllUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
            int limit = request.getParameter("limit") != null ? Integer.parseInt(request.getParameter("limit")) : 10;

            FindAllUserDto dto = new FindAllUserDto(page, limit);
            List<UserEntity> users = userService.findAllUser(dto);

            response.getWriter().write(objectMapper.writeValueAsString(users));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    private void findById(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\": \"ID is required\"}");
                return;
            }

            int id = Integer.parseInt(idParam);
            FindUserByIdDto dto = new FindUserByIdDto(id);
            UserEntity user = userService.findById(dto);

            if (user != null) {
                response.getWriter().write(objectMapper.writeValueAsString(user));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\": \"User not found\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Handle POST for Create/Update/Delete if needed, or redirect to processRequest
    }
}
