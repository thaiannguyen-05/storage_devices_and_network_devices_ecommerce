package module.bussiness.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import module.bussiness.order.repository.impl.OrderRepository;
import module.bussiness.payment.repository.impl.PaymentRepository;
import module.bussiness.payment.repository.impl.VoucherRepository;
import module.core.config.ConfigService;

public class PaymentService implements IPayment {

    private static final ConcurrentHashMap<String, Boolean> PROCESSED_WEBHOOK_KEYS = new ConcurrentHashMap<>();

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
                    "message", "Đặt hàng COD thành công."
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
        Object status = payload.get("status");
        return Map.of("success", true, "receivedStatus", status == null ? "" : String.valueOf(status));
    }

    private String normalizePaymentMethod(String method) {
        if (method == null) return "";
        String m = method.trim().toLowerCase();
        if (m.contains("cod") || m.contains("khi_nhan")) return Constant.PAYMENT_METHOD_COD;
        return Constant.PAYMENT_METHOD_SEPAY;
    }

    private String normalizeProviderPaymentMethod(String method) {
        if (method == null) return "VISA";
        String m = method.trim().toUpperCase();
        if (m.equals("VISA") || m.equals("ATM") || m.equals("BANK_TRANSFER")) return m;
        return "VISA";
    }

    private String defaultInvoiceNumber() {
        return "INV" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String safe(Object value, String fallback) {
        if (value == null) return fallback;
        String s = String.valueOf(value).trim();
        return s.isBlank() ? fallback : s;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> body(Object raw) {
        if (raw instanceof Map) return (Map<String, Object>) raw;
        return new HashMap<>();
    }

    private BigDecimal parseAmount(String value) {
        try {
            return new BigDecimal(value.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String signFields(Map<String, Object> fields) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        String rawData = sb.toString();
        String secret = ConfigService.getOrDefault("SEPAY_SECRET_KEY", "");
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] bytes = mac.doFinal(rawData.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String mapProviderStatus(String providerStatus) {
        if (providerStatus == null) return "";
        String s = providerStatus.trim().toUpperCase();
        switch (s) {
            case "SUCCESS":
            case "COMPLETED":
            case "PAID":
                return Constant.PAYMENT_STATUS_SUCCESS;
            case "FAILED":
            case "ERROR":
            case "DECLINED":
                return Constant.PAYMENT_STATUS_FAILED;
            case "CANCELLED":
            case "CANCEL":
                return Constant.PAYMENT_STATUS_CANCELLED;
            case "PENDING":
            case "PROCESSING":
                return Constant.PAYMENT_STATUS_PENDING;
            default:
                return "";
        }
    }

    private void syncOrderStatus(String orderId, String paymentStatus) {
        try {
            if (Constant.PAYMENT_STATUS_SUCCESS.equals(paymentStatus)) {
                orderRepository.updateStatusByOrderId(orderId, Constant.ORDER_STATUS_CONFIRMED);
            } else if (Constant.PAYMENT_STATUS_FAILED.equals(paymentStatus) || Constant.PAYMENT_STATUS_CANCELLED.equals(paymentStatus)) {
                orderRepository.updateStatusByOrderId(orderId, Constant.ORDER_STATUS_CANCELLED);
            }
        } catch (Exception e) {
            // ignore sync errors
        }
    }

    private Map<String, Object> finalizeSePayMutationResult(Map<String, Object> payload, Map<String, Object> response, String action) {
        boolean success = Boolean.TRUE.equals(response.get("success"));
        Map<String, Object> body = body(response.get("body"));
        String orderId = safe(payload.get("order_invoice_number"), safe(payload.get("orderId"), ""));

        if (success && !orderId.isBlank()) {
            String status = action.equals("cancel") ? Constant.PAYMENT_STATUS_CANCELLED : Constant.PAYMENT_STATUS_FAILED;
            paymentRepository.updateStatusByOrderId(orderId, status);
            syncOrderStatus(orderId, status);
        }

        return Map.of(
                "success", success,
                "action", action,
                "raw", body
        );
    }
}
