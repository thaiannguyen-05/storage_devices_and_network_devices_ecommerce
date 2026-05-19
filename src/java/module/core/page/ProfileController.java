package module.core.page;

import common.controller.BaseController;
import common.type.UserPayload;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import module.core.common.BaseResponse;
import module.core.user.UserService;
import module.core.user.dto.UpdateUserDto;

@WebServlet(name = "Profile", urlPatterns = {"/profile"})
public class ProfileController extends BaseController {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        UserPayload currentUser = getUserFromSession(req);
        if (currentUser == null) {
            redirect(req, res, "/auth?action=login");
            return;
        }
        req.setAttribute("profileUser", userService.getUserById(currentUser.getUserId()).getUser());
        forwardToJsp(req, res, "/pages/profile.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        UserPayload currentUser = getUserFromSession(req);
        if (currentUser == null) {
            redirect(req, res, "/auth?action=login");
            return;
        }
        String action = req.getParameter("action");
        BaseResponse result;
        if ("change-password".equals(action)) {
            result = userService.changePassword(currentUser.getUserId(), req.getParameter("currentPassword"),
                    req.getParameter("newPassword"), req.getParameter("confirmPassword"));
        } else {
            entity.UserEntity existingUser = userService.getUserById(currentUser.getUserId()).getUser();
            UpdateUserDto dto = new UpdateUserDto();
            dto.setId(currentUser.getUserId());
            dto.setName(req.getParameter("displayName"));
            dto.setEmail(req.getParameter("email"));
            dto.setDateOfBirth(req.getParameter("dateOfBirth"));
            dto.setRole(existingUser == null ? currentUser.getRole() : existingUser.getRole());
            dto.setStatus(existingUser == null ? "ACTIVE" : existingUser.getStatus());
            result = userService.updateUser(dto);
            if (result.isSuccess()) {
                currentUser.setName(req.getParameter("displayName"));
                currentUser.setEmail(req.getParameter("email"));
                req.getSession().setAttribute("currentUser", currentUser);
            }
        }
        if (result.isSuccess()) {
            req.setAttribute("success", result.getSuccessMessage());
        } else {
            req.setAttribute("error", result.getErrorMessage());
        }
        req.setAttribute("profileUser", userService.getUserById(currentUser.getUserId()).getUser());
        forwardToJsp(req, res, "/pages/profile.jsp");
    }
}
