package module.bussiness.payment;

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
}
