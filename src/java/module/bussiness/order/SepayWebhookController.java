package module.bussiness.order;

import common.controller.BaseController;
import entity.OrderEntity;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import module.bussiness.order.dto.SepayWebhookPayload;
import module.core.config.AppConfig;
import module.core.sql.JdbcHelper;

import common.annotation.Public;

@Public
@WebServlet(name = "SepayWebhook", urlPatterns = {"/api/sepay/webhook"})
public class SepayWebhookController extends BaseController {
    private static final Jsonb JSONB = JsonbBuilder.create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        res.getWriter().write("Webhook expects POST requests.");
    }

    private void debugLog(String message) {
        System.out.println(message);
        try {
            java.nio.file.Files.write(
                java.nio.file.Paths.get("c:\\Users\\An\\Documents\\NetBeansProjects\\WebApplication3\\webhook_debug.log"),
                (message + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND
            );
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        debugLog("\n=======================================================");
        debugLog("[SepayWebhook] Request received at " + LocalDateTime.now());
        
        // Log all request headers
        java.util.Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            debugLog("Header [" + name + "]: " + req.getHeader(name));
        }

        // 1. Verify Authorization header
        String authHeader = req.getHeader("Authorization");
        debugLog("[SepayWebhook] Authorization header: " + authHeader + " (Expected Token: " + AppConfig.SEPAY_WEBHOOK_TOKEN + ")");
        if (authHeader != null && authHeader.startsWith("Apikey ")) {
            authHeader = authHeader.substring(7).trim();
        }
        if (AppConfig.SEPAY_WEBHOOK_TOKEN != null && !AppConfig.SEPAY_WEBHOOK_TOKEN.trim().isEmpty()) {
            if (authHeader == null || !AppConfig.SEPAY_WEBHOOK_TOKEN.equals(authHeader)) {
                debugLog("[SepayWebhook] Authorization FAILED!");
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"success\": false, \"message\": \"Unauthorized\"}");
                return;
            }
        }
        debugLog("[SepayWebhook] Authorization SUCCESS!");

        // 2. Read Body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String body = sb.toString();
        debugLog("[SepayWebhook] Raw body: " + body);

        try {
            // 3. Parse JSON payload
            SepayWebhookPayload payload = JSONB.fromJson(body, SepayWebhookPayload.class);
            debugLog("[SepayWebhook] Parsed payload - id: " + payload.getId() 
                + ", transferType: " + payload.getTransferType() 
                + ", content: \"" + payload.getContent() + "\""
                + ", transferAmount: " + payload.getTransferAmount());

            // 4. Process payment transaction
            if ("in".equalsIgnoreCase(payload.getTransferType()) && payload.getContent() != null) {
                String content = payload.getContent();
                debugLog("[SepayWebhook] Transaction matches 'in'. Content: \"" + content + "\"");
                
                // Match "DH" followed by 8 characters or matches general pattern
                Pattern pattern = Pattern.compile("DH([a-zA-Z0-9]{8})");
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    String code = matcher.group(1);
                    debugLog("[SepayWebhook] Found order prefix code: " + code);
                    
                    // Locate first order matching this code prefix
                    List<OrderEntity> matched = JdbcHelper.executeQuery(
                        "SELECT * FROM `Order` WHERE id LIKE ? AND status = 'PENDING'",
                        rs -> {
                            Timestamp updatedAt = rs.getTimestamp("updatedAt");
                            OrderEntity order = new OrderEntity(rs.getString("id"), rs.getString("userId"), rs.getString("productId"),
                                    rs.getString("variantId"), rs.getInt("quantity"), rs.getTimestamp("createdAt").toLocalDateTime(),
                                    updatedAt == null ? null : updatedAt.toLocalDateTime(), rs.getString("status"));
                            order.setPhone(rs.getString("phone"));
                            order.setAddress(rs.getString("address"));
                            order.setCustomerName(rs.getString("customerName"));
                            order.setEmail(rs.getString("email"));
                            order.setNote(rs.getString("note"));
                            order.setPaymentMethod(rs.getString("paymentMethod"));
                            order.setVoucherId(rs.getString("voucherId"));
                            order.setTotalAmount(rs.getBigDecimal("totalAmount"));
                            return order;
                        }, code + "%"
                    );
                    
                    debugLog("[SepayWebhook] Number of matching pending orders in DB: " + matched.size());

                    if (!matched.isEmpty()) {
                        OrderEntity firstOrder = matched.get(0);
                        debugLog("[SepayWebhook] Matching order found. ID: " + firstOrder.getId() + ", totalAmount: " + firstOrder.getTotalAmount() + ", userId: " + firstOrder.getUserId());
                        
                        // Update all matching orders from the same transaction to PAID
                        int updated = JdbcHelper.executeUpdate(
                            "UPDATE `Order` SET status = 'PAID', updatedAt = ? WHERE userId = ? AND totalAmount = ? AND status = 'PENDING' AND paymentMethod = 'SEPAY' AND createdAt >= ?",
                            Timestamp.valueOf(LocalDateTime.now()),
                            firstOrder.getUserId(),
                            firstOrder.getTotalAmount(),
                            Timestamp.valueOf(firstOrder.getCreatedAt().minusMinutes(120)) // Increased buffer to 120 minutes
                        );
                        debugLog("[SepayWebhook] Successfully updated status of " + updated + " orders to PAID.");
                    } else {
                        debugLog("[SepayWebhook] No pending order matches prefix code: " + code + "%");
                    }
                } else {
                    debugLog("[SepayWebhook] Transfer content does not match regex pattern 'DHxxxxxxxx': \"" + content + "\"");
                }
            } else {
                debugLog("[SepayWebhook] Skipped: Transfer type is not 'in' or content is null.");
            }
            
            // 5. Respond success to Sepay
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write("{\"success\": true}");
            debugLog("[SepayWebhook] Response sent: {\"success\": true}");
        } catch (Exception ex) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            ex.printStackTrace(pw);
            debugLog("[SepayWebhook] Error processing webhook: " + ex.getMessage() + "\n" + sw.toString());
            
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.setContentType("application/json");
            res.getWriter().write("{\"success\": false, \"message\": \"" + ex.getMessage() + "\"}");
        }
    }
}
