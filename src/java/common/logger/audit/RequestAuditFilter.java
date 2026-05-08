package common.logger.audit;

import common.logger.LoggerService;
import common.middleware.RequestIdFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.WriteListener;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@WebFilter(urlPatterns = "/*", filterName = "03_RequestAuditFilter")
public class RequestAuditFilter implements Filter {
    private static final long SLOW_REQUEST_THRESHOLD_MS = 30_000L;
    private final LoggerService logger = LoggerService.getLogger(RequestAuditFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (shouldSkipAudit(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);
        long startedAt = System.currentTimeMillis();
        Throwable error = null;

        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } catch (Throwable throwable) {
            error = throwable;
            if (throwable instanceof IOException ioException) {
                throw ioException;
            }
            if (throwable instanceof ServletException servletException) {
                throw servletException;
            }
            if (throwable instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (throwable instanceof Error fatalError) {
                throw fatalError;
            }
            throw new ServletException(throwable);
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            responseWrapper.flushBuffer();
            AuditLogEntry entry = buildEntry(requestWrapper, responseWrapper, durationMs, error);
            writeAudit(entry);

            if (durationMs > SLOW_REQUEST_THRESHOLD_MS) {
                logger.warn(requestWrapper, "Slow request exceeded 30000ms: " + durationMs + "ms");
            }

            responseWrapper.copyBodyToResponse();
        }
    }

    public static boolean shouldSkipAudit(String uri) {
        if (uri == null || uri.isBlank()) {
            return false;
        }

        String path = uri.split("\\?", 2)[0].toLowerCase(Locale.ROOT);
        return path.startsWith("/assets/")
                || path.startsWith("/views/")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".ico")
                || path.endsWith(".woff")
                || path.endsWith(".woff2")
                || path.endsWith(".ttf")
                || path.endsWith(".svg");
    }

    private AuditLogEntry buildEntry(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            long durationMs,
            Throwable error
    ) {
        AuditLogEntry entry = new AuditLogEntry();
        int status = response.getStatus() == 0 ? HttpServletResponse.SC_OK : response.getStatus();
        entry.setTimestamp(Instant.now().toString());
        entry.setRequestId(resolveRequestId(request));
        entry.setMethod(request.getMethod());
        entry.setUri(request.getRequestURI());
        entry.setQueryString(request.getQueryString());
        entry.setContentType(request.getContentType());
        entry.setParameters(BodyExtractor.maskParameters(request.getParameterMap()));
        entry.setRequestBody(extractRequestBody(request));
        entry.setResponseStatus(status);
        entry.setResponseBody(BodyExtractor.extractResponseBody(
                response.getContentType(),
                response.getContentAsByteArray(),
                response.getCharacterEncoding()
        ));
        entry.setDurationMs(durationMs);
        entry.setClientIp(resolveClientIp(request));
        entry.setUserAgent(value(request.getHeader("User-Agent")));
        entry.setAuthUserEmail(attributeValue(request, "authUserEmail"));
        entry.setAuthUserRole(attributeValue(request, "authUserRole"));
        entry.setError(error != null || status >= 400);
        entry.setErrorMessage(error == null ? "" : value(error.getMessage()));
        return entry;
    }

    private String extractRequestBody(ContentCachingRequestWrapper request) {
        if (request.isCacheSkipped()) {
            return request.getSkippedBodyMessage();
        }

        return BodyExtractor.extractRequestBody(
                request.getMethod(),
                request.getContentType(),
                request.getCachedBody(),
                request.getCharacterEncoding()
        );
    }

    private void writeAudit(AuditLogEntry entry) {
        CompletableFuture.runAsync(() -> logger.audit(entry));
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = attributeValue(request, RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        if (!requestId.isBlank()) {
            return requestId;
        }

        requestId = value(request.getHeader(RequestIdFilter.REQUEST_ID_KEY));
        return requestId.isBlank() ? "N/A" : requestId;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = value(request.getHeader("X-Forwarded-For"));
        if (!forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        return value(request.getRemoteAddr());
    }

    private String attributeValue(HttpServletRequest request, String name) {
        Object value = request.getAttribute(name);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }

    private static class ContentCachingResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream capture = new ByteArrayOutputStream();
        private ServletOutputStream outputStream;
        private PrintWriter writer;
        private int status = SC_OK;

        ContentCachingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() {
            if (writer != null) {
                throw new IllegalStateException("getWriter() has already been called");
            }

            if (outputStream == null) {
                outputStream = new ServletOutputStream() {
                    @Override
                    public void write(int b) {
                        capture.write(b);
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setWriteListener(WriteListener writeListener) {
                        throw new UnsupportedOperationException("Async write listener is not supported");
                    }
                };
            }

            return outputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (outputStream != null) {
                throw new IllegalStateException("getOutputStream() has already been called");
            }

            if (writer == null) {
                String encoding = getCharacterEncoding();
                if (encoding == null || encoding.isBlank()) {
                    encoding = StandardCharsets.UTF_8.name();
                }
                writer = new PrintWriter(new OutputStreamWriter(capture, encoding));
            }

            return writer;
        }

        @Override
        public void flushBuffer() throws IOException {
            if (writer != null) {
                writer.flush();
            }
            if (outputStream != null) {
                outputStream.flush();
            }
        }

        @Override
        public void setStatus(int sc) {
            status = sc;
            super.setStatus(sc);
        }

        @Override
        public void sendError(int sc) throws IOException {
            status = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            status = sc;
            super.sendError(sc, msg);
        }

        @Override
        public int getStatus() {
            return status;
        }

        byte[] getContentAsByteArray() {
            return capture.toByteArray();
        }

        void copyBodyToResponse() throws IOException {
            flushBuffer();
            byte[] body = capture.toByteArray();
            if (body.length > 0 && !getResponse().isCommitted()) {
                getResponse().getOutputStream().write(body);
                getResponse().flushBuffer();
            }
        }
    }
}
