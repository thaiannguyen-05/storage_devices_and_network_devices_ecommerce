package module.core.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import module.core.auth.dto.ForgotPasswordRequestDto;
import module.core.auth.dto.ProfileRequestDto;
import module.core.auth.dto.RefreshTokenRequestDto;
import module.core.auth.dto.ResetPasswordRequestDto;
import module.core.auth.dto.SigninRequestDto;
import module.core.auth.dto.SignupRequestDto;
import module.core.auth.dto.VerifyEmailCodeRequestDto;
import module.core.auth.response_dto.ForgotPasswordResponseDto;
import module.core.auth.response_dto.ProfileResponseDto;
import module.core.auth.response_dto.RefreshTokenResponseDto;
import module.core.auth.response_dto.ResetPasswordResponseDto;
import module.core.auth.response_dto.SigninResponseDto;
import module.core.auth.response_dto.SignupResponseDto;
import module.core.auth.response_dto.VerifyEmailCodeResponseDto;
import module.core.config.ConfigService;

@WebServlet(name = "auth", urlPatterns = {"/auth"})
public class AuthController extends HttpServlet {

    private final AuthService authService = new AuthService();
    private final TokenService tokenService = new TokenService();

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
                request.getRequestDispatcher("/views/auth/reset-password.jsp").forward(request, response);
                break;
            case "verifyEmail":
                request.getRequestDispatcher("/views/auth/verify-email.jsp").forward(request, response);
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

        if ("verifyEmail".equalsIgnoreCase(action)) {
            handleVerifyEmail(request, response);
            return;
        }

        if ("refresh".equalsIgnoreCase(action)) {
            handleRefreshToken(request, response);
            return;
        }

        doGet(request, response);
    }

    private void handleSignup(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SignupRequestDto dto = new SignupRequestDto();
        dto.setFullname(value(request.getParameter("fullname")));
        dto.setEmail(value(request.getParameter("email")));
        dto.setPassword(value(request.getParameter("password")));
        dto.setConfirmPassword(value(request.getParameter("confirm_password")));
        dto.setDateOfBirth(value(request.getParameter("dateOfBirth")));
        dto.setAcceptTerms(request.getParameter("acceptTerms") != null);

        request.setAttribute("fullname", dto.getFullname());
        request.setAttribute("email", value(dto.getEmail()).toLowerCase());
        request.setAttribute("dateOfBirth", dto.getDateOfBirth());
        if (dto.isAcceptTerms()) {
            request.setAttribute("acceptTerms", "1");
        }

        SignupResponseDto result = authService.signup(dto);
        if (!result.isSuccess()) {
            request.setAttribute("error", result.getErrorMessage());
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        request.setAttribute("success", "Registration succeeded. Please enter the verification code sent to your email.");
        request.setAttribute("email", result.getUserEmail());
        request.getRequestDispatcher("/views/auth/verify-email.jsp").forward(request, response);
    }

    private void handleSignin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SigninRequestDto dto = new SigninRequestDto();
        dto.setUsername(value(request.getParameter("username")));
        dto.setPassword(value(request.getParameter("password")));
        dto.setIpAddress(resolveClientIp(request));

        SigninResponseDto result = authService.signin(dto);
        if (!result.isSuccess()) {
            request.setAttribute("error", result.getErrorMessage());
            request.setAttribute("username", result.getUsername());
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
            return;
        }

        request.getSession(true).setAttribute("authUserName", result.getUserName());
        request.getSession().setAttribute("authUserEmail", result.getUserEmail());
        request.getSession().setAttribute("authUserRole", result.getUserRole());
        request.getSession().setAttribute("authSessionId", result.getSessionId());

        addAuthCookie(request, response, "accessToken", result.getAccessToken(), tokenService.getAccessTokenMaxAgeSeconds());
        addAuthCookie(request, response, "refreshToken", result.getRefreshToken(), tokenService.getRefreshTokenMaxAgeSeconds());
        addAuthCookie(request, response, "sessionId", result.getSessionId(), tokenService.getRefreshTokenMaxAgeSeconds());
        response.sendRedirect(request.getContextPath() + "/product");
    }

    private void handleRefreshToken(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RefreshTokenRequestDto dto = new RefreshTokenRequestDto();
        dto.setAccessToken(resolveTokenValue(request, "accessToken"));
        dto.setRefreshToken(resolveTokenValue(request, "refreshToken"));
        dto.setSessionId(resolveTokenValue(request, "sessionId"));

        RefreshTokenResponseDto result = authService.refreshToken(dto);
        if (!result.isSuccess()) {
            request.setAttribute("error", result.getErrorMessage());
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
            return;
        }

        request.getSession(true).setAttribute("authUserName", result.getUserName());
        request.getSession().setAttribute("authUserEmail", result.getUserEmail());
        request.getSession().setAttribute("authUserRole", result.getUserRole());
        request.getSession().setAttribute("authSessionId", result.getSessionId());

        addAuthCookie(request, response, "accessToken", result.getAccessToken(), tokenService.getAccessTokenMaxAgeSeconds());
        addAuthCookie(request, response, "refreshToken", result.getRefreshToken(), tokenService.getRefreshTokenMaxAgeSeconds());
        addAuthCookie(request, response, "sessionId", result.getSessionId(), tokenService.getRefreshTokenMaxAgeSeconds());
        response.sendRedirect(request.getContextPath() + "/product");
    }

    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ForgotPasswordRequestDto dto = new ForgotPasswordRequestDto();
        dto.setEmail(value(request.getParameter("email")));

        ForgotPasswordResponseDto result = authService.forgotPassword(dto);
        request.setAttribute("email", result.getEmail());

        if (!result.isSuccess()) {
            request.setAttribute("error", result.getErrorMessage());
            request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        request.setAttribute("success", result.getSuccessMessage());
        request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ResetPasswordRequestDto dto = new ResetPasswordRequestDto();
        dto.setEmail(value(request.getParameter("email")));
        dto.setCode(value(request.getParameter("code")));
        dto.setNewPassword(value(request.getParameter("newPassword")));
        dto.setConfirmNewPassword(value(request.getParameter("confirmNewPassword")));

        ResetPasswordResponseDto result = authService.resetPassword(dto);
        request.setAttribute("email", dto.getEmail());
        request.setAttribute("code", dto.getCode());

        if (!result.isSuccess()) {
            request.setAttribute("error", result.getErrorMessage());
            request.getRequestDispatcher("/views/auth/reset-password.jsp").forward(request, response);
            return;
        }

        request.setAttribute("success", "Your password has been reset. Please sign in.");
        request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
    }

    private void handleVerifyEmail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        VerifyEmailCodeRequestDto dto = new VerifyEmailCodeRequestDto();
        dto.setEmail(value(request.getParameter("email")));
        dto.setCode(value(request.getParameter("code")));

        request.setAttribute("email", dto.getEmail());

        VerifyEmailCodeResponseDto result = authService.verifyEmailCode(dto);
        if (!result.isSuccess()) {
            request.setAttribute("error", result.getErrorMessage());
            request.getRequestDispatcher("/views/auth/verify-email.jsp").forward(request, response);
            return;
        }

        request.setAttribute("success", result.getSuccessMessage());
        request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String[] headerNames = new String[]{"X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP"};
        for (String header : headerNames) {
            String raw = request.getHeader(header);
            if (raw != null && !raw.isBlank() && !"unknown".equalsIgnoreCase(raw)) {
                return raw.split(",")[0].trim();
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr == null ? "unknown" : remoteAddr;
    }

    private String resolveTokenValue(HttpServletRequest request, String name) {
        String fromParam = value(request.getParameter(name));
        if (!fromParam.isBlank()) {
            return fromParam;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "";
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return value(cookie.getValue());
            }
        }
        return "";
    }

    private void addAuthCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value == null ? "" : value);
        cookie.setHttpOnly(true);
        cookie.setSecure(shouldUseSecureCookies(request));
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }

    private boolean shouldUseSecureCookies(HttpServletRequest request) {
        String configured = value(ConfigService.getOrDefault("COOKIE_SECURE", ""));
        if (!configured.isBlank()) {
            return "true".equalsIgnoreCase(configured)
                    || "1".equals(configured)
                    || "yes".equalsIgnoreCase(configured);
        }

        if (request.isSecure()) {
            return true;
        }

        String forwardedProto = value(request.getHeader("X-Forwarded-Proto"));
        return "https".equalsIgnoreCase(forwardedProto);
    }

    @Override
    public String getServletInfo() {
        return "Auth controller";
    }
}
