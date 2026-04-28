package common.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class BaseController extends HttpServlet {

    @FunctionalInterface
    protected interface RouteHandler {
        void handle(HttpServletRequest request, HttpServletResponse response) throws Exception;
    }

    private final Map<String, RouteHandler> getRoutes = new HashMap<>();
    private final Map<String, RouteHandler> postRoutes = new HashMap<>();
    private final Map<String, RouteHandler> putRoutes = new HashMap<>();
    private final Map<String, RouteHandler> deleteRoutes = new HashMap<>();

    private boolean routesRegistered = false;

    protected abstract void registerRoutes();

    private synchronized void ensureRoutesRegistered() {
        if (!routesRegistered) {
            registerRoutes();
            routesRegistered = true;
        }
    }

    protected final void registerGet(String path, RouteHandler handler) {
        getRoutes.put(normalizePath(path), handler);
    }

    protected final void registerPost(String path, RouteHandler handler) {
        postRoutes.put(normalizePath(path), handler);
    }

    protected final void registerPut(String path, RouteHandler handler) {
        putRoutes.put(normalizePath(path), handler);
    }

    protected final void registerDelete(String path, RouteHandler handler) {
        deleteRoutes.put(normalizePath(path), handler);
    }

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        dispatch(request, response, getRoutes);
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        dispatch(request, response, postRoutes);
    }

    @Override
    protected final void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        dispatch(request, response, putRoutes);
    }

    @Override
    protected final void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        dispatch(request, response, deleteRoutes);
    }

    private void dispatch(HttpServletRequest request, HttpServletResponse response, Map<String, RouteHandler> routes)
            throws ServletException, IOException {
        ensureRoutesRegistered();
        String path = normalizePath(request.getPathInfo());
        RouteHandler handler = routes.get(path);

        if (handler == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            handler.handle(request, response);
        } catch (ServletException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "/";
        }

        String normalized = path.startsWith("/") ? path : "/" + path;
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}
