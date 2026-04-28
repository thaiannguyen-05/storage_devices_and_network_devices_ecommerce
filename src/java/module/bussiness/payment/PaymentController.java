/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package module.bussiness.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.controller.BaseController;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "payment", urlPatterns = {"/payment/*"})
public class PaymentController extends BaseController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final PaymentService paymentService = new PaymentService();

    @Override
    protected void registerRoutes() {
        registerGet("/", this::handleRoot);
        registerPost("/", this::handleRoot);
        registerPost("/sepay/webhook", this::handleSePayWebhook);
    }

    private void handleRoot(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet PaymentController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet PaymentController at" + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    private void handleSePayWebhook(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> payload;
        try {
            payload = OBJECT_MAPPER.readValue(
                    request.getInputStream(),
                    new TypeReference<Map<String, Object>>() {
                    }
            );
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            OBJECT_MAPPER.writeValue(response.getWriter(), Map.of("success", false, "message", "Invalid webhook payload"));
            return;
        }

        Map<String, Object> result = paymentService.handleSePayWebhook(payload);
        response.setStatus(HttpServletResponse.SC_OK);
        OBJECT_MAPPER.writeValue(response.getWriter(), result);
    }
}
