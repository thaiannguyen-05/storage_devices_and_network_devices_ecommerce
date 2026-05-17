package common.guard;

import common.annotation.Public;
import common.annotation.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import module.core.auth.AuthPayload;
import module.core.auth.TokenService;

@WebFilter(urlPatterns = "/*", filterName = "03_RoleGuard")
public class RoleGuard implements Filter {
    private final TokenService tokenService = new TokenService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        Class<?> servletClass = resolveServletClass(httpRequest);

        if (servletClass == null) {
            chain.doFilter(request, response);
            return;
        }

        Role requiredRole = resolveRoleAnnotation(
                servletClass,
                httpRequest.getMethod(),
                resolveRequestPath(httpRequest),
                resolveRequestParameters(httpRequest));
        if (requiredRole == null) {
            chain.doFilter(request, response);
            return;
        }

        Claims tokenPayload = resolveTokenPayload(httpRequest);
        if (tokenPayload == null) {
            rejectUnauthenticated(httpRequest, httpResponse);
            return;
        }

        if (!roleMatches(roleFromClaims(tokenPayload), requiredRole.value())) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        attachAuthPayload(httpRequest, tokenPayload);
        chain.doFilter(request, response);
    }

    static Role resolveRoleAnnotation(Class<?> servletClass, String httpMethod) {
        return resolveRoleAnnotation(servletClass, httpMethod, "", new LinkedHashMap<>());
    }

    static Role resolveRoleAnnotation(Class<?> servletClass, String httpMethod, String requestPath, Map<String, String> parameters) {
        Method handlerMethod = resolveHandlerMethod(servletClass, httpMethod);
        if (handlerMethod != null) {
            Role methodRole = selectRole(handlerMethod.getAnnotationsByType(Role.class), requestPath, parameters);
            if (methodRole != null) {
                return methodRole;
            }
        }
        if (handlerMethod != null && handlerMethod.isAnnotationPresent(Public.class)) {
            return null;
        }
        return selectRole(servletClass.getAnnotationsByType(Role.class), requestPath, parameters);
    }

    static boolean isPublicEndpoint(Class<?> servletClass, String httpMethod) {
        Method handlerMethod = resolveHandlerMethod(servletClass, httpMethod);
        if (handlerMethod != null && handlerMethod.isAnnotationPresent(Role.class)) {
            return false;
        }
        if (handlerMethod != null && handlerMethod.isAnnotationPresent(Public.class)) {
            return true;
        }
        if (servletClass.getAnnotationsByType(Role.class).length > 0) {
            return false;
        }
        return servletClass.isAnnotationPresent(Public.class);
    }

    static boolean roleMatches(String currentRole, String[] allowedRoles) {
        String normalizedCurrentRole = value(currentRole).toUpperCase(Locale.ROOT);
        if (normalizedCurrentRole.isBlank() || allowedRoles == null || allowedRoles.length == 0) {
            return false;
        }

        for (String allowedRole : allowedRoles) {
            if (normalizedCurrentRole.equals(value(allowedRole).toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    static String roleFromClaims(Claims claims) {
        if (claims == null) {
            return "";
        }
        return value(claims.get("role", String.class));
    }

    private static Role selectRole(Role[] roles, String requestPath, Map<String, String> parameters) {
        Role fallbackRole = null;
        if (roles == null) {
            return null;
        }

        for (Role role : roles) {
            if (hasConstraints(role)) {
                if (roleAppliesToRequest(role, requestPath, parameters)) {
                    return role;
                }
            } else if (fallbackRole == null) {
                fallbackRole = role;
            }
        }
        return fallbackRole;
    }

    private static boolean hasConstraints(Role role) {
        return role.paths().length > 0 || role.actions().length > 0;
    }

    private static boolean roleAppliesToRequest(Role role, String requestPath, Map<String, String> parameters) {
        if (role.paths().length > 0 && !matchesAnyPath(role.paths(), requestPath)) {
            return false;
        }

        if (role.actions().length == 0) {
            return true;
        }

        String actualAction = value(parameters.get(value(role.parameter()))).toLowerCase(Locale.ROOT);
        for (String expectedAction : role.actions()) {
            if (actualAction.equals(value(expectedAction).toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAnyPath(String[] paths, String requestPath) {
        for (String path : paths) {
            if (matchesPath(path, requestPath)) {
                return true;
            }
        }
        return false;
    }

    private Claims resolveTokenPayload(HttpServletRequest request) {
        String accessToken = resolveTokenValue(request, "accessToken");
        if (accessToken.isBlank()) {
            return null;
        }

        try {
            return tokenService.parseAccessToken(accessToken);
        } catch (Exception e) {
            return null;
        }
    }

    private void attachAuthPayload(HttpServletRequest request, Claims claims) {
        String userId = value(claims.getSubject());
        String email = value(claims.get("email", String.class)).toLowerCase(Locale.ROOT);
        String role = roleFromClaims(claims);
        String sessionId = value(claims.get("sessionId", String.class));

        AuthPayload authPayload = new AuthPayload(userId, email, role, sessionId);
        request.setAttribute(AuthPayload.REQUEST_ATTRIBUTE, authPayload);
        request.setAttribute("authUserId", authPayload.getUserId());
        request.setAttribute("authUserEmail", authPayload.getEmail());
        request.setAttribute("authUserRole", authPayload.getRole());
        request.setAttribute("authSessionId", authPayload.getSessionId());
    }

    private void rejectUnauthenticated(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (expectsJson(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String loginUrl = request.getContextPath() + "/auth?action=signin&next=" + encode(resolveCurrentUrl(request));
        response.sendRedirect(loginUrl);
    }

    private boolean expectsJson(HttpServletRequest request) {
        String requestedWith = value(request.getHeader("X-Requested-With"));
        String accept = value(request.getHeader("Accept")).toLowerCase(Locale.ROOT);
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith) || accept.contains("application/json");
    }

    private String resolveCurrentUrl(HttpServletRequest request) {
        String uri = value(request.getRequestURI());
        String query = value(request.getQueryString());
        if (query.isBlank()) {
            return uri;
        }
        return uri + "?" + query;
    }

    private String resolveRequestPath(HttpServletRequest request) {
        String requestPath = value(request.getServletPath());
        if (request.getPathInfo() != null) {
            requestPath += request.getPathInfo();
        }
        return requestPath;
    }

    private Map<String, String> resolveRequestParameters(HttpServletRequest request) {
        Map<String, String> parameters = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String[] values = entry.getValue();
            parameters.put(entry.getKey(), values == null || values.length == 0 ? "" : value(values[0]));
        }
        return parameters;
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

    private Class<?> resolveServletClass(HttpServletRequest request) {
        String servletClassName = resolveServletClassName(request);
        if (servletClassName.isBlank()) {
            return null;
        }

        try {
            Class<?> servletClass = Class.forName(servletClassName);
            if (HttpServlet.class.isAssignableFrom(servletClass)) {
                return servletClass;
            }
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private String resolveServletClassName(HttpServletRequest request) {
        ServletContext servletContext = request.getServletContext();
        String requestPath = value(request.getServletPath());
        if (request.getPathInfo() != null) {
            requestPath += request.getPathInfo();
        }

        Map<String, ? extends ServletRegistration> registrations = servletContext.getServletRegistrations();
        for (ServletRegistration registration : registrations.values()) {
            for (String mapping : registration.getMappings()) {
                if (matches(mapping, requestPath)) {
                    return value(registration.getClassName());
                }
            }
        }
        return "";
    }

    private boolean matches(String mapping, String requestPath) {
        return matchesPath(mapping, requestPath);
    }

    private static boolean matchesPath(String mapping, String requestPath) {
        String normalizedMapping = value(mapping);
        String normalizedPath = value(requestPath);

        if (normalizedMapping.equals(normalizedPath)) {
            return true;
        }

        if (normalizedMapping.endsWith("/*")) {
            String prefix = normalizedMapping.substring(0, normalizedMapping.length() - 2);
            return normalizedPath.equals(prefix) || normalizedPath.startsWith(prefix + "/");
        }

        return false;
    }

    private static Method resolveHandlerMethod(Class<?> servletClass, String httpMethod) {
        String methodName = handlerMethodName(httpMethod);
        if (methodName.isBlank()) {
            return null;
        }

        Class<?> currentClass = servletClass;
        while (currentClass != null && HttpServlet.class.isAssignableFrom(currentClass)) {
            try {
                return currentClass.getDeclaredMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
            } catch (NoSuchMethodException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    private static String handlerMethodName(String httpMethod) {
        switch (value(httpMethod).toUpperCase(Locale.ROOT)) {
            case "GET":
                return "doGet";
            case "POST":
                return "doPost";
            case "PUT":
                return "doPut";
            case "DELETE":
                return "doDelete";
            case "PATCH":
                return "doPatch";
            default:
                return "";
        }
    }

    private static String encode(String input) {
        return URLEncoder.encode(value(input), StandardCharsets.UTF_8);
    }

    private static String value(String input) {
        return input == null ? "" : input.trim();
    }
}
