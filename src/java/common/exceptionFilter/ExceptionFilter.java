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

        httpResponse.setContentType("application/json;charset=UTF-8");

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
            httpResponse.getWriter().print(
                    mapper.writeValueAsString(error)
            );

        }
    }

}
