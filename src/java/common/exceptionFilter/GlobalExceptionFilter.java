package common.exceptionFilter;

import common.logger.AppLogger;
import common.type.Result;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class GlobalExceptionFilter implements Filter {
    private static final AppLogger LOGGER = AppLogger.of(GlobalExceptionFilter.class);
    private static final Jsonb JSONB = JsonbBuilder.create();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception error) {
            LOGGER.error("Unhandled request error", error);
            System.err.println("!!! GlobalExceptionFilter caught: " + error.getClass().getName());
            System.err.println("!!! Message: " + error.getMessage());
            if (error.getCause() != null) {
                System.err.println("!!! Cause: " + error.getCause().getClass().getName() + ": " + error.getCause().getMessage());
            }
            error.printStackTrace(System.err);
            if (response instanceof HttpServletResponse && !response.isCommitted()) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write(JSONB.toJson(Result.fail("Internal server error")));
                return;
            }
            if (error instanceof ServletException) {
                throw (ServletException) error;
            }
            if (error instanceof IOException) {
                throw (IOException) error;
            }
            throw new ServletException(error);
        }
    }
}
