package module.bussiness.payment;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import module.bussiness.order.repository.impl.OrderRepository;
import module.bussiness.payment.repository.impl.PaymentRepository;
import module.bussiness.payment.repository.impl.VoucherRepository;
import module.core.config.ConfigService;

public class PaymentService implements IPayment {

    private static final Set<String> PROCESSED_WEBHOOK_KEYS = ConcurrentHashMap.newKeySet();

    private final PaymentRepository paymentRepository;
    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;
    private final SePayClient sePayClient;

    public PaymentService() {
        this.paymentRepository = new PaymentRepository();
        this.voucherRepository = new VoucherRepository();
        this.orderRepository = new OrderRepository();
        this.sePayClient = new SePayClient();
    }

    public PaymentRepository getPaymentRepository() {
        return paymentRepository;
    }

    public VoucherRepository getVoucherRepository() {
        return voucherRepository;
    }

    @Override
    public Map<String, Object> processPayment(String paymentMethod, Map<String, Object> payload) {
        String normalizedMethod = normalizePaymentMethod(paymentMethod);

        if (Constant.PAYMENT_METHOD_COD.equals(normalizedMethod)) {
            return Map.of(
                    "success", true,
                    "method", Constant.PAYMENT_METHOD_COD,
                    "message", "Đặt hàng COD thành công (demo)."
            );
        }

        String providerPaymentMethod = normalizeProviderPaymentMethod(paymentMethod);
        return initSePayCheckout(payload, providerPaymentMethod);
    }

    public Map<String, Object> initSePayCheckout(Map<String, Object> payload, String paymentMethod) {
        Map<String, Object> requestFields = new LinkedHashMap<>();
        requestFields.put("order_amount", safe(payload.get("order_amount"), "0"));
        requestFields.put("merchant", safe(payload.get("merchant"), ConfigService.getOrDefault("SEPAY_MERCHANT", "")));
        requestFields.put("currency", safe(payload.get("currency"), "VND"));
        requestFields.put("operation", safe(payload.get("operation"), "PURCHASE"));
        requestFields.put("order_description", safe(payload.get("order_description"), "Thanh toán đơn hàng"));
        requestFields.put("order_invoice_number", safe(payload.get("order_invoice_number"), defaultInvoiceNumber()));
        requestFields.put("customer_id", safe(payload.get("customer_id"), "guest"));
        requestFields.put("payment_method", paymentMethod.toUpperCase());
        requestFields.put("success_url", safe(payload.get("success_url"), ConfigService.getOrDefault("SEPAY_SUCCESS_URL", "")));
        requestFields.put("error_url", safe(payload.get("error_url"), ConfigService.getOrDefault("SEPAY_ERROR_URL", "")));
        requestFields.put("cancel_url", safe(payload.get("cancel_url"), ConfigService.getOrDefault("SEPAY_CANCEL_URL", "")));
        requestFields.put("ipn_url", safe(payload.get("ipn_url"), ConfigService.getOrDefault("SEPAY_IPN_URL", "")));

        String signature = signFields(requestFields);
        requestFields.put("signature", signature);

        Map<String, Object> clientResult = sePayClient.initCheckout(requestFields);
        boolean success = Boolean.TRUE.equals(clientResult.get("success"));

        Map<String, Object> body = body(clientResult.get("body"));
        String redirectUrl = safe(body.get("redirect_url"), "");

        String orderId = safe(payload.get("orderId"), "");
        String userId = safe(payload.get("userId"), "");
        String invoiceNumber = safe(requestFields.get("order_invoice_number"), "");
        BigDecimal amount = parseAmount(safe(requestFields.get("order_amount"), "0"));

        paymentRepository.saveInitPayment(orderId, userId, invoiceNumber, amount, redirectUrl, signature, success ? Constant.PAYMENT_STATUS_PENDING : Constant.PAYMENT_STATUS_FAILED);

        if (!success) {
            return Map.of(
                    "success", false,
                    "method", Constant.PAYMENT_METHOD_SEPAY,
                    "message", safe(clientResult.get("message"), "Khởi tạo thanh toán SePay thất bại."),
                    "raw", body
            );
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("method", Constant.PAYMENT_METHOD_SEPAY);
        result.put("message", safe(body.get("message"), "Redirect to SePay payment page"));
        result.put("redirect_url", redirectUrl);
        result.put("order_invoice_number", invoiceNumber);
        result.put("raw", body);
        return result;
    }

    @Override
    public Map<String, Object> querySePayTransaction(Map<String, Object> payload) {
        Map<String, Object> queryPayload = new LinkedHashMap<>();
        queryPayload.put("merchant", safe(payload.get("merchant"), ConfigService.getOrDefault("SEPAY_MERCHANT", "")));
        queryPayload.put("order_invoice_number", safe(payload.get("order_invoice_number"), ""));
        queryPayload.put("transaction_id", safe(payload.get("transaction_id"), ""));

        String signature = signFields(queryPayload);
        queryPayload.put("signature", signature);

        Map<String, Object> response = sePayClient.queryTransaction(queryPayload);
        Map<String, Object> body = body(response.get("body"));
        String providerStatus = safe(body.get("status"), "");
        String mappedStatus = mapProviderStatus(providerStatus);

        Map<String, Object> result = new HashMap<>();
        result.put("success", Boolean.TRUE.equals(response.get("success")));
        result.put("status", mappedStatus);
        result.put("provider_status", providerStatus);
        result.put("raw", body);

        String orderId = safe(payload.get("orderId"), "");
        if (!orderId.isBlank() && !mappedStatus.isBlank()) {
            paymentRepository.updateStatusByOrderId(orderId, mappedStatus);
            syncOrderStatus(orderId, mappedStatus);
        }

        return result;
    }

    @Override
    public Map<String, Object> cancelSePayOrder(Map<String, Object> payload) {
        Map<String, Object> cancelPayload = new LinkedHashMap<>();
        cancelPayload.put("merchant", safe(payload.get("merchant"), ConfigService.getOrDefault("SEPAY_MERCHANT", "")));
        cancelPayload.put("order_invoice_number", safe(payload.get("order_invoice_number"), ""));

        String signature = signFields(cancelPayload);
        cancelPayload.put("signature", signature);

        Map<String, Object> response = sePayClient.cancelOrder(cancelPayload);
        return finalizeSePayMutationResult(payload, response, "cancel");
    }

    @Override
    public Map<String, Object> voidSePayTransaction(Map<String, Object> payload) {
        Map<String, Object> voidPayload = new LinkedHashMap<>();
        voidPayload.put("merchant", safe(payload.get("merchant"), ConfigService.getOrDefault("SEPAY_MERCHANT", "")));
        voidPayload.put("order_invoice_number", safe(payload.get("order_invoice_number"), ""));
        voidPayload.put("transaction_id", safe(payload.get("transaction_id"), ""));

        String signature = signFields(voidPayload);
        voidPayload.put("signature", signature);

        Map<String, Object> response = sePayClient.voidTransaction(voidPayload);
        return finalizeSePayMutationResult(payload, response, "void");
    }

    @Override
    public Map<String, Object> handleSePayWebhook(Map<String, Object> payload) {
        String transactionId = firstNonBlank(
                safe(payload.get("transaction_id"), ""),
                safe(payload.get("transactionId"), ""),
                safe(payload.get("id"), "")
        );
        String providerStatus = firstNonBlank(
                safe(payload.get("status"), ""),
                safe(payload.get("payment_status"), "")
        );
        String webhookSignature = safe(payload.get("signature"), "");
        String transferAmount = firstNonBlank(
                safe(payload.get("transferAmount"), ""),
                safe(payload.get("transfer_amount"), ""),
                safe(payload.get("amount"), ""),
                safe(payload.get("order_amount"), "")
        );
        String reference = firstNonBlank(
                safe(payload.get("reference"), ""),
                safe(payload.get("content"), ""),
                safe(payload.get("description"), ""),
                safe(payload.get("code"), "")
        );

        if (webhookSignature.isBlank()) {
            return Map.of("success", false, "code", 400, "message", "Thiếu chữ ký webhook.");
        }

        if (!verifyWebhookSignature(payload, webhookSignature)) {
            return Map.of("success", false, "code", 401, "message", "Chữ ký webhook không hợp lệ.");
        }

        String idempotencyKey = transactionId + "|" + providerStatus + "|" + transferAmount + "|" + reference;
        if (!transactionId.isBlank() && PROCESSED_WEBHOOK_KEYS.contains(idempotencyKey)) {
            return Map.of("success", true, "duplicate", true, "message", "Webhook đã được xử lý trước đó.");
        }

        String orderId = firstNonBlank(
                safe(payload.get("orderId"), ""),
                safe(payload.get("order_id"), "")
        );

        String paymentStatus = mapProviderStatus(providerStatus);
        if (paymentStatus.isBlank() && isIncomingTransfer(payload, transferAmount)) {
            paymentStatus = Constant.PAYMENT_STATUS_SUCCESS;
        }

        if (!orderId.isBlank() && !paymentStatus.isBlank()) {
            paymentRepository.updateStatusByOrderId(orderId, paymentStatus);
            syncOrderStatus(orderId, paymentStatus);
        }

        if (!transactionId.isBlank()) {
            PROCESSED_WEBHOOK_KEYS.add(idempotencyKey);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Webhook SePay đã được xử lý.");
        result.put("paymentStatus", paymentStatus);
        result.put("providerStatus", providerStatus);
        result.put("reference", reference);
        return result;
    }

    private Map<String, Object> finalizeSePayMutationResult(Map<String, Object> payload, Map<String, Object> response, String action) {
        Map<String, Object> body = body(response.get("body"));
        String providerStatus = safe(body.get("status"), "");
        String mappedStatus = mapProviderStatus(providerStatus);

        if (mappedStatus.isBlank() && Boolean.TRUE.equals(response.get("success"))) {
            mappedStatus = Constant.PAYMENT_STATUS_CANCELLED;
        }

        String orderId = safe(payload.get("orderId"), "");
        if (!orderId.isBlank() && !mappedStatus.isBlank()) {
            paymentRepository.updateStatusByOrderId(orderId, mappedStatus);
            syncOrderStatus(orderId, mappedStatus);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", Boolean.TRUE.equals(response.get("success")));
        result.put("action", action);
        result.put("status", mappedStatus);
        result.put("provider_status", providerStatus);
        result.put("message", safe(body.get("message"), safe(response.get("message"), "SePay " + action + " thất bại.")));
        result.put("raw", body);
        return result;
    }

    private boolean isIncomingTransfer(Map<String, Object> payload, String transferAmount) {
        String transferType = safe(payload.get("transferType"), "");
        BigDecimal amount = parseAmount(transferAmount);
        return ("in".equalsIgnoreCase(transferType) || transferType.isBlank()) && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private void syncOrderStatus(String orderId, String paymentStatus) {
        if (Constant.PAYMENT_STATUS_SUCCESS.equals(paymentStatus)) {
            orderRepository.updateStatus(orderId, Constant.ORDER_STATUS_CONFIRMED);
            return;
        }

        if (Constant.PAYMENT_STATUS_FAILED.equals(paymentStatus) || Constant.PAYMENT_STATUS_CANCELLED.equals(paymentStatus)) {
            orderRepository.updateStatus(orderId, Constant.ORDER_STATUS_CANCELLED);
        }
    }

    private String normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return Constant.PAYMENT_METHOD_COD;
        }

        String method = paymentMethod.trim().toLowerCase();
        if (Constant.PAYMENT_METHOD_COD.equals(method)) {
            return Constant.PAYMENT_METHOD_COD;
        }

        return Constant.PAYMENT_METHOD_SEPAY;
    }

    private String normalizeProviderPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return "SEPAY";
        }

        String method = paymentMethod.trim().toUpperCase();
        if ("COD".equals(method)) {
            return "SEPAY";
        }

        if ("SEPAY".equals(method) || "BANK_TRANSFER".equals(method) || "VISA".equals(method) || "ATM".equals(method)) {
            return method;
        }

        return "SEPAY";
    }

    private boolean verifyWebhookSignature(Map<String, Object> payload, String signature) {
        Map<String, Object> signingFields = new LinkedHashMap<>();
        List<String> allowed = allowedFields();
        for (String field : allowed) {
            if (payload.containsKey(field)) {
                String value = safe(payload.get(field), "");
                if (!value.isBlank()) {
                    signingFields.put(field, value);
                }
            }
        }

        String computed = signFields(signingFields);
        if (!computed.isBlank() && computed.equals(signature)) {
            return true;
        }

        String transactionId = firstNonBlank(
                safe(payload.get("transaction_id"), ""),
                safe(payload.get("transactionId"), "")
        );
        String orderInvoice = firstNonBlank(
                safe(payload.get("order_invoice_number"), ""),
                safe(payload.get("orderId"), ""),
                safe(payload.get("order_id"), "")
        );
        String status = firstNonBlank(
                safe(payload.get("status"), ""),
                safe(payload.get("payment_status"), "")
        );
        String amount = safe(payload.get("order_amount"), safe(payload.get("amount"), ""));

        if (transactionId.isBlank() || orderInvoice.isBlank() || status.isBlank()) {
            return false;
        }

        String secretKey = ConfigService.getOrDefault("SEPAY_SECRET_KEY", "");
        if (secretKey.isBlank()) {
            return false;
        }

        String webhookString = transactionId + "|" + orderInvoice + "|" + status + "|" + amount;
        String fallbackSignature = hmacBase64(webhookString, secretKey);
        return !fallbackSignature.isBlank() && fallbackSignature.equals(signature);
    }

    private String signFields(Map<String, Object> fields) {
        String secretKey = ConfigService.getOrDefault("SEPAY_SECRET_KEY", "");
        if (secretKey.isBlank()) {
            return "";
        }

        List<String> signed = new ArrayList<>();
        List<String> allowedFields = allowedFields();

        for (String field : allowedFields) {
            if (!fields.containsKey(field)) {
                continue;
            }
            String value = safe(fields.get(field), "");
            if (value.isBlank()) {
                continue;
            }
            signed.add(field + "=" + value);
        }

        String signedString = String.join(",", signed);
        return hmacBase64(signedString, secretKey);
    }

    private String hmacBase64(String content, String secretKey) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKeySpec);
            byte[] signature = sha256Hmac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Không thể tạo chữ ký SePay", e);
        }
    }

    private List<String> allowedFields() {
        return List.of(
                "order_amount",
                "merchant",
                "currency",
                "operation",
                "order_description",
                "order_invoice_number",
                "customer_id",
                "payment_method",
                "success_url",
                "error_url",
                "cancel_url"
        );
    }

    private String mapProviderStatus(String providerStatus) {
        String value = providerStatus == null ? "" : providerStatus.trim().toUpperCase();
        if ("SUCCESS".equals(value) || "PAID".equals(value) || "COMPLETED".equals(value)) {
            return Constant.PAYMENT_STATUS_SUCCESS;
        }

        if ("FAILED".equals(value) || "ERROR".equals(value)) {
            return Constant.PAYMENT_STATUS_FAILED;
        }

        if ("CANCELLED".equals(value) || "CANCELED".equals(value)) {
            return Constant.PAYMENT_STATUS_CANCELLED;
        }

        if ("PENDING".equals(value) || "PROCESSING".equals(value)) {
            return Constant.PAYMENT_STATUS_PENDING;
        }

        return "";
    }

    private String defaultInvoiceNumber() {
        return "INV_" + DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
    }

    private Map<String, Object> body(Object value) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) value;
            return body;
        }
        return new HashMap<>();
    }

    private String safe(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }

        String text = String.valueOf(value).trim();
        return text.isEmpty() ? fallback : text;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private BigDecimal parseAmount(String value) {
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
