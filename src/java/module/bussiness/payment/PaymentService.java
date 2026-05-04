package module.bussiness.payment;

import java.util.Map;
import module.bussiness.payment.repository.impl.PaymentRepository;
import module.bussiness.payment.repository.impl.VoucherRepository;

public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final VoucherRepository voucherRepository;

    public PaymentService() {
        this.paymentRepository = new PaymentRepository();
        this.voucherRepository = new VoucherRepository();
    }

    public PaymentRepository getPaymentRepository() {
        return paymentRepository;
    }

    public VoucherRepository getVoucherRepository() {
        return voucherRepository;
    }

    public Map<String, Object> handleSePayWebhook(Map<String, Object> payload) {
        Object status = payload.get("status");
        return Map.of("success", true, "receivedStatus", status == null ? "" : String.valueOf(status));
    }
}
