package module.bussiness.payment;

import module.core.sql.repository.PaymentRepository;
import module.core.sql.repository.VoucherRepository;

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
}
