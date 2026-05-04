package module.core.auth;

import entity.UserEntity;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import entity.PasswordResetTokenEntity;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import module.core.config.ConfigService;
import module.core.user.dto.CreateUserDto;

@WebServlet(name = "auth", urlPatterns = {"/auth"})
public class AuthController extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "signin";
        }

        switch (action) {
            case "signup":
                request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
                break;
            case "forgotPassword":
                request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
                break;
            case "resetPassword":
                String token = value(request.getParameter("token"));
                request.setAttribute("token", token);
                if (token.isBlank()) {
                    request.setAttribute("error", "Link đặt lại mật khẩu không hợp lệ.");
                }
                request.getRequestDispatcher("/views/auth/reset-password.jsp").forward(request, response);
                break;
            case "profile":
                handleProfile(request, response);
                break;
            case "signin":
            default:
                request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "signin";
        }

        if ("signup".equalsIgnoreCase(action)) {
            handleSignup(request, response);
            return;
        }

        if ("signin".equalsIgnoreCase(action)) {
            handleSignin(request, response);
            return;
        }

        if ("forgotPassword".equalsIgnoreCase(action)) {
            handleForgotPassword(request, response);
            return;
        }

        if ("resetPassword".equalsIgnoreCase(action)) {
            handleResetPassword(request, response);
            return;
        }

        doGet(request, response);
    }

    private void handleSignup(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullname = value(request.getParameter("fullname"));
        String email = value(request.getParameter("email")).toLowerCase();
        String password = value(request.getParameter("password"));
        String confirmPassword = value(request.getParameter("confirm_password"));
        String dateOfBirthRaw = value(request.getParameter("dateOfBirth"));
        boolean acceptTerms = request.getParameter("acceptTerms") != null;

        request.setAttribute("fullname", fullname);
        request.setAttribute("email", email);
        request.setAttribute("dateOfBirth", dateOfBirthRaw);
        if (acceptTerms) {
            request.setAttribute("acceptTerms", "1");
        }

        if (fullname.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() || dateOfBirthRaw.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (!email.endsWith("@gmail.com")) {
            request.setAttribute("error", "Email phải có đuôi @gmail.com.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Xác nhận mật khẩu không khớp.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (!acceptTerms) {
            request.setAttribute("error", "Bạn cần đồng ý điều khoản trước khi đăng ký.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        LocalDate dateOfBirth;
        try {
            dateOfBirth = LocalDate.parse(dateOfBirthRaw);
        } catch (DateTimeParseException e) {
            request.setAttribute("error", "Ngày sinh không hợp lệ.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < 18) {
            request.setAttribute("error", "Bạn phải đủ 18 tuổi trở lên để đăng ký.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        List<UserEntity> users = authService.getUserRepository().findAll();
        for (UserEntity user : users) {
            if (email.equalsIgnoreCase(user.getEmail())) {
                request.setAttribute("error", "Email đã tồn tại.");
                request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
                return;
            }
        }

        CreateUserDto dto = new CreateUserDto();
        dto.setName(fullname);
        dto.setEmail(email);
        dto.setDateOfBirth(dateOfBirth);
        dto.setHashPassword(sha256(password));

        UserEntity createdUser = authService.getUserRepository().createUser(dto);
        request.getSession(true).setAttribute("authUserId", createdUser.getId());
        request.getSession().setAttribute("authUserName", createdUser.getName());
        request.getSession().setAttribute("authUserEmail", createdUser.getEmail());
        request.getSession().setAttribute("authUserRole", createdUser.getRole());
        response.sendRedirect(request.getContextPath() + "/product");
    }

    private void handleSignin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = value(request.getParameter("username")).toLowerCase();
        String password = value(request.getParameter("password"));

        if (username.isBlank() || password.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập email và mật khẩu.");
            request.setAttribute("username", username);
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
            return;
        }

        List<UserEntity> users = authService.getUserRepository().findAll();
        UserEntity matched = null;
        for (UserEntity user : users) {
            if (username.equalsIgnoreCase(user.getEmail())) {
                matched = user;
                break;
            }
        }

        if (matched == null) {
            request.setAttribute("error", "Tài khoản không tồn tại.");
            request.setAttribute("username", username);
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
            return;
        }

        if (!sha256(password).equals(matched.getHashPassword())) {
            request.setAttribute("error", "Mật khẩu không đúng.");
            request.setAttribute("username", username);
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
            return;
        }

        request.getSession(true).setAttribute("authUserId", matched.getId());
        request.getSession().setAttribute("authUserName", matched.getName());
        request.getSession().setAttribute("authUserEmail", matched.getEmail());
        request.getSession().setAttribute("authUserRole", matched.getRole());
        String next = value(request.getParameter("next"));
        if (!next.isBlank()) {
            response.sendRedirect(next);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/product");
    }

    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = value(request.getParameter("email")).toLowerCase();
        request.setAttribute("email", email);

        if (email.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập email.");
            request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        List<UserEntity> users = authService.getUserRepository().findAll();
        UserEntity matched = null;
        for (UserEntity user : users) {
            if (email.equalsIgnoreCase(user.getEmail())) {
                matched = user;
                break;
            }
        }

        if (matched != null) {
            try {
                authService.getPasswordResetTokenRepository().invalidateAllByUserId(matched.getId());
                String rawToken = UUID.randomUUID().toString() + UUID.randomUUID().toString().replace("-", "");
                String tokenHash = sha256(rawToken);
                authService.getPasswordResetTokenRepository().create(matched.getId(), tokenHash, 15);

                String fallbackBaseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
                String baseUrl = ConfigService.getOrDefault("APP_BASE_URL", fallbackBaseUrl);
                String resetLink = baseUrl + "/auth?action=resetPassword&token=" + rawToken;
                authService.getEmailService().sendPasswordResetEmail(matched.getEmail(), resetLink);
            } catch (Exception e) {
                String msg = e.getMessage() == null ? "Không thể gửi email lúc này." : e.getMessage();
                request.setAttribute("error", msg);
                request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
                return;
            }
        }

        request.setAttribute("success", "Nếu email tồn tại, hệ thống đã gửi link đặt lại mật khẩu (hiệu lực 15 phút).");
        request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = value(request.getParameter("token"));
        String newPassword = value(request.getParameter("newPassword"));
        String confirmNewPassword = value(request.getParameter("confirmNewPassword"));

        request.setAttribute("token", token);

        if (token.isBlank()) {
            request.setAttribute("error", "Link đặt lại mật khẩu không hợp lệ.");
            request.getRequestDispatcher("/views/auth/reset-password.jsp").forward(request, response);
            return;
        }

        if (newPassword.isBlank() || confirmNewPassword.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ mật khẩu mới.");
            request.getRequestDispatcher("/views/auth/reset-password.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            request.setAttribute("error", "Xác nhận mật khẩu mới không khớp.");
            request.getRequestDispatcher("/views/auth/reset-password.jsp").forward(request, response);
            return;
        }

        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) {
            request.setAttribute("error", "Mật khẩu mới chưa đúng định dạng bảo mật.");
            request.getRequestDispatcher("/views/auth/reset-password.jsp").forward(request, response);
            return;
        }

        String tokenHash = sha256(token);
        String tokenPreview = token.length() <= 8 ? token : token.substring(0, 8) + "...";
        System.out.println("[RESET_DEBUG] rawTokenPreview=" + tokenPreview + ", tokenLength=" + token.length());
        System.out.println("[RESET_DEBUG] tokenHash=" + tokenHash);

        PasswordResetTokenEntity validToken = authService.getPasswordResetTokenRepository().findValidByTokenHash(tokenHash);
        if (validToken == null) {
            System.out.println("[RESET_DEBUG] validToken=NULL (invalid/expired/used/not-found)");
            request.setAttribute("error", "Link không hợp lệ hoặc đã hết hạn.");
            request.getRequestDispatcher("/views/auth/reset-password.jsp").forward(request, response);
            return;
        }

        System.out.println("[RESET_DEBUG] validTokenFound id=" + validToken.getId()
                + ", userId=" + validToken.getUserId()
                + ", expiresAt=" + validToken.getExpiresAt()
                + ", usedAt=" + validToken.getUsedAt());

        boolean updated = authService.getUserRepository().updatePasswordById(validToken.getUserId(), sha256(newPassword));
        if (!updated) {
            request.setAttribute("error", "Không thể cập nhật mật khẩu. Vui lòng thử lại.");
            request.getRequestDispatcher("/views/auth/reset-password.jsp").forward(request, response);
            return;
        }

        authService.getPasswordResetTokenRepository().markUsed(validToken.getId());
        authService.getPasswordResetTokenRepository().invalidateAllByUserId(validToken.getUserId());

        UserEntity updatedUser = authService.getUserRepository().findById(validToken.getUserId());
        if (updatedUser != null) {
            request.getSession(true).setAttribute("authUserId", updatedUser.getId());
            request.getSession().setAttribute("authUserName", updatedUser.getName());
            request.getSession().setAttribute("authUserEmail", updatedUser.getEmail());
            request.getSession().setAttribute("authUserRole", updatedUser.getRole());
        }
        response.sendRedirect(request.getContextPath() + "/product");
    }

    private void handleProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object authEmail = request.getSession(false) == null ? null : request.getSession(false).getAttribute("authUserEmail");
        if (authEmail == null) {
            request.setAttribute("error", "Không tìm thấy thông tin tài khoản. Vui lòng đăng nhập lại.");
            request.getRequestDispatcher("/views/auth/profile.jsp").forward(request, response);
            return;
        }

        List<UserEntity> users = authService.getUserRepository().findAll();
        UserEntity matched = null;
        for (UserEntity user : users) {
            if (authEmail.toString().equalsIgnoreCase(user.getEmail())) {
                matched = user;
                break;
            }
        }

        if (matched == null) {
            request.setAttribute("error", "Không tìm thấy thông tin tài khoản. Vui lòng đăng nhập lại.");
        } else {
            request.setAttribute("profileUser", matched);
        }
        request.getRequestDispatcher("/views/auth/profile.jsp").forward(request, response);
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) {
                    hex.append('0');
                }
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot hash password", e);
        }
    }

    @Override
    public String getServletInfo() {
        return "Auth controller";
    }
}
