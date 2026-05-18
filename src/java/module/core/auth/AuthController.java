package module.core.auth;

import common.annotation.Public;
import common.controller.BaseController;
import common.guard.AuthGuard;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import module.core.common.BaseResponse;
import module.core.auth.dto.LoginRequestDto;
import module.core.auth.dto.RegisterRequestDto;
import module.core.auth.response_dto.LoginResponseDto;
import module.core.auth.response_dto.RegisterResponseDto;
import module.core.auth.response_dto.VerifyEmailResponseDto;

@Public
@WebServlet(name = "Auth", urlPatterns = {"/auth"})
public class AuthController extends BaseController {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = action(req, "login");
        if ("logout".equals(action)) {
            processLogout(req);
            redirect(req, res, "/auth?action=login");
            return;
        }
        if ("verify-email".equals(action)) {
            VerifyEmailResponseDto verify = authService.verifyEmail(req.getParameter("code"), req.getParameter("userId"));
            if (verify.isSuccess()) {
                redirect(req, res, "/auth?action=login&verified=1");
            } else {
                req.setAttribute("error", verify.getErrorMessage());
                forwardToJsp(req, res, "/pages/verify-email.jsp");
            }
            return;
        }
        if ("reset-password".equals(action)) {
            String email = req.getParameter("email");
            if (!hasText(email)) {
                Object sessionEmail = req.getSession().getAttribute("resetPasswordEmail");
                email = sessionEmail == null ? null : sessionEmail.toString();
            }
            req.setAttribute("email", email);
        }
        forwardToJsp(req, res, "/pages/" + action + ".jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = action(req, "login");
        switch (action) {
            case "verify-email":
                VerifyEmailResponseDto verify = authService.verifyEmail(req.getParameter("code"), req.getParameter("userId"));
                if (verify.isSuccess()) {
                    redirect(req, res, "/auth?action=login&verified=1");
                } else {
                    req.setAttribute("error", verify.getErrorMessage());
                    forwardToJsp(req, res, "/pages/verify-email.jsp");
                }
                break;
            case "forgot-password":
                String email = req.getParameter("email");
                if (!hasText(email)) {
                    req.setAttribute("error", "Email is required");
                    forwardToJsp(req, res, "/pages/forgot-password.jsp");
                    break;
                }
                BaseResponse forgot = authService.requestPasswordReset(email);
                if (forgot.isSuccess()) {
                    req.getSession().setAttribute("resetPasswordEmail", email.trim());
                    redirect(req, res, "/auth?action=reset-password&sent=1");
                } else {
                    req.setAttribute("error", forgot.getErrorMessage());
                    forwardToJsp(req, res, "/pages/forgot-password.jsp");
                }
                break;
            case "reset-password":
                String resetEmail = req.getParameter("email");
                if (!hasText(resetEmail)) {
                    Object sessionEmail = req.getSession().getAttribute("resetPasswordEmail");
                    resetEmail = sessionEmail == null ? null : sessionEmail.toString();
                }
                BaseResponse reset = authService.resetPassword(resetEmail, req.getParameter("code"),
                        req.getParameter("newPassword"), req.getParameter("confirmPassword"));
                if (reset.isSuccess()) {
                    req.getSession().removeAttribute("resetPasswordEmail");
                    redirect(req, res, "/auth?action=login&reset=1");
                } else {
                    req.setAttribute("error", reset.getErrorMessage());
                    req.setAttribute("email", resetEmail);
                    forwardToJsp(req, res, "/pages/reset-password.jsp");
                }
                break;
            case "register":
                RegisterRequestDto registerDto = new RegisterRequestDto();
                registerDto.setName(req.getParameter("name"));
                registerDto.setEmail(req.getParameter("email"));
                registerDto.setDateOfBirth(req.getParameter("dateOfBirth"));
                registerDto.setPassword(req.getParameter("password"));
                registerDto.setConfirmPassword(req.getParameter("confirmPassword"));
                RegisterResponseDto register = authService.register(registerDto);
                if (register.isSuccess()) {
                    redirect(req, res, "/auth?action=login&registered=1");
                } else {
                    req.setAttribute("error", register.getErrorMessage());
                    forwardToJsp(req, res, "/pages/register.jsp");
                }
                break;
            case "logout":
                processLogout(req);
                redirect(req, res, "/auth?action=login");
                break;
            case "login":
            default:
                LoginResponseDto login = authService.login(new LoginRequestDto(req.getParameter("email"), req.getParameter("password")),
                        req.getRemoteAddr());
                if (login.isSuccess()) {
                    HttpSession oldSession = req.getSession(false);
                    if (oldSession != null) {
                        oldSession.invalidate();
                    }
                    HttpSession newSession = req.getSession(true);
                    newSession.setAttribute(AuthGuard.SESSION_USER_KEY, login.getUser());
                    newSession.setAttribute("sessionId", login.getSessionId());
                    rotateCsrfToken(req);
                    String target = resolvePostLoginRedirect(req.getParameter("redirect"), req.getContextPath());
                    if (target != null) {
                        res.sendRedirect(target);
                    } else {
                        redirect(req, res, "/home");
                    }
                } else {
                    req.setAttribute("error", login.getErrorMessage());
                    forwardToJsp(req, res, "/pages/login.jsp");
                }
                break;
        }
    }

    private String action(HttpServletRequest req, String defaultAction) {
        String action = req.getParameter("action");
        return action == null || action.trim().isEmpty() ? defaultAction : action;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void processLogout(HttpServletRequest req) {
        HttpSession logoutSession = req.getSession(false);
        if (logoutSession != null && logoutSession.getAttribute(AuthGuard.SESSION_USER_KEY) != null) {
            authService.logout((String) logoutSession.getAttribute("sessionId"));
            logoutSession.invalidate();
        }
    }

    private String resolvePostLoginRedirect(String redirectParam, String contextPath) {
        if (!hasText(redirectParam)) {
            return null;
        }
        String decoded = URLDecoder.decode(redirectParam, StandardCharsets.UTF_8);
        if (!decoded.startsWith(contextPath + "/")) {
            return null;
        }
        if (decoded.startsWith(contextPath + "/auth")) {
            return null;
        }
        return decoded;
    }
}
