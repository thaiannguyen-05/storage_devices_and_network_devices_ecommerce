package module.bussiness.payment;

import java.util.Map;

public interface IPayment {

    Map<String, Object> processPayment(String paymentMethod, Map<String, Object> payload);

    Map<String, Object> querySePayTransaction(Map<String, Object> payload);

    Map<String, Object> cancelSePayOrder(Map<String, Object> payload);

    Map<String, Object> voidSePayTransaction(Map<String, Object> payload);

    Map<String, Object> handleSePayWebhook(Map<String, Object> payload);
}
