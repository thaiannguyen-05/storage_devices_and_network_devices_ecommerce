package common.guard;

import common.type.Result;
import common.type.UserPayload;
import entity.UserEntity;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AuthGuard {
    private static final Jsonb JSONB = JsonbBuilder.create();
    public static final String SESSION_USER_KEY = "currentUser";

    public boolean check(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (getCurrentUser(request) != null || getCurrentUserId(request) != null) {
            return true;
        }
        if (shouldReturnJson(request)) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        } else {
            redirectToLogin(request, response);
        }
        return false;
    }

    public boolean checkRole(HttpServletRequest request, HttpServletResponse response, String requiredRole) throws IOException {
        if (!check(request, response)) {
            return false;
        }
        if (hasRole(request, requiredRole)) {
            return true;
        }

        sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        return false;
    }

    public boolean hasRole(HttpServletRequest request, String requiredRole) {
        if (requiredRole == null || requiredRole.trim().isEmpty()) {
            return true;
        }
        String actualRole = getCurrentRole(request);
        return actualRole != null && requiredRole.equalsIgnoreCase(actualRole);
    }

    public UserEntity getCurrentUser(HttpServletRequest request) {
        Object user = getAttribute(request, "user");
        if (user instanceof UserEntity) {
            return (UserEntity) user;
        }

        user = getAttribute(request, "currentUser");
        if (user instanceof UserEntity) {
            return (UserEntity) user;
        }

        user = getAttribute(request, "authUser");
        if (user instanceof UserEntity) {
            return (UserEntity) user;
        }

        return null;
    }

    public String getCurrentUserId(HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user != null) {
            return user.getId();
        }

        Object userId = getAttribute(request, "userId");
        if (userId == null) {
            userId = getAttribute(request, "currentUserId");
        }
        if (userId == null) {
            Object payload = getAttribute(request, SESSION_USER_KEY);
            if (payload instanceof UserPayload) {
                return ((UserPayload) payload).getUserId();
            }
        }
        return userId == null ? null : String.valueOf(userId);
    }

    public String getCurrentRole(HttpServletRequest request) {
        UserEntity user = getCurrentUser(request);
        if (user != null) {
            return user.getRole();
        }

        Object role = getAttribute(request, "role");
        if (role == null) {
            role = getAttribute(request, "currentRole");
        }
        if (role == null) {
            Object payload = getAttribute(request, SESSION_USER_KEY);
            if (payload instanceof UserPayload) {
                return ((UserPayload) payload).getRole();
            }
        }
        return role == null ? null : String.valueOf(role);
    }

    private Object getAttribute(HttpServletRequest request, String name) {
        Object value = request.getAttribute(name);
        if (value != null) {
            return value;
        }

        HttpSession session = request.getSession(false);
        return session == null ? null : session.getAttribute(name);
    }

    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONB.toJson(Result.fail(message)));
    }

    private boolean shouldReturnJson(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        if (requestedWith != null && "XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return true;
        }

        String accept = request.getHeader("Accept");
        if (accept != null && accept.toLowerCase().contains("application/json")) {
            return true;
        }

        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        String requestUri = request.getRequestURI();
        String query = request.getQueryString();
        String fullPath = query == null || query.trim().isEmpty() ? requestUri : requestUri + "?" + query;
        String encodedRedirect = URLEncoder.encode(fullPath, StandardCharsets.UTF_8.name());
        response.sendRedirect(request.getContextPath() + "/auth?action=login&redirect=" + encodedRedirect);
    }
}
