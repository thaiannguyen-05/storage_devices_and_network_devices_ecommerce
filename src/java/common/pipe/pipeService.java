package common.pipe;


import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
@WebFilter("/*")
public class PipeService implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // wrap request
        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(httpRequest) {

            @Override
            public String getParameter(String name) {
                String value = super.getParameter(name);
                return value != null ? value.trim() : null;
            }

            @Override
            public String[] getParameterValues(String name) {
                String[] values = super.getParameterValues(name);
                if (values == null) return null;

                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null) {
                        values[i] = values[i].trim();
                    }
                }
                return values;
            }
        };

        chain.doFilter(wrappedRequest, response);
    }
}