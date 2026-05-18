package common.controller;

import common.annotation.Public;
import common.annotation.RequiresRole;
import common.annotation.Route;
import common.annotation.User;
import common.guard.AuthGuard;
import common.type.Result;
import common.type.UserPayload;
import entity.UserEntity;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class BaseController extends HttpServlet {
    private static final Jsonb JSONB = JsonbBuilder.create();
    private static final String CSRF_TOKEN_KEY = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, Method> routes = new HashMap<String, Method>();
    private final AuthGuard authGuard = new AuthGuard();

    @Override
    public void init() throws ServletException {
        super.init();
        routes.clear();

        Class<?> currentClass = getClass();
        while (currentClass != null && BaseController.class.isAssignableFrom(currentClass)) {
            for (Method method : currentClass.getDeclaredMethods()) {
                Route route = method.getAnnotation(Route.class);
                if (route != null) {
                    method.setAccessible(true);
                    routes.put(routeKey(route.method(), route.value()), method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        ensureCsrfToken(request);

        UserPayload user = getUserFromSession(request);
        if (user != null) {
            updateCartCount(request, user.getUserId());
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("cartCount", 0);
            }
        }

        if (isUnsafeMethod(request) && !isValidCsrfToken(request)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return;
        }

        if (!isPublicController()) {
            RequiresRole requiresRole = getClass().getAnnotation(RequiresRole.class);
            boolean allowed = requiresRole == null
                    ? authGuard.check(request, response)
                    : authGuard.checkRole(request, response, requiresRole.value());
            if (!allowed) {
                return;
            }
        }

        Method handler = findHandler(request);
        if (handler == null) {
            super.service(request, response);
            return;
        }

        try {
            Object result = handler.invoke(this, resolveHandlerArgs(handler, request, response));
            handleResult(result, response);
        } catch (IllegalAccessException error) {
            throw new ServletException(error);
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause();
            if (cause instanceof ServletException) {
                throw (ServletException) cause;
            }
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new ServletException(cause);
        }
    }

    protected void sendJson(HttpServletResponse response, Object data) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONB.toJson(data));
    }

    protected void sendError(HttpServletResponse response, int status, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status);
        sendJson(response, Result.fail(message));
    }

    protected void sendUnauthorized(HttpServletResponse response) throws IOException {
        sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    protected void sendForbidden(HttpServletResponse response) throws IOException {
        sendError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    }

    protected void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException {
        request.getRequestDispatcher(path).forward(request, response);
    }

    protected void forwardToJsp(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException {
        forward(request, response, path);
    }

    protected void redirect(HttpServletResponse response, String location) throws IOException {
        response.sendRedirect(location);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String location) throws IOException {
        response.sendRedirect(request.getContextPath() + location);
    }

    protected void setError(HttpServletRequest request, String message) {
        request.setAttribute("error", message);
    }

    protected void setSuccess(HttpServletRequest request, String message) {
        request.setAttribute("success", message);
    }

    protected UserEntity getCurrentUser(HttpServletRequest request) {
        return authGuard.getCurrentUser(request);
    }

    protected UserPayload getUserFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object payload = session.getAttribute("currentUser");
        return payload instanceof UserPayload ? (UserPayload) payload : null;
    }

    private void updateCartCount(HttpServletRequest request, String userId) {
        try {
            java.util.List<Integer> count = module.core.sql.JdbcHelper.executeQuery(
                    "SELECT COUNT(i.id) AS total FROM ItemCart i JOIN OrderCart c ON i.cartId = c.id WHERE c.userId = ?",
                    rs -> rs.getInt("total"), userId);
            int cartCount = count.isEmpty() ? 0 : count.get(0);
            request.getSession().setAttribute("cartCount", cartCount);
        } catch (Exception ex) {
            request.getSession().setAttribute("cartCount", 0);
        }
    }

    protected String getCurrentUserId(HttpServletRequest request) {
        return authGuard.getCurrentUserId(request);
    }

    protected String getCsrfToken(HttpServletRequest request) {
        return String.valueOf(request.getSession().getAttribute(CSRF_TOKEN_KEY));
    }

    protected void rotateCsrfToken(HttpServletRequest request) {
        request.getSession().setAttribute(CSRF_TOKEN_KEY, generateCsrfToken());
    }

    private boolean isPublicController() {
        return getClass().isAnnotationPresent(Public.class);
    }

    private void ensureCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute(CSRF_TOKEN_KEY) == null) {
            session.setAttribute(CSRF_TOKEN_KEY, generateCsrfToken());
        }
    }

    private boolean isUnsafeMethod(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);
    }

    private boolean isValidCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object expected = session == null ? null : session.getAttribute(CSRF_TOKEN_KEY);
        String actual = request.getParameter(CSRF_TOKEN_KEY);
        return expected != null && actual != null && expected.equals(actual);
    }

    private String generateCsrfToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Method findHandler(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (path == null || path.trim().isEmpty()) {
            path = "/";
        }
        return routes.get(routeKey(request.getMethod(), path));
    }

    private Object[] resolveHandlerArgs(Method handler, HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Class<?>[] parameterTypes = handler.getParameterTypes();
        Annotation[][] annotations = handler.getParameterAnnotations();
        Object[] args = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            User userAnnotation = findUserAnnotation(annotations[i]);
            if (userAnnotation != null) {
                args[i] = resolveUserProperty(request, userAnnotation.value());
            } else if (HttpServletRequest.class.isAssignableFrom(parameterTypes[i])) {
                args[i] = request;
            } else if (HttpServletResponse.class.isAssignableFrom(parameterTypes[i])) {
                args[i] = response;
            } else if (HttpSession.class.isAssignableFrom(parameterTypes[i])) {
                args[i] = request.getSession();
            } else {
                throw new ServletException("Unsupported handler parameter type: " + parameterTypes[i].getName());
            }
        }

        return args;
    }

    private User findUserAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof User) {
                return (User) annotation;
            }
        }
        return null;
    }

    private Object resolveUserProperty(HttpServletRequest request, String property) throws ServletException {
        if (property == null || property.trim().isEmpty()) {
            throw new ServletException("@User property must not be empty");
        }

        UserPayload payload = getUserFromSession(request);
        if (payload != null) {
            return invokeGetter(payload, property);
        }

        UserEntity user = authGuard.getCurrentUser(request);
        if (user != null) {
            return invokeGetter(user, property);
        }

        if ("id".equals(property) || "userId".equals(property)) {
            return authGuard.getCurrentUserId(request);
        }
        throw new ServletException("No authenticated user found for @User(\"" + property + "\")");
    }

    private Object invokeGetter(Object source, String property) throws ServletException {
        try {
            String getterName = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
            return source.getClass().getMethod(getterName).invoke(source);
        } catch (NoSuchMethodException error) {
            throw new ServletException("No getter found for @User(\"" + property + "\")", error);
        } catch (IllegalAccessException error) {
            throw new ServletException(error);
        } catch (InvocationTargetException error) {
            throw new ServletException(error.getCause());
        }
    }

    private void handleResult(Object result, HttpServletResponse response) throws IOException {
        if (result == null || response.isCommitted()) {
            return;
        }
        sendJson(response, result);
    }

    private String routeKey(String httpMethod, String path) {
        return normalizeMethod(httpMethod) + " " + normalizePath(path);
    }

    private String normalizeMethod(String httpMethod) {
        return httpMethod == null ? "GET" : httpMethod.trim().toUpperCase(Locale.ENGLISH);
    }

    private String normalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "/";
        }
        String normalized = path.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
