package common.exceptionFilter;

import common.middleware.RequestIdFilter;
import common.type.ApiError;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebFilter("/*")
public class ExceptionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chainFilter) throws IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestUri = httpRequest.getRequestURI();

        boolean isUiRoute = requestUri.contains("/views/")
                || requestUri.contains("/assets/")
                || requestUri.endsWith(".css")
                || requestUri.endsWith(".js")
                || requestUri.endsWith(".png")
                || requestUri.endsWith(".jpg")
                || requestUri.endsWith(".jpeg")
                || requestUri.endsWith(".svg")
                || requestUri.endsWith("/product")
                || requestUri.endsWith("/cart")
                || requestUri.endsWith("/payment")
                || requestUri.endsWith("/auth");
        if (!isUiRoute) {
            httpResponse.setContentType("application/json;charset=UTF-8");
        }

        try {
            chainFilter.doFilter(request, response);

        } catch (Exception e) {

            int status = 500;
            String message = (e.getMessage() != null) ? e.getMessage() : "Internal server error";

            String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);

            ApiError error = new ApiError(
                    false,
                    status,
                    message,
                    new Date().toString(),
                    httpRequest.getRequestURI(),
                    httpRequest.getMethod(),
                    requestId
            );
            ObjectMapper mapper = new ObjectMapper();
            httpResponse.setStatus(status);
            if (isUiRoute) {
                httpResponse.setContentType("text/plain;charset=UTF-8");
            }
            httpResponse.getWriter().print(
                    mapper.writeValueAsString(error)
            );

        }
    }

}
