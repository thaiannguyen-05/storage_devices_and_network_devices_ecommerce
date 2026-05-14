package module.bussiness.payment.repository.interfaces;

import java.math.BigDecimal;

public interface IPaymentRepository {

    boolean saveInitPayment(String orderId, String userId, String invoiceNumber, BigDecimal amount, String redirectUrl, String signature, String status);

    boolean updateStatusByOrderId(String orderId, String status);

    java.util.List<entity.PaymentEntity> findByOrderId(String orderId);
}
