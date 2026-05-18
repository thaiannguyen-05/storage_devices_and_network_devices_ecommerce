package module.core.user;

import common.controller.BaseController;
import common.annotation.RequiresRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import module.core.user.dto.CreateUserDto;
import module.core.user.dto.UpdateUserDto;

@WebServlet(name = "AdminUsers", urlPatterns = {"/admin/users"})
@RequiresRole("ADMIN")
public class UserController extends BaseController {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = action(req, "list");
        if ("create".equals(action)) {
            forwardToJsp(req, res, "/admin/user-form.jsp");
            return;
        }
        if ("edit".equals(action)) {
            req.setAttribute("userResult", userService.getUserById(req.getParameter("id")));
            forwardToJsp(req, res, "/admin/user-form.jsp");
            return;
        } else {
            req.setAttribute("usersResult", userService.listUsers(parseInt(req.getParameter("page"), 1), 12));
        }
        forwardToJsp(req, res, "/admin/user-list.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = action(req, "list");
        if ("create".equals(action)) {
            userService.createUser(createDto(req));
        } else if ("edit".equals(action)) {
            UpdateUserDto dto = updateDto(req);
            userService.updateUser(dto);
        } else if ("change-status".equals(action)) {
            userService.changeStatus(req.getParameter("id"), req.getParameter("status"));
        } else if ("delete".equals(action)) {
            userService.deleteUser(req.getParameter("id"));
        }
        redirect(req, res, "/admin/users?action=list");
    }

    private CreateUserDto createDto(HttpServletRequest req) {
        CreateUserDto dto = new CreateUserDto();
        dto.setName(req.getParameter("name"));
        dto.setEmail(req.getParameter("email"));
        dto.setPassword(req.getParameter("password"));
        dto.setRole(req.getParameter("role"));
        dto.setStatus(req.getParameter("status"));
        dto.setDateOfBirth(req.getParameter("dateOfBirth"));
        return dto;
    }

    private UpdateUserDto updateDto(HttpServletRequest req) {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setId(req.getParameter("id"));
        dto.setName(req.getParameter("name"));
        dto.setEmail(req.getParameter("email"));
        dto.setRole(req.getParameter("role"));
        dto.setStatus(req.getParameter("status"));
        dto.setDateOfBirth(req.getParameter("dateOfBirth"));
        return dto;
    }

    private String action(HttpServletRequest req, String fallback) {
        String action = req.getParameter("action");
        return action == null || action.trim().isEmpty() ? fallback : action;
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
