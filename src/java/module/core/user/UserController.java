package module.core.user;

import common.controller.BaseController;
import common.annotation.RequiresRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import module.core.config.AppConfig;
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
            forwardToJsp(req, res, "/views/admin/users/create.jsp");
            return;
        }
        if ("edit".equals(action)) {
            req.setAttribute("userResult", userService.getUserById(req.getParameter("id")));
            req.setAttribute("userOrders", userService.getRecentOrdersForUser(req.getParameter("id"), 8));
            forwardToJsp(req, res, "/views/admin/users/edit.jsp");
            return;
        }
        int page = parseInt(req.getParameter("page"), 1);
        String role = trimToNull(req.getParameter("role"));
        String status = trimToNull(req.getParameter("status"));
        String keyword = trimToNull(req.getParameter("keyword"));
        module.core.user.response_dto.ListUserResponseDto usersResult =
                userService.listUsers(page, AppConfig.PAGE_SIZE, role, status, keyword);
        req.setAttribute("usersResult", usersResult);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", Math.max(1, (int) Math.ceil(usersResult.getTotal() / (double) AppConfig.PAGE_SIZE)));
        req.setAttribute("selectedRole", role);
        req.setAttribute("selectedStatus", status);
        req.setAttribute("keyword", keyword == null ? "" : keyword);
        req.setAttribute("userStats", userService.getStats());
        forwardToJsp(req, res, "/views/admin/users/list.jsp");
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
        } else if ("update-role".equals(action)) {
            if ("ADMIN".equalsIgnoreCase(req.getParameter("role"))) {
                userService.promoteToAdmin(req.getParameter("id"));
            } else {
                userService.demoteToUser(req.getParameter("id"));
            }
        } else if ("update-status".equals(action)) {
            String status = req.getParameter("status");
            if ("BANNED".equalsIgnoreCase(status)) {
                userService.banUser(req.getParameter("id"));
            } else if ("ACTIVE".equalsIgnoreCase(status)) {
                userService.activateUser(req.getParameter("id"));
            } else {
                userService.changeStatus(req.getParameter("id"), status);
            }
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
