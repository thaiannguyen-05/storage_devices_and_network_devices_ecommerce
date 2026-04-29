package module.core.auth;

import entity.PasswordResetTokenEntity;
import entity.UserEntity;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;
import module.core.config.ConfigService;
import module.core.user.dto.CreateUserDto;

@WebServlet(name = "auth", urlPatterns = {"/auth"})
public class AuthController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    private static final Pattern GMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@gmail\\.com$");
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("signup".equalsIgnoreCase(action)) {
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        if ("profile".equalsIgnoreCase(action)) {
            renderProfile(request, response);
            return;
        }

        if ("forgotPassword".equalsIgnoreCase(action)) {
            request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        if ("resetPassword".equalsIgnoreCase(action)) {
            handleResetPasswordPage(request, response);
            return;
        }

        request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("signup".equalsIgnoreCase(action)) {
            handleSignup(request, response);
            return;
        }

        if ("signin".equalsIgnoreCase(action)) {
            handleSignin(request, response);
            return;
        }

        if ("updateProfile".equalsIgnoreCase(action)) {
            handleUpdateProfile(request, response);
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

        if ("logout".equalsIgnoreCase(action)) {
            handleLogout(request, response);
            return;
        }

        if ("testMail".equalsIgnoreCase(action)) {
            handleTestMail(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/auth?action=signin");
    }

    private void handleSignup(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String fullName = trim(request.getParameter("fullname"));
        String email = trim(request.getParameter("email")).toLowerCase();
        String password = trim(request.getParameter("password"));
        String confirmPassword = trim(request.getParameter("confirm_password"));
        String acceptTerms = trim(request.getParameter("acceptTerms"));

        request.setAttribute("fullname", fullName);
        request.setAttribute("email", email);
        if (!acceptTerms.isEmpty()) {
            request.setAttribute("acceptTerms", "1");
        }

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (!"1".equals(acceptTerms)) {
            request.setAttribute("error", "Bạn cần đồng ý điều khoản và quy định trước khi đăng ký.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (!GMAIL_PATTERN.matcher(email).matches()) {
            request.setAttribute("error", "Email phải có định dạng {tên_bạn}@gmail.com.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            request.setAttribute("error", "Mật khẩu phải tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu xác nhận không khớp.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        try {
            UserEntity existing = authService.getUserRepository().findByEmail(email);
            if (existing != null) {
                request.setAttribute("error", "Email đã tồn tại, vui lòng dùng email khác.");
                request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
                return;
            }

            CreateUserDto dto = new CreateUserDto();
            dto.setName(fullName);
            dto.setEmail(email);
            dto.setDateOfBirth(LocalDate.now());
            dto.setHashPassword(sha256(password));

            authService.getUserRepository().createUser(dto);

            request.setAttribute("success", "Đăng ký thành công. Bạn có thể đăng nhập ngay bây giờ.");
            request.setAttribute("fullname", "");
            request.setAttribute("email", "");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Không thể đăng ký lúc này. Vui lòng kiểm tra cấu trúc bảng User hoặc kết nối DB.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
        }
    }

    private void handleSignin(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String email = trim(request.getParameter("username")).toLowerCase();
        String password = trim(request.getParameter("password"));

        request.setAttribute("username", email);

        if (email.isEmpty() || password.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập email và mật khẩu.");
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
            return;
        }

        try {
            UserEntity user = authService.getUserRepository().findByEmail(email);
            if (user == null) {
                request.setAttribute("error", "Tài khoản không tồn tại.");
                request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
                return;
            }

            String incomingHash = sha256(password);
            if (!incomingHash.equals(user.getHashPassword())) {
                request.setAttribute("error", "Mật khẩu không đúng.");
                request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
                return;
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("authUserId", user.getId());
            session.setAttribute("authUserEmail", user.getEmail());
            session.setAttribute("authUserName", user.getName());
            response.sendRedirect(request.getContextPath() + "/product");
        } catch (Exception e) {
            request.setAttribute("error", "Không thể đăng nhập lúc này. Vui lòng kiểm tra kết nối DB.");
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
        }
    }

    private void renderProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("authUserEmail") == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=signin");
            return;
        }

        String email = String.valueOf(session.getAttribute("authUserEmail"));
        UserEntity user = null;
        try {
            user = authService.getUserRepository().findByEmail(email);
        } catch (Exception e) {
            request.setAttribute("error", "Không thể tải thông tin tài khoản.");
        }

        if (request.getAttribute("profileName") == null && user != null) {
            request.setAttribute("profileName", user.getName() == null ? "" : user.getName());
        }
        request.setAttribute("profileUser", user);
        request.getRequestDispatcher("/views/auth/profile.jsp").forward(request, response);
    }

    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String email = trim(request.getParameter("email")).toLowerCase();
        request.setAttribute("email", email);

        if (email.isEmpty() || !email.contains("@")) {
            request.setAttribute("error", "Vui lòng nhập email hợp lệ.");
            request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        try {
            UserEntity user = authService.getUserRepository().findByEmail(email);
            if (user != null) {
                String rawToken = generateRawToken();
                String tokenHash = sha256(rawToken);
                int expiryMinutes = ConfigService.getInt("RESET_TOKEN_EXPIRE_MINUTES", 30);
                authService.getPasswordResetTokenRepository().createToken(email, tokenHash, expiryMinutes);

                String baseUrl = ConfigService.getOrDefault("APP_BASE_URL", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath());
                String resetLink = baseUrl + "/auth?action=resetPassword&token=" + rawToken;
                authService.getMailService().sendResetPasswordMail(email, resetLink);
                request.setAttribute("success", "Đã gửi link đặt lại mật khẩu. Vui lòng kiểm tra email (và thư rác). ");
            } else {
                request.setAttribute("success", "Nếu email tồn tại, chúng tôi đã gửi link đặt lại mật khẩu.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Forgot password send mail failed for email=" + email, e);
            String detail = e.getMessage();
            Throwable cause = e.getCause();
            if (cause != null && cause.getMessage() != null && !cause.getMessage().isEmpty()) {
                detail = cause.getMessage();
            }
            if (detail == null || detail.isEmpty()) {
                detail = "Unknown SMTP error";
            }
            request.setAttribute("error", "Gửi mail reset thất bại: " + detail);
        }

        request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
    }

    private void handleResetPasswordPage(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String token = trim(request.getParameter("token"));
        if (token.isEmpty()) {
            request.setAttribute("error", "Link reset không hợp lệ hoặc đã hết hạn.");
            request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        PasswordResetTokenEntity resetToken = authService.getPasswordResetTokenRepository().findValidByTokenHash(sha256(token));
        if (resetToken == null) {
            request.setAttribute("error", "Link reset không hợp lệ hoặc đã hết hạn.");
            request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        request.setAttribute("token", token);
        request.getRequestDispatcher("/views/auth/reset-password.jsp").forward(request, response);
    }

    private void handleTestMail(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String email = trim(request.getParameter("email")).toLowerCase();
        request.setAttribute("email", email);
        if (email.isEmpty() || !email.contains("@")) {
            request.setAttribute("error", "Vui lòng nhập email hợp lệ để test gửi mail.");
            request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        try {
            String baseUrl = ConfigService.getOrDefault("APP_BASE_URL", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath());
            String testLink = baseUrl + "/auth?action=signin";
            authService.getMailService().sendResetPasswordMail(email, testLink);
            request.setAttribute("success", "Đã gửi mail test thành công tới: " + email);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Test mail failed for email=" + email, e);
            String detail = e.getMessage();
            Throwable cause = e.getCause();
            if (cause != null && cause.getMessage() != null && !cause.getMessage().isEmpty()) {
                detail = cause.getMessage();
            }
            if (detail == null || detail.isEmpty()) {
                detail = "Unknown SMTP error";
            }
            request.setAttribute("error", "Gửi mail test thất bại: " + detail);
        }
        request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/auth?action=signin");
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String token = trim(request.getParameter("token"));
        String newPassword = trim(request.getParameter("newPassword"));
        String confirmNewPassword = trim(request.getParameter("confirmNewPassword"));

        request.setAttribute("token", token);

        if (token.isEmpty()) {
            request.setAttribute("error", "Link reset không hợp lệ hoặc đã hết hạn.");
            request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        if (newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ mật khẩu mới và xác nhận.");
            request.getRequestDispatcher("/views/auth/reset-password.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            request.setAttribute("error", "Mật khẩu mới và xác nhận mật khẩu mới không khớp.");
            request.getRequestDispatcher("/views/auth/reset-password.jsp").forward(request, response);
            return;
        }

        try {
            PasswordResetTokenEntity resetToken = authService.getPasswordResetTokenRepository().findValidByTokenHash(sha256(token));
            if (resetToken == null) {
                request.setAttribute("error", "Link reset không hợp lệ hoặc đã hết hạn.");
                request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
                return;
            }

            authService.getUserRepository().updatePasswordByEmail(resetToken.getEmail(), sha256(newPassword));
            authService.getPasswordResetTokenRepository().markUsed(resetToken.getId());

            UserEntity user = authService.getUserRepository().findByEmail(resetToken.getEmail());
            if (user != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("authUserId", user.getId());
                session.setAttribute("authUserEmail", user.getEmail());
                session.setAttribute("authUserName", user.getName());
                response.sendRedirect(request.getContextPath() + "/product");
                return;
            }

            request.setAttribute("success", "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập ngay bây giờ.");
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Không thể đặt lại mật khẩu lúc này. Vui lòng thử lại.");
            request.getRequestDispatcher("/views/auth/reset-password.jsp").forward(request, response);
        }
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("authUserEmail") == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=signin");
            return;
        }

        String email = String.valueOf(session.getAttribute("authUserEmail"));
        String fullName = trim(request.getParameter("fullname"));
        String currentPassword = trim(request.getParameter("currentPassword"));
        String newPassword = trim(request.getParameter("newPassword"));
        String confirmNewPassword = trim(request.getParameter("confirmNewPassword"));

        request.setAttribute("profileName", fullName);

        if (fullName.isEmpty()) {
            request.setAttribute("error", "Họ tên không được để trống.");
            renderProfile(request, response);
            return;
        }

        boolean wantsChangePassword = !currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmNewPassword.isEmpty();

        try {
            UserEntity user = authService.getUserRepository().findByEmail(email);
            if (user == null) {
                request.setAttribute("error", "Không tìm thấy tài khoản.");
                renderProfile(request, response);
                return;
            }

            authService.getUserRepository().updateProfileByEmail(email, fullName);
            session.setAttribute("authUserName", fullName);

            if (wantsChangePassword) {
                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                    request.setAttribute("error", "Vui lòng nhập đầy đủ mật khẩu hiện tại, mật khẩu mới và xác nhận mật khẩu mới.");
                    renderProfile(request, response);
                    return;
                }

                String currentHash = sha256(currentPassword);
                if (!currentHash.equals(user.getHashPassword())) {
                    request.setAttribute("error", "Mật khẩu hiện tại không đúng.");
                    renderProfile(request, response);
                    return;
                }

                if (!newPassword.equals(confirmNewPassword)) {
                    request.setAttribute("error", "Mật khẩu mới và xác nhận mật khẩu mới không khớp.");
                    renderProfile(request, response);
                    return;
                }

                authService.getUserRepository().updatePasswordByEmail(email, sha256(newPassword));
                request.setAttribute("success", "Cập nhật thông tin và đổi mật khẩu thành công.");
                renderProfile(request, response);
                return;
            }

            request.setAttribute("success", "Cập nhật thông tin thành công.");
            renderProfile(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Không thể cập nhật tài khoản lúc này. Vui lòng thử lại.");
            renderProfile(request, response);
        }
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
