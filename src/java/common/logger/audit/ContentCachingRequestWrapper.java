package common.logger.audit;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ContentCachingRequestWrapper extends HttpServletRequestWrapper {
    private static final int MAX_CACHE_BYTES = 1024 * 1024;
    private final byte[] cachedBody;
    private final boolean cacheSkipped;
    private final String skippedBodyMessage;
    private Map<String, String[]> parameterMap;

    public ContentCachingRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        String contentType = value(request.getContentType()).toLowerCase();
        long contentLength = request.getContentLengthLong();
        if (contentType.startsWith("multipart/")) {
            this.cachedBody = new byte[0];
            this.cacheSkipped = true;
            this.skippedBodyMessage = "[multipart content skipped]";
            return;
        }

        if (contentLength > MAX_CACHE_BYTES) {
            this.cachedBody = new byte[0];
            this.cacheSkipped = true;
            this.skippedBodyMessage = "[truncated - size: " + contentLength + " bytes]";
            return;
        }

        this.cachedBody = request.getInputStream().readAllBytes();
        this.cacheSkipped = false;
        this.skippedBodyMessage = "";
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cacheSkipped) {
            return super.getInputStream();
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            @Override
            public int read() {
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException("Async read listener is not supported");
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        String encoding = getCharacterEncoding();
        if (encoding == null || encoding.isBlank()) {
            encoding = StandardCharsets.UTF_8.name();
        }
        return new BufferedReader(new InputStreamReader(getInputStream(), encoding));
    }

    @Override
    public String getParameter(String name) {
        String[] values = getParameterMap().get(name);
        return values == null || values.length == 0 ? null : values[0];
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = getParameterMap().get(name);
        return values == null ? null : values.clone();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (cacheSkipped) {
            return super.getParameterMap();
        }

        if (parameterMap == null) {
            parameterMap = buildParameterMap();
        }

        return Collections.unmodifiableMap(parameterMap);
    }

    public byte[] getCachedBody() {
        return cachedBody.clone();
    }

    public boolean isCacheSkipped() {
        return cacheSkipped;
    }

    public String getSkippedBodyMessage() {
        return skippedBodyMessage;
    }

    private Map<String, String[]> buildParameterMap() {
        Map<String, String[]> parameters = new LinkedHashMap<>();
        parameters.putAll(BodyExtractor.parseFormEncoded(getQueryString(), getCharacterEncoding()));

        String contentType = value(getContentType()).toLowerCase();
        if (contentType.contains("application/x-www-form-urlencoded")) {
            String body = new String(cachedBody, StandardCharsets.UTF_8);
            merge(parameters, BodyExtractor.parseFormEncoded(body, getCharacterEncoding()));
        }

        return parameters;
    }

    private void merge(Map<String, String[]> destination, Map<String, String[]> source) {
        for (Map.Entry<String, String[]> entry : source.entrySet()) {
            destination.merge(entry.getKey(), entry.getValue(), this::append);
        }
    }

    private String[] append(String[] current, String[] next) {
        String[] combined = new String[current.length + next.length];
        System.arraycopy(current, 0, combined, 0, current.length);
        System.arraycopy(next, 0, combined, current.length, next.length);
        return combined;
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }
}
