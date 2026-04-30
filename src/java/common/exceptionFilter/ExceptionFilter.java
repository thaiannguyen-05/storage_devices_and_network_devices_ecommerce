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

        try {
            chainFilter.doFilter(request, response);

        } catch (Throwable e) {

            int status = 500;
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            String rootMessage = root.getClass().getName() + ": " + (root.getMessage() != null ? root.getMessage() : "Internal server error");

            String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);

            ApiError error = new ApiError(
                    false,
                    status,
                    rootMessage,
                    new Date().toString(),
                    httpRequest.getRequestURI(),
                    httpRequest.getMethod(),
                    requestId
            );
            ObjectMapper mapper = new ObjectMapper();
            httpResponse.setStatus(status);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().print(
                    mapper.writeValueAsString(error)
            );

        }
    }

}
