package common.middleware;


import jakarta.servlet.Filter;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@WebFilter(urlPatterns = "/*", filterName = "RequestIdFilter")
public class RequestIdFilter implements Filter {
    public static final String REQUEST_ID_KEY = "X-Request-Id";
    public static final String REQUEST_ID_ATTRIBUTE = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestId = httpRequest.getHeader(REQUEST_ID_KEY);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        httpRequest.setAttribute(REQUEST_ID_KEY, requestId);
        httpRequest.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        httpResponse.setHeader(REQUEST_ID_KEY, requestId);

        filterChain.doFilter(request, response);
    }
}