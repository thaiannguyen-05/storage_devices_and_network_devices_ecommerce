package common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.type.ApiResponse;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;

@WebFilter("/*")
public class TransformFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestUri = httpRequest.getRequestURI();

        // Bypass UI routes so JSP renders HTML directly instead of wrapped JSON.
        if (requestUri.contains("/views/")
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
                || requestUri.endsWith("/auth")) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingResponseWrapper responseWrapper =
                new ContentCachingResponseWrapper(httpResponse);

        chain.doFilter(request, (ServletResponse) responseWrapper);

        String originalBody = responseWrapper.getContent();
        int statusCode = httpResponse.getStatus() == 0 ? 200 : httpResponse.getStatus();

        if (statusCode >= 400) {
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            if (originalBody != null && !originalBody.isBlank()) {
                httpResponse.getWriter().write(originalBody);
            }
            httpResponse.getWriter().flush();
            return;
        }

        if (originalBody == null || originalBody.trim().isEmpty()) {
            originalBody = "null";
        }

        Object data;
        try {
            data = objectMapper.readValue(originalBody, Object.class);
        } catch (Exception e) {
            data = originalBody;
        }

        ApiResponse<Object> apiResponse = new ApiResponse<>(
                true,
                statusCode,
                data,
                "Request successful",
                Instant.now().toString()
        );

        String jsonResponse = objectMapper.writeValueAsString(apiResponse);

        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.getWriter().write(jsonResponse);
        httpResponse.getWriter().flush();
    }

    public static class ContentCachingResponseWrapper extends HttpServletResponseWrapper {
        private final CharArrayWriter charArrayWriter = new CharArrayWriter();

        public ContentCachingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public PrintWriter getWriter() {
            return new PrintWriter(charArrayWriter);
        }

        public String getContent() {
            return charArrayWriter.toString();
        }
    }
}
