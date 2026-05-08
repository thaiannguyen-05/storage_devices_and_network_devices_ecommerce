package common.middleware;

import common.annotation.Public;
import entity.UserEntity;
import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import module.core.auth.AuthPayload;
import module.core.auth.TokenService;
import module.core.user.UserService;

@WebFilter(urlPatterns = "/*", filterName = "02_AuthPayloadFilter")
public class AuthPayloadFilter implements Filter {

    private final TokenService tokenService = new TokenService();
    private final UserService userService = new UserService();
    private static final String UTF_8 = "UTF-8";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        request.setCharacterEncoding(UTF_8);
        response.setCharacterEncoding(UTF_8);
        if (isPublicRoute(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        String accessToken = resolveTokenValue(httpRequest, "accessToken");

        if (!accessToken.isBlank()) {
            AuthPayload authPayload = resolveAuthPayload(accessToken);
            if (authPayload != null) {
                httpRequest.setAttribute(AuthPayload.REQUEST_ATTRIBUTE, authPayload);
                httpRequest.setAttribute("authUserId", authPayload.getUserId());
                httpRequest.setAttribute("authUserEmail", authPayload.getEmail());
                httpRequest.setAttribute("authUserRole", authPayload.getRole());
                httpRequest.setAttribute("authSessionId", authPayload.getSessionId());
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicRoute(HttpServletRequest request) {
        String servletClassName = resolveServletClassName(request);
        if (servletClassName.isBlank()) {
            return false;
        }

        try {
            Class<?> servletClass = Class.forName(servletClassName);
            return servletClass.isAnnotationPresent(Public.class);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private String resolveServletClassName(HttpServletRequest request) {
        ServletContext servletContext = request.getServletContext();
        String requestPath = request.getServletPath();
        if (request.getPathInfo() != null) {
            requestPath += request.getPathInfo();
        }

        for (Map.Entry<String, ? extends ServletRegistration> entry : servletContext.getServletRegistrations().entrySet()) {
            ServletRegistration registration = entry.getValue();
            for (String mapping : registration.getMappings()) {
                if (matchesMapping(requestPath, mapping)) {
                    return value(registration.getClassName());
                }
            }
        }

        return "";
    }

    private boolean matchesMapping(String requestPath, String mapping) {
        String normalizedPath = value(requestPath);
        String normalizedMapping = value(mapping);

        if (normalizedPath.isBlank() || normalizedMapping.isBlank()) {
            return false;
        }

        if (normalizedMapping.equals(normalizedPath)) {
            return true;
        }

        if ("/".equals(normalizedMapping)) {
            return true;
        }

        if (normalizedMapping.startsWith("*.")) {
            String extension = normalizedMapping.substring(1);
            return normalizedPath.endsWith(extension);
        }

        if (normalizedMapping.endsWith("/*")) {
            String prefix = normalizedMapping.substring(0, normalizedMapping.length() - 2);
            return normalizedPath.equals(prefix) || normalizedPath.startsWith(prefix + "/");
        }

        return false;
    }

    private AuthPayload resolveAuthPayload(String accessToken) {
        try {
            Claims claims = tokenService.parseAccessToken(accessToken);
            String userId = value(claims.getSubject());
            String email = value(claims.get("email", String.class)).toLowerCase();
            String role = value(claims.get("role", String.class));
            String sessionId = value(claims.get("sessionId", String.class));

            if (userId.isBlank() || email.isBlank()) {
                return null;
            }

            UserEntity user = userService.getUserById(userId);
            if (user == null) {
                return null;
            }

            if (!email.equalsIgnoreCase(value(user.getEmail()))) {
                return null;
            }

            return new AuthPayload(user.getId(), user.getEmail(), user.getRole(), sessionId);
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveTokenValue(HttpServletRequest request, String name) {
        String fromHeader = value(request.getHeader("Authorization"));
        if (fromHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return value(fromHeader.substring(7));
        }

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

    private String value(String input) {
        return input == null ? "" : input.trim();
    }
}
